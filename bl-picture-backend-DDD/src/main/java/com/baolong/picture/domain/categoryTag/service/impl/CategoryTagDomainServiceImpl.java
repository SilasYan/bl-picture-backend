package com.baolong.picture.domain.categoryTag.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.domain.categoryTag.repository.CategoryTagRepository;
import com.baolong.picture.domain.categoryTag.service.CategoryTagDomainService;
import com.baolong.picture.infrastructure.utils.MyBatisPlusUtils;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类标签领域服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryTagDomainServiceImpl implements CategoryTagDomainService {

	private final CategoryTagRepository categoryTagRepository;

	// region 增删改

	/**
	 * 新增分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	@Override
	public Boolean addCategoryTag(CategoryTag categoryTag) {
		return categoryTagRepository.save(categoryTag);
	}

	/**
	 * 删除分类标签
	 *
	 * @param categoryTagId 分类标签ID
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteCategoryTagById(Long categoryTagId) {
		return categoryTagRepository.removeById(categoryTagId);
	}

	/**
	 * 更新分类标签
	 *
	 * @param categoryTag 分类标签
	 * @return 是否成功
	 */
	@Override
	public Boolean updateCategoryTagById(CategoryTag categoryTag) {
		return categoryTagRepository.updateById(categoryTag);
	}

	// endregion 增删改

	/**
	 * 获取查询条件对象
	 *
	 * @param categoryQueryRequest 分类标签查询请求
	 * @return 查询条件对象
	 */
	@Override
	public LambdaQueryWrapper<CategoryTag> getQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
		LambdaQueryWrapper<CategoryTag> lambdaQueryWrapper = new LambdaQueryWrapper<CategoryTag>();
		if (categoryQueryRequest == null) return lambdaQueryWrapper;
		Long id = categoryQueryRequest.getId();
		Integer type = categoryQueryRequest.getType();
		String name = categoryQueryRequest.getName();
		Long userId = categoryQueryRequest.getUserId();
		String sortField = categoryQueryRequest.getSortField();
		String sortOrder = categoryQueryRequest.getSortOrder();
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(id), CategoryTag::getId, id);
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(type), CategoryTag::getType, type);
		lambdaQueryWrapper.like(StrUtil.isNotBlank(name), CategoryTag::getName, name);
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(userId), CategoryTag::getUserId, userId);
		lambdaQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"),
				MyBatisPlusUtils.getSFunction(CategoryTag.class, sortField));
		return lambdaQueryWrapper;
	}

	/**
	 * 获取分类标签列表
	 *
	 * @param queryWrapper 查询条件
	 * @return 分类标签列表
	 */
	@Override
	public List<CategoryTag> getCategoryTagList(LambdaQueryWrapper<CategoryTag> queryWrapper) {
		return categoryTagRepository.list(queryWrapper);
	}

	/**
	 * 获取分类标签列表（分页）
	 *
	 * @param page         分页对象
	 * @param queryWrapper 查询条件
	 * @return 分类标签分页列表
	 */
	@Override
	public Page<CategoryTag> getCategoryTagListPage(Page<CategoryTag> page, LambdaQueryWrapper<CategoryTag> queryWrapper) {
		return categoryTagRepository.page(page, queryWrapper);
	}

	/**
	 * 根据分类标签Id获取分类标签
	 *
	 * @param categoryTagId 分类标签ID
	 * @return 分类标签
	 */
	@Override
	public CategoryTag getCategoryTagById(Long categoryTagId) {
		return categoryTagRepository.getById(categoryTagId);
	}
}




