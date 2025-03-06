package com.baolong.picture.application.service;

import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserAddRequest;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserQueryRequest;
import com.baolong.picture.interfaces.vo.space.SpaceUserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间用户应用服务接口
 */
public interface SpaceUserApplicationService {

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

	/**
	 * 获取空间用户（根据空间用户ID）
	 *
	 * @param spaceUserId 空间用户ID
	 * @return 空间用户
	 */
	SpaceUser getSpaceUserById(Long spaceUserId);

	/**
	 * 删除空间用户（根据空间用户ID）
	 *
	 * @param spaceUserId 空间用户ID
	 * @return 是否成功
	 */
	Boolean deleteSpaceUser(Long spaceUserId);

	/**
	 * 获取空间用户（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 用户空间
	 */
	SpaceUser getSpaceUser(QueryWrapper<SpaceUser> queryWrapper);

	/**
	 * 获取空间用户列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间用户列表
	 */
	List<SpaceUser> getSpaceUserList(QueryWrapper<SpaceUser> queryWrapper);

	/**
	 * 更新空间用户
	 *
	 * @param spaceUser 空间用户
	 * @return 空间用户
	 */
	Boolean updateSpaceUser(SpaceUser spaceUser);
}
