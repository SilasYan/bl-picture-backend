package com.baolong.blpicturebackend.service;

import com.baolong.blpicturebackend.model.dto.space.SpaceAddRequest;
import com.baolong.blpicturebackend.model.dto.space.SpaceQueryRequest;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.blpicturebackend.model.vo.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ADMIN
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-02-17 21:48:11
 */
public interface SpaceService extends IService<Space> {

	/**
	 * 校验空间参数
	 *
	 * @param space 空间对象
	 * @param add   是否新增
	 */
	void validSpace(Space space, boolean add);

	/**
	 * 填充空间等级信息
	 *
	 * @param space 空间对象
	 */
	void fillSpaceBySpaceLevel(Space space);

	/**
	 * 获取查询条件
	 *
	 * @param spaceQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

	/**
	 * 获取空间封装类
	 *
	 * @param space   空间
	 * @param request HttpServletRequest
	 * @return 空间封装类
	 */
	SpaceVO getSpaceVO(Space space, HttpServletRequest request);

	/**
	 * 分页获取空间封装
	 *
	 * @param spacePage 空间分页对象
	 * @param request   HttpServletRequest
	 * @return Page<SpaceVO>
	 */
	Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

	/**
	 * 创建空间
	 *
	 * @param spaceAddRequest 创建空间请求
	 * @param loginUser       当前登录用户
	 * @return long
	 */
	long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

	/**
	 * 校验空间权限
	 *
	 * @param loginUser 当前登录用户
	 * @param space     空间对象
	 */
	void checkSpaceAuth(User loginUser, Space space);
}
