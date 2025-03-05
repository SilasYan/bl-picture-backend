package com.baolong.picture.domain.user.entity;

import cn.hutool.core.util.StrUtil;
import com.baolong.picture.domain.user.enums.UserRoleEnum;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 账号
	 */
	private String userAccount;

	/**
	 * 密码
	 */
	private String userPassword;

	/**
	 * 用户昵称
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 用户简介
	 */
	private String userProfile;

	/**
	 * 用户邮箱
	 */
	private String userEmail;

	/**
	 * 用户手机号
	 */
	private String userPhone;

	/**
	 * 用户角色：user/admin
	 */
	private String userRole;

	/**
	 * 出生日期
	 */
	private Date birthday;

	/**
	 * 编辑时间
	 */
	private Date editTime;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 是否删除
	 */
	@TableLogic
	private Integer isDelete;

	/**
	 * 会员过期时间
	 */
	private Date vipExpireTime;

	/**
	 * 会员兑换码
	 */
	private String vipCode;

	/**
	 * 会员编号
	 */
	private Long vipNumber;

	/**
	 * 会员标识：VIP/SVIP
	 */
	private String vipSign;

	/**
	 * 分享码
	 */
	private String shareCode;

	/**
	 * 邀请用户 id
	 */
	private Long inviteUser;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	/**
	 * 校验用户注册
	 *
	 * @param userAccount   用户账号
	 * @param userPassword  用户密码
	 * @param checkPassword 确认密码
	 */
	public static void validUserRegister(String userAccount, String userPassword, String checkPassword) {
		if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
	}

	/**
	 * 校验用户登录
	 *
	 * @param userAccount  用户账号
	 * @param userPassword 用户密码
	 */
	public static void validUserLogin(String userAccount, String userPassword) {
		if (StrUtil.hasBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4 || userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
		}
	}

	/**
	 * 是否为管理员
	 *
	 * @return 是否为管理员
	 */
	public Boolean isAdmin() {
		return UserRoleEnum.ADMIN.getValue().equals(this.getUserRole());
	}

}
