package com.baolong.pictures.interfaces.dto.tag;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签新增请求
 */
@Data
public class TagAddRequest implements Serializable {

	/**
	 * 名称
	 */
	private String name;

	private static final long serialVersionUID = 1L;
}
