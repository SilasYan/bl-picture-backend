package com.baolong.pictures.domain.picture.service;

import com.baolong.pictures.domain.picture.entity.Picture;
import com.baolong.pictures.domain.user.entity.User;
import com.baolong.pictures.interfaces.dto.picture.PictureQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 图片领域服务接口
 */
public interface PictureDomainService {

	// region 增删改相关（包含上传图片）

	/**
	 * 上传图片
	 *
	 * @param pictureInputSource 图片输入源
	 * @param picture            图片
	 * @return 是否成功
	 */
	Boolean uploadPicture(Object pictureInputSource, Picture picture);

	/**
	 * 删除图片
	 *
	 * @param pictureId 图片 ID
	 * @return 是否成功
	 */
	Boolean deletePicture(Long pictureId);

	/**
	 * 更新图片
	 *
	 * @param picture 图片实体
	 * @return 是否成功
	 */
	Boolean updatePicture(Picture picture);

	/**
	 * 编辑图片
	 *
	 * @param picture 图片实体
	 * @return 是否成功
	 */
	Boolean editPicture(Picture picture);

	/**
	 * 审核图片
	 *
	 * @param pictureList 图片列表
	 * @return 是否成功
	 */
	Boolean reviewPicture(List<Picture> pictureList);

	/**
	 * 编辑图片（批量）
	 *
	 * @param pictureList 图片列表
	 * @return 是否成功
	 */
	Boolean editPictureBatch(List<Picture> pictureList);

	// endregion 增删改相关（包含上传图片）

	// region 查询相关

	/**
	 * 根据图片 ID 判断图片是否存在
	 *
	 * @param pictureId 图片 ID
	 * @return 是否存在
	 */
	Boolean existPictureById(Long pictureId);

	/**
	 * 根据图片 ID 获取图片信息
	 *
	 * @param pictureId 图片 ID
	 * @return 图片信息
	 */
	Picture getPictureById(Long pictureId);

	/**
	 * 获取图片分页列表（条件查询, 简单固定字段）
	 *
	 * @param page               分页对象
	 * @param lambdaQueryWrapper 查询条件
	 * @return 图片分页列表
	 */
	Page<Picture> getPicturePageListAsSimple(Page<Picture> page, LambdaQueryWrapper<Picture> lambdaQueryWrapper);

	/**
	 * 获取图片分页列表（管理员, 条件查询）
	 *
	 * @param page               分页对象
	 * @param lambdaQueryWrapper 查询条件
	 * @return 图片分页列表
	 */
	Page<Picture> getPicturePageListAsAdmin(Page<Picture> page, LambdaQueryWrapper<Picture> lambdaQueryWrapper);

	// endregion 查询相关

	// region 其他方法

	/**
	 * 获取查询条件对象（Lambda）
	 *
	 * @param pictureQueryRequest 图片查询请求
	 * @return 查询条件对象（Lambda）
	 */
	LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest);

	/**
	 * 校验并填充审核参数
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	void checkAndFillReviewParams(Picture picture, User loginUser);

	/**
	 * 校验图片操作权限
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	void checkPictureChangeAuth(Picture picture, User loginUser);

	/**
	 * 填充图片名称规则
	 *
	 * @param pictureList 图片列表
	 * @param nameRule    名称规则
	 */
	void fillPictureNameRuleBatch(List<Picture> pictureList, String nameRule);

	/**
	 * 清理图片文件
	 *
	 * @param picture 图片对象
	 */
	void clearPictureFile(Picture picture);

	// endregion 其他方法

	// /**
	//  * 创建图片扩图任务
	//  *
	//  * @param createPictureOutPaintingTaskRequest 图片扩图任务请求
	//  * @param loginTUser                          当前登录用户
	//  * @return 扩图任务响应
	//  */
	// CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginTUser);
}
