package com.baolong.picture.interfaces.assembler;

import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.interfaces.dto.user.UserAddRequest;
import com.baolong.picture.interfaces.dto.user.UserUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 用户转换类
 *
 * @author Baolong 2025年03月04 23:37
 * @version 1.0
 * @since 1.8
 */
public class UserAssembler {

	/**
	 * 将用户添加请求转换为用户实体
	 */
	public static User toUserEntity(UserAddRequest userAddRequest) {
		User user = new User();
		BeanUtils.copyProperties(userAddRequest, user);
		return user;
	}

	/**
	 * 将用户更新请求转换为用户实体
	 */
	public static User toUserEntity(UserUpdateRequest userUpdateRequest) {
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		return user;
	}
}
