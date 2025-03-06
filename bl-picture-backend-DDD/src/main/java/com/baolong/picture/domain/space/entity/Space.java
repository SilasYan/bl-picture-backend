package com.baolong.picture.domain.space.entity;

import cn.hutool.core.util.StrUtil;
import com.baolong.picture.domain.space.enums.SpaceLevelEnum;
import com.baolong.picture.domain.space.enums.SpaceTypeEnum;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间
 *
 * @TableName space
 */
@TableName(value = "space")
@Data
public class Space implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 空间名称
	 */
	private String spaceName;

	/**
	 * 空间级别：0-普通版 1-专业版 2-旗舰版
	 */
	private Integer spaceLevel;

	/**
	 * 空间图片的最大总大小
	 */
	private Long maxSize;

	/**
	 * 空间图片的最大数量
	 */
	private Long maxCount;

	/**
	 * 当前空间下图片的总大小
	 */
	private Long totalSize;

	/**
	 * 当前空间下的图片数量
	 */
	private Long totalCount;

	/**
	 * 创建用户 id
	 */
	private Long userId;

	/**
	 * 空间类型：0-私有 1-团队
	 */
	private Integer spaceType;

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

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	/**
	 * 校验空间参数
	 *
	 * @param add 是否新增
	 */
	public void validSpace(boolean add) {
		// 从对象中取值
		String spaceName = this.getSpaceName();
		Integer spaceLevel = this.getSpaceLevel();
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
		Integer spaceType = this.getSpaceType();
		SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
		// 要创建
		if (add) {
			if (StrUtil.isBlank(spaceName)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
			}
			if (spaceLevel == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
			}
			if (spaceType == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
			}
		}
		// 修改数据时，如果要改空间级别
		if (spaceLevel != null && spaceLevelEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
		}
		// 修改数据时，如果要改空间级别
		if (spaceType != null && spaceTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
		}
		if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
		}
	}

	/**
	 * 填充默认值
	 */
	public void fillDefaultValue() {
		if (StrUtil.isBlank(this.getSpaceName())) {
			this.setSpaceName("默认空间");
		}
		if (this.getSpaceLevel() == null) {
			this.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
		}
		if (this.getSpaceType() == null) {
			this.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
		}
	}

	/**
	 * 填充空间等级信息
	 */
	public void fillSpaceLevelValue() {
		// 根据空间级别，自动填充限额
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(this.getSpaceLevel());
		if (spaceLevelEnum != null) {
			long maxSize = spaceLevelEnum.getMaxSize();
			if (this.getMaxSize() == null) {
				this.setMaxSize(maxSize);
			}
			long maxCount = spaceLevelEnum.getMaxCount();
			if (this.getMaxCount() == null) {
				this.setMaxCount(maxCount);
			}
		} else {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间等级不存在");
		}
	}
}
