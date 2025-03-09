package com.baolong.pictures.interfaces.vo.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片简单 VO
 */
@Data
public class PictureSimpleVO implements Serializable {
	/**
	 * 图片 ID
	 */
	private Long id;

	/**
	 * 图片地址（展示, 压缩图地址）
	 */
	private String picUrl;

	/**
	 * 缩略图地址
	 */
	private String thumbnailUrl;

	/**
	 * 分类 ID
	 */
	private Long category;

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

	private static final long serialVersionUID = 1L;
}
