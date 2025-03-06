package com.baolong.picture.interfaces.controller;

import com.baolong.picture.application.service.SpaceApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.space.enums.SpaceLevelEnum;
import com.baolong.picture.domain.user.constant.UserConstant;
import com.baolong.picture.infrastructure.annotation.AuthCheck;
import com.baolong.picture.infrastructure.comment.BaseResponse;
import com.baolong.picture.infrastructure.comment.DeleteRequest;
import com.baolong.picture.infrastructure.comment.ResultUtils;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.dto.space.SpaceAddRequest;
import com.baolong.picture.interfaces.dto.space.SpaceEditRequest;
import com.baolong.picture.interfaces.dto.space.SpaceLevel;
import com.baolong.picture.interfaces.dto.space.SpaceQueryRequest;
import com.baolong.picture.interfaces.dto.space.SpaceUpdateRequest;
import com.baolong.picture.interfaces.vo.space.SpaceVO;
import com.baolong.picture.shared.auth.SpaceUserAuthManager;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

	@Resource
	private SpaceApplicationService spaceApplicationService;

	// region 增删改

	/**
	 * 创建空间
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(spaceApplicationService.addSpace(spaceAddRequest, request));
	}

	/**
	 * 删除空间
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(spaceApplicationService.deleteSpace(deleteRequest, request));
	}

	/**
	 * 编辑空间（用户）
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(spaceApplicationService.editSpace(spaceEditRequest, request));
	}

	/**
	 * 更新空间（管理员）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
		ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(spaceApplicationService.updateSpaceAsAdmin(spaceUpdateRequest));
	}

	// endregion 增删改

	/**
	 * 根据空间ID获取空间（管理员）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Space> getSpaceById(Long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(spaceApplicationService.getSpaceById(id));
	}

	/**
	 * 根据 id 获取空间（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(spaceApplicationService.getSpaceVOById(id, request));
	}

	/**
	 * 分页获取空间列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
		return ResultUtils.success(spaceApplicationService.getSpaceListOfPage(spaceQueryRequest));
	}

	/**
	 * 分页获取空间列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
														 HttpServletRequest request) {
		// 查询数据库
		Page<Space> spacePage = spaceApplicationService.getSpaceListOfPage(spaceQueryRequest);
		// 获取封装类
		return ResultUtils.success(spaceApplicationService.getSpaceVOPage(spacePage, request));
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
