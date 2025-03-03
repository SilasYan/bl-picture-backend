package com.baolong.blpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum UserVipEnum {

	VIP("普通会员", "VIP"),
	S_VIP("超级会员", "SVIP");

	private final String text;

	private final String value;

	UserVipEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}

	/**
	 * 根据 value 获取枚举
	 *
	 * @param value 枚举值的value
	 * @return 枚举值
	 */
	public static UserVipEnum getEnumByValue(String value) {
		if (ObjUtil.isEmpty(value)) {
			return null;
		}
		for (UserVipEnum anEnum : UserVipEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
}
