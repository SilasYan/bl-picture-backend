package com.baolong.pictures.interfaces.vo.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片 VO
 */
@Data
public class PictureVO implements Serializable {
	/**
	 * 图片 ID
	 */
	private Long id;

	/**
	 * 原图大小（单位: B）
	 */
	private Long originSize;

	/**
	 * 原图格式
	 */
	private String originFormat;

	/**
	 * 原图宽度
	 */
	private Integer originWidth;

	/**
	 * 原图高度
	 */
	private Integer originHeight;

	/**
	 * 原图比例（宽高比）
	 */
	private Double originScale;

	/**
	 * 原图主色调
	 */
	private String originColor;

	/**
	 * 图片名称（展示）
	 */
	private String picName;

	/**
	 * 图片描述（展示）
	 */
	private String picDesc;

	/**
	 * 图片地址（展示, 压缩图地址）
	 */
	private String picUrl;

	/**
	 * 压缩图大小
	 */
	private Long compressSize;

	/**
	 * 压缩图格式
	 */
	private String compressFormat;

	/**
	 * 缩略图地址
	 */
	private String thumbnailUrl;

	/**
	 * 分类 ID
	 */
	private Long category;

	/**
	 * 标签（逗号分隔的标签 ID 列表）
	 */
	private String tags;

	/**
	 * 创建用户 ID
	 */
	private Long userId;

	/**
	 * 创建用户昵称
	 */
	private String userName;

	/**
	 * 创建用户头像
	 */
	private String userAvatar;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private static final long serialVersionUID = 1L;
}
