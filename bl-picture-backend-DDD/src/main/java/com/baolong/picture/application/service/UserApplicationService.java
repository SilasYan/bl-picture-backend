package com.baolong.picture.application.service;

import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.interfaces.dto.user.UserLoginRequest;
import com.baolong.picture.interfaces.dto.user.UserQueryRequest;
import com.baolong.picture.interfaces.dto.user.UserRegisterRequest;
import com.baolong.picture.interfaces.vo.user.LoginUserVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 用户应用服务接口
 */
public interface UserApplicationService {

	/**
	 * 发送邮箱验证码
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return 验证码 key
	 */
	String sendEmailCode(UserRegisterRequest userRegisterRequest);

	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return 用户ID
	 */
	Long userRegister(UserRegisterRequest userRegisterRequest);

	/**
	 * 用户登录
	 *
	 * @param userLoginRequest 用户登录请求
	 * @return 登录用户信息
	 */
	LoginUserVO userLogin(UserLoginRequest userLoginRequest);

	/**
	 * 获取加密密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	String getEncryptPassword(String userPassword);

	/**
	 * 用户退出（退出登录）
	 *
	 * @param request HttpServletRequest
	 * @return 是否成功
	 */
	Boolean userLogout(HttpServletRequest request);

	/**
	 * 获取登录用户信息
	 *
	 * @param request HttpServletRequest
	 * @return 当前登录用户信息
	 */
	User getLoginUser(HttpServletRequest request);

	/**
	 * 获取登录用户信息（脱敏）
	 *
	 * @param user 用户
	 * @return 当前登录用户脱敏信息
	 */
	LoginUserVO getLoginUserVO(User user);

	/**
	 * 获取用户信息（脱敏）
	 *
	 * @param user 用户
	 * @return 用户脱敏信息
	 */
	UserVO getUserVO(User user);

	/**
	 * 获取用户信息列表（脱敏）
	 *
	 * @param userList 用户列表
	 * @return 用户脱敏信息列表
	 */
	List<UserVO> getUserVOList(List<User> userList);

	/**
	 * 获取查询条件对象
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 查询条件对象
	 */
	QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

	/**
	 * 新增用户
	 *
	 * @param user 用户
	 * @return 用户ID
	 */
	Long addUser(User user);

	/**
	 * 删除用户
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	Boolean deleteUser(DeleteRequest deleteRequest);

	/**
	 * 修改用户
	 *
	 * @param user 用户
	 * @return 是否成功
	 */
	Boolean updateUser(User user);

	/**
	 * 根据用户ID获取用户
	 *
	 * @param userId 用户ID
	 * @return 用户
	 */
	User getUserById(Long userId);

	/**
	 * 根据用户ID获取用户（脱敏）
	 *
	 * @param userId 用户ID
	 * @return 用户脱敏信息
	 */
	UserVO getUserVOById(Long userId);

	/**
	 * 分页查询用户
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 用户分页列表
	 */
	Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

	/**
	 * 根据用户ID集合获取用户列表
	 *
	 * @param userIdSet 用户ID集合
	 * @return 用户列表
	 */
	List<User> listUserByIds(Set<Long> userIdSet);
	//
	// /**
	//  * 会员码兑换兑换会员
	//  *
	//  * @param user    用户
	//  * @param vipCode 会员码
	//  * @return 是否兑换成功
	//  */
	// Boolean exchangeVip(User user, String vipCode);
	//
	// /**
	//  * 上传头像
	//  *
	//  * @param avatarFile 头像文件
	//  * @param request    HttpServletRequest
	//  * @param loginUser  登录的用户
	//  * @return 头像地址
	//  */
	// String uploadAvatar(MultipartFile avatarFile, HttpServletRequest request, User loginUser);
}
