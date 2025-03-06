package com.baolong.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baolong.picture.domain.categoryTag.service.CategoryTagDomainService;
import com.baolong.picture.infrastructure.manager.upload.FilePictureUpload;
import com.baolong.picture.infrastructure.manager.upload.PictureUploadTemplate;
import com.baolong.picture.infrastructure.manager.upload.UrlPictureUpload;
import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.categoryTag.enums.CategoryTagEnum;
import com.baolong.picture.application.service.CategoryTagApplicationService;
import com.baolong.picture.application.service.SpaceApplicationService;
import com.baolong.picture.application.service.PictureApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.domain.picture.service.PictureDomainService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.infrastructure.utils.ColorSimilarUtils;
import com.baolong.picture.interfaces.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.baolong.picture.interfaces.dto.picture.PictureEditByBatchRequest;
import com.baolong.picture.interfaces.dto.picture.PictureEditRequest;
import com.baolong.picture.interfaces.dto.picture.PictureQueryRequest;
import com.baolong.picture.interfaces.dto.picture.PictureReviewRequest;
import com.baolong.picture.interfaces.dto.picture.PictureUploadByBatchRequest;
import com.baolong.picture.interfaces.dto.picture.PictureUploadRequest;
import com.baolong.picture.infrastructure.manager.upload.model.UploadPictureResult;
import com.baolong.picture.interfaces.vo.picture.PictureVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 图片应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PictureApplicationServiceImpl implements PictureApplicationService {

	private final PictureDomainService pictureDomainService;

	@Resource
	private UserApplicationService userApplicationService;
	@Resource
	private CategoryTagApplicationService categoryTagApplicationService;
	@Resource
	private CategoryTagDomainService categoryTagDomainService;
	@Resource
	private FilePictureUpload filePictureUpload;
	@Resource
	private UrlPictureUpload urlPictureUpload;
	@Resource
	private SpaceApplicationService spaceApplicationService;

	@Resource
	private TransactionTemplate transactionTemplate;

	/**
	 * 上传图片
	 *
	 * @param inputSource          文件输入源
	 * @param pictureUploadRequest 上传图片的请求对象
	 * @param loginUser            登录的用户
	 * @return PictureVO
	 */
	@Override
	public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

		// 校验空间是否存在
		Long spaceId = pictureUploadRequest.getSpaceId();
		if (spaceId != null) {
			Space space = spaceApplicationService.getSpaceById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
			// // 必须空间创建人（管理员）才能上传  这里改为了注解校验
			// if (!loginUser.getId().equals(space.getUserId())) {
			// 	throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
			// }

			// 校验额度
			if (space.getTotalCount() >= space.getMaxCount()) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
			}
			if (space.getTotalSize() >= space.getMaxSize()) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
			}

		}

		// 用于判断是新增还是更新图片
		Long pictureId = null;
		if (pictureUploadRequest != null) {
			pictureId = pictureUploadRequest.getId();
		}
		// 如果是更新图片，需要校验图片是否存在
		if (pictureId != null) {
			Picture oldPicture = pictureDomainService.getById(pictureId);
			ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
			// // 仅本人或管理员可编辑  这里改为了注解校验
			// if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			// 	throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			// }

			// 校验空间是否一致
			// 没传 spaceId，则复用原有图片的 spaceId
			if (spaceId == null) {
				if (oldPicture.getSpaceId() != null) {
					spaceId = oldPicture.getSpaceId();
				}
			} else {
				// 传了 spaceId，必须和原有图片一致
				if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
				}
			}

			// 删除 COS 的文件
			this.clearPictureFile(oldPicture);
		}

		// 上传图片，得到信息
		// 按照用户 id 划分目录 => 按照空间划分目录
		String uploadPathPrefix;
		if (spaceId == null) {
			uploadPathPrefix = String.format("public/%s", loginUser.getId());
		} else {
			uploadPathPrefix = String.format("space/%s", spaceId);
		}
		// UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
		// 根据 inputSource 类型区分上传方式
		PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
		if (inputSource instanceof String) {
			pictureUploadTemplate = urlPictureUpload;
		}
		UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
		// 构造要入库的图片信息
		Picture picture = new Picture();
		picture.setUrl(uploadPictureResult.getUrl());
		picture.setSpaceId(spaceId);
		// 设置缩略图
		picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
		// 原图大小/原图 url/缩略图 url
		picture.setOriginSize(uploadPictureResult.getOriginSize());
		picture.setOriginUrl(uploadPictureResult.getOriginUrl());

		// 要入库的图片名称
		String picName = uploadPictureResult.getPicName();
		if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
			picName = pictureUploadRequest.getPicName();
		}
		if (pictureUploadRequest != null && ObjUtil.isNotEmpty(pictureUploadRequest.getCategory())) {
			picture.setCategory(pictureUploadRequest.getCategory());
		}
		if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getTags())) {
			picture.setTags(pictureUploadRequest.getTags());
		}
		picture.setName(picName);
		picture.setPicSize(uploadPictureResult.getPicSize());
		picture.setPicWidth(uploadPictureResult.getPicWidth());
		picture.setPicHeight(uploadPictureResult.getPicHeight());
		picture.setPicScale(uploadPictureResult.getPicScale());
		picture.setPicFormat(uploadPictureResult.getPicFormat());
		// 存储图片主色调
		picture.setPicColor(uploadPictureResult.getPicColor());

		picture.setUserId(loginUser.getId());
		// 补充审核参数
		this.fillReviewParams(picture, loginUser);
		// 如果 pictureId 不为空，表示更新，否则是新增
		if (pictureId != null) {
			// 如果是更新，需要补充 id 和编辑时间
			picture.setId(pictureId);
			picture.setEditTime(new Date());
		}

		// 开启事务
		Long finalSpaceId = spaceId;
		transactionTemplate.execute(status -> {
			boolean result = pictureDomainService.saveOrUpdate(picture);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
			if (finalSpaceId != null) {
				LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
				updateWrapper.eq(Space::getId, finalSpaceId);
				updateWrapper.setSql("totalSize = totalSize + " + picture.getPicSize());
				updateWrapper.setSql("totalCount = totalCount + 1");
				boolean update = spaceApplicationService.updateSpaceAsAdmin(updateWrapper);
				ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
			}
			return picture;
		});

		return PictureVO.objToVo(picture);
	}

	/**
	 * 填充审核参数
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	@Override
	public void fillReviewParams(Picture picture, User loginUser) {
		pictureDomainService.fillReviewParams(picture, loginUser);
	}

	/**
	 * 获取图片封装类
	 *
	 * @param picture 图片
	 * @param request HttpServletRequest
	 * @return 图片封装类
	 */
	@Override
	public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
		// 对象转封装类
		PictureVO pictureVO = PictureVO.objToVo(picture);
		// 关联查询用户信息
		Long userId = picture.getUserId();
		if (userId != null && userId > 0) {
			User user = userApplicationService.getUserById(userId);
			UserVO userVO = userApplicationService.getUserVO(user);
			pictureVO.setUser(userVO);
		}
		// 查询当前图片的分类和标签的信息
		List<String> ctIds = new ArrayList<>();
		if (StrUtil.isNotBlank(picture.getTags())) {
			ctIds.addAll(Arrays.stream(picture.getTags().split(",")).collect(Collectors.toList()));
		}
		if (StrUtil.isNotBlank(picture.getCategory())) {
			ctIds.add(picture.getCategory());
		}
		if (!ctIds.isEmpty()) {
			List<CategoryTag> categoryTagList = categoryTagDomainService.getCategoryTagList(new LambdaQueryWrapper<CategoryTag>()
					.in(CategoryTag::getId, ctIds)
			);
			Map<Integer, List<CategoryTag>> typeMap = categoryTagList.stream().collect(Collectors.groupingBy(CategoryTag::getType));
			if (typeMap.get(CategoryTagEnum.CATEGORY.getValue()) != null) {
				pictureVO.setCategory(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getName());
				pictureVO.setCategoryId(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getId());
			}
			if (typeMap.get(CategoryTagEnum.TAG.getValue()) != null) {
				pictureVO.setTags(typeMap.get(CategoryTagEnum.TAG.getValue())
						.stream()
						.map(CategoryTag::getName)
						.collect(Collectors.toList()));
				pictureVO.setTagIds(typeMap.get(CategoryTagEnum.TAG.getValue())
						.stream()
						.map(CategoryTag::getId)
						.collect(Collectors.toList()));
			}
		}
		return pictureVO;
	}

	/**
	 * 分页获取图片封装
	 *
	 * @param picturePage 图片分页对象
	 * @param request     HttpServletRequest
	 * @return Page<PictureVO>
	 */
	@Override
	public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
		List<Picture> pictureList = picturePage.getRecords();
		Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
		if (CollUtil.isEmpty(pictureList)) {
			return pictureVOPage;
		}
		// 对象列表 => 封装对象列表
		List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userApplicationService.listUserByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		pictureVOList.forEach(pictureVO -> {
			Long userId = pictureVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			pictureVO.setUser(userApplicationService.getUserVO(user));
			// 查询当前图片的分类和标签信息
			List<String> ctIds = new ArrayList<>();
			if (pictureVO.getTags() != null && !pictureVO.getTags().isEmpty()) {
				ctIds.addAll(pictureVO.getTags());
			}
			if (StrUtil.isNotBlank(pictureVO.getCategory())) {
				ctIds.add(pictureVO.getCategory());
			}
			if (!ctIds.isEmpty()) {
				List<CategoryTag> categoryTagList = categoryTagDomainService.getCategoryTagList(new LambdaQueryWrapper<CategoryTag>()
						.in(CategoryTag::getId, ctIds)
				);
				Map<Integer, List<CategoryTag>> typeMap = categoryTagList.stream().collect(Collectors.groupingBy(CategoryTag::getType));
				if (typeMap.get(CategoryTagEnum.CATEGORY.getValue()) != null) {
					pictureVO.setCategory(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getName());
				}
				if (typeMap.get(CategoryTagEnum.TAG.getValue()) != null) {
					pictureVO.setTags(typeMap.get(CategoryTagEnum.TAG.getValue())
							.stream()
							.map(CategoryTag::getName)
							.collect(Collectors.toList()));
				}
			}
		});
		pictureVOPage.setRecords(pictureVOList);
		return pictureVOPage;
	}

	/**
	 * 获取查询条件
	 *
	 * @param pictureQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
		return pictureDomainService.getQueryWrapper(pictureQueryRequest);
	}

	/**
	 * 图片审核
	 *
	 * @param pictureReviewRequest 图片审核请求对象
	 * @param loginUser            登录用户
	 */
	@Override
	public Boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
		return pictureDomainService.doPictureReview(pictureReviewRequest, loginUser);
	}

	/**
	 * 批量抓取和创建图片
	 *
	 * @param pictureUploadByBatchRequest 图片批量上传请求对象
	 * @param loginUser                   登录用户
	 * @return 成功创建的图片数
	 */
	@Override
	public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
		String searchText = pictureUploadByBatchRequest.getSearchText();
		// 格式化数量
		Integer count = pictureUploadByBatchRequest.getCount();
		// 获取图片名称前缀, 如果没有则默认使用关键词
		String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
		if (StrUtil.isBlank(namePrefix)) {
			namePrefix = searchText;
		}
		ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
		// 要抓取的地址
		String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
		Document document;
		try {
			document = Jsoup.connect(fetchUrl).get();
		} catch (IOException e) {
			log.error("获取页面失败", e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
		}
		Element div = document.getElementsByClass("dgControl").first();
		if (ObjUtil.isNull(div)) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
		}
		// TODO 获取高清的图片
		// Elements imgElementList = div.select("img.mimg");
		Elements imgElementList = div.select(".iusc");
		int uploadCount = 0;
		for (Element imgElement : imgElementList) {
			// TODO 获取高清的图片 START
			// String fileUrl = imgElement.attr("src");
			String dataM = imgElement.attr("m");
			String fileUrl;
			try {
				fileUrl = JSONUtil.parseObj(dataM).getStr("murl");
			} catch (Exception e) {
				log.error("解析图片数据失败", e);
				continue;
			}
			// TODO 获取高清的图片 END

			if (StrUtil.isBlank(fileUrl)) {
				log.info("当前链接为空，已跳过: {}", fileUrl);
				continue;
			}
			// 处理图片上传地址，防止出现转义问题
			int questionMarkIndex = fileUrl.indexOf("?");
			if (questionMarkIndex > -1) {
				fileUrl = fileUrl.substring(0, questionMarkIndex);
			}
			// 上传图片
			PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
			if (StrUtil.isNotBlank(namePrefix)) {
				// 设置图片名称，序号连续递增
				pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
			}
			if (ObjUtil.isNotEmpty(pictureUploadByBatchRequest.getCategory())) {
				pictureUploadRequest.setCategory(pictureUploadByBatchRequest.getCategory());
			}
			if (pictureUploadByBatchRequest.getTags() != null && !pictureUploadByBatchRequest.getTags().isEmpty()) {
				pictureUploadRequest.setTags(String.join(",", pictureUploadByBatchRequest.getTags()));
			}

			try {
				PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
				log.info("图片上传成功, id = {}", pictureVO.getId());
				uploadCount++;
			} catch (Exception e) {
				log.error("图片上传失败", e);
				continue;
			}
			if (uploadCount >= count) {
				break;
			}
		}
		return uploadCount;
	}

	/**
	 * 清理图片文件
	 *
	 * @param oldPicture 图片对象
	 */
	@Override
	public void clearPictureFile(Picture oldPicture) {
		pictureDomainService.clearPictureFile(oldPicture);
	}

	private String urlPath(String url) {
		// 定义正则表达式，匹配协议和域名后面的路径部分
		String regex = "https?://[^/]+(/.*)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			// 获取匹配的路径部分
			return matcher.group(1);
		}
		return "";
	}

	/**
	 * 编辑图片
	 *
	 * @param pictureEditRequest 图片编辑请求
	 * @param loginUser          当前登录用户
	 */
	@Override
	public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
		// 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureEditRequest, picture);

		List<String> inputTagList = pictureEditRequest.getTags();
		// 使用分类标签表的方式
		if (pictureEditRequest.getInputTagList() != null && !pictureEditRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureEditRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginUser.getId());
				categoryTagApplicationService.addCategoryTag(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		picture.setTags(inputTagListStr);

		// 设置编辑时间
		picture.setEditTime(new Date());
		// 数据校验
		picture.validPictureEdit();
		// 判断是否存在
		long id = pictureEditRequest.getId();
		Picture oldPicture = pictureDomainService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 校验权限
		checkPictureAuth(loginUser, oldPicture);
		// 补充审核参数
		this.fillReviewParams(picture, loginUser);
		// 操作数据库
		boolean result = pictureDomainService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}

	/**
	 * 删除图片
	 *
	 * @param pictureId 图片ID
	 * @param loginUser 当前登录用户
	 */
	@Override
	public Boolean deletePicture(long pictureId, User loginUser) {
		ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 判断是否存在
		Picture oldPicture = pictureDomainService.getById(pictureId);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

		// 校验权限
		checkPictureAuth(loginUser, oldPicture);
		// 开启事务
		transactionTemplate.execute(status -> {
			// 操作数据库
			boolean result = pictureDomainService.removeById(pictureId);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
			// 释放额度
			Long spaceId = oldPicture.getSpaceId();
			if (spaceId != null) {
				LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
				updateWrapper.eq(Space::getId, spaceId);
				updateWrapper.setSql("totalSize = totalSize - " + oldPicture.getPicSize());
				updateWrapper.setSql("totalCount = totalCount - 1");
				boolean update = spaceApplicationService.updateSpaceAsAdmin(updateWrapper);
				ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
			}
			return true;
		});
		// 异步清理文件
		this.clearPictureFile(oldPicture);
		return true;
	}

	/**
	 * 检查图片权限
	 *
	 * @param loginUser 当前登录用户
	 * @param picture   当前图片对象
	 */
	@Override
	public void checkPictureAuth(User loginUser, Picture picture) {
		pictureDomainService.checkPictureAuth(loginUser, picture);
	}

	@Override
	public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
		// 1. 校验参数
		ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 2. 校验空间权限
		Space space = spaceApplicationService.getSpaceById(spaceId);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		if (!loginUser.getId().equals(space.getUserId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
		}
		// 3. 查询该空间下所有图片（必须有主色调）
		List<Picture> pictureList = pictureDomainService.lambdaQuery()
				.eq(Picture::getSpaceId, spaceId)
				.isNotNull(Picture::getPicColor)
				.list();
		// 如果没有图片，直接返回空列表
		if (CollUtil.isEmpty(pictureList)) {
			return Collections.emptyList();
		}
		// 将目标颜色转为 Color 对象
		Color targetColor = Color.decode(picColor);
		// 4. 计算相似度并排序
		List<Picture> sortedPictures = pictureList.stream()
				.sorted(Comparator.comparingDouble(picture -> {
					// 提取图片主色调
					String hexColor = picture.getPicColor();
					// 没有主色调的图片放到最后
					if (StrUtil.isBlank(hexColor)) {
						return Double.MAX_VALUE;
					}
					Color pictureColor = Color.decode(hexColor);
					// 越大越相似
					return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
				}))
				// 取前 12 个
				.limit(12)
				.collect(Collectors.toList());

		// 转换为 PictureVO
		return sortedPictures.stream()
				.map(PictureVO::objToVo)
				.collect(Collectors.toList());
	}

	/**
	 * 批量编辑图片
	 *
	 * @param pictureEditByBatchRequest 图片批量编辑请求
	 * @param loginUser                 当前图片对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
		List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
		Long spaceId = pictureEditByBatchRequest.getSpaceId();
		String category = pictureEditByBatchRequest.getCategory();
		List<String> tags = pictureEditByBatchRequest.getTags();

		// 1. 校验参数
		ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 2. 校验空间权限
		Space space = spaceApplicationService.getSpaceById(spaceId);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		if (!loginUser.getId().equals(space.getUserId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
		}

		List<String> inputTagList = pictureEditByBatchRequest.getTags();
		// 使用分类标签表的方式
		if (pictureEditByBatchRequest.getInputTagList() != null && !pictureEditByBatchRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureEditByBatchRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginUser.getId());
				categoryTagApplicationService.addCategoryTag(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);

		// 3. 查询指定图片，仅选择需要的字段
		List<Picture> pictureList = pictureDomainService.lambdaQuery()
				.select(Picture::getId, Picture::getSpaceId)
				.eq(Picture::getSpaceId, spaceId)
				.in(Picture::getId, pictureIdList)
				.list();

		if (pictureList.isEmpty()) {
			return;
		}
		// 4. 更新分类和标签
		pictureList.forEach(picture -> {
			if (StrUtil.isNotBlank(category)) {
				picture.setCategory(category);
			}
			if (StrUtil.isNotBlank(inputTagListStr)) {
				picture.setTags(inputTagListStr);
			}
		});

		// 批量重命名
		String nameRule = pictureEditByBatchRequest.getNameRule();
		fillPictureWithNameRule(pictureList, nameRule);

		// 5. 批量更新
		boolean result = pictureDomainService.updateBatchById(pictureList);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}

	/**
	 * nameRule 格式：图片{序号}
	 *
	 * @param pictureList 图片列表
	 * @param nameRule    命名规则
	 */
	private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
		if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
			return;
		}
		long count = 1;
		try {
			for (Picture picture : pictureList) {
				String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
				picture.setName(pictureName);
			}
		} catch (Exception e) {
			log.error("名称解析错误", e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
		}
	}

	/**
	 * 创建图片扩图任务
	 *
	 * @param createPictureOutPaintingTaskRequest 图片扩图任务请求
	 * @param loginUser                           当前登录用户
	 * @return 扩图任务响应
	 */
	@Override
	public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
		return pictureDomainService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
	}

	/**
	 * 根据图片ID获取图片
	 *
	 * @param pictureId 图片ID
	 * @return 图片
	 */
	@Override
	public Picture getPictureById(Long pictureId) {
		return pictureDomainService.getById(pictureId);
	}

	/**
	 * 更新图片
	 *
	 * @param picture 图片
	 * @return 是否成功
	 */
	@Override
	public Boolean updatePictureById(Picture picture) {
		return pictureDomainService.updateById(picture);
	}

	/**
	 * 获取图片列表（分页）
	 *
	 * @param page         分页对象
	 * @param queryWrapper 查询条件
	 * @return 图片分页列表
	 */
	@Override
	public Page<Picture> getPictureListPage(Page<Picture> page, QueryWrapper<Picture> queryWrapper) {
		return pictureDomainService.page(page, queryWrapper);
	}

	/**
	 * 获取图片指定字段列表
	 *
	 * @param queryWrapper 查询条件
	 * @return 图片指定字段的列表
	 */
	@Override
	public List<Object> getPictureAppointFieldList(QueryWrapper<Picture> queryWrapper) {
		return pictureDomainService.getBaseMapper().selectObjs(queryWrapper);
	}

	/**
	 * 获取图片指定字段 Map 列表
	 *
	 * @param queryWrapper 查询条件
	 * @return 图片指定字段的列表
	 */
	@Override
	public List<Map<String, Object>> getPictureAppointFieldMaps(QueryWrapper<Picture> queryWrapper) {
		return pictureDomainService.getBaseMapper().selectMaps(queryWrapper);
	}
}




