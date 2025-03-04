package com.baolong.blpicturebackend.service;

import com.baolong.blpicturebackend.model.dto.spaceUser.SpaceUserAddRequest;
import com.baolong.blpicturebackend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.baolong.blpicturebackend.model.entity.SpaceUser;
import com.baolong.blpicturebackend.model.vo.SpaceUserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ADMIN
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-02-28 20:23:27
 */
public interface SpaceUserService extends IService<SpaceUser> {

	/**
	 * 添加空间用户
	 *
	 * @param spaceUserAddRequest 空间用户添加请求
	 * @return long
	 */
	long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

	/**
	 * 校验空间用户
	 *
	 * @param spaceUser 空间用户
	 * @param add       是否为创建
	 */
	void validSpaceUser(SpaceUser spaceUser, boolean add);

	/**
	 * 获取查询条件
	 *
	 * @param spaceUserQueryRequest 空间用户查询请求
	 * @return QueryWrapper<SpaceUser>
	 */
	QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

	/**
	 * 获取空间成员封装类
	 *
	 * @param spaceUser 空间用户
	 * @param request   HttpServletRequest
	 * @return SpaceUserVO
	 */
	SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

	/**
	 * 获取空间成员封装类列表
	 *
	 * @param spaceUserList 空间用户列表
	 * @return List<SpaceUserVO>
	 */
	List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
