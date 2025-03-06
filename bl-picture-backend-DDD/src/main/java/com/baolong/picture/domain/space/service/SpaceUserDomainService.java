package com.baolong.picture.domain.space.service;

import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserQueryRequest;
import com.baolong.picture.interfaces.vo.space.SpaceUserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间用户领域服务接口
 */
public interface SpaceUserDomainService {

	/**
	 * 添加空间用户
	 *
	 * @param spaceUser 空间用户
	 * @return 是否成功
	 */
	Boolean addSpaceUser(SpaceUser spaceUser);

	/**
	 * 获取查询条件
	 *
	 * @param spaceUserQueryRequest 空间用户查询请求
	 * @return QueryWrapper<SpaceUser>
	 */
	QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

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
