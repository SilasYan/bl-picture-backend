package com.baolong.picture.interfaces.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 */
@Data
public class UserRegisterRequest implements Serializable {

	/**
	 * 账号
	 */
	private String userAccount;

	/**
	 * 密码
	 */
	private String userPassword;

	/**
	 * 确认密码
	 */
	private String checkPassword;

	/**
	 * 用户邮箱
	 */
	private String userEmail;

	/**
	 * 验证码 key
	 */
	private String codeKey;

	/**
	 * 验证码 value
	 */
	private String codeValue;

	private static final long serialVersionUID = 1L;
}
