package com.baolong.picture.domain.picture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.domain.picture.enums.PictureReviewStatusEnum;
import com.baolong.picture.domain.picture.service.PictureDomainService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.api.CosManager;
import com.baolong.picture.infrastructure.api.aliyunai.AliYunAiApi;
import com.baolong.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.baolong.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.infrastructure.mapper.PictureMapper;
import com.baolong.picture.interfaces.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.baolong.picture.interfaces.dto.picture.PictureQueryRequest;
import com.baolong.picture.interfaces.dto.picture.PictureReviewRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片领域服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PictureDomainServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureDomainService {

	@Resource
	private CosManager cosManager;

	@Resource
	private AliYunAiApi aliYunAiApi;

	/**
	 * 填充审核参数
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	@Override
	public void fillReviewParams(Picture picture, User loginUser) {
		if (loginUser.isAdmin()) {
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
	public Boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
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
		return this.updateById(updatePicture);
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
			if (!picture.getUserId().equals(loginUser.getId()) && !loginUser.isAdmin()) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
		} else {
			// 私有空间，仅空间管理员可操作
			if (!picture.getUserId().equals(loginUser.getId())) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
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




