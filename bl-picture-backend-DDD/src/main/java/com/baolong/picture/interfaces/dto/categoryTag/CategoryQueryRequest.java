package com.baolong.picture.interfaces.dto.categoryTag;

import com.baolong.picture.infrastructure.comment.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分类标签查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryQueryRequest extends PageRequest implements Serializable {

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
	 * 用户 id
	 */
	private Long userId;

	private static final long serialVersionUID = 1L;
}
