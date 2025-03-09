package com.baolong.pictures.interfaces.dto.tag;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签更新请求
 */
@Data
public class TagUpdateRequest implements Serializable {

	/**
	 * 标签 ID
	 */
	private Long id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 使用数量
	 */
	private Integer useNum;

	private static final long serialVersionUID = 1L;
}
