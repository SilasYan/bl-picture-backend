package com.baolong.picture.domain.space.service;

import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.interfaces.dto.space.SpaceQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Set;

/**
 * 空间领域服务接口
 */
public interface SpaceDomainService {

	/**
	 * 获取查询条件
	 *
	 * @param spaceQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

	/**
	 * 创建空间
	 *
	 * @param space     空间
	 * @param loginUser 当前登录用户
	 * @return long
	 */
	long addSpace(Space space, User loginUser);

	/**
	 * 根据空间ID获取空间
	 *
	 * @param spaceId 空间ID
	 * @return 空间
	 */
	Space getSpaceById(Long spaceId);

	/**
	 * 更新空间
	 *
	 * @param space 空间
	 * @return 是否成功
	 */
	Boolean updateSpace(Space space);

	/**
	 * 删除空间
	 *
	 * @param spaceId 空间ID
	 * @return 是否成功
	 */
	Boolean deleteSpace(Long spaceId);

	/**
	 * 获取空间列表（分页）
	 *
	 * @param page         分页对象
	 * @param queryWrapper 查询条件
	 * @return 空间分页列表
	 */
	Page<Space> getSpaceListOfPage(Page<Space> page, QueryWrapper<Space> queryWrapper);

	/**
	 * 获取空间列表（根据空间ID列表）
	 *
	 * @param spaceIdSet 空间ID列表
	 * @return 空间列表
	 */
	List<Space> getSpaceListByIds(Set<Long> spaceIdSet);

	/**
	 * 获取空间列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间列表
	 */
	List<Space> getSpaceList(QueryWrapper<Space> queryWrapper);

	/**
	 * 更新空间（管理员）
	 *
	 * @param updateWrapper 更新条件
	 * @return 是否成功
	 */
	Boolean updateSpaceAsAdmin(LambdaUpdateWrapper<Space> updateWrapper);
}
