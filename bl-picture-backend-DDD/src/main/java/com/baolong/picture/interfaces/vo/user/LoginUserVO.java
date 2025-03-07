package com.baolong.picture.interfaces.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 已登录用户信息（脱敏）
 */
@Data
public class LoginUserVO implements Serializable {
	/**
	 * 用户 ID
	 */
	private Long id;

	/**
	 * 账号
	 */
	private String userAccount;

	/**
	 * 用户昵称
	 */
	private String userName;

	/**
	 * 用户邮箱
	 */
	private String userEmail;

	/**
	 * 用户手机号
	 */
	private String userPhone;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 用户简介
	 */
	private String userProfile;

	/**
	 * 用户角色：user/admin
	 */
	private String userRole;

	/**
	 * 会员标识：VIP/SVIP
	 */
	private String vipSign;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private static final long serialVersionUID = 1L;
}
