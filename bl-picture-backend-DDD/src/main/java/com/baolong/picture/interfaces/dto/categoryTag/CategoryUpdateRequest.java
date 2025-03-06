package com.baolong.picture.interfaces.dto.categoryTag;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类标签更新请求
 */
@Data
public class CategoryUpdateRequest implements Serializable {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 类型（0-分类、1-标签）
	 */
	private Integer type;

	/**
	 * 图片名称
	 */
	private String name;

	private static final long serialVersionUID = 1L;
}
