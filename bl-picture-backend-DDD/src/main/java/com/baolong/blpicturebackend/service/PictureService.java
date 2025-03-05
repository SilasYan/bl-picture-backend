package com.baolong.blpicturebackend.service;

import com.baolong.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.baolong.blpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditByBatchRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureQueryRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureReviewRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadRequest;
import com.baolong.blpicturebackend.model.entity.Picture;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.blpicturebackend.model.vo.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ADMIN
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-02-13 23:18:16
 */
public interface PictureService extends IService<Picture> {

	/**
	 * 上传图片
	 *
	 * @param inputSource          文件输入源
	 * @param pictureUploadRequest 上传图片的请求对象
	 * @param loginUser            登录的用户
	 * @return PictureVO
	 */
	PictureVO uploadPicture(Object inputSource,
							PictureUploadRequest pictureUploadRequest,
							User loginUser);

	/**
	 * 填充审核参数
	 *
	 * @param picture   图片对象
	 * @param loginUser 登录用户
	 */
	void fillReviewParams(Picture picture, User loginUser);

	/**
	 * 校验图片对象
	 *
	 * @param picture 图片对象
	 */
	void validPicture(Picture picture);

	/**
	 * 获取图片封装类
	 *
	 * @param picture 图片
	 * @param request HttpServletRequest
	 * @return 图片封装类
	 */
	PictureVO getPictureVO(Picture picture, HttpServletRequest request);

	/**
	 * 分页获取图片封装
	 *
	 * @param picturePage 图片分页对象
	 * @param request     HttpServletRequest
	 * @return Page<PictureVO>
	 */
	Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

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
	void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

	/**
	 * 批量抓取和创建图片
	 *
	 * @param pictureUploadByBatchRequest 图片批量上传请求对象
	 * @param loginUser                   登录用户
	 * @return 成功创建的图片数
	 */
	Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

	/**
	 * 清理图片文件
	 *
	 * @param oldPicture 图片对象
	 */
	void clearPictureFile(Picture oldPicture);

	/**
	 * 编辑图片
	 *
	 * @param pictureEditRequest 图片编辑请求
	 * @param loginUser          当前登录用户
	 */
	void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

	/**
	 * 删除图片
	 *
	 * @param pictureId 图片ID
	 * @param loginUser 当前登录用户
	 */
	void deletePicture(long pictureId, User loginUser);

	/**
	 * 检查图片权限
	 *
	 * @param loginUser 当前登录用户
	 * @param picture   当前图片对象
	 */
	void checkPictureAuth(User loginUser, Picture picture);

	/**
	 * 根据颜色搜索图片
	 *
	 * @param spaceId   空间ID
	 * @param picColor  图片颜色 RGB
	 * @param loginUser 当前登录用户
	 * @return 图片列表
	 */
	List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

	/**
	 * 批量编辑图片
	 *
	 * @param pictureEditByBatchRequest 图片批量编辑请求
	 * @param loginUser                 当前图片对象
	 */
	void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

	/**
	 * 创建图片扩图任务
	 *
	 * @param createPictureOutPaintingTaskRequest 图片扩图任务请求
	 * @param loginUser                           当前登录用户
	 * @return 扩图任务响应
	 */
	CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
