package com.baolong.blpicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baolong.blpicturebackend.annotation.AuthCheck;
import com.baolong.blpicturebackend.api.aliyunai.AliYunAiApi;
import com.baolong.blpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.baolong.blpicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.baolong.blpicturebackend.api.imageSearch.baidu.ImageSearchApiFacade;
import com.baolong.blpicturebackend.api.imageSearch.baidu.model.ImageSearchResult;
import com.baolong.blpicturebackend.api.imageSearch.so.SoImageSearchApiFacade;
import com.baolong.blpicturebackend.api.imageSearch.so.model.SoImageSearchResult;
import com.baolong.blpicturebackend.auth.SpaceUserAuthManager;
import com.baolong.blpicturebackend.auth.StpKit;
import com.baolong.blpicturebackend.auth.annotation.SaSpaceCheckPermission;
import com.baolong.blpicturebackend.auth.model.SpaceUserPermissionConstant;
import com.baolong.blpicturebackend.comment.BaseResponse;
import com.baolong.blpicturebackend.comment.DeleteRequest;
import com.baolong.blpicturebackend.comment.ResultUtils;
import com.baolong.blpicturebackend.constant.UserConstant;
import com.baolong.blpicturebackend.exception.BusinessException;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditByBatchRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureQueryRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureReviewRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadRequest;
import com.baolong.blpicturebackend.model.dto.picture.SearchPictureByColorRequest;
import com.baolong.blpicturebackend.model.dto.picture.SearchPictureByPictureRequest;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.model.entity.Picture;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.CategoryTagEnum;
import com.baolong.blpicturebackend.model.enums.PictureReviewStatusEnum;
import com.baolong.blpicturebackend.model.vo.PictureTagCategory;
import com.baolong.blpicturebackend.model.vo.PictureVO;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.blpicturebackend.service.PictureService;
import com.baolong.blpicturebackend.service.SpaceService;
import com.baolong.blpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

	@Resource
	private UserService userService;
	@Resource
	private PictureService pictureService;
	@Resource
	private CategoryTagService categoryTagService;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private SpaceService spaceService;
	@Resource
	private AliYunAiApi aliYunAiApi;
	@Resource
	private SpaceUserAuthManager spaceUserAuthManager;

	/**
	 * 上传图片（可重新上传）
	 */
	@PostMapping("/upload")
	// @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
	public BaseResponse<PictureVO> uploadPicture(
			@RequestPart("file") MultipartFile multipartFile,
			PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 通过 URL 上传图片（可重新上传）
	 */
	@PostMapping("/upload/url")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
	public BaseResponse<PictureVO> uploadPictureByUrl(
			@RequestBody PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		String fileUrl = pictureUploadRequest.getFileUrl();
		PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 删除图片
	 */
	@PostMapping("/delete")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
	public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		pictureService.deletePicture(deleteRequest.getId(), loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 更新图片（仅管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
		if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureUpdateRequest, picture);

		/*// 注意将 list 转为 string
		picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));*/

		User loginUser = userService.getLoginUser(request);
		List<String> inputTagList = pictureUpdateRequest.getTags();
		// 使用分类标签表的方式
		if (pictureUpdateRequest.getInputTagList() != null && !pictureUpdateRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureUpdateRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginUser.getId());
				categoryTagService.save(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		picture.setTags(inputTagListStr);

		// 数据校验
		pictureService.validPicture(picture);
		// 判断是否存在
		long id = pictureUpdateRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 补充审核参数
		pictureService.fillReviewParams(picture, loginUser);
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取图片（仅管理员可用）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

		// 空间权限校验
		Long spaceId = picture.getSpaceId();
		if (spaceId != null) {
			User loginUser = userService.getLoginUser(request);
			pictureService.checkPictureAuth(loginUser, picture);
		}

		// 获取封装类
		return ResultUtils.success(picture);
	}

	/**
	 * 根据 id 获取图片（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 空间的图片，需要校验权限
		Space space = null;
		Long spaceId = picture.getSpaceId();
		if (spaceId != null) {
			boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
			ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
			space = spaceService.getById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		}
		// 获取权限列表
		User loginUser = null;
		try {
			loginUser = userService.getLoginUser(request);
		} catch (Exception e) {
			log.info("未登录");
		}
		PictureVO pictureVO = pictureService.getPictureVO(picture, request);
		if (loginUser != null) {
			List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
			pictureVO.setPermissionList(permissionList);
		}
		// 获取封装类
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 分页获取图片列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 处理分类和标签
		for (Picture picture : picturePage.getRecords()) {
			// 查询当前图片的分类和标签的信息
			List<String> ctIds = new ArrayList<>();
			if (StrUtil.isNotBlank(picture.getTags())) {
				ctIds.addAll(Arrays.stream(picture.getTags().split(",")).collect(Collectors.toList()));
			}
			if (StrUtil.isNotBlank(picture.getCategory())) {
				ctIds.add(picture.getCategory());
			}
			if (!ctIds.isEmpty()) {
				List<CategoryTag> categoryTagList = categoryTagService.list(new LambdaQueryWrapper<CategoryTag>()
						.in(CategoryTag::getId, ctIds)
				);
				Map<Integer, List<CategoryTag>> typeMap = categoryTagList.stream().collect(Collectors.groupingBy(CategoryTag::getType));
				if (typeMap.get(CategoryTagEnum.CATEGORY.getValue()) != null) {
					picture.setCategory(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getName());
				}
				if (typeMap.get(CategoryTagEnum.TAG.getValue()) != null) {
					picture.setTags(JSONUtil.toJsonStr(
							typeMap.get(CategoryTagEnum.TAG.getValue())
									.stream()
									.map(CategoryTag::getName)
									.collect(Collectors.toList())
					));
				}
			}
		}

		return ResultUtils.success(picturePage);
	}

	/**
	 * 分页获取图片列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
															 HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

		// 空间权限校验
		Long spaceId = pictureQueryRequest.getSpaceId();
		// 公开图库
		if (spaceId == null) {
			// 普通用户默认只能查看已过审的公开数据
			pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
			pictureQueryRequest.setNullSpaceId(true);
		} else {
			boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
			ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
			// 私有空间
			User loginUser = userService.getLoginUser(request);
			Space space = spaceService.getById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
			// if (!loginUser.getId().equals(space.getUserId())) {
			// 	throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
			// }
		}

		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
	}

	/**
	 * 分页获取图片列表（封装类）集成 Redis 缓存
	 */
	@PostMapping("/list/page/vo/cacheRedis")
	public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
																	  HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 普通用户默认只能查看已过审的数据
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

		// 构建缓存 key
		String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
		String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
		String redisKey = "baolong:listPictureVOByPage:" + hashKey;
		// 从 Redis 缓存中查询
		ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
		String cachedValue = valueOps.get(redisKey);
		if (cachedValue != null) {
			// 如果缓存命中，返回结果
			Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
			return ResultUtils.success(cachedPage);
		}

		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

		// 存入 Redis 缓存
		String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
		// 5 - 10 分钟随机过期，防止雪崩
		int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
		valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

		// 返回结果
		return ResultUtils.success(pictureVOPage);
	}

	private final Cache<String, String> LOCAL_CACHE =
			Caffeine.newBuilder().initialCapacity(1024)
					// 最多一万条数据
					.maximumSize(10000L)
					// 缓存 5 分钟移除
					.expireAfterWrite(5L, TimeUnit.MINUTES)
					.build();

	/**
	 * 分页获取图片列表（封装类）集成 Caffeine 缓存
	 */
	@PostMapping("/list/page/vo/cacheCaffeine")
	public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCacheCaffeine(@RequestBody PictureQueryRequest pictureQueryRequest,
																			  HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 普通用户默认只能查看已过审的数据
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

		// 构建缓存 key
		String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
		String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
		String caffeineKey = "baolong:listPictureVOByPage:" + hashKey;
		// 从本地缓存中查询
		String cachedValue = LOCAL_CACHE.getIfPresent(caffeineKey);
		if (cachedValue != null) {
			// 如果缓存命中，返回结果
			Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
			return ResultUtils.success(cachedPage);
		}

		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

		// 存入本地缓存
		String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
		LOCAL_CACHE.put(caffeineKey, cacheValue);

		// 返回结果
		return ResultUtils.success(pictureVOPage);
	}

	/**
	 * 分页获取图片列表（封装类）集成 Caffeine 缓存
	 */
	@PostMapping("/list/page/vo/cacheLevels")
	public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCacheLevels(@RequestBody PictureQueryRequest pictureQueryRequest,
																			HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 普通用户默认只能查看已过审的数据
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

		// 构建缓存 key
		String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
		String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
		String cacheKey = "baolong:listPictureVOByPage:" + hashKey;
		// 1.从本地缓存中查询
		String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
		if (cachedValue != null) {
			log.info("本地缓存命中, key = {}", cacheKey);
			// 如果缓存命中，返回结果
			Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
			return ResultUtils.success(cachedPage);
		}
		// 2.从 Redis 缓存中查询
		ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
		cachedValue = valueOps.get(cacheKey);
		if (cachedValue != null) {
			log.info("Redis 缓存命中, key = {}", cacheKey);
			// 存入本地缓存
			LOCAL_CACHE.put(cacheKey, cachedValue);
			// 如果缓存命中，返回结果
			Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
			return ResultUtils.success(cachedPage);
		}

		// 3.查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

		// 4.更新缓存
		String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
		// 存入本地缓存
		LOCAL_CACHE.put(cacheKey, cacheValue);
		// 存入 Redis 缓存
		valueOps.set(cacheKey, cacheValue, 300 + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);

		// 返回结果
		return ResultUtils.success(pictureVOPage);
	}

	/**
	 * 编辑图片（给用户使用）
	 */
	@PostMapping("/edit")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
		if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureEditRequest, picture);
		User loginUser = userService.getLoginUser(request);
		pictureService.editPicture(pictureEditRequest, loginUser);
		return ResultUtils.success(true);
	}

	@GetMapping("/tag_category")
	public BaseResponse<PictureTagCategory> listPictureTagCategory() {
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
		List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
		pictureTagCategory.setTagList(tagList);
		pictureTagCategory.setCategoryList(categoryList);
		return ResultUtils.success(pictureTagCategory);
	}

	/**
	 * 图片审核
	 */
	@PostMapping("/review")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
												 HttpServletRequest request) {
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		pictureService.doPictureReview(pictureReviewRequest, loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 上传图片（根据条件批量爬取上传）
	 */
	@PostMapping("/upload/batch")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Integer> uploadPictureByBatch(
			@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
			HttpServletRequest request) {
		ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);

		List<String> tagList = pictureUploadByBatchRequest.getTags();
		// 使用分类标签表的方式
		if (pictureUploadByBatchRequest.getInputTagList() != null && !pictureUploadByBatchRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureUploadByBatchRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginUser.getId());
				categoryTagService.save(categoryTag);
				// 把新增的id放到 inputTagList 中
				tagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		pictureUploadByBatchRequest.setTags(tagList);

		int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
		return ResultUtils.success(uploadCount);
	}

	/**
	 * 以图搜图
	 */
	@PostMapping("/search/picture")
	public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
		ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
		Long pictureId = searchPictureByPictureRequest.getPictureId();
		ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
		Picture oldPicture = pictureService.getById(pictureId);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldPicture.getOriginUrl());
		return ResultUtils.success(resultList);
	}

	/**
	 * 以图搜图
	 */
	@PostMapping("/search/picture/so")
	public BaseResponse<List<SoImageSearchResult>> searchPictureByPictureIsSo(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
		ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
		Long pictureId = searchPictureByPictureRequest.getPictureId();
		ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
		Picture oldPicture = pictureService.getById(pictureId);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		List<SoImageSearchResult> resultList = new ArrayList<>();
		// 这个 start 是控制查询多少页, 每页是 20 条
		int start = 0;
		while (resultList.size() <= 50) {
			List<SoImageSearchResult> tempList = SoImageSearchApiFacade.searchImage(
					StrUtil.isNotBlank(oldPicture.getOriginUrl()) ? oldPicture.getOriginUrl() : oldPicture.getUrl(), start
			);
			if (tempList.isEmpty()) {
				break;
			}
			resultList.addAll(tempList);
			start += tempList.size();
		}
		return ResultUtils.success(resultList);
	}

	/**
	 * 根据颜色搜索图片
	 */
	@PostMapping("/search/color")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
	public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
		String picColor = searchPictureByColorRequest.getPicColor();
		Long spaceId = searchPictureByColorRequest.getSpaceId();
		User loginUser = userService.getLoginUser(request);
		List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
		return ResultUtils.success(result);
	}

	/**
	 * 批量修改图片
	 */
	@PostMapping("/edit/batch")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 创建 AI 扩图任务
	 */
	@PostMapping("/out_painting/create_task")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
			@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
			HttpServletRequest request) {
		if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
		return ResultUtils.success(response);
	}

	/**
	 * 查询 AI 扩图任务
	 */
	@GetMapping("/out_painting/get_task")
	public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
		ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
		GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
		return ResultUtils.success(task);
	}

}
