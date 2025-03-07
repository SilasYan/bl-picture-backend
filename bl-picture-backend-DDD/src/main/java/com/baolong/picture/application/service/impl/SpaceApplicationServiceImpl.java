package com.baolong.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baolong.picture.application.service.SpaceUserApplicationService;
import com.baolong.picture.application.service.SpaceApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.space.enums.SpaceLevelEnum;
import com.baolong.picture.domain.space.service.SpaceDomainService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.assembler.SpaceAssembler;
import com.baolong.picture.interfaces.dto.space.SpaceAddRequest;
import com.baolong.picture.interfaces.dto.space.SpaceEditRequest;
import com.baolong.picture.interfaces.dto.space.SpaceQueryRequest;
import com.baolong.picture.interfaces.dto.space.SpaceUpdateRequest;
import com.baolong.picture.interfaces.vo.space.SpaceVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baolong.picture.shared.auth.SpaceUserAuthManager;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间应用服务实现
 */
@Service
@RequiredArgsConstructor
public class SpaceApplicationServiceImpl implements SpaceApplicationService {

	private final SpaceDomainService spaceDomainService;

	@Resource
	private UserApplicationService userApplicationService;

	@Resource
	private SpaceUserAuthManager spaceUserAuthManager;

	@Resource
	private SpaceUserApplicationService spaceUserApplicationService;

	@Resource
	private TransactionTemplate transactionTemplate;

	// region 增删改

	/**
	 * 创建空间
	 *
	 * @param spaceAddRequest 创建空间请求
	 * @param request         HttpServletRequest
	 * @return 空间ID
	 */
	@Override
	public Long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
		// 权限校验
		User loginUser = userApplicationService.getLoginUser(request);
		Long userId = loginUser.getId();
		// 权限校验
		if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !loginUser.isAdmin()) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
		}
		// 实体转换
		Space space = SpaceAssembler.toPictureEntity(spaceAddRequest);
		space.setUserId(userId);
		// 填充默认值
		space.fillDefaultValue();
		// 填充数据
		space.fillSpaceLevelValue();
		// 数据校验
		space.validSpace(true);
		return spaceDomainService.addSpace(space, loginUser);
	}

	/**
	 * 删除空间
	 *
	 * @param deleteRequest 删除请求
	 * @param request       HttpServletRequest
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request) {
		User loginUser = userApplicationService.getLoginUser(request);
		Long spaceId = deleteRequest.getId();
		// 判断是否存在
		Space oldSpace = this.getSpaceById(spaceId);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		this.checkSpaceAuth(oldSpace, loginUser);
		return spaceDomainService.deleteSpace(spaceId);
	}

	/**
	 * 编辑空间
	 *
	 * @param spaceEditRequest 编辑空间请求
	 * @param request          HttpServletRequest
	 * @return 是否成功
	 */
	@Override
	public Boolean editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
		// 实体转换
		Space space = SpaceAssembler.toPictureEntity(spaceEditRequest);
		// 设置编辑时间
		space.setEditTime(new Date());
		// 填充空间等级参数
		space.fillSpaceLevelValue();
		// 数据校验
		space.validSpace(false);
		// 判断是否存在
		Space oldSpace = this.getSpaceById(spaceEditRequest.getId());
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取登录用户信息
		User loginUser = userApplicationService.getLoginUser(request);
		this.checkSpaceAuth(oldSpace, loginUser);
		return spaceDomainService.updateSpace(space);
	}

	/**
	 * 更新空间（管理员）
	 *
	 * @param spaceUpdateRequest 更新空间请求
	 * @return 是否成功
	 */
	@Override
	public Boolean updateSpaceAsAdmin(SpaceUpdateRequest spaceUpdateRequest) {
		// 实体转换
		Space space = SpaceAssembler.toPictureEntity(spaceUpdateRequest);
		// 设置编辑时间
		space.setEditTime(new Date());
		// 填充空间等级参数
		space.fillSpaceLevelValue();
		// 数据校验
		space.validSpace(false);
		// 判断是否存在
		Space oldSpace = this.getSpaceById(spaceUpdateRequest.getId());
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		return spaceDomainService.updateSpace(space);
	}

	/**
	 * 更新空间（管理员）
	 *
	 * @param updateWrapper 更新条件
	 * @return 是否成功
	 */
	@Override
	public Boolean updateSpaceAsAdmin(LambdaUpdateWrapper<Space> updateWrapper) {
		return spaceDomainService.updateSpaceAsAdmin(updateWrapper);
	}

	// endregion 增删改

	/**
	 * 获取查询条件
	 *
	 * @param spaceQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
		return spaceDomainService.getQueryWrapper(spaceQueryRequest);
	}

	/**
	 * 获取空间封装类
	 *
	 * @param space   空间
	 * @param request HttpServletRequest
	 * @return 空间封装类
	 */
	@Override
	public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
		// 对象转封装类
		SpaceVO spaceVO = SpaceVO.objToVo(space);
		// 关联查询用户信息
		Long userId = space.getUserId();
		if (userId != null && userId > 0) {
			User user = userApplicationService.getUserById(userId);
			UserVO userVO = userApplicationService.getUserVO(user);
			spaceVO.setUser(userVO);
		}
		return spaceVO;
	}

	/**
	 * 分页获取空间封装
	 *
	 * @param spacePage 空间分页对象
	 * @param request   HttpServletRequest
	 * @return Page<SpaceVO>
	 */
	@Override
	public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
		List<Space> spaceList = spacePage.getRecords();
		Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
		if (CollUtil.isEmpty(spaceList)) {
			return spaceVOPage;
		}
		// 对象列表 => 封装对象列表
		List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userApplicationService.listUserByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		spaceVOList.forEach(spaceVO -> {
			Long userId = spaceVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			spaceVO.setUser(userApplicationService.getUserVO(user));
		});
		spaceVOPage.setRecords(spaceVOList);
		return spaceVOPage;
	}

	/**
	 * 校验空间权限
	 *
	 * @param loginUser 当前登录用户
	 * @param space     空间对象
	 */
	@Override
	public void checkSpaceAuth(Space space, User loginUser) {
		// 仅本人或管理员可编辑
		if (!space.getUserId().equals(loginUser.getId()) && !loginUser.isAdmin()) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
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
		Space space = spaceDomainService.getSpaceById(spaceId);
		if (space == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		return space;
	}

	/**
	 * 根据空间ID获取空间（脱敏）
	 *
	 * @param spaceId 空间ID
	 * @param request HttpServletRequest
	 * @return 空间脱敏信息
	 */
	@Override
	public SpaceVO getSpaceVOById(Long spaceId, HttpServletRequest request) {
		Space space = this.getSpaceById(spaceId);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
		SpaceVO spaceVO = this.getSpaceVO(space, request);
		User loginUser = userApplicationService.getLoginUser(request);
		List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
		spaceVO.setPermissionList(permissionList);
		return spaceVO;
	}

	/**
	 * 获取空间列表（分页）
	 *
	 * @param spaceQueryRequest 空间查询请求
	 * @return 空间分页列表
	 */
	@Override
	public Page<Space> getSpaceListOfPage(SpaceQueryRequest spaceQueryRequest) {
		return spaceDomainService.getSpaceListOfPage(
				new Page<>(spaceQueryRequest.getCurrent(), spaceQueryRequest.getPageSize())
				, this.getQueryWrapper(spaceQueryRequest)
		);
	}

	/**
	 * 获取空间列表（根据空间ID列表）
	 *
	 * @param spaceIdSet 空间ID列表
	 * @return 空间列表
	 */
	@Override
	public List<Space> getSpaceListByIds(Set<Long> spaceIdSet) {
		return spaceDomainService.getSpaceListByIds(spaceIdSet);
	}

	/**
	 * 获取空间列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间列表
	 */
	@Override
	public List<Space> getSpaceList(QueryWrapper<Space> queryWrapper) {
		return spaceDomainService.getSpaceList(queryWrapper);
	}
}




