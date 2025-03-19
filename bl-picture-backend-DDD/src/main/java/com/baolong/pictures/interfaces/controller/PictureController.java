package com.baolong.pictures.interfaces.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baolong.pictures.application.service.PictureApplicationService;
import com.baolong.pictures.application.shared.auth.annotation.AuthCheck;
import com.baolong.pictures.domain.picture.entity.Picture;
import com.baolong.pictures.domain.user.constant.UserConstant;
import com.baolong.pictures.infrastructure.api.grab.model.GrabPictureResult;
import com.baolong.pictures.infrastructure.common.BaseResponse;
import com.baolong.pictures.infrastructure.common.DeleteRequest;
import com.baolong.pictures.infrastructure.common.ResultUtils;
import com.baolong.pictures.infrastructure.common.page.PageVO;
import com.baolong.pictures.infrastructure.exception.ErrorCode;
import com.baolong.pictures.infrastructure.exception.ThrowUtils;
import com.baolong.pictures.infrastructure.function.limit.annotation.Limit;
import com.baolong.pictures.infrastructure.function.limit.enums.LimitType;
import com.baolong.pictures.interfaces.dto.picture.PictureBatchEditRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureEditRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureGrabRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureInteractionRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureQueryRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureReviewRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureUpdateRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureUploadRequest;
import com.baolong.pictures.interfaces.vo.picture.PictureDetailVO;
import com.baolong.pictures.interfaces.vo.picture.PictureHomeVO;
import com.baolong.pictures.interfaces.vo.picture.PictureVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片接口
 */
@RestController
@RequestMapping("/picture")
@RequiredArgsConstructor
public class PictureController {

	private final PictureApplicationService pictureApplicationService;

	// region 增删改相关（包含上传图片）

	/**
	 * 上传图片（文件）
	 */
	@PostMapping("/upload/file")
	public BaseResponse<PictureDetailVO> uploadPictureByFile(@RequestPart("file") MultipartFile multipartFile,
															 PictureUploadRequest pictureUploadRequest) {
		ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
		PictureDetailVO pictureDetailVO = pictureApplicationService.uploadPicture(multipartFile, pictureUploadRequest);
		return ResultUtils.success(pictureDetailVO);
	}

	/**
	 * 上传图片（URL）
	 */
	@PostMapping("/upload/url")
	public BaseResponse<PictureDetailVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest) {
		String pictureUrl = pictureUploadRequest.getPictureUrl();
		ThrowUtils.throwIf(StrUtil.isEmpty(pictureUrl), ErrorCode.PARAMS_ERROR);
		PictureDetailVO pictureDetailVO = pictureApplicationService.uploadPicture(pictureUrl, pictureUploadRequest);
		return ResultUtils.success(pictureDetailVO);
	}

	/**
	 * 删除图片
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest) {
		ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(deleteRequest.getId()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.deletePicture(deleteRequest));
	}

	/**
	 * 更新图片
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
		ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureUpdateRequest.getId()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.updatePicture(pictureUpdateRequest));
	}

	/**
	 * 编辑图片
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest) {
		System.out.println(JSONUtil.parse(pictureEditRequest));
		ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureEditRequest.getId()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.editPicture(pictureEditRequest));
	}

	/**
	 * 编辑图片（批量）
	 */
	@PostMapping("/edit/batch")
	public BaseResponse<Boolean> editPictureBatch(@RequestBody PictureBatchEditRequest pictureBatchEditRequest) {
		ThrowUtils.throwIf(pictureBatchEditRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(CollUtil.isEmpty(pictureBatchEditRequest.getIdList()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.editPictureBatch(pictureBatchEditRequest));
	}

	/**
	 * 审核图片（包含批量审核）
	 */
	@PostMapping("/review")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest) {
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(pictureReviewRequest.getId() == null && CollUtil.isEmpty(pictureReviewRequest.getIdList())
				, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.reviewPicture(pictureReviewRequest));
	}

	/**
	 * 爬取图片
	 */
	@PostMapping("/grab")
	public BaseResponse<List<GrabPictureResult>> grabPicture(@RequestBody PictureGrabRequest pictureGrabRequest) {
		ThrowUtils.throwIf(pictureGrabRequest == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.grabPicture(pictureGrabRequest));
	}

	// endregion 增删改相关（包含上传图片）

	// region 查询相关

	/**
	 * 根据图片 ID 获取图片信息
	 */
	@GetMapping("/info")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Picture> getPictureInfoById(Long id) {
		ThrowUtils.throwIf(ObjectUtil.isEmpty(id), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.getPictureInfoById(id));
	}

	/**
	 * 根据图片 ID 获取图片详情
	 */
	@GetMapping("/detail")
	public BaseResponse<PictureDetailVO> getPictureDetailById(Long id) {
		ThrowUtils.throwIf(ObjectUtil.isEmpty(id), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.getPictureDetailById(id));
	}

	/**
	 * 获取首页图片列表
	 */
	@GetMapping("/home/list")
	public BaseResponse<PageVO<PictureHomeVO>> getPicturePageListAsHome(PictureQueryRequest pictureQueryRequest) {
		ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 20, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.getPicturePageListAsHome(pictureQueryRequest));
	}

	/**
	 * 获取图片管理分页列表
	 */
	@PostMapping("/manage/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<PageVO<PictureVO>> getPicturePageListAsManage(@RequestBody PictureQueryRequest pictureQueryRequest) {
		return ResultUtils.success(pictureApplicationService.getPicturePageListAsManage(pictureQueryRequest));
	}

	/**
	 * 获取个人空间图片分页列表
	 */
	@PostMapping("/personSpace/page")
	public BaseResponse<PageVO<PictureVO>> getPicturePageListAsPersonSpace(@RequestBody PictureQueryRequest pictureQueryRequest) {
		return ResultUtils.success(pictureApplicationService.getPicturePageListAsPersonSpace(pictureQueryRequest));
	}

	// /**
	//  * 根据登录用户获取图片标签列表
	//  */
	// @GetMapping("/tags")
	// public BaseResponse<List<String>> getPictureTagListByLoginUser() {
	// 	return ResultUtils.success(pictureApplicationService.getPictureTagListByLoginUser());
	// }

	// /**
	//  * 根据颜色搜索图片
	//  */
	// @GetMapping("/search/color")
	// public BaseResponse<List<PictureVO>> searchPictureByColor(PictureColorSearchRequest pictureColorSearchRequest) {
	// 	ThrowUtils.throwIf(pictureColorSearchRequest == null, ErrorCode.PARAMS_ERROR);
	// 	List<PictureVO> result = pictureApplicationService.searchPictureByColor(pictureColorSearchRequest);
	// 	return ResultUtils.success(result);
	// }

	// endregion 查询相关

	/**
	 * 图片下载
	 */
	@Limit(key = "PictureDownload:", count = 1, limitType = LimitType.IP, errMsg = "图片下载太频繁，请稍后再试!")
	@PostMapping("/download")
	public BaseResponse<String> pictureDownload(@RequestBody PictureInteractionRequest pictureInteractionRequest) {
		ThrowUtils.throwIf(pictureInteractionRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureInteractionRequest.getId()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.pictureDownload(pictureInteractionRequest.getId()));
	}

	/**
	 * 图片分享
	 */
	@Limit(key = "PictureShare:", time = 5, count = 1, limitType = LimitType.IP, errMsg = "图片分享太频繁，请稍后再试!")
	@PostMapping("/share")
	public BaseResponse<Boolean> pictureShare(@RequestBody PictureInteractionRequest pictureInteractionRequest) {
		ThrowUtils.throwIf(pictureInteractionRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureInteractionRequest.getId()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.pictureShare(pictureInteractionRequest.getId()));
	}

	/**
	 * 图片点赞
	 */
	@Limit(key = "PictureLike:", time = 5, count = 2, limitType = LimitType.IP, errMsg = "图片点赞太频繁，请稍后再试!")
	@PostMapping("/interaction")
	public BaseResponse<Boolean> pictureLikeOrCollect(@RequestBody PictureInteractionRequest pictureInteractionRequest) {
		ThrowUtils.throwIf(pictureInteractionRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureInteractionRequest.getId()), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureInteractionRequest.getType()), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureInteractionRequest.getChange()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(pictureApplicationService.pictureLikeOrCollect(pictureInteractionRequest));
	}

	//
	// /**
	//  * 分页获取图片列表（封装类）集成 Redis 缓存
	//  */
	// @PostMapping("/list/page/vo/cacheRedis")
	// public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
	// 																  HttpServletRequest request) {
	// 	long current = pictureQueryRequest.getCurrent();
	// 	long size = pictureQueryRequest.getPageSize();
	// 	// 限制爬虫
	// 	ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
	// 	// 普通用户默认只能查看已过审的数据
	// 	pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
	//
	// 	// 构建缓存 key
	// 	String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
	// 	String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
	// 	String redisKey = "baolong:listPictureVOByPage:" + hashKey;
	// 	// 从 Redis 缓存中查询
	// 	ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
	// 	String cachedValue = valueOps.get(redisKey);
	// 	if (cachedValue != null) {
	// 		// 如果缓存命中，返回结果
	// 		Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
	// 		return ResultUtils.success(cachedPage);
	// 	}
	//
	// 	// 查询数据库
	// 	Page<TePicture> picturePage = pictureApplicationService.getPictureListPage(new Page<>(current, size),
	// 			pictureApplicationService.getQueryWrapper(pictureQueryRequest));
	// 	// 获取封装类
	// 	Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
	//
	// 	// 存入 Redis 缓存
	// 	String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
	// 	// 5 - 10 分钟随机过期，防止雪崩
	// 	int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
	// 	valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
	//
	// 	// 返回结果
	// 	return ResultUtils.success(pictureVOPage);
	// }
	//
	// private final Cache<String, String> LOCAL_CACHE =
	// 		Caffeine.newBuilder().initialCapacity(1024)
	// 				// 最多一万条数据
	// 				.maximumSize(10000L)
	// 				// 缓存 5 分钟移除
	// 				.expireAfterWrite(5L, TimeUnit.MINUTES)
	// 				.build();
	//
	// /**
	//  * 分页获取图片列表（封装类）集成 Caffeine 缓存
	//  */
	// @PostMapping("/list/page/vo/cacheCaffeine")
	// public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCacheCaffeine(@RequestBody PictureQueryRequest pictureQueryRequest,
	// 																		  HttpServletRequest request) {
	// 	long current = pictureQueryRequest.getCurrent();
	// 	long size = pictureQueryRequest.getPageSize();
	// 	// 限制爬虫
	// 	ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
	// 	// 普通用户默认只能查看已过审的数据
	// 	pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
	//
	// 	// 构建缓存 key
	// 	String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
	// 	String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
	// 	String caffeineKey = "baolong:listPictureVOByPage:" + hashKey;
	// 	// 从本地缓存中查询
	// 	String cachedValue = LOCAL_CACHE.getIfPresent(caffeineKey);
	// 	if (cachedValue != null) {
	// 		// 如果缓存命中，返回结果
	// 		Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
	// 		return ResultUtils.success(cachedPage);
	// 	}
	//
	// 	// 查询数据库
	// 	Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size),
	// 			pictureApplicationService.getQueryWrapper(pictureQueryRequest));
	// 	// 获取封装类
	// 	Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
	//
	// 	// 存入本地缓存
	// 	String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
	// 	LOCAL_CACHE.put(caffeineKey, cacheValue);
	//
	// 	// 返回结果
	// 	return ResultUtils.success(pictureVOPage);
	// }
	//
	// /**
	//  * 分页获取图片列表（封装类）集成 Caffeine 缓存
	//  */
	// @PostMapping("/list/page/vo/cacheLevels")
	// public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCacheLevels(@RequestBody PictureQueryRequest pictureQueryRequest,
	// 																		HttpServletRequest request) {
	// 	long current = pictureQueryRequest.getCurrent();
	// 	long size = pictureQueryRequest.getPageSize();
	// 	// 限制爬虫
	// 	ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
	// 	// 普通用户默认只能查看已过审的数据
	// 	pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
	//
	// 	// 构建缓存 key
	// 	String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
	// 	String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
	// 	String cacheKey = "baolong:listPictureVOByPage:" + hashKey;
	// 	// 1.从本地缓存中查询
	// 	String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
	// 	if (cachedValue != null) {
	// 		log.info("本地缓存命中, key = {}", cacheKey);
	// 		// 如果缓存命中，返回结果
	// 		Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
	// 		return ResultUtils.success(cachedPage);
	// 	}
	// 	// 2.从 Redis 缓存中查询
	// 	ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
	// 	cachedValue = valueOps.get(cacheKey);
	// 	if (cachedValue != null) {
	// 		log.info("Redis 缓存命中, key = {}", cacheKey);
	// 		// 存入本地缓存
	// 		LOCAL_CACHE.put(cacheKey, cachedValue);
	// 		// 如果缓存命中，返回结果
	// 		Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
	// 		return ResultUtils.success(cachedPage);
	// 	}
	//
	// 	// 3.查询数据库
	// 	Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size),
	// 			pictureApplicationService.getQueryWrapper(pictureQueryRequest));
	// 	// 获取封装类
	// 	Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
	//
	// 	// 4.更新缓存
	// 	String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
	// 	// 存入本地缓存
	// 	LOCAL_CACHE.put(cacheKey, cacheValue);
	// 	// 存入 Redis 缓存
	// 	valueOps.set(cacheKey, cacheValue, 300 + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
	//
	// 	// 返回结果
	// 	return ResultUtils.success(pictureVOPage);
	// }

	//
	// /**
	//  * 上传图片（根据条件批量爬取上传）
	//  */
	// @PostMapping("/upload/batch")
	// @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	// public BaseResponse<Integer> uploadPictureByBatch(
	// 		@RequestBody PictureGrabUploadRequest pictureGrabUploadRequest,
	// 		HttpServletRequest request) {
	// 	ThrowUtils.throwIf(pictureGrabUploadRequest == null, ErrorCode.PARAMS_ERROR);
	// 	User loginTUser = userApplicationService.getLoginUser();
	//
	// 	List<String> tagList = pictureGrabUploadRequest.getTags();
	// 	// 使用分类标签表的方式
	// 	if (pictureGrabUploadRequest.getInputTagList() != null && !pictureGrabUploadRequest.getInputTagList().isEmpty()) {
	// 		// 需要把这个里面的标签新增到数据库中
	// 		for (String tag : pictureGrabUploadRequest.getInputTagList()) {
	// 			CategoryTag categoryTag = new CategoryTag();
	// 			categoryTag.setName(tag);
	// 			categoryTag.setType(CategoryTagEnum.TAG.getValue());
	// 			categoryTag.setUserId(loginTUser.getId());
	// 			categoryTagApplicationService.addCategoryTag(categoryTag);
	// 			// 把新增的id放到 inputTagList 中
	// 			tagList.add(String.valueOf(categoryTag.getId()));
	// 		}
	// 	}
	// 	pictureGrabUploadRequest.setTags(tagList);
	//
	// 	int uploadCount = pictureApplicationService.uploadPictureByBatch(pictureGrabUploadRequest, loginTUser);
	// 	return ResultUtils.success(uploadCount);
	// }
	//
	// /**
	//  * 以图搜图
	//  */
	// @PostMapping("/search/picture")
	// public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
	// 	ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
	// 	Long pictureId = searchPictureByPictureRequest.getPictureId();
	// 	ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
	// 	TePicture oldTePicture = pictureApplicationService.getPictureById(pictureId);
	// 	ThrowUtils.throwIf(oldTePicture == null, ErrorCode.NOT_FOUND_ERROR);
	// 	List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldTePicture.getOriginUrl());
	// 	return ResultUtils.success(resultList);
	// }
	//
	// /**
	//  * 以图搜图
	//  */
	// @PostMapping("/search/picture/so")
	// public BaseResponse<List<SoImageSearchResult>> searchPictureByPictureIsSo(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
	// 	ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
	// 	Long pictureId = searchPictureByPictureRequest.getPictureId();
	// 	ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
	// 	TePicture oldTePicture = pictureApplicationService.getPictureById(pictureId);
	// 	ThrowUtils.throwIf(oldTePicture == null, ErrorCode.NOT_FOUND_ERROR);
	// 	List<SoImageSearchResult> resultList = new ArrayList<>();
	// 	// 这个 start 是控制查询多少页, 每页是 20 条
	// 	int start = 0;
	// 	while (resultList.size() <= 50) {
	// 		List<SoImageSearchResult> tempList = SoImageSearchApiFacade.searchImage(
	// 				StrUtil.isNotBlank(oldTePicture.getOriginUrl()) ? oldTePicture.getOriginUrl() : oldTePicture.getUrl(), start
	// 		);
	// 		if (tempList.isEmpty()) {
	// 			break;
	// 		}
	// 		resultList.addAll(tempList);
	// 		start += tempList.size();
	// 	}
	// 	return ResultUtils.success(resultList);
	// }
	//
	//
	// /**
	//  * 创建 AI 扩图任务
	//  */
	// @PostMapping("/out_painting/create_task")
	// @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	// public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
	// 		@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
	// 		HttpServletRequest request) {
	// 	if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
	// 		throw new BusinessException(ErrorCode.PARAMS_ERROR);
	// 	}
	// 	User loginTUser = userApplicationService.getLoginUser();
	// 	CreateOutPaintingTaskResponse response = pictureApplicationService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginTUser);
	// 	return ResultUtils.success(response);
	// }
	//
	// /**
	//  * 查询 AI 扩图任务
	//  */
	// @GetMapping("/out_painting/get_task")
	// public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
	// 	ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
	// 	GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
	// 	return ResultUtils.success(task);
	// }
}
