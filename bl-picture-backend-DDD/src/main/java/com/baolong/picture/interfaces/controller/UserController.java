package com.baolong.picture.interfaces.controller;

import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.user.constant.UserConstant;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.annotation.AuthCheck;
import com.baolong.picture.infrastructure.common.BaseResponse;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.infrastructure.common.ResultUtils;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.assembler.UserAssembler;
import com.baolong.picture.interfaces.dto.user.UserAddRequest;
import com.baolong.picture.interfaces.dto.user.UserLoginRequest;
import com.baolong.picture.interfaces.dto.user.UserQueryRequest;
import com.baolong.picture.interfaces.dto.user.UserRegisterRequest;
import com.baolong.picture.interfaces.dto.user.UserUpdateRequest;
import com.baolong.picture.interfaces.vo.user.LoginUserVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserApplicationService userApplicationService;

	/**
	 * 发送邮箱验证码
	 */
	@PostMapping("/send/email/code")
	public BaseResponse<String> sendEmailCode(@RequestBody UserRegisterRequest userRegisterRequest) {
		ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
		return ResultUtils.success(userApplicationService.sendEmailCode(userRegisterRequest));
	}

	/**
	 * 用户注册
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
		return ResultUtils.success(userApplicationService.userRegister(userRegisterRequest));
	}

	/**
	 * 用户登录
	 */
	@PostMapping("/login")
	public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
		ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
		return ResultUtils.success(userApplicationService.userLogin(userLoginRequest));
	}

	/**
	 * 退出登录
	 */
	@PostMapping("/logout")
	public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
		return ResultUtils.success(userApplicationService.userLogout(request));
	}

	/**
	 * 获取登录用户信息
	 */
	@GetMapping("/get/login")
	public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
		User user = userApplicationService.getLoginUser(request);
		return ResultUtils.success(userApplicationService.getLoginUserVO(user));
	}

	// region 增删改查

	/**
	 * 新增用户
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
		ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
		User userEntity = UserAssembler.toUserEntity(userAddRequest);
		return ResultUtils.success(userApplicationService.addUser(userEntity));
	}

	/**
	 * 删除用户
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
		boolean b = userApplicationService.deleteUser(deleteRequest);
		return ResultUtils.success(b);
	}

	/**
	 * 修改用户
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
		ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		User userEntity = UserAssembler.toUserEntity(userUpdateRequest);
		return ResultUtils.success(userApplicationService.updateUser(userEntity));
	}

	/**
	 * 根据用户ID获取用户（仅管理员）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<User> getUserById(long id) {
		return ResultUtils.success(userApplicationService.getUserById(id));
	}

	/**
	 * 根据用户ID获取用户（脱敏）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<UserVO> getUserVOById(long id) {
		return ResultUtils.success(userApplicationService.getUserVOById(id));
	}

	/**
	 * 分页查询用户（仅管理员）
	 *
	 * @param userQueryRequest 查询请求参数
	 */
	@PostMapping("/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
		return ResultUtils.success(userApplicationService.listUserVOByPage(userQueryRequest));
	}

	/**
	 * 根据用户ID获取用户
	 */
	@GetMapping("/getInfo")
	public BaseResponse<UserVO> getUserInfoById(HttpServletRequest httpServletRequest) {
		User loginUser = userApplicationService.getLoginUser(httpServletRequest);
		User user = userApplicationService.getUserById(loginUser.getId());
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
		return ResultUtils.success(userApplicationService.getUserVO(user));
	}

	/**
	 * 更新用户信息
	 */
	@PostMapping("/updateInfo")
	public BaseResponse<Boolean> updateUserInfo(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest httpServletRequest) {
		if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userApplicationService.getLoginUser(httpServletRequest);
		if (!loginUser.getId().equals(userUpdateRequest.getId()) || !loginUser.isAdmin()) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "修改信息失败");
		}
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		boolean result = userApplicationService.updateUser(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	//
	// /**
	//  * 兑换会员
	//  */
	// @PostMapping("/exchange/vip")
	// public BaseResponse<Boolean> exchangeVip(@RequestBody UserVipExchangeRequest vipExchangeRequest,
	// 										 HttpServletRequest httpServletRequest) {
	// 	ThrowUtils.throwIf(vipExchangeRequest == null, ErrorCode.PARAMS_ERROR);
	// 	String vipCode = vipExchangeRequest.getVipCode();
	// 	User loginUser = userApplicationService.getLoginUser(httpServletRequest);
	// 	// 调用 service 层的方法进行会员兑换
	// 	boolean result = userApplicationService.exchangeVip(loginUser, vipCode);
	// 	return ResultUtils.success(result);
	// }
	//
	// /**
	//  * 上传头像
	//  */
	// @PostMapping("/uploadAvatar")
	// public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile avatarFile, HttpServletRequest request) {
	// 	User loginUser = userApplicationService.getLoginUser(request);
	// 	return ResultUtils.success(userApplicationService.uploadAvatar(avatarFile, request, loginUser));
	// }
}
