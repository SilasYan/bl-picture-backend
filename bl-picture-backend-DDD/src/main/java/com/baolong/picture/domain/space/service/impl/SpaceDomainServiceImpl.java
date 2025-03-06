package com.baolong.picture.domain.space.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.domain.space.enums.SpaceRoleEnum;
import com.baolong.picture.domain.space.enums.SpaceTypeEnum;
import com.baolong.picture.domain.space.repository.SpaceRepository;
import com.baolong.picture.domain.space.repository.SpaceUserRepository;
import com.baolong.picture.domain.space.service.SpaceDomainService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.dto.space.SpaceQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 空间领域服务实现
 */
@Service
@RequiredArgsConstructor
public class SpaceDomainServiceImpl implements SpaceDomainService {

	private final SpaceRepository spaceRepository;
	private final SpaceUserRepository spaceUserRepository;

	@Resource
	private TransactionTemplate transactionTemplate;

	/**
	 * 获取查询条件
	 *
	 * @param spaceQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
		QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
		if (spaceQueryRequest == null) {
			return queryWrapper;
		}
		Long id = spaceQueryRequest.getId();
		Long userId = spaceQueryRequest.getUserId();
		String spaceName = spaceQueryRequest.getSpaceName();
		Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
		String sortField = spaceQueryRequest.getSortField();
		String sortOrder = spaceQueryRequest.getSortOrder();
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
		queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);

		Integer spaceType = spaceQueryRequest.getSpaceType();
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);

		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

	/**
	 * 创建空间
	 *
	 * @param space     空间
	 * @param loginUser 当前登录用户
	 * @return long
	 */
	@Override
	public long addSpace(Space space, User loginUser) {
		Long userId = loginUser.getId();
		// 针对用户进行加锁
		Map<Long, Object> lockMap = new ConcurrentHashMap<>();
		Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
		synchronized (lock) {
			try {
				Long newSpaceId = transactionTemplate.execute(status -> {
					boolean exists = spaceRepository.lambdaQuery()
							.eq(Space::getUserId, userId)
							.eq(Space::getSpaceType, space.getSpaceType())
							.exists();
					ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
					// 写入数据库
					boolean result = spaceRepository.save(space);
					ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
					// 如果是团队空间，关联新增团队成员记录
					if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
						SpaceUser spaceUser = new SpaceUser();
						spaceUser.setSpaceId(space.getId());
						spaceUser.setUserId(userId);
						spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
						result = spaceUserRepository.save(spaceUser);
						ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
					}
					// 返回新写入的数据 id
					return space.getId();
				});
				// 返回结果是包装类，可以做一些处理
				return Optional.ofNullable(newSpaceId).orElse(-1L);
			} finally {
				lockMap.remove(userId);
			}
		}
	}

	/**
	 * 根据空间ID获取空间
	 *
	 * @param spaceId 空间ID
	 * @return 空间
	 */
	@Override
	public Space getSpaceById(Long spaceId) {
		return spaceRepository.getById(spaceId);
	}

	/**
	 * 更新空间
	 *
	 * @param space 空间
	 * @return 是否成功
	 */
	@Override
	public Boolean updateSpace(Space space) {
		return spaceRepository.updateById(space);
	}

	/**
	 * 删除空间
	 *
	 * @param spaceId 空间ID
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteSpace(Long spaceId) {
		return spaceRepository.removeById(spaceId);
	}

	/**
	 * 获取空间列表（分页）
	 *
	 * @param page         分页对象
	 * @param queryWrapper 查询条件
	 * @return 空间分页列表
	 */
	@Override
	public Page<Space> getSpaceListOfPage(Page<Space> page, QueryWrapper<Space> queryWrapper) {
		return spaceRepository.page(page, queryWrapper);
	}

	/**
	 * 获取空间列表（根据空间ID列表）
	 *
	 * @param spaceIdSet 空间ID列表
	 * @return 空间列表
	 */
	@Override
	public List<Space> getSpaceListByIds(Set<Long> spaceIdSet) {
		return spaceRepository.listByIds(spaceIdSet);
	}

	/**
	 * 获取空间列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间列表
	 */
	@Override
	public List<Space> getSpaceList(QueryWrapper<Space> queryWrapper) {
		return spaceRepository.list(queryWrapper);
	}

	/**
	 * 更新空间（管理员）
	 *
	 * @param updateWrapper 更新条件
	 * @return 是否成功
	 */
	@Override
	public Boolean updateSpaceAsAdmin(LambdaUpdateWrapper<Space> updateWrapper) {
		return spaceRepository.update(updateWrapper);
	}
}




