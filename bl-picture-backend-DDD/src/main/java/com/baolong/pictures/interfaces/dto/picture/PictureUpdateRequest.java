package com.baolong.pictures.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求
 */
@Data
public class PictureUpdateRequest implements Serializable {

	/**
	 * 图片 ID
	 */
	private Long id;

	/**
	 * 图片名称（展示）
	 */
	private String picName;

	/**
	 * 图片描述（展示）
	 */
	private String picDesc;

	/**
	 * 分类 ID
	 */
	private Long category;

	/**
	 * 标签 ID 列表
	 */
	private List<Long> tags;

	/**
	 * 输入的标签列表
	 */
	private List<String> inputTagList;

	/**
	 * 空间 ID
	 */
	private Long spaceId;

	private static final long serialVersionUID = 1L;
}
