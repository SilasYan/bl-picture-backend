package com.baolong.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baolong.picture.application.service.SpaceApplicationService;
import com.baolong.picture.application.service.SpaceUserApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.domain.space.enums.SpaceRoleEnum;
import com.baolong.picture.domain.space.service.SpaceUserDomainService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.assembler.SpaceUserAssembler;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserAddRequest;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserQueryRequest;
import com.baolong.picture.interfaces.vo.space.SpaceUserVO;
import com.baolong.picture.interfaces.vo.space.SpaceVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间用户应用服务实现
 */
@Service
@RequiredArgsConstructor
public class SpaceUserApplicationServiceImpl implements SpaceUserApplicationService {

	private final SpaceUserDomainService spaceUserDomainService;

	@Resource
	private UserApplicationService userApplicationService;
	@Resource
	@Lazy
	private SpaceApplicationService spaceApplicationService;

	/**
	 * 添加空间用户
	 *
	 * @param spaceUserAddRequest 空间用户添加请求
	 * @return long
	 */
	@Override
	public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
		// 参数校验
		ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
		// 转为实体类
		SpaceUser spaceUser = SpaceUserAssembler.toSpaceUserEntity(spaceUserAddRequest);
		validSpaceUser(spaceUser, true);
		// 数据库操作
		boolean result = spaceUserDomainService.addSpaceUser(spaceUser);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return spaceUser.getId();
	}

	/**
	 * 校验空间用户
	 *
	 * @param spaceUser 空间用户
	 * @param add       是否为创建
	 */
	@Override
	public void validSpaceUser(SpaceUser spaceUser, boolean add) {
		ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
		// 创建时，空间 id 和用户 id 必填
		Long spaceId = spaceUser.getSpaceId();
		Long userId = spaceUser.getUserId();
		if (add) {
			ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
			User user = userApplicationService.getUserById(userId);
			ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
			Space space = spaceApplicationService.getSpaceById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		}
		// 校验空间角色
		String spaceRole = spaceUser.getSpaceRole();
		SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
		if (spaceRole != null && spaceRoleEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
		}
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
	 * 获取空间成员封装类
	 *
	 * @param spaceUser 空间用户
	 * @param request   HttpServletRequest
	 * @return SpaceUserVO
	 */
	@Override
	public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
		// 对象转封装类
		SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
		// 关联查询用户信息
		Long userId = spaceUser.getUserId();
		if (userId != null && userId > 0) {
			User user = userApplicationService.getUserById(userId);
			UserVO userVO = userApplicationService.getUserVO(user);
			spaceUserVO.setUser(userVO);
		}
		// 关联查询空间信息
		Long spaceId = spaceUser.getSpaceId();
		if (spaceId != null && spaceId > 0) {
			Space space = spaceApplicationService.getSpaceById(spaceId);
			SpaceVO spaceVO = spaceApplicationService.getSpaceVO(space, request);
			spaceUserVO.setSpace(spaceVO);
		}
		return spaceUserVO;
	}

	/**
	 * 获取空间成员封装类列表
	 *
	 * @param spaceUserList 空间用户列表
	 * @return List<SpaceUserVO>
	 */
	@Override
	public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
		// 判断输入列表是否为空
		if (CollUtil.isEmpty(spaceUserList)) {
			return Collections.emptyList();
		}
		// 对象列表 => 封装对象列表
		List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
		// 1. 收集需要关联查询的用户 ID 和空间 ID
		Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
		Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
		// 2. 批量查询用户和空间
		Map<Long, List<User>> userIdUserListMap = userApplicationService.listUserByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		Map<Long, List<Space>> spaceIdSpaceListMap = spaceApplicationService.getSpaceListByIds(spaceIdSet).stream()
				.collect(Collectors.groupingBy(Space::getId));
		// 3. 填充 SpaceUserVO 的用户和空间信息
		spaceUserVOList.forEach(spaceUserVO -> {
			Long userId = spaceUserVO.getUserId();
			Long spaceId = spaceUserVO.getSpaceId();
			// 填充用户信息
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			spaceUserVO.setUser(userApplicationService.getUserVO(user));
			// 填充空间信息
			Space space = null;
			if (spaceIdSpaceListMap.containsKey(spaceId)) {
				space = spaceIdSpaceListMap.get(spaceId).get(0);
			}
			spaceUserVO.setSpace(SpaceVO.objToVo(space));
		});
		return spaceUserVOList;
	}

	/**
	 * 获取空间用户（根据空间用户ID）
	 *
	 * @param spaceUserId 空间用户ID
	 * @return 空间用户
	 */
	@Override
	public SpaceUser getSpaceUserById(Long spaceUserId) {
		return spaceUserDomainService.getSpaceUserById(spaceUserId);
	}

	/**
	 * 删除空间用户（根据空间用户ID）
	 *
	 * @param spaceUserId 空间用户ID
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteSpaceUser(Long spaceUserId) {
		return spaceUserDomainService.deleteSpaceUser(spaceUserId);
	}

	/**
	 * 获取空间用户（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 用户空间
	 */
	@Override
	public SpaceUser getSpaceUser(QueryWrapper<SpaceUser> queryWrapper) {
		return spaceUserDomainService.getSpaceUser(queryWrapper);
	}

	/**
	 * 获取空间用户列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间用户列表
	 */
	@Override
	public List<SpaceUser> getSpaceUserList(QueryWrapper<SpaceUser> queryWrapper) {
		return spaceUserDomainService.getSpaceUserList(queryWrapper);
	}

	/**
	 * 更新空间用户
	 *
	 * @param spaceUser 空间用户
	 * @return 空间用户
	 */
	@Override
	public Boolean updateSpaceUser(SpaceUser spaceUser) {
		return spaceUserDomainService.updateSpaceUser(spaceUser);
	}
}




