package com.baolong.picture.domain.user.entity;

import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
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
import lombok.experimental.Accessors;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体
 */
@TableName(value = "user")
@Data
@Accessors(chain = true)
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
	 * 校验用户邮箱
	 *
	 * @param userEmail 用户邮箱
	 */
	public static void validUserEmail(String userEmail) {
		if (StrUtil.isEmpty(userEmail) || !ReUtil.isMatch(RegexPool.EMAIL, userEmail)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
		}
	}

	/**
	 * 校验用户注册
	 *
	 * @param userEmail 用户邮箱
	 * @param codeKey   验证码 key
	 * @param codeValue 验证码 value
	 */
	public static void validUserRegister(String userEmail, String codeKey, String codeValue) {
		if (StrUtil.hasBlank(userEmail, codeKey, codeValue)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		validUserEmail(userEmail);
	}

	/**
	 * 校验用户登录
	 *
	 * @param userAccount  用户账号
	 * @param userPassword 用户密码
	 */
	public static void validUserLogin(String userAccount, String userPassword, String captchaKey, String captchaCode) {
		if (StrUtil.hasBlank(userAccount, userPassword, captchaKey, captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4 || userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
		}
	}

	/**
	 * 填充默认值
	 */
	public void fillDefaultValue() {
		String random = RandomUtil.randomString(6);
		this.setUserAccount("user_" + random);
		this.setUserName("用户_" + random);
		this.setUserRole(UserRoleEnum.USER.getValue());
		this.setUserPassword(this.getEncryptPassword("12345678"));
	}

	/**
	 * 获取加密的密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	public String getEncryptPassword(String userPassword) {
		final String SALT = "baolong";
		return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
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
