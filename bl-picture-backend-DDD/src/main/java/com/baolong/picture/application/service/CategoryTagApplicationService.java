package com.baolong.picture.application.service;

import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryQueryRequest;
import com.baolong.picture.interfaces.vo.categoryTag.CategoryVOAndTagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 分类标签应用服务接口
 */
public interface CategoryTagApplicationService {

	// region 增删改

	/**
	 * 新增分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	Boolean addCategoryTag(CategoryTag categoryTag);

	/**
	 * 新增分类标签
	 *
	 * @param categoryTag 分类标签
	 * @param request     HttpServletRequest
	 * @return 是否成功
	 */
	Boolean addCategoryTag(CategoryTag categoryTag, HttpServletRequest request);

	/**
	 * 删除分类标签
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	Boolean deleteCategoryTag(DeleteRequest deleteRequest);

	/**
	 * 修改分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	Boolean updateCategoryTag(CategoryTag categoryTag);

	// endregion 增删改

	/**
	 * 获取分类和标签数据
	 *
	 * @return 分类标签VO
	 */
	CategoryVOAndTagVO getCategoryVOAndTagVO();

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
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 分类标签列表
	 */
	List<CategoryTag> getCategoryTagList(CategoryQueryRequest categoryQueryRequest);

	/**
	 * 获取分类标签列表（分页）
	 *
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 分类标签分页列表
	 */
	Page<CategoryTag> getCategoryTagListPage(CategoryQueryRequest categoryQueryRequest);

	/**
	 * 根据分类标签Id获取分类标签
	 *
	 * @param categoryTagId 分类标签ID
	 * @return 分类标签
	 */
	CategoryTag getCategoryTagById(Long categoryTagId);
}
