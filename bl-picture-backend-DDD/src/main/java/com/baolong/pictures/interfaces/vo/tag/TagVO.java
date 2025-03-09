package com.baolong.pictures.interfaces.vo.tag;

import java.io.Serializable;

/**
 * 标签 VO
 *
 * @author Baolong 2025年03月09 21:13
 * @version 1.0
 * @since 1.8
 */
public class TagVO implements Serializable {

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
