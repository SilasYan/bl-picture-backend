package com.baolong.blpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户兑换会员请求
 */
@Data
public class UserVipExchangeRequest implements Serializable {

	// 兑换码
	private String vipCode;

	private static final long serialVersionUID = 1L;
}
