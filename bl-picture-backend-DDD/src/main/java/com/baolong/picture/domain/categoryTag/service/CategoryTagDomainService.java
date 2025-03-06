package com.baolong.picture.domain.categoryTag.service;

import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 分类标签领域服务接口
 */
public interface CategoryTagDomainService {

	// region 增删改

	/**
	 * 新增分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	Boolean addCategoryTag(CategoryTag categoryTag);

	/**
	 * 删除分类标签
	 *
	 * @param categoryTagId 分类标签ID
	 * @return 是否成功
	 */
	Boolean deleteCategoryTagById(Long categoryTagId);

	/**
	 * 更新分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	Boolean updateCategoryTagById(CategoryTag categoryTag);

	// endregion 增删改

	/**
	 * 获取查询条件对象
	 *
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 查询条件对象
	 */
	LambdaQueryWrapper<CategoryTag> getQueryWrapper(CategoryQueryRequest categoryQueryRequest);

	/**
	 * 获取分类标签列表
	 *
	 * @param queryWrapper 查询条件
	 * @return 分类标签列表
	 */
	List<CategoryTag> getCategoryTagList(LambdaQueryWrapper<CategoryTag> queryWrapper);

	/**
	 * 获取分类标签列表（分页）
	 *
	 * @param page         分页对象
	 * @param queryWrapper 查询条件
	 * @return 分类标签分页列表
	 */
	Page<CategoryTag> getCategoryTagListPage(Page<CategoryTag> page, LambdaQueryWrapper<CategoryTag> queryWrapper);

	/**
	 * 根据分类标签Id获取分类标签
	 *
	 * @param categoryTagId 分类标签ID
	 * @return 分类标签
	 */
	CategoryTag getCategoryTagById(Long categoryTagId);
}
