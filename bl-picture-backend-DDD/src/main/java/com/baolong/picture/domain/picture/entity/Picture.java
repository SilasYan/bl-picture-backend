package com.baolong.picture.domain.picture.entity;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片
 *
 * @TableName picture
 */
@TableName(value = "picture")
@Data
public class Picture implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
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
	 * 分类
	 */
	private String category;

	/**
	 * 标签（JSON 数组）
	 */
	private String tags;

	/**
	 * 图片体积
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
	 * 图片宽高比例
	 */
	private Double picScale;

	/**
	 * 图片格式
	 */
	private String picFormat;

	/**
	 * 图片主色调
	 */
	private String picColor;

	/**
	 * 创建用户 id
	 */
	private Long userId;

	/**
	 * 空间 id
	 */
	private Long spaceId;

	/**
	 * 状态：0-待审核; 1-通过; 2-拒绝
	 */
	private Integer reviewStatus;

	/**
	 * 审核信息
	 */
	private String reviewMessage;

	/**
	 * 审核人 id
	 */
	private Long reviewerId;

	/**
	 * 审核时间
	 */
	private Date reviewTime;

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
	 * 是否删除
	 */
	private Integer isDelete;

	/**
	 * 资源状态：0-存在存储服务器中、1-从存储服务器中删除
	 */
	private Integer resourceStatus;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	/**
	 * 校验图片编辑
	 */
	public void validPictureEdit() {
		if (ObjUtil.isNull(this.getId())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID不能为空");
		}
		if (StrUtil.isNotBlank(this.getUrl())) {
			ThrowUtils.throwIf(this.getUrl().length() > 1024, ErrorCode.PARAMS_ERROR, "图片地址过长");
		}
		if (StrUtil.isNotBlank(this.getIntroduction())) {
			ThrowUtils.throwIf(this.getIntroduction().length() > 500, ErrorCode.PARAMS_ERROR, "简介过长");
		}
	}
}
