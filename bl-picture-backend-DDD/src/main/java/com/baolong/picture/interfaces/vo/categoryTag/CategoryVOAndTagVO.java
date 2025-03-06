package com.baolong.picture.interfaces.vo.categoryTag;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分类标签（视图）
 *
 * @author Baolong 2025年02月13 23:53
 * @version 1.0
 * @since 1.8
 */
@Data
public class CategoryVOAndTagVO implements Serializable {

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
