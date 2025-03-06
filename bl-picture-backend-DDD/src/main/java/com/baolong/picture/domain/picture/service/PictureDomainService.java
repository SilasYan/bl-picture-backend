package com.baolong.picture.domain.picture.service;

import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.baolong.picture.interfaces.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.baolong.picture.interfaces.dto.picture.PictureQueryRequest;
import com.baolong.picture.interfaces.dto.picture.PictureReviewRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 图片领域服务接口
 */
public interface PictureDomainService extends IService<Picture> {

	/**
	 * 填充审核参数
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	void fillReviewParams(Picture picture, User loginUser);

	/**
	 * 获取查询条件
	 *
	 * @param pictureQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

	/**
	 * 图片审核
	 *
	 * @param pictureReviewRequest 图片审核请求对象
	 * @param loginUser            登录用户
	 */
	Boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

	/**
	 * 清理图片文件
	 *
	 * @param oldPicture 图片对象
	 */
	void clearPictureFile(Picture oldPicture);

	/**
	 * 检查图片权限
	 *
	 * @param loginUser 当前登录用户
	 * @param picture   当前图片对象
	 */
	void checkPictureAuth(User loginUser, Picture picture);

	/**
	 * 创建图片扩图任务
	 *
	 * @param createPictureOutPaintingTaskRequest 图片扩图任务请求
	 * @param loginUser                           当前登录用户
	 * @return 扩图任务响应
	 */
	CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
