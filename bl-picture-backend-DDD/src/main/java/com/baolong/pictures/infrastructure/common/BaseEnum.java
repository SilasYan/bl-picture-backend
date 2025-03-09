package com.baolong.pictures.infrastructure.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基础枚举接口
 *
 * @author Baolong 2025年03月08 01:10
 * @version 1.0
 * @since 1.8
 */
public interface BaseEnum<T> {
	/**
	 * 枚举值
	 *
	 * @return T
	 */
	T key();

	/**
	 * 枚举内容
	 *
	 * @return String
	 */
	String label();


	/**
	 * 获取枚举
	 *
	 * @param key   枚举值
	 * @param clazz 枚举类
	 * @param <E>   枚举类型
	 * @return 枚举对象
	 */
	static <E extends Enum<E> & BaseEnum> E getEnum(Object key, Class<E> clazz) {
		Objects.requireNonNull(key);
		return EnumSet.allOf(clazz).stream()
				.filter(e -> Objects.equals(e.key(), key))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 获取枚举值
	 *
	 * @param label 枚举内容
	 * @param clazz 枚举类
	 * @param <E>   枚举类型
	 * @return 枚举值
	 */
	static <E extends Enum<E> & BaseEnum> Object getKey(String label, Class<E> clazz) {
		Objects.requireNonNull(label);
		E key = EnumSet.allOf(clazz).stream()
				.filter(e -> Objects.equals(e.label(), label))
				.findFirst()
				.orElse(null);
		return key != null ? key.key() : null;
	}

	/**
	 * 根据文本标签获取值
	 *
	 * @param key   枚举值
	 * @param clazz 枚举类
	 * @param <E>   枚举类型
	 * @return 枚举内容
	 */
	static <E extends Enum<E> & BaseEnum> String getLabel(Object key, Class<E> clazz) {
		Objects.requireNonNull(key);
		E label = EnumSet.allOf(clazz).stream()
				.filter(e -> Objects.equals(e.key(), key))
				.findFirst()
				.orElse(null);
		return label != null ? label.label() : null;
	}

	/**
	 * 把枚举转成列表
	 *
	 * @param clazz 枚举类
	 * @param <E>   枚举类型
	 * @return 枚举列表
	 */
	static <E extends Enum<E> & BaseEnum> List<Map<String, Object>> toList(Class<E> clazz) {
		return EnumSet.allOf(clazz).stream()
				.map(e -> {
					Map<String, Object> map = new HashMap<>(2);
					map.put("key", e.key());
					map.put("label", e.label());
					return map;
				})
				.collect(Collectors.toList());
	}
}
