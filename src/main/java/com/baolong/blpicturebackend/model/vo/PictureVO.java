package com.baolong.blpicturebackend.model.vo;

import cn.hutool.core.util.StrUtil;
import com.baolong.blpicturebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片上传成功响应类
 */
@Data
public class PictureVO implements Serializable {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 图片 url
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
	private String name;

	/**
	 * 简介
	 */
	private String introduction;

	/**
	 * 标签
	 */
	private List<String> tags;

	/**
	 * 标签 id 列表
	 */
	private List<Long> tagIds;

	/**
	 * 分类
	 */
	private String category;

	/**
	 * 分类 id
	 */
	private Long categoryId;

	/**
	 * 文件体积
	 */
	private Long picSize;

	/**
	 * 图片宽度
	 */
	private Integer picWidth;

	/**
	 * 图片高度
	 */
	private Integer picHeight;

	/**
	 * 图片比例
	 */
	private Double picScale;

	/**
	 * 图片格式
	 */
	private String picFormat;

	/**
	 * 用户 id
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 编辑时间
	 */
	private Date editTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 创建用户信息
	 */
	private UserVO user;

	private static final long serialVersionUID = 1L;

	/**
	 * 封装类转对象
	 */
	public static Picture voToObj(PictureVO pictureVO) {
		if (pictureVO == null) {
			return null;
		}
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureVO, picture);
		// 类型不同，需要转换
		// picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
		// 把VO的标签List转为字符串
		if (pictureVO.getTags() != null && !pictureVO.getTags().isEmpty()) {
			picture.setTags(String.join(",", pictureVO.getTags()));
		}
		return picture;
	}

	/**
	 * 对象转封装类
	 */
	public static PictureVO objToVo(Picture picture) {
		if (picture == null) {
			return null;
		}
		PictureVO pictureVO = new PictureVO();
		BeanUtils.copyProperties(picture, pictureVO);
		// 类型不同，需要转换
		// pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
		// 把原来标签格式转为List
		if (StrUtil.isNotBlank(picture.getTags())) {
			pictureVO.setTags(Arrays.stream(picture.getTags().split(",")).collect(Collectors.toList()));
		}
		return pictureVO;
	}
}
