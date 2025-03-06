package com.baolong.picture.infrastructure.utils;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 MyBatisPlus 的反射工具类
 *
 * @author Baolong 2025年03月05 21:55
 * @version 1.0
 * @since 1.8
 */
public class MyBatisPlusUtils {
	/**
	 * 获取类中声明的所有字段名称
	 *
	 * @param clazz 类
	 * @return 字段名称列表
	 */
	public static List<String> getFieldNames(Class<?> clazz) {
		List<String> fieldNames = new ArrayList<>();

		// 获取类中声明的所有字段（包括私有字段）
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			fieldNames.add(field.getName());
		}

		return fieldNames;
	}

	/**
	 * 根据字段名称获取对象中对应字段的值
	 *
	 * @param obj       对象
	 * @param fieldName 字段名称
	 * @return 字段的值，如果字段不存在或无法访问则返回 null
	 */
	public static Object getFieldValue(Object obj, String fieldName) {
		try {
			// 获取对象的 Class 对象
			Class<?> clazz = obj.getClass();

			// 获取指定名称的字段（包括私有字段）
			Field field = clazz.getDeclaredField(fieldName);

			// 设置字段可访问（如果是私有字段）
			field.setAccessible(true);

			// 返回字段的值
			return field.get(obj);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			// 如果字段不存在或无法访问，打印异常信息并返回 null
			e.printStackTrace();
			return null;
		}
	}

	// 缓存字段对应的 SFunction
	private static final Map<String, SFunction<?, ?>> CACHE = new ConcurrentHashMap<>();

	/**
	 * 根据字段名称返回 SFunction<T, ?> 类型的 Lambda 表达式
	 *
	 * @param clazz     目标类
	 * @param fieldName 字段名称
	 * @param <T>       目标类的类型
	 * @return SFunction<T, ?> 类型的 Lambda 表达式
	 */
	public static <T> SFunction<T, ?> getSFunction(Class<T> clazz, String fieldName) {
		String cacheKey = clazz.getName() + "#" + fieldName;
		return (SFunction<T, ?>) CACHE.computeIfAbsent(cacheKey, key -> {
			try {
				// 获取目标类的 getter 方法
				Method getter = clazz.getMethod("get" + capitalize(fieldName));

				// 动态生成 SFunction
				return createSFunction(getter);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Field getter not found: " + fieldName, e);
			}
		});
	}

	/**
	 * 动态生成 SFunction
	 *
	 * @param getter 字段的 getter 方法
	 * @return SFunction<T, ?>
	 */
	private static <T> SFunction<T, ?> createSFunction(Method getter) {
		return (SFunction<T, ?>) Proxy.newProxyInstance(
				MyBatisPlusUtils.class.getClassLoader(),
				new Class[]{SFunction.class},
				(proxy, method, args) -> {
					// 调用 getter 方法
					return getter.invoke(args[0]);
				}
		);
	}

	/**
	 * 将字符串首字母大写
	 *
	 * @param str 字符串
	 * @return 首字母大写的字符串
	 */
	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
