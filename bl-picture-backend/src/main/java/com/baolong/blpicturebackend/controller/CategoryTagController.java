package com.baolong.blpicturebackend.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.blpicturebackend.annotation.AuthCheck;
import com.baolong.blpicturebackend.comment.BaseResponse;
import com.baolong.blpicturebackend.comment.DeleteRequest;
import com.baolong.blpicturebackend.comment.ResultUtils;
import com.baolong.blpicturebackend.constant.UserConstant;
import com.baolong.blpicturebackend.exception.BusinessException;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.model.dto.category.CategoryAddRequest;
import com.baolong.blpicturebackend.model.dto.category.CategoryQueryRequest;
import com.baolong.blpicturebackend.model.dto.category.CategoryUpdateRequest;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.CategoryTagEnum;
import com.baolong.blpicturebackend.model.vo.CategoryTagVO;
import com.baolong.blpicturebackend.model.vo.PictureTagCategory;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.blpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/categoryTag")
public class CategoryTagController {

	@Resource
	private UserService userService;
	@Resource
	private CategoryTagService categoryTagService;

	/**
	 * 获取分类标签数据
	 */
	@GetMapping("/data/vo")
	public BaseResponse<PictureTagCategory> pictureCategoryTagData() {
		List<CategoryTag> categoryTagList = categoryTagService.list();
		List<CategoryTagVO> voList = categoryTagList.stream().map(CategoryTagVO::objToVo).collect(Collectors.toList());
		Map<Integer, List<CategoryTagVO>> typeMap = voList.stream().collect(Collectors.groupingBy(CategoryTagVO::getType));
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		pictureTagCategory.setCategoryVOList(typeMap.get(CategoryTagEnum.CATEGORY.getValue()));
		pictureTagCategory.setTagVOList(typeMap.get(CategoryTagEnum.TAG.getValue()));
		return ResultUtils.success(pictureTagCategory);
	}

	/**
	 * 分页获取分类标签列表（管理员可见）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<CategoryTag>> listCategoryTagByPage(@RequestBody CategoryQueryRequest categoryQueryRequest) {
		long current = categoryQueryRequest.getCurrent();
		long size = categoryQueryRequest.getPageSize();
		Page<CategoryTag> categoryTagPage = categoryTagService.page(new Page<>(current, size),
				new LambdaQueryWrapper<CategoryTag>()
						.eq(ObjUtil.isNotEmpty(categoryQueryRequest.getType()), CategoryTag::getType, categoryQueryRequest.getType())
						.like(StrUtil.isNotBlank(categoryQueryRequest.getName()), CategoryTag::getName, categoryQueryRequest.getName())
						.orderByDesc(CategoryTag::getCreateTime)
		);
		return ResultUtils.success(categoryTagPage);
	}

	/**
	 * 删除分类标签（管理员可用）
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteCategoryTag(@RequestBody DeleteRequest deleteRequest) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long id = deleteRequest.getId();
		// 判断是否存在
		CategoryTag categoryTag = categoryTagService.getById(id);
		ThrowUtils.throwIf(categoryTag == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = categoryTagService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 更新分类标签（管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateCategoryTag(@RequestBody CategoryUpdateRequest categoryUpdateRequest) {
		if (categoryUpdateRequest == null || categoryUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		CategoryTag categoryTag = new CategoryTag();
		BeanUtils.copyProperties(categoryUpdateRequest, categoryTag);
		categoryTag.setEditTime(new Date());

		// 判断是否存在
		long id = categoryUpdateRequest.getId();
		CategoryTag oldCategoryTag = categoryTagService.getById(id);
		ThrowUtils.throwIf(oldCategoryTag == null, ErrorCode.NOT_FOUND_ERROR);

		// 操作数据库
		boolean result = categoryTagService.updateById(categoryTag);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 新增分类标签（管理员可用）
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> addCategoryTag(@RequestBody CategoryAddRequest categoryAddRequest, HttpServletRequest request) {
		if (categoryAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		CategoryTag categoryTag = new CategoryTag();
		BeanUtils.copyProperties(categoryAddRequest, categoryTag);
		User loginUser = userService.getLoginUser(request);
		categoryTag.setUserId(loginUser.getId());

		// 操作数据库
		boolean result = categoryTagService.save(categoryTag);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

}
