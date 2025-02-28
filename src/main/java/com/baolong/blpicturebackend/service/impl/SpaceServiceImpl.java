package com.baolong.blpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.blpicturebackend.exception.BusinessException;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.mapper.SpaceMapper;
import com.baolong.blpicturebackend.model.dto.space.SpaceAddRequest;
import com.baolong.blpicturebackend.model.dto.space.SpaceQueryRequest;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.SpaceLevelEnum;
import com.baolong.blpicturebackend.model.vo.SpaceVO;
import com.baolong.blpicturebackend.model.vo.UserVO;
import com.baolong.blpicturebackend.service.SpaceService;
import com.baolong.blpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ADMIN
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-02-17 21:48:11
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
		implements SpaceService {

	@Resource
	private UserService userService;

	@Resource
	private TransactionTemplate transactionTemplate;

	/**
	 * 校验空间参数
	 *
	 * @param space 空间对象
	 * @param add   是否新增
	 */
	@Override
	public void validSpace(Space space, boolean add) {
		ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
		// 从对象中取值
		String spaceName = space.getSpaceName();
		Integer spaceLevel = space.getSpaceLevel();
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
		// 要创建
		if (add) {
			if (StrUtil.isBlank(spaceName)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
			}
			if (spaceLevel == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
			}
		}
		// 修改数据时，如果要改空间级别
		if (spaceLevel != null && spaceLevelEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
		}
		if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
		}
	}

	/**
	 * 填充空间等级信息
	 *
	 * @param space 空间对象
	 */
	@Override
	public void fillSpaceBySpaceLevel(Space space) {
		// 根据空间级别，自动填充限额
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
		if (spaceLevelEnum != null) {
			long maxSize = spaceLevelEnum.getMaxSize();
			if (space.getMaxSize() == null) {
				space.setMaxSize(maxSize);
			}
			long maxCount = spaceLevelEnum.getMaxCount();
			if (space.getMaxCount() == null) {
				space.setMaxCount(maxCount);
			}
		}
	}

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
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
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
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
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
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		spaceVOList.forEach(spaceVO -> {
			Long userId = spaceVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			spaceVO.setUser(userService.getUserVO(user));
		});
		spaceVOPage.setRecords(spaceVOList);
		return spaceVOPage;
	}

	/**
	 * 创建空间
	 *
	 * @param spaceAddRequest 创建空间请求
	 * @param loginUser       当前登录用户
	 * @return long
	 */
	@Override
	public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
		// 在此处将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceAddRequest, space);
		// 默认值
		if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
			space.setSpaceName("默认空间");
		}
		if (spaceAddRequest.getSpaceLevel() == null) {
			space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
		}
		// 填充数据
		this.fillSpaceBySpaceLevel(space);
		// 数据校验
		this.validSpace(space, true);
		Long userId = loginUser.getId();
		space.setUserId(userId);
		// 权限校验
		if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
		}
		// 针对用户进行加锁
		// String lock = String.valueOf(userId).intern();
		// 这里可以对本地所进行优化, 因为这里获取字符串的内存空间, 该内存空间是不会释放的
		Map<Long, Object> lockMap = new ConcurrentHashMap<>();
		Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
		synchronized (lock) {
			try {
				Long newSpaceId = transactionTemplate.execute(status -> {
					boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
					ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
					// 写入数据库
					boolean result = this.save(space);
					ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
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
	 * 校验空间权限
	 *
	 * @param loginUser 当前登录用户
	 * @param space     空间对象
	 */
	@Override
	public void checkSpaceAuth(User loginUser, Space space) {
		// 仅本人或管理员可编辑
		if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
	}
}




