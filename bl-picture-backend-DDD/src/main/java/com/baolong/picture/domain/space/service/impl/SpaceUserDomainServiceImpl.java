package com.baolong.picture.domain.space.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.domain.space.repository.SpaceUserRepository;
import com.baolong.picture.domain.space.service.SpaceUserDomainService;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 空间用户领域服务实现
 */
@Service
@RequiredArgsConstructor
public class SpaceUserDomainServiceImpl implements SpaceUserDomainService {

	private final SpaceUserRepository spaceUserRepository;

	/**
	 * 添加空间用户
	 *
	 * @param spaceUser 空间用户
	 * @return 是否成功
	 */
	@Override
	public Boolean addSpaceUser(SpaceUser spaceUser) {
		return spaceUserRepository.save(spaceUser);
	}

	/**
	 * 获取查询条件
	 *
	 * @param spaceUserQueryRequest 空间用户查询请求
	 * @return QueryWrapper<SpaceUser>
	 */
	@Override
	public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
		QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
		if (spaceUserQueryRequest == null) {
			return queryWrapper;
		}
		// 从对象中取值
		Long id = spaceUserQueryRequest.getId();
		Long spaceId = spaceUserQueryRequest.getSpaceId();
		Long userId = spaceUserQueryRequest.getUserId();
		String spaceRole = spaceUserQueryRequest.getSpaceRole();
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
		return queryWrapper;
	}

	/**
	 * 获取空间用户（根据空间用户ID）
	 *
	 * @param spaceUserId 空间用户ID
	 * @return 空间用户
	 */
	@Override
	public SpaceUser getSpaceUserById(Long spaceUserId) {
		return spaceUserRepository.getById(spaceUserId);
	}

	/**
	 * 删除空间用户（根据空间用户ID）
	 *
	 * @param spaceUserId 空间用户ID
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteSpaceUser(Long spaceUserId) {
		return spaceUserRepository.removeById(spaceUserId);
	}

	/**
	 * 获取空间用户（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 用户空间
	 */
	@Override
	public SpaceUser getSpaceUser(QueryWrapper<SpaceUser> queryWrapper) {
		return spaceUserRepository.getOne(queryWrapper);
	}

	/**
	 * 获取空间用户列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间用户列表
	 */
	@Override
	public List<SpaceUser> getSpaceUserList(QueryWrapper<SpaceUser> queryWrapper) {
		return spaceUserRepository.list(queryWrapper);
	}

	/**
	 * 更新空间用户
	 *
	 * @param spaceUser 空间用户
	 * @return 空间用户
	 */
	@Override
	public Boolean updateSpaceUser(SpaceUser spaceUser) {
		return spaceUserRepository.updateById(spaceUser);
	}
}




