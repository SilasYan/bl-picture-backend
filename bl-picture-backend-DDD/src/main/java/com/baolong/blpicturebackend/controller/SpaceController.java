package com.baolong.blpicturebackend.controller;

import com.baolong.picture.infrastructure.annotation.AuthCheck;
import com.baolong.blpicturebackend.auth.SpaceUserAuthManager;
import com.baolong.picture.infrastructure.comment.BaseResponse;
import com.baolong.picture.infrastructure.comment.DeleteRequest;
import com.baolong.picture.infrastructure.comment.ResultUtils;
import com.baolong.picture.domain.user.constant.UserConstant;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.blpicturebackend.model.dto.space.SpaceAddRequest;
import com.baolong.blpicturebackend.model.dto.space.SpaceEditRequest;
import com.baolong.blpicturebackend.model.dto.space.SpaceLevel;
import com.baolong.blpicturebackend.model.dto.space.SpaceQueryRequest;
import com.baolong.blpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.blpicturebackend.model.enums.SpaceLevelEnum;
import com.baolong.blpicturebackend.model.vo.SpaceVO;
import com.baolong.blpicturebackend.service.SpaceService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

	@Resource
	private UserApplicationService userApplicationService;
	@Resource
	private SpaceService spaceService;
	@Resource
	private SpaceUserAuthManager spaceUserAuthManager;

	/**
	 * 创建空间
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userApplicationService.getLoginUser(request);
		long newId = spaceService.addSpace(spaceAddRequest, loginUser);
		return ResultUtils.success(newId);
	}

	/**
	 * 删除空间
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userApplicationService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		spaceService.checkSpaceAuth(loginUser, oldSpace);
		// 操作数据库
		boolean result = spaceService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 更新空间（仅管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
		if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceUpdateRequest, space);
		// 填充空间等级参数
		spaceService.fillSpaceBySpaceLevel(space);
		// 数据校验
		spaceService.validSpace(space, false);
		// 判断是否存在
		long id = spaceUpdateRequest.getId();
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = spaceService.updateById(space);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取空间（仅管理员可用）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Space space = spaceService.getById(id);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(space);
	}

	/**
	 * 根据 id 获取空间（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Space space = spaceService.getById(id);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
		SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
		User loginUser = userApplicationService.getLoginUser(request);
		List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
		spaceVO.setPermissionList(permissionList);
		// 获取封装类
		return ResultUtils.success(spaceVO);
	}

	/**
	 * 分页获取空间列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		return ResultUtils.success(spacePage);
	}

	/**
	 * 分页获取空间列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
														 HttpServletRequest request) {
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		// 获取封装类
		return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
	}

	/**
	 * 编辑空间（给用户使用）
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
		if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 在此处将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceEditRequest, space);
		User loginUser = userApplicationService.getLoginUser(request);
		// 设置编辑时间
		space.setEditTime(new Date());
		// 填充空间等级参数
		spaceService.fillSpaceBySpaceLevel(space);
		// 数据校验
		spaceService.validSpace(space, false);
		// 判断是否存在
		long id = spaceEditRequest.getId();
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		spaceService.checkSpaceAuth(loginUser, oldSpace);
		// 操作数据库
		boolean result = spaceService.updateById(space);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	@GetMapping("/list/level")
	public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
		List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
				.map(spaceLevelEnum -> new SpaceLevel(
						spaceLevelEnum.getValue(),
						spaceLevelEnum.getText(),
						spaceLevelEnum.getMaxCount(),
						spaceLevelEnum.getMaxSize()))
				.collect(Collectors.toList());
		return ResultUtils.success(spaceLevelList);
	}

}
