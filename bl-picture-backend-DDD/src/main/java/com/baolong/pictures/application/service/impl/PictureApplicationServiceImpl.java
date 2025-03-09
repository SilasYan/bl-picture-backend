package com.baolong.pictures.application.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baolong.pictures.application.service.PictureApplicationService;
import com.baolong.pictures.application.service.SpaceApplicationService;
import com.baolong.pictures.application.service.UserApplicationService;
import com.baolong.pictures.domain.picture.entity.Picture;
import com.baolong.pictures.domain.picture.service.PictureDomainService;
import com.baolong.pictures.domain.user.entity.User;
import com.baolong.pictures.infrastructure.common.DeleteRequest;
import com.baolong.pictures.infrastructure.common.page.PageVO;
import com.baolong.pictures.infrastructure.exception.ErrorCode;
import com.baolong.pictures.infrastructure.exception.ThrowUtils;
import com.baolong.pictures.interfaces.assembler.PictureAssembler;
import com.baolong.pictures.interfaces.dto.picture.PictureBatchEditRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureEditRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureQueryRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureReviewRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureUpdateRequest;
import com.baolong.pictures.interfaces.dto.picture.PictureUploadRequest;
import com.baolong.pictures.interfaces.vo.picture.PictureSimpleVO;
import com.baolong.pictures.interfaces.vo.picture.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图片应用服务实现类
 */
@Service
@RequiredArgsConstructor
public class PictureApplicationServiceImpl implements PictureApplicationService {

	private final PictureDomainService pictureDomainService;
	private final UserApplicationService userApplicationService;
	private final SpaceApplicationService spaceApplicationService;

	@Resource
	private TransactionTemplate transactionTemplate;

	// region 增删改相关（包含上传图片）

	/**
	 * 上传图片
	 *
	 * @param pictureInputSource   图片输入源
	 * @param pictureUploadRequest 图片上传请求
	 * @return PictureVO
	 */
	@Override
	public PictureVO uploadPicture(Object pictureInputSource, PictureUploadRequest pictureUploadRequest) {
		Picture picture = PictureAssembler.toPictureEntity(pictureUploadRequest);
		User loginUser = userApplicationService.getLoginUser();
		Long userId = loginUser.getId();
		picture.setUserId(userId);

		Picture oldPicture;
		// 图片 ID 有值则是更新, 需要判断权限
		if (picture.getId() != null) {
			// 查询旧的图片是否存在
			oldPicture = pictureDomainService.getPictureById(picture.getId());
			ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
			// 校验图片操作权限
			this.checkPictureChangeAuth(oldPicture, loginUser);
		} else {
			oldPicture = null;
		}

		// 跟空间相关, 则需要校验空间权限
		Long spaceId = pictureUploadRequest.getSpaceId();
		spaceApplicationService.checkSpaceUploadAuth(picture.getSpaceId(), loginUser);

		// 校验并填充审核参数
		this.checkAndFillReviewParams(picture, loginUser);

		// 开启事务执行数据库操作
		Picture resultPicture = transactionTemplate.execute(status -> {
			Boolean result = pictureDomainService.uploadPicture(
					pictureInputSource == null ? pictureUploadRequest.getFileUrl() : pictureInputSource, picture
			);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
			if (oldPicture != null) {
				// 更新空间额度
				Boolean updated = spaceApplicationService.updateSpaceSizeAndCount(spaceId, -oldPicture.getOriginSize(), -1L);
				ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
				// 删除存储服务器中的旧图片
				pictureDomainService.clearPictureFile(oldPicture);
			}
			if (ObjectUtil.isNotNull(spaceId) || !spaceId.equals(0L)) {
				Boolean updated = spaceApplicationService.updateSpaceSizeAndCount(spaceId, picture.getOriginSize(), 1L);
				ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
			}
			return picture;
		});

		return PictureAssembler.toPictureVO(resultPicture);
	}

	/**
	 * 删除图片
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	@Override
	public Boolean deletePicture(DeleteRequest deleteRequest) {
		// 查询是否存在
		Picture picture = pictureDomainService.getPictureById(deleteRequest.getId());
		ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
		User loginUser = userApplicationService.getLoginUser();
		// 校验图片操作权限
		this.checkPictureChangeAuth(picture, loginUser);
		// 跟空间相关, 则需要校验空间权限
		spaceApplicationService.checkSpaceUploadAuth(picture.getSpaceId(), loginUser);

		// 开启事务执行数据库操作
		transactionTemplate.execute(status -> {
			// 操作数据库
			Boolean result = pictureDomainService.deletePicture(deleteRequest.getId());
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
			// 更新空间额度
			Boolean updated = spaceApplicationService.updateSpaceSizeAndCount(picture.getSpaceId(), -picture.getOriginSize(), -1L);
			ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
			// 删除存储服务器中的旧图片
			pictureDomainService.clearPictureFile(picture);
			return true;
		});
		return true;
	}

	/**
	 * 更新图片
	 *
	 * @param pictureUpdateRequest 图片更新请求
	 * @return 是否成功
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean updatePicture(PictureUpdateRequest pictureUpdateRequest) {
		Picture picture = PictureAssembler.toPictureEntity(pictureUpdateRequest);
		// 数据校验
		picture.validPictureUpdateAndEdit();
		// 判断图片是否存在
		Boolean existed = pictureDomainService.existPictureById(pictureUpdateRequest.getId());
		ThrowUtils.throwIf(!existed, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
		User loginUser = userApplicationService.getLoginUser();
		// 校验图片操作权限
		this.checkPictureChangeAuth(picture, loginUser);
		// 校验并填充审核参数
		this.checkAndFillReviewParams(picture, loginUser);
		/* TODO 标签的处理没完成
		    // 使用分类标签表的方式
		List<String> inputTagList = pictureUpdateRequest.getTags();
		if (pictureUpdateRequest.getInputTagList() != null && !pictureUpdateRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureUpdateRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginTUser.getId());
				categoryTagApplicationService.addCategoryTag(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		tePicture.setTags(inputTagListStr);*/
		Boolean flag = pictureDomainService.updatePicture(picture);
		ThrowUtils.throwIf(!flag, ErrorCode.OPERATION_ERROR, "更新图片失败");
		return true;
	}

	/**
	 * 编辑图片
	 *
	 * @param pictureEditRequest 图片编辑请求
	 * @return 是否成功
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean editPicture(PictureEditRequest pictureEditRequest) {
		Picture picture = PictureAssembler.toPictureEntity(pictureEditRequest);
		// 数据校验
		picture.validPictureUpdateAndEdit();
		// 判断图片是否存在
		Boolean existed = pictureDomainService.existPictureById(pictureEditRequest.getId());
		ThrowUtils.throwIf(!existed, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
		User loginUser = userApplicationService.getLoginUser();
		// 校验图片操作权限
		this.checkPictureChangeAuth(picture, loginUser);
		// 校验并填充审核参数
		this.checkAndFillReviewParams(picture, loginUser);
		// 跟空间相关, 则需要校验空间权限
		spaceApplicationService.checkSpaceUploadAuth(picture.getSpaceId(), loginUser);
		// 设置编辑时间
		picture.setEditTime(new Date());
		/* TODO 标签的处理没完成
		   List<String> inputTagList = pictureEditRequest.getTags();
		// 使用分类标签表的方式
		if (pictureEditRequest.getInputTagList() != null && !pictureEditRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureEditRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginTUser.getId());
				categoryTagApplicationService.addCategoryTag(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		tePicture.setTags(inputTagListStr);*/
		Boolean flag = pictureDomainService.editPicture(picture);
		ThrowUtils.throwIf(!flag, ErrorCode.OPERATION_ERROR, "编辑图片失败");
		return true;
	}

	/**
	 * 编辑图片（批量）
	 *
	 * @param pictureBatchEditRequest 图片批量编辑请求
	 * @return 是否成功
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean editPictureBatch(PictureBatchEditRequest pictureBatchEditRequest) {
		User loginUser = userApplicationService.getLoginUser();
		// 跟空间相关, 则需要校验空间权限
		Long spaceId = pictureBatchEditRequest.getSpaceId();
		spaceApplicationService.checkSpaceUploadAuth(spaceId, loginUser);
		List<Picture> pictureList = PictureAssembler.toPictureEntityList(pictureBatchEditRequest);
		// 填充图片名称规则
		this.fillPictureNameRuleBatch(pictureList, pictureBatchEditRequest.getNameRule());
		/* TODO 标签未处理
		List<String> inputTagList = pictureBatchEditRequest.getTags();
		// 使用分类标签表的方式
		if (pictureBatchEditRequest.getInputTagList() != null && !pictureBatchEditRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureBatchEditRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginTUser.getId());
				categoryTagApplicationService.addCategoryTag(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		// 遍历更新图片的相关信息
		pictureList.forEach(picture -> {
		});*/
		Boolean flag = pictureDomainService.editPictureBatch(pictureList);
		ThrowUtils.throwIf(!flag, ErrorCode.OPERATION_ERROR, "图片批量编辑失败");
		return false;
	}

	/**
	 * 审核图片
	 *
	 * @param pictureReviewRequest 图片审核请求
	 * @return 是否成功
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean reviewPicture(PictureReviewRequest pictureReviewRequest) {
		List<Picture> pictureList = PictureAssembler.toPictureEntityList(pictureReviewRequest);
		User loginUser = userApplicationService.getLoginUser();
		pictureList.forEach(picture -> picture.setReviewerUser(loginUser.getId()));
		Boolean flag = pictureDomainService.reviewPicture(pictureList);
		ThrowUtils.throwIf(!flag, ErrorCode.OPERATION_ERROR, "审核图片失败");
		return true;
	}

	// endregion 增删改相关（包含上传图片）

	// region 查询相关

	/**
	 * 根据图片 ID 获取图片信息
	 *
	 * @param pictureId 图片 ID
	 * @return 图片信息
	 */
	@Override
	public Picture getPictureInfoById(Long pictureId) {
		return pictureDomainService.getPictureById(pictureId);
	}

	/**
	 * 根据图片 ID 获取图片详情
	 *
	 * @param pictureId 图片 ID
	 * @return 图片详情
	 */
	@Override
	public PictureVO getPictureDetailById(Long pictureId) {
		Picture picture = pictureDomainService.getPictureById(pictureId);
		PictureVO pictureVO = PictureAssembler.toPictureVO(picture);
		// 查询用户信息
		User user = userApplicationService.getUserInfoById(picture.getUserId());
		if (user != null) {
			pictureVO.setUserName(user.getUserName());
			pictureVO.setUserAvatar(user.getUserAvatar());
		} else {
			pictureVO.setUserName("未知用户");
		}
		// TODO 查询分类信息、标签信息
		return pictureVO;
	}

	/**
	 * 获取首页图片分页列表（简单字段）
	 *
	 * @param pictureQueryRequest 图片查询请求
	 * @return 图片分页列表
	 */
	@Override
	public PageVO<PictureSimpleVO> getPicturePageListAsSimple(PictureQueryRequest pictureQueryRequest) {
		Page<Picture> picturePage = pictureDomainService.getPicturePageListAsSimple(
				pictureQueryRequest.getPage(Picture.class), this.getLambdaQueryWrapper(pictureQueryRequest)
		);
		List<PictureSimpleVO> simpleVOS = picturePage.getRecords().stream()
				.map(PictureAssembler::toPictureSimpleVO)
				.collect(Collectors.toList());
		// 查询图片的用户信息
		Set<Long> userIds = simpleVOS.stream().map(PictureSimpleVO::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userListMap = userApplicationService.getUserListByIds(userIds)
				.stream()
				.collect(Collectors.groupingBy(User::getId));
		simpleVOS.forEach(picture -> {
			Long userId = picture.getUserId();
			if (userListMap.containsKey(userId)) {
				picture.setUserName(userListMap.get(userId).get(0).getUserName());
				picture.setUserAvatar(userListMap.get(userId).get(0).getUserAvatar());
			}
			// TODO 分类和标签的处理
		});

		// TODO 是否需要加入到缓存?
		return new PageVO<>(picturePage.getCurrent()
				, picturePage.getSize()
				, picturePage.getTotal()
				, picturePage.getPages()
				, simpleVOS
		);
	}

	/**
	 * 获取图片分页列表（管理员）
	 *
	 * @param pictureQueryRequest 图片查询请求
	 * @return 图片分页列表
	 */
	@Override
	public PageVO<Picture> getPicturePageListAsAdmin(PictureQueryRequest pictureQueryRequest) {
		Page<Picture> picturePage = pictureDomainService.getPicturePageListAsAdmin(
				pictureQueryRequest.getPage(Picture.class), this.getLambdaQueryWrapper(pictureQueryRequest)
		);
		return PageVO.from(picturePage);
	}

	// /**
	//  * 根据颜色搜索图片
	//  *
	//  * @param pictureColorSearchRequest 图片颜色搜索请求
	//  * @return 图片列表
	//  */
	// @Override
	// public List<PictureVO> searchPictureByColor(PictureColorSearchRequest pictureColorSearchRequest) {
	//	
	// 	// 3. 查询该空间下所有图片（必须有主色调）
	// 	List<TePicture> tePictureList = pictureDomainService.lambdaQuery()
	// 			.eq(TePicture::getSpaceId, spaceId)
	// 			.isNotNull(TePicture::getPicColor)
	// 			.list();
	// 	// 如果没有图片，直接返回空列表
	// 	if (CollUtil.isEmpty(tePictureList)) {
	// 		return Collections.emptyList();
	// 	}
	// 	// 将目标颜色转为 Color 对象
	// 	Color targetColor = Color.decode(picColor);
	// 	// 4. 计算相似度并排序
	// 	List<TePicture> sortedTePictures = tePictureList.stream()
	// 			.sorted(Comparator.comparingDouble(picture -> {
	// 				// 提取图片主色调
	// 				String hexColor = picture.getPicColor();
	// 				// 没有主色调的图片放到最后
	// 				if (StrUtil.isBlank(hexColor)) {
	// 					return Double.MAX_VALUE;
	// 				}
	// 				Color pictureColor = Color.decode(hexColor);
	// 				// 越大越相似
	// 				return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
	// 			}))
	// 			// 取前 12 个
	// 			.limit(12)
	// 			.collect(Collectors.toList());
	//
	// 	// 转换为 PictureVO
	// 	return sortedTePictures.stream()
	// 			.map(PictureVO::objToVo)
	// 			.collect(Collectors.toList());
	// }

	// endregion 查询相关

	// region 其他方法

	/**
	 * 获取查询条件对象（Lambda）
	 *
	 * @param pictureQueryRequest 图片查询请求
	 * @return 查询条件对象（Lambda）
	 */
	@Override
	public LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest) {
		ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
		return pictureDomainService.getLambdaQueryWrapper(pictureQueryRequest);
	}

	/**
	 * 校验并填充审核参数
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	@Override
	public void checkAndFillReviewParams(Picture picture, User loginUser) {
		pictureDomainService.checkAndFillReviewParams(picture, loginUser);
	}

	/**
	 * 校验图片操作权限
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	@Override
	public void checkPictureChangeAuth(Picture picture, User loginUser) {
		pictureDomainService.checkPictureChangeAuth(picture, loginUser);
	}

	/**
	 * 填充图片名称规则
	 *
	 * @param pictureList 图片列表
	 * @param nameRule    名称规则
	 */
	@Override
	public void fillPictureNameRuleBatch(List<Picture> pictureList, String nameRule) {
		pictureDomainService.fillPictureNameRuleBatch(pictureList, nameRule);
	}

	// endregion 其他方法

	//
	// /**
	//  * 批量抓取和创建图片
	//  *
	//  * @param pictureGrabUploadRequest 图片批量上传请求对象
	//  * @param loginTUser               登录用户
	//  * @return 成功创建的图片数
	//  */
	// @Override
	// public Integer uploadPictureByBatch(PictureGrabUploadRequest pictureGrabUploadRequest, User loginTUser) {
	// 	String searchText = pictureGrabUploadRequest.getSearchText();
	// 	// 格式化数量
	// 	Integer count = pictureGrabUploadRequest.getCount();
	// 	// 获取图片名称前缀, 如果没有则默认使用关键词
	// 	String namePrefix = pictureGrabUploadRequest.getNamePrefix();
	// 	if (StrUtil.isBlank(namePrefix)) {
	// 		namePrefix = searchText;
	// 	}
	// 	ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
	// 	// 要抓取的地址
	// 	String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
	// 	Document document;
	// 	try {
	// 		document = Jsoup.connect(fetchUrl).get();
	// 	} catch (IOException e) {
	// 		log.error("获取页面失败", e);
	// 		throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
	// 	}
	// 	Element div = document.getElementsByClass("dgControl").first();
	// 	if (ObjUtil.isNull(div)) {
	// 		throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
	// 	}
	// 	// TODO 获取高清的图片
	// 	// Elements imgElementList = div.select("img.mimg");
	// 	Elements imgElementList = div.select(".iusc");
	// 	int uploadCount = 0;
	// 	for (Element imgElement : imgElementList) {
	// 		// TODO 获取高清的图片 START
	// 		// String fileUrl = imgElement.attr("src");
	// 		String dataM = imgElement.attr("m");
	// 		String fileUrl;
	// 		try {
	// 			fileUrl = JSONUtil.parseObj(dataM).getStr("murl");
	// 		} catch (Exception e) {
	// 			log.error("解析图片数据失败", e);
	// 			continue;
	// 		}
	// 		// TODO 获取高清的图片 END
	//
	// 		if (StrUtil.isBlank(fileUrl)) {
	// 			log.info("当前链接为空，已跳过: {}", fileUrl);
	// 			continue;
	// 		}
	// 		// 处理图片上传地址，防止出现转义问题
	// 		int questionMarkIndex = fileUrl.indexOf("?");
	// 		if (questionMarkIndex > -1) {
	// 			fileUrl = fileUrl.substring(0, questionMarkIndex);
	// 		}
	// 		// 上传图片
	// 		PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
	// 		if (StrUtil.isNotBlank(namePrefix)) {
	// 			// 设置图片名称，序号连续递增
	// 			pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
	// 		}
	// 		if (ObjUtil.isNotEmpty(pictureGrabUploadRequest.getCategory())) {
	// 			pictureUploadRequest.setCategory(pictureGrabUploadRequest.getCategory());
	// 		}
	// 		if (pictureGrabUploadRequest.getTags() != null && !pictureGrabUploadRequest.getTags().isEmpty()) {
	// 			pictureUploadRequest.setTags(String.join(",", pictureGrabUploadRequest.getTags()));
	// 		}
	//
	// 		try {
	// 			PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginTUser);
	// 			log.info("图片上传成功, id = {}", pictureVO.getId());
	// 			uploadCount++;
	// 		} catch (Exception e) {
	// 			log.error("图片上传失败", e);
	// 			continue;
	// 		}
	// 		if (uploadCount >= count) {
	// 			break;
	// 		}
	// 	}
	// 	return uploadCount;
	// }
	// /**
	//  * 创建图片扩图任务
	//  *
	//  * @param createPictureOutPaintingTaskRequest 图片扩图任务请求
	//  * @param loginTUser                          当前登录用户
	//  * @return 扩图任务响应
	//  */
	// @Override
	// public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginTUser) {
	// 	return pictureDomainService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginTUser);
	// }
	//
	// /**
	//  * 获取图片指定字段列表
	//  *
	//  * @param queryWrapper 查询条件
	//  * @return 图片指定字段的列表
	//  */
	// @Override
	// public List<Object> getPictureAppointFieldList(QueryWrapper<TePicture> queryWrapper) {
	// 	return pictureDomainService.getBaseMapper().selectObjs(queryWrapper);
	// }
	//
	// /**
	//  * 获取图片指定字段 Map 列表
	//  *
	//  * @param queryWrapper 查询条件
	//  * @return 图片指定字段的列表
	//  */
	// @Override
	// public List<Map<String, Object>> getPictureAppointFieldMaps(QueryWrapper<TePicture> queryWrapper) {
	// 	return pictureDomainService.getBaseMapper().selectMaps(queryWrapper);
	// }
}




