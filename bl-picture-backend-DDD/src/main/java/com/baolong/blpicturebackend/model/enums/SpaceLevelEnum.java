package com.baolong.blpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 空间级别枚举
 */
@Getter
public enum SpaceLevelEnum {

	COMMON("普通版", 0, 100, 100L * 1024 * 1024),
	PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
	FLAGSHIP("💎旗舰版", 2, 10000, 10000L * 1024 * 1024);

	private final String text;

	private final int value;

	private final long maxCount;

	private final long maxSize;

	/**
	 * @param text     文本
	 * @param value    值
	 * @param maxSize  最大图片总大小
	 * @param maxCount 最大图片总数量
	 */
	SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
		this.text = text;
		this.value = value;
		this.maxCount = maxCount;
		this.maxSize = maxSize;
	}

	/**
	 * 根据 value 获取枚举
	 */
	public static SpaceLevelEnum getEnumByValue(Integer value) {
		if (ObjUtil.isEmpty(value)) {
			return null;
		}
		for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
			if (spaceLevelEnum.value == value) {
				return spaceLevelEnum;
			}
		}
		return null;
	}
}
