package com.baolong.blpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baolong.blpicturebackend.api.aliyunai.AliYunAiApi;
import com.baolong.blpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.baolong.blpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.baolong.blpicturebackend.exception.BusinessException;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.manager.CosManager;
import com.baolong.blpicturebackend.manager.FileManager;
import com.baolong.blpicturebackend.manager.upload.FilePictureUpload;
import com.baolong.blpicturebackend.manager.upload.PictureUploadTemplate;
import com.baolong.blpicturebackend.manager.upload.UrlPictureUpload;
import com.baolong.blpicturebackend.mapper.PictureMapper;
import com.baolong.blpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditByBatchRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureQueryRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureReviewRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadRequest;
import com.baolong.blpicturebackend.model.dto.picture.UploadPictureResult;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.model.entity.Picture;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.CategoryTagEnum;
import com.baolong.blpicturebackend.model.enums.PictureReviewStatusEnum;
import com.baolong.blpicturebackend.model.vo.PictureVO;
import com.baolong.blpicturebackend.model.vo.UserVO;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.blpicturebackend.service.PictureService;
import com.baolong.blpicturebackend.service.SpaceService;
import com.baolong.blpicturebackend.service.UserService;
import com.baolong.blpicturebackend.utils.ColorSimilarUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author ADMIN
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-02-13 23:18:16
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
		implements PictureService {

	@Resource
	private FileManager fileManager;
	@Resource
	private UserService userService;
	@Resource
	private CategoryTagService categoryTagService;
	@Resource
	private FilePictureUpload filePictureUpload;
	@Resource
	private UrlPictureUpload urlPictureUpload;
	@Resource
	private CosManager cosManager;
	@Resource
	private SpaceService spaceService;

	@Resource
	private TransactionTemplate transactionTemplate;

	@Resource
	private AliYunAiApi aliYunAiApi;

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
			Space space = spaceService.getById(spaceId);
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
			Picture oldPicture = this.getById(pictureId);
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
			boolean result = this.saveOrUpdate(picture);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
			if (finalSpaceId != null) {
				boolean update = spaceService.lambdaUpdate()
						.eq(Space::getId, finalSpaceId)
						.setSql("totalSize = totalSize + " + picture.getPicSize())
						.setSql("totalCount = totalCount + 1")
						.update();
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
		if (userService.isAdmin(loginUser)) {
			// 管理员自动过审
			picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
			picture.setReviewerId(loginUser.getId());
			picture.setReviewMessage("管理员自动过审");
			picture.setReviewTime(new Date());
		} else {
			// 非管理员，创建或编辑都要改为待审核
			picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
		}
	}

	/**
	 * 校验图片对象
	 *
	 * @param picture 图片对象
	 */
	@Override
	public void validPicture(Picture picture) {
		ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
		// 从对象中取值
		Long id = picture.getId();
		String url = picture.getUrl();
		String introduction = picture.getIntroduction();
		// 修改数据时，id 不能为空，有参数则校验
		ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
		if (StrUtil.isNotBlank(url)) {
			ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
		}
		if (StrUtil.isNotBlank(introduction)) {
			ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
		}
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
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
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
			List<CategoryTag> categoryTagList = categoryTagService.list(new LambdaQueryWrapper<CategoryTag>()
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
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		pictureVOList.forEach(pictureVO -> {
			Long userId = pictureVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			pictureVO.setUser(userService.getUserVO(user));
			// 查询当前图片的分类和标签信息
			List<String> ctIds = new ArrayList<>();
			if (pictureVO.getTags() != null && !pictureVO.getTags().isEmpty()) {
				ctIds.addAll(pictureVO.getTags());
			}
			if (StrUtil.isNotBlank(pictureVO.getCategory())) {
				ctIds.add(pictureVO.getCategory());
			}
			if (!ctIds.isEmpty()) {
				List<CategoryTag> categoryTagList = categoryTagService.list(new LambdaQueryWrapper<CategoryTag>()
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
		QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
		if (pictureQueryRequest == null) {
			return queryWrapper;
		}
		// 从对象中取值
		Long id = pictureQueryRequest.getId();
		String name = pictureQueryRequest.getName();
		String introduction = pictureQueryRequest.getIntroduction();
		String category = pictureQueryRequest.getCategory();
		List<String> tags = pictureQueryRequest.getTags();
		Long picSize = pictureQueryRequest.getPicSize();
		Integer picWidth = pictureQueryRequest.getPicWidth();
		Integer picHeight = pictureQueryRequest.getPicHeight();
		Double picScale = pictureQueryRequest.getPicScale();
		String picFormat = pictureQueryRequest.getPicFormat();
		String searchText = pictureQueryRequest.getSearchText();
		Long userId = pictureQueryRequest.getUserId();
		String sortField = pictureQueryRequest.getSortField();
		String sortOrder = pictureQueryRequest.getSortOrder();
		// 从多字段中搜索
		if (StrUtil.isNotBlank(searchText)) {
			// 需要拼接查询条件
			queryWrapper.and(qw -> qw.like("name", searchText)
					.or()
					.like("introduction", searchText)
			);
		}
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
		queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
		queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
		queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
		queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
		queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
		queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
		queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
		// // JSON 数组查询
		// if (CollUtil.isNotEmpty(tags)) {
		// 	for (String tag : tags) {
		// 		queryWrapper.like("tags", "\"" + tag + "\"");
		// 	}
		// }

		Integer reviewStatus = pictureQueryRequest.getReviewStatus();
		String reviewMessage = pictureQueryRequest.getReviewMessage();
		Long reviewerId = pictureQueryRequest.getReviewerId();
		queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
		queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
		queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

		Long spaceId = pictureQueryRequest.getSpaceId();
		boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
		queryWrapper.isNull(nullSpaceId, "spaceId");

		Date startEditTime = pictureQueryRequest.getStartEditTime();
		Date endEditTime = pictureQueryRequest.getEndEditTime();
		queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
		queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

		// 拼接 分类标签列表的SQL
		if (CollUtil.isNotEmpty(tags)) {
			StringBuilder FIND_IN_SET_SQL = new StringBuilder(" (");
			for (String tag : tags) {
				FIND_IN_SET_SQL.append(" FIND_IN_SET (").append(tag).append(", tags) > 0 OR");
			}
			// 去掉字符串 FIND_IN_SET_SQL 末尾的 OR
			if (FIND_IN_SET_SQL.length() > 0 && FIND_IN_SET_SQL.lastIndexOf("OR") != -1) {
				FIND_IN_SET_SQL.setLength(FIND_IN_SET_SQL.lastIndexOf("OR"));
			}
			FIND_IN_SET_SQL.append(") ");
			queryWrapper.apply(FIND_IN_SET_SQL.toString());
		}

		// 排序
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

	/**
	 * 图片审核
	 *
	 * @param pictureReviewRequest 图片审核请求对象
	 * @param loginUser            登录用户
	 */
	@Override
	public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
		Long id = pictureReviewRequest.getId();
		Integer reviewStatus = pictureReviewRequest.getReviewStatus();
		PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
		if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 判断是否存在
		Picture oldPicture = this.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 已是该状态
		if (oldPicture.getReviewStatus().equals(reviewStatus)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
		}
		// 更新审核状态
		Picture updatePicture = new Picture();
		BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
		updatePicture.setReviewerId(loginUser.getId());
		updatePicture.setReviewTime(new Date());
		boolean result = this.updateById(updatePicture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
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
	@Async
	@Override
	public void clearPictureFile(Picture oldPicture) {
		// TODO 判断该图片是否被多条记录使用, 我觉得不会出现这种情况
		if (oldPicture == null) {
			return;
		}
		// FIXME 注意，这里的 url 包含了域名，实际上只要传 key 值（存储路径）就够了
		String url = oldPicture.getUrl();
		if (StrUtil.isNotBlank(url)) {
			cosManager.deleteObject(urlPath(url));
		}
		// 清理缩略图
		String thumbnailUrl = oldPicture.getThumbnailUrl();
		if (StrUtil.isNotBlank(thumbnailUrl)) {
			cosManager.deleteObject(urlPath(thumbnailUrl));
		}
		// 清理原图
		String originUrl = oldPicture.getOriginUrl();
		if (StrUtil.isNotBlank(originUrl)) {
			cosManager.deleteObject(urlPath(originUrl));
		}
		// TODO 这里有问题, MyBatisPlus 默认会拼接 idDelete=0 的条件; 更新当前图片在数据库中 resourceStatus
		// boolean result = this.update(null, new LambdaUpdateWrapper<Picture>()
		// 		.set(Picture::getResourceStatus, 1)
		// 		.eq(Picture::getId, oldPicture.getId())
		// 		.eq(Picture::getIsDelete,0)
		// );
		// if (!result) {
		// 	log.error("更新资源状态失败, id = {}", oldPicture.getId());
		// }
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
				categoryTagService.save(categoryTag);
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
		this.validPicture(picture);
		// 判断是否存在
		long id = pictureEditRequest.getId();
		Picture oldPicture = this.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 校验权限
		checkPictureAuth(loginUser, oldPicture);
		// 补充审核参数
		this.fillReviewParams(picture, loginUser);
		// 操作数据库
		boolean result = this.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}

	/**
	 * 删除图片
	 *
	 * @param pictureId 图片ID
	 * @param loginUser 当前登录用户
	 */
	@Override
	public void deletePicture(long pictureId, User loginUser) {
		ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 判断是否存在
		Picture oldPicture = this.getById(pictureId);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

		// 校验权限
		checkPictureAuth(loginUser, oldPicture);
		// 开启事务
		transactionTemplate.execute(status -> {
			// 操作数据库
			boolean result = this.removeById(pictureId);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
			// 释放额度
			Long spaceId = oldPicture.getSpaceId();
			if (spaceId != null) {
				boolean update = spaceService.lambdaUpdate()
						.eq(Space::getId, spaceId)
						.setSql("totalSize = totalSize - " + oldPicture.getPicSize())
						.setSql("totalCount = totalCount - 1")
						.update();
				ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
			}
			return true;
		});
		// 异步清理文件
		this.clearPictureFile(oldPicture);
	}

	/**
	 * 检查图片权限
	 *
	 * @param loginUser 当前登录用户
	 * @param picture   当前图片对象
	 */
	@Override
	public void checkPictureAuth(User loginUser, Picture picture) {
		Long spaceId = picture.getSpaceId();
		if (spaceId == null) {
			// 公共图库，仅本人或管理员可操作
			if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
		} else {
			// 私有空间，仅空间管理员可操作
			if (!picture.getUserId().equals(loginUser.getId())) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
		}
	}

	@Override
	public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
		// 1. 校验参数
		ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 2. 校验空间权限
		Space space = spaceService.getById(spaceId);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		if (!loginUser.getId().equals(space.getUserId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
		}
		// 3. 查询该空间下所有图片（必须有主色调）
		List<Picture> pictureList = this.lambdaQuery()
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
		Space space = spaceService.getById(spaceId);
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
				categoryTagService.save(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);

		// 3. 查询指定图片，仅选择需要的字段
		List<Picture> pictureList = this.lambdaQuery()
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
		boolean result = this.updateBatchById(pictureList);
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
		// 获取图片信息
		Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
		Picture picture = Optional.ofNullable(this.getById(pictureId))
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
		// 权限校验
		checkPictureAuth(loginUser, picture);
		// 构造请求参数
		CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
		CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
		input.setImageUrl(picture.getUrl());
		taskRequest.setInput(input);
		BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);
		// 创建任务
		return aliYunAiApi.createOutPaintingTask(taskRequest);
	}

}




