package com.baolong.pictures.interfaces.dto.tag;

import com.baolong.pictures.infrastructure.common.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 标签标签查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TagQueryRequest extends PageRequest implements Serializable {

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

	/**
	 * 创建用户 ID
	 */
	private Long userId;

	private static final long serialVersionUID = 1L;
}
