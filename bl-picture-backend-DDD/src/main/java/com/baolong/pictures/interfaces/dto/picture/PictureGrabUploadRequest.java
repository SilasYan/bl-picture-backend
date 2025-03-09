package com.baolong.pictures.interfaces.dto.picture;

import lombok.Data;

import java.util.List;

/**
 * 图片抓取上传请求
 */
@Data
public class PictureGrabUploadRequest {

	/**
	 * 搜索词
	 */
	private String searchText;

	/**
	 * 抓取数量
	 */
	private Integer count = 10;

	/**
	 * 名称前缀
	 */
	private String namePrefix;

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
}
