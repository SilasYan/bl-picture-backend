package com.baolong.pictures.infrastructure.constant;

/**
 * 缓存 KEY 常量接口
 *
 * @author Baolong 2025年03月06 22:58
 * @version 1.0
 * @since 1.8
 */
public interface CacheKeyConstant {
	/**
	 * 邮箱验证码缓存 KEY 前缀
	 */
	String EMAIL_CODE_KEY = "EMAIL_CODE_KEY:%s:%s";

	/**
	 * 图形验证码缓存 KEY 前缀
	 */
	String CAPTCHA_CODE_KEY = "CAPTCHA_CODE_KEY:%s";
}
