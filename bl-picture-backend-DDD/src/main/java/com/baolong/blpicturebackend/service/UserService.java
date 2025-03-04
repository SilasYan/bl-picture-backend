package com.baolong.blpicturebackend.service;

import com.baolong.blpicturebackend.model.dto.user.UserQueryRequest;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.vo.LoginUserVO;
import com.baolong.blpicturebackend.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ADMIN
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-02-11 22:32:08
 */
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return 新用户 id
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword);

	/**
	 * 获取加密密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	String getEncryptPassword(String userPassword);

	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request      HttpServletRequest
	 * @return 脱敏后的用户信息
	 */
	LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

	/**
	 * 获取脱敏的已登录用户信息
	 *
	 * @return 脱敏后的用户信息
	 */
	LoginUserVO getLoginUserVO(User user);

	/**
	 * 获取当前登录用户
	 *
	 * @param request HttpServletRequest
	 * @return 当前登录用户
	 */
	User getLoginUser(HttpServletRequest request);

	/**
	 * 用户注销
	 *
	 * @param request HttpServletRequest
	 * @return 注销成功与否
	 */
	boolean userLogout(HttpServletRequest request);

	/**
	 * 获取脱敏的用户信息
	 *
	 * @param user 用户
	 * @return 脱敏的用户信息
	 */
	UserVO getUserVO(User user);

	/**
	 * 获取脱敏的用户信息列表
	 *
	 * @param userList 用户列表
	 * @return 脱敏的用户信息列表
	 */
	List<UserVO> getUserVOList(List<User> userList);

	/**
	 * 获取查询条件对象
	 *
	 * @param userQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

	/**
	 * 是否为管理员
	 *
	 * @param user 用户
	 * @return 是否为管理员
	 */
	boolean isAdmin(User user);

	/**
	 * 用户兑换会员（会员码兑换）
	 *
	 * @param user    登录的用户
	 * @param vipCode 会员码
	 * @return 是否兑换成功
	 */
	boolean exchangeVip(User user, String vipCode);

	/**
	 * 上传头像
	 *
	 * @param avatarFile 头像文件
	 * @param request    HttpServletRequest
	 * @param loginUser  登录的用户
	 * @return 头像地址
	 */
	String uploadAvatar(MultipartFile avatarFile, HttpServletRequest request, User loginUser);
}
