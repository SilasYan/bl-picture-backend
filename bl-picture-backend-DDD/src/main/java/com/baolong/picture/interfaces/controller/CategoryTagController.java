package com.baolong.picture.interfaces.controller;

import com.baolong.picture.application.service.CategoryTagApplicationService;
import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.domain.categoryTag.enums.CategoryTagEnum;
import com.baolong.picture.domain.user.constant.UserConstant;
import com.baolong.picture.infrastructure.annotation.AuthCheck;
import com.baolong.picture.infrastructure.common.BaseResponse;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.infrastructure.common.ResultUtils;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.assembler.CategoryTagAssembler;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryAddRequest;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryQueryRequest;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryUpdateRequest;
import com.baolong.picture.interfaces.vo.categoryTag.CategoryTagVO;
import com.baolong.picture.interfaces.vo.categoryTag.CategoryVOAndTagVO;
import com.baolong.picture.interfaces.vo.picture.PictureTagCategory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/categoryTag")
public class CategoryTagController {

	@Resource
	private CategoryTagApplicationService categoryTagApplicationService;

	// region 增删改

	/**
	 * 新增分类标签（管理员）
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> addCategoryTag(@RequestBody CategoryAddRequest categoryAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(categoryAddRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
		CategoryTag categoryTag = CategoryTagAssembler.toPictureEntity(categoryAddRequest);
		return ResultUtils.success(categoryTagApplicationService.addCategoryTag(categoryTag, request));
	}

	/**
	 * 删除分类标签（管理员）
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteCategoryTag(@RequestBody DeleteRequest deleteRequest) {
		ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
		return ResultUtils.success(categoryTagApplicationService.deleteCategoryTag(deleteRequest));
	}

	/**
	 * 更新分类标签（管理员）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateCategoryTag(@RequestBody CategoryUpdateRequest categoryUpdateRequest) {
		ThrowUtils.throwIf(categoryUpdateRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
		CategoryTag categoryTag = CategoryTagAssembler.toPictureEntity(categoryUpdateRequest);
		return ResultUtils.success(categoryTagApplicationService.updateCategoryTag(categoryTag));
	}

	// endregion 增删改

	/**
	 * 获取分类和标签数据
	 */
	@GetMapping("/categoryAndTag/vo")
	public BaseResponse<CategoryVOAndTagVO> getCategoryVOAndTagVO() {
		return ResultUtils.success(categoryTagApplicationService.getCategoryVOAndTagVO());
	}

	/**
	 * 获取分类标签列表（分页, 管理员）
	 */
	@GetMapping("/list/pageNew")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<CategoryTag>> getCategoryTagListPage(CategoryQueryRequest categoryQueryRequest) {
		return ResultUtils.success(categoryTagApplicationService.getCategoryTagListPage(categoryQueryRequest));
	}

	// TODO 下面方法均有新方法代替

	/**
	 * 获取分类标签数据 TODO 新方法-getCategoryVOAndTagVO
	 */
	@GetMapping("/data/vo")
	public BaseResponse<PictureTagCategory> pictureCategoryTagData() {
		List<CategoryTag> categoryTagList = categoryTagApplicationService.getCategoryTagList(null);
		List<CategoryTagVO> voList = categoryTagList.stream().map(CategoryTagVO::objToVo).collect(Collectors.toList());
		Map<Integer, List<CategoryTagVO>> typeMap = voList.stream().collect(Collectors.groupingBy(CategoryTagVO::getType));
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		pictureTagCategory.setCategoryVOList(typeMap.get(CategoryTagEnum.CATEGORY.getValue()));
		pictureTagCategory.setTagVOList(typeMap.get(CategoryTagEnum.TAG.getValue()));
		return ResultUtils.success(pictureTagCategory);
	}

	/**
	 * 分页获取分类标签列表（管理员可见） TODO 新方法-getCategoryTagListPage
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<CategoryTag>> listCategoryTagByPage(@RequestBody CategoryQueryRequest categoryQueryRequest) {
		Page<CategoryTag> categoryTagPage = categoryTagApplicationService.getCategoryTagListPage(categoryQueryRequest);
		return ResultUtils.success(categoryTagPage);
	}

}
