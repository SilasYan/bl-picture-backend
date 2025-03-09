package com.baolong.pictures.domain.user.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

	USER("USER", "用户"),
	ADMIN("ADMIN", "管理员");

	private final String key;

	private final String label;

	UserRoleEnum(String key, String label) {
		this.key = key;
		this.label = label;
	}

	/**
	 * 根据 key 获取枚举
	 *
	 * @param key 枚举 key
	 * @return key
	 */
	public static UserRoleEnum getEnumByValue(String key) {
		if (ObjUtil.isEmpty(key)) {
			return null;
		}
		for (UserRoleEnum anEnum : UserRoleEnum.values()) {
			if (anEnum.key.equals(key)) {
				return anEnum;
			}
		}
		return null;
	}
}
