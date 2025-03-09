package com.baolong.pictures.interfaces.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.pictures.application.service.TagApplicationService;
import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.domain.user.constant.UserConstant;
import com.baolong.pictures.infrastructure.annotation.AuthCheck;
import com.baolong.pictures.infrastructure.common.BaseResponse;
import com.baolong.pictures.infrastructure.common.DeleteRequest;
import com.baolong.pictures.infrastructure.common.ResultUtils;
import com.baolong.pictures.infrastructure.common.page.PageVO;
import com.baolong.pictures.infrastructure.exception.ErrorCode;
import com.baolong.pictures.infrastructure.exception.ThrowUtils;
import com.baolong.pictures.interfaces.dto.tag.TagQueryRequest;
import com.baolong.pictures.interfaces.dto.tag.TagAddRequest;
import com.baolong.pictures.interfaces.dto.tag.TagUpdateRequest;
import com.baolong.pictures.interfaces.vo.tag.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签接口
 *
 * @author Baolong 2025年03月09 22:01
 * @version 1.0
 * @since 1.8
 */
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

	private final TagApplicationService tagApplicationService;

	// region 增删改相关

	/**
	 * 新增标签
	 */
	@PostMapping("/add")
	public BaseResponse<Boolean> addTag(@RequestBody TagAddRequest tagAddRequest) {
		ThrowUtils.throwIf(tagAddRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(StrUtil.isEmpty(tagAddRequest.getName()), ErrorCode.PARAMS_ERROR, "标签名称不能为空");
		return ResultUtils.success(tagApplicationService.addTag(tagAddRequest));
	}

	/**
	 * 删除标签
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
		ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(deleteRequest.getId()), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(tagApplicationService.deleteTag(deleteRequest));
	}

	/**
	 * 更新标签
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateTag(@RequestBody TagUpdateRequest tagUpdateRequest) {
		ThrowUtils.throwIf(tagUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(tagUpdateRequest.getId()), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(StrUtil.isEmpty(tagUpdateRequest.getName()), ErrorCode.PARAMS_ERROR, "标签名称不能为空");
		return ResultUtils.success(tagApplicationService.updateTag(tagUpdateRequest));
	}

	// endregion 增删改相关

	// region 查询相关

	/**
	 * 获取首页标签列表
	 */
	@GetMapping("/list")
	public BaseResponse<List<TagVO>> getTagListAsUser() {
		return ResultUtils.success(tagApplicationService.getTagListAsUser());
	}

	/**
	 * 获取标签分页列表（管理员）
	 */
	@GetMapping("/admin/list")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<PageVO<Tag>> getTagPageListAsAdmin(TagQueryRequest tagQueryRequest) {
		return ResultUtils.success(tagApplicationService.getTagPageListAsAdmin(tagQueryRequest));
	}
	
	// endregion 查询相关
}
