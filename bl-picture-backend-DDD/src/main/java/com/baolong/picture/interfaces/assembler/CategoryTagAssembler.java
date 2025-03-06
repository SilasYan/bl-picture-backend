package com.baolong.picture.interfaces.assembler;

import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryAddRequest;
import com.baolong.picture.interfaces.dto.categoryTag.CategoryUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 分类标签转换类
 *
 * @author Baolong 2025年03月05 22:30
 * @version 1.0
 * @since 1.8
 */
public class CategoryTagAssembler {
	/**
	 * 将分类标签新增请求转换为分类标签实体
	 */
	public static CategoryTag toPictureEntity(CategoryAddRequest categoryAddRequest) {
		CategoryTag categoryTag = new CategoryTag();
		BeanUtils.copyProperties(categoryAddRequest, categoryTag);
		return categoryTag;
	}

	/**
	 * 将分类标签修改请求转换为分类标签实体
	 */
	public static CategoryTag toPictureEntity(CategoryUpdateRequest categoryUpdateRequest) {
		CategoryTag categoryTag = new CategoryTag();
		BeanUtils.copyProperties(categoryUpdateRequest, categoryTag);
		return categoryTag;
	}
}
