package com.baolong.blpicturebackend.model.dto.picture;

import lombok.Data;

/**
 * 图片上传返回结果（解析过后的图片结果）
 */
@Data
public class UploadPictureResult {

	/**
	 * 图片地址
	 */
	private String url;

	/**
	 * 缩略图 url
	 */
	private String thumbnailUrl;

	/**
	 * 原图大小
	 */
	private Long originSize;

	/**
	 * 原图 url
	 */
	private String originUrl;

	/**
	 * 图片名称
	 */
	private String picName;

	/**
	 * 文件体积
	 */
	private Long picSize;

	/**
	 * 图片宽度
	 */
	private int picWidth;

	/**
	 * 图片高度
	 */
	private int picHeight;

	/**
	 * 图片宽高比
	 */
	private Double picScale;

	/**
	 * 图片格式
	 */
	private String picFormat;

}
