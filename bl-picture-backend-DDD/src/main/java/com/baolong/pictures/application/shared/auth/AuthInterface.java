package com.baolong.pictures.application.shared.auth;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限接口
 *
 * @author Baolong 2025年03月10 23:41
 * @version 1.0
 * @since 1.8
 */
@Component
public class AuthInterface implements StpInterface {

	/**
	 * 返回一个账号所拥有的权限码集合
	 */
	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		// 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询权限
		List<String> list = new ArrayList<String>();
		list.add("101");
		list.add("user.add");
		list.add("user.update");
		list.add("user.get");
		// list.add("user.delete");
		list.add("art.*");
		return list;
	}

	/**
	 * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
	 */
	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		// 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询角色
		List<String> list = new ArrayList<String>();
		list.add("admin");
		list.add("super-admin");
		return list;
	}

}
