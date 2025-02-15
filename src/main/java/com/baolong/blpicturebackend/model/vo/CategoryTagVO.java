package com.baolong.blpicturebackend.model.vo;

import com.baolong.blpicturebackend.model.entity.CategoryTag;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 分类标签响应类
 */
@Data
public class CategoryTagVO implements Serializable {
	/**
	 * id
	 */
	private Long id;

	/**
	 * 类型（0-分类、1-标签）
	 */
	private Integer type;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 使用数量
	 */
	private Integer useNum;

	private static final long serialVersionUID = 1L;

	/**
	 * 对象转封装类
	 */
	public static CategoryTagVO objToVo(CategoryTag categoryTag) {
		if (categoryTag == null) {
			return null;
		}
		CategoryTagVO categoryTagVO = new CategoryTagVO();
		BeanUtils.copyProperties(categoryTag, categoryTagVO);
		return categoryTagVO;
	}
}
