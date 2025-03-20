package com.baolong.blpicturebackend.model.dto.picture;

import lombok.Data;

import java.util.List;

/**
 * 批量上传图片请求
 */
@Data
public class PictureUploadByBatchRequest {

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
	 * 分类
	 */
	private String category;

	/**
	 * 标签
	 */
	private List<String> tags;

	/**
	 * 输入的标签列表
	 */
	private List<String> inputTagList;

}
