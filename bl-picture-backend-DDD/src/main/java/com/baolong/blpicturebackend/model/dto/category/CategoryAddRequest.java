package com.baolong.blpicturebackend.model.dto.category;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类标签新增请求
 */
@Data
public class CategoryAddRequest implements Serializable {

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
