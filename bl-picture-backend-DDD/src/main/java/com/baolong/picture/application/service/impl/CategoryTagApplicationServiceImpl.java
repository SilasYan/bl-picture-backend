package com.baolong.picture.application.service.impl;

import com.baolong.picture.application.service.CategoryTagApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.domain.categoryTag.enums.CategoryTagEnum;
import com.baolong.picture.domain.categoryTag.service.CategoryTagDomainService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryQueryRequest;
import com.baolong.picture.interfaces.vo.categoryTag.CategoryTagVO;
import com.baolong.picture.interfaces.vo.categoryTag.CategoryVOAndTagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类标签应用服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryTagApplicationServiceImpl implements CategoryTagApplicationService {

	private final CategoryTagDomainService categoryTagDomainService;
	private final UserApplicationService userApplicationService;

	// region 增删改

	/**
	 * 新增分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	@Override
	public Boolean addCategoryTag(CategoryTag categoryTag) {
		return categoryTagDomainService.addCategoryTag(categoryTag);
	}

	/**
	 * 新增分类标签
	 *
	 * @param categoryTag 分类标签
	 * @param request     HttpServletRequest
	 * @return 是否成功
	 */
	@Override
	public Boolean addCategoryTag(CategoryTag categoryTag, HttpServletRequest request) {
		User loginUser = userApplicationService.getLoginUser(request);
		categoryTag.setUserId(loginUser.getId());
		return categoryTagDomainService.addCategoryTag(categoryTag);
	}

	/**
	 * 删除分类标签
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteCategoryTag(DeleteRequest deleteRequest) {
		// 判断是否存在
		Long categoryTagId = deleteRequest.getId();
		CategoryTag categoryTag = this.getCategoryTagById(categoryTagId);
		ThrowUtils.throwIf(categoryTag == null, ErrorCode.NOT_FOUND_ERROR);
		return categoryTagDomainService.deleteCategoryTagById(categoryTagId);
	}

	/**
	 * 修改分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	@Override
	public Boolean updateCategoryTag(CategoryTag categoryTag) {
		// 判断是否存在
		Long categoryTagId = categoryTag.getId();
		CategoryTag oldCategoryTag = this.getCategoryTagById(categoryTagId);
		ThrowUtils.throwIf(oldCategoryTag == null, ErrorCode.NOT_FOUND_ERROR);
		// 设置编辑时间
		categoryTag.setEditTime(new Date());
		return categoryTagDomainService.updateCategoryTagById(categoryTag);
	}

	// endregion 增删改

	/**
	 * 获取分类和标签数据
	 *
	 * @return 分类标签VO
	 */
	@Override
	public CategoryVOAndTagVO getCategoryVOAndTagVO() {
		List<CategoryTag> categoryTagList = this.getCategoryTagList(null);
		List<CategoryTagVO> voList = categoryTagList.stream().map(CategoryTagVO::objToVo).collect(Collectors.toList());
		Map<Integer, List<CategoryTagVO>> typeMap = voList.stream().collect(Collectors.groupingBy(CategoryTagVO::getType));
		CategoryVOAndTagVO pictureTagCategory = new CategoryVOAndTagVO();
		pictureTagCategory.setCategoryVOList(typeMap.get(CategoryTagEnum.CATEGORY.getValue()));
		pictureTagCategory.setTagVOList(typeMap.get(CategoryTagEnum.TAG.getValue()));
		return pictureTagCategory;
	}

	/**
	 * 获取查询条件对象
	 *
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 查询条件对象
	 */
	@Override
	public LambdaQueryWrapper<CategoryTag> getQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
		return categoryTagDomainService.getQueryWrapper(categoryQueryRequest);
	}

	/**
	 * 获取分类标签列表
	 *
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 分类标签列表
	 */
	@Override
	public List<CategoryTag> getCategoryTagList(CategoryQueryRequest categoryQueryRequest) {
		return categoryTagDomainService.getCategoryTagList(getQueryWrapper(categoryQueryRequest));
	}

	/**
	 * 获取分类标签列表（分页）
	 *
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 分类标签分页列表
	 */
	@Override
	public Page<CategoryTag> getCategoryTagListPage(CategoryQueryRequest categoryQueryRequest) {
		return categoryTagDomainService.getCategoryTagListPage(
				new Page<>(categoryQueryRequest.getCurrent(), categoryQueryRequest.getPageSize())
				, this.getQueryWrapper(categoryQueryRequest)
		);
	}

	/**
	 * 根据分类标签Id获取分类标签
	 *
	 * @param categoryTagId 分类标签ID
	 * @return 分类标签
	 */
	@Override
	public CategoryTag getCategoryTagById(Long categoryTagId) {
		return categoryTagDomainService.getCategoryTagById(categoryTagId);
	}
}




