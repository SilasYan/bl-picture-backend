package com.baolong.blpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum CategoryTagEnum {

	CATEGORY("分类", 0),
	TAG("标签", 1);

	private final String text;

	private final Integer value;

	CategoryTagEnum(String text, Integer value) {
		this.text = text;
		this.value = value;
	}

	/**
	 * 根据 value 获取枚举
	 *
	 * @param value 枚举值的value
	 * @return 枚举值
	 */
	public static CategoryTagEnum getEnumByValue(Integer value) {
		if (ObjUtil.isEmpty(value)) {
			return null;
		}
		for (CategoryTagEnum anEnum : CategoryTagEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
}
