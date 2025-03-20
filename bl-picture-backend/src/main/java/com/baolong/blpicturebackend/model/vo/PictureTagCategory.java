package com.baolong.blpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片片标签分类VO
 *
 * @author Baolong 2025年02月13 23:53
 * @version 1.0
 * @since 1.8
 */
@Data
public class PictureTagCategory implements Serializable {
	/**
	 * 标签列表
	 */
	private List<String> tagList;

	/**
	 * 分类列表
	 */
	private List<String> categoryList;

	/**
	 * 标签列表
	 */
	private List<CategoryTagVO> tagVOList;

	/**
	 * 分类列表
	 */
	private List<CategoryTagVO> categoryVOList;

	private static final long serialVersionUID = 1L;
}
