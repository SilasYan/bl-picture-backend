package com.baolong.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baolong.picture.application.service.UserApplicationService;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.domain.user.service.UserDomainService;
import com.baolong.picture.infrastructure.common.DeleteRequest;
import com.baolong.picture.domain.user.constant.UserConstant;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.assembler.UserAssembler;
import com.baolong.picture.interfaces.dto.user.UserLoginRequest;
import com.baolong.picture.interfaces.dto.user.UserQueryRequest;
import com.baolong.picture.interfaces.dto.user.UserRegisterRequest;
import com.baolong.picture.interfaces.vo.user.LoginUserVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 用户应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

	private final UserDomainService userDomainService;

	/**
	 * 发送邮箱验证码
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return 验证码 key
	 */
	@Override
	public String sendEmailCode(UserRegisterRequest userRegisterRequest) {
		User.validUserEmail(userRegisterRequest.getUserEmail());
		return userDomainService.sendEmailCode(userRegisterRequest.getUserEmail());
	}

	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return 用户ID
	 */
	@Override
	public Long userRegister(UserRegisterRequest userRegisterRequest) {
		String userEmail = userRegisterRequest.getUserEmail();
		String codeKey = userRegisterRequest.getCodeKey();
		String codeValue = userRegisterRequest.getCodeValue();
		User.validUserRegister(userEmail, codeKey, codeValue);
		return userDomainService.userRegister(userEmail, codeKey, codeValue);
	}

	/**
	 * 用户登录
	 *
	 * @param userLoginRequest 用户登录请求
	 * @return 登录用户信息
	 */
	@Override
	public LoginUserVO userLogin(UserLoginRequest userLoginRequest) {
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		String captchaKey = userLoginRequest.getCaptchaKey();
		String captchaCode = userLoginRequest.getCaptchaCode();
		User.validUserLogin(userAccount, userPassword, captchaKey, captchaCode);
		User user = userDomainService.userLogin(userAccount, userPassword, captchaKey, captchaCode);
		return UserAssembler.toLoginUserVO(user);
	}

	/**
	 * 获取加密密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	@Override
	public String getEncryptPassword(String userPassword) {
		return userDomainService.getEncryptPassword(userPassword);
	}

	/**
	 * 用户退出（退出登录）
	 *
	 * @param request HttpServletRequest
	 * @return 是否成功
	 */
	@Override
	public Boolean userLogout(HttpServletRequest request) {
		// 先判断是否已登录
		Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		if (userObj == null) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
		}
		return userDomainService.userLogout(request);
	}

	/**
	 * 获取登录用户信息
	 *
	 * @param request HttpServletRequest
	 * @return 当前登录用户信息
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		// 先判断是否已登录
		Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if (currentUser == null || currentUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return userDomainService.getLoginUser(currentUser);
	}

	/**
	 * 获取登录用户信息（脱敏）
	 *
	 * @param user 用户实体
	 * @return 当前登录用户脱敏信息
	 */
	@Override
	public LoginUserVO getLoginUserVO(User user) {
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "参数为空");
		return userDomainService.getLoginUserVO(user);
	}

	/**
	 * 获取用户信息（脱敏）
	 *
	 * @param user 用户
	 * @return 用户脱敏信息
	 */
	@Override
	public UserVO getUserVO(User user) {
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "参数为空");
		return userDomainService.getUserVO(user);
	}

	/**
	 * 获取用户信息列表（脱敏）
	 *
	 * @param userList 用户列表
	 * @return 用户脱敏信息列表
	 */
	@Override
	public List<UserVO> getUserVOList(List<User> userList) {
		if (CollUtil.isEmpty(userList)) {
			return new ArrayList<>();
		}
		return userDomainService.getUserVOList(userList);
	}

	/**
	 * 获取查询条件对象
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
		return userDomainService.getQueryWrapper(userQueryRequest);
	}

	/**
	 * 新增用户
	 *
	 * @param user 用户
	 * @return 用户ID
	 */
	@Override
	public Long addUser(User user) {
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "参数为空");
		return userDomainService.addUser(user);
	}

	/**
	 * 删除用户
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteUser(DeleteRequest deleteRequest) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		return userDomainService.deleteUser(deleteRequest.getId());
	}

	/**
	 * 修改用户
	 *
	 * @param user 用户
	 * @return 是否成功
	 */
	@Override
	public Boolean updateUser(User user) {
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "参数为空");
		if (user.getId() == null || user.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		return userDomainService.updateUserById(user);
	}

	/**
	 * 根据用户ID获取用户
	 *
	 * @param userId 用户ID
	 * @return 用户
	 */
	@Override
	public User getUserById(Long userId) {
		ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR);
		User user = userDomainService.getUserById(userId);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
		return user;
	}

	/**
	 * 根据用户ID获取用户（脱敏）
	 *
	 * @param userId 用户ID
	 * @return 用户脱敏信息
	 */
	@Override
	public UserVO getUserVOById(Long userId) {
		return userDomainService.getUserVO(getUserById(userId));
	}

	/**
	 * 分页查询用户
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 用户分页列表
	 */
	@Override
	public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
		ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
		long current = userQueryRequest.getCurrent();
		long size = userQueryRequest.getPageSize();
		Page<User> userPage = userDomainService.listUserVOByPage(new Page<>(current, size),
				userDomainService.getQueryWrapper(userQueryRequest));
		Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
		List<UserVO> userVO = userDomainService.getUserVOList(userPage.getRecords());
		userVOPage.setRecords(userVO);
		return userVOPage;
	}

	/**
	 * 根据用户ID集合获取用户列表
	 *
	 * @param userIdSet 用户ID集合
	 * @return 用户列表
	 */
	@Override
	public List<User> listUserByIds(Set<Long> userIdSet) {
		return userDomainService.listUserByIds(userIdSet);
	}

	// // region ------- 以下代码为用户兑换会员功能 --------
	//
	// private final ResourceLoader resourceLoader;
	//
	// // 文件读写锁（确保并发安全）
	// private final ReentrantLock fileLock = new ReentrantLock();
	//
	// /**
	//  * 会员码兑换兑换会员
	//  *
	//  * @param user    用户
	//  * @param vipCode 会员码
	//  * @return 是否兑换成功
	//  */
	// @Override
	// public Boolean exchangeVip(User user, String vipCode) {
	// 	// 1. 参数校验
	// 	if (user == null || StrUtil.isBlank(vipCode)) {
	// 		throw new BusinessException(ErrorCode.PARAMS_ERROR);
	// 	}
	// 	// 2. 读取并校验兑换码
	// 	UserVipCode targetCode = validateAndMarkVipCode(vipCode);
	// 	// 3. 更新用户信息
	// 	updateUserVipInfo(user, targetCode.getCode());
	// 	return true;
	// }
	//
	// /**
	//  * 校验兑换码并标记为已使用
	//  */
	// private UserVipCode validateAndMarkVipCode(String vipCode) {
	// 	fileLock.lock(); // 加锁保证文件操作原子性
	// 	try {
	// 		// 读取 JSON 文件
	// 		JSONArray jsonArray = readVipCodeFile();
	//
	// 		// 查找匹配的未使用兑换码
	// 		List<UserVipCode> codes = JSONUtil.toList(jsonArray, UserVipCode.class);
	// 		UserVipCode target = codes.stream()
	// 				.filter(code -> code.getCode().equals(vipCode) && !code.isHasUsed())
	// 				.findFirst()
	// 				.orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "无效的兑换码"));
	//
	// 		// 标记为已使用
	// 		target.setHasUsed(true);
	//
	// 		// 写回文件
	// 		writeVipCodeFile(JSONUtil.parseArray(codes));
	// 		return target;
	// 	} finally {
	// 		fileLock.unlock();
	// 	}
	// }
	//
	// /**
	//  * 读取兑换码文件
	//  */
	// private JSONArray readVipCodeFile() {
	// 	try {
	// 		// Resource resource = resourceLoader.getResource("classpath:biz/userVipCode.json");
	// 		Resource resource = resourceLoader.getResource("/userVipCode.json");
	// 		String content = FileUtil.readString(resource.getFile(), StandardCharsets.UTF_8);
	// 		return JSONUtil.parseArray(content);
	// 	} catch (IOException e) {
	// 		log.error("读取兑换码文件失败", e);
	// 		throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
	// 	}
	// }
	//
	// /**
	//  * 写入兑换码文件
	//  */
	// private void writeVipCodeFile(JSONArray jsonArray) {
	// 	try {
	// 		// Resource resource = resourceLoader.getResource("classpath:biz/userVipCode.json");
	// 		Resource resource = resourceLoader.getResource("/userVipCode.json");
	// 		FileUtil.writeString(jsonArray.toStringPretty(), resource.getFile(), StandardCharsets.UTF_8);
	// 	} catch (IOException e) {
	// 		log.error("更新兑换码文件失败", e);
	// 		throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
	// 	}
	// }
	//
	// /**
	//  * 更新用户会员信息
	//  */
	// private void updateUserVipInfo(User user, String usedVipCode) {
	// 	User currentUser = this.lambdaQuery().eq(User::getId, user.getId()).one();
	// 	// 如果当前用户已经有了会员, 并且会员未过期, 则在该日期基础上加上365天, 否则为当前时间加365天
	// 	Date expireTime;
	// 	if (currentUser.getVipExpireTime() != null && currentUser.getVipExpireTime().after(new Date())) {
	// 		expireTime = DateUtil.offsetDay(currentUser.getVipExpireTime(), 365); // 计算当前时间加 365 天后的时间
	// 	} else {
	// 		expireTime = DateUtil.offsetDay(new Date(), 365); // 计算当前时间加 365 天后的时间
	// 	}
	// 	// 构建更新对象
	// 	User updateUser = new User();
	// 	updateUser.setId(user.getId());
	// 	updateUser.setVipExpireTime(expireTime); // 设置过期时间
	// 	updateUser.setVipCode(usedVipCode);     // 记录使用的兑换码
	// 	updateUser.setVipSign(UserVipEnum.VIP.getValue());       // 修改用户会员角色
	// 	if (currentUser.getVipNumber() == null) {
	// 		// 查询用户表中 vipNumber 最大的那一条数据
	// 		User maxVipNumberUser = this.lambdaQuery().select(User::getVipNumber).orderByDesc(User::getVipNumber).last("limit 1").one();
	// 		if (maxVipNumberUser == null) {
	// 			updateUser.setVipNumber(10000L); // 如果没有数据，则设置会员编号为 1
	// 		} else {
	// 			updateUser.setVipNumber(maxVipNumberUser.getVipNumber() + 1); // 修改用户会员编号
	// 		}
	// 	}
	// 	// 执行更新
	// 	boolean updated = this.updateById(updateUser);
	// 	if (!updated) {
	// 		throw new BusinessException(ErrorCode.OPERATION_ERROR, "开通会员失败，操作数据库失败");
	// 	}
	// }
	//
	// // endregion ------- 以下代码为用户兑换会员功能 --------
	//
	// /**
	//  * 上传头像
	//  *
	//  * @param avatarFile 头像文件
	//  * @param request    HttpServletRequest
	//  * @param loginUser  登录的用户
	//  * @return 头像地址
	//  */
	// @Override
	// public String uploadAvatar(MultipartFile avatarFile, HttpServletRequest request, User loginUser) {
	// 	ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
	// 	// 上传头像, 头像统一管理
	// 	String uploadPathPrefix = String.format("avatar/%s", loginUser.getId());
	// 	PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
	// 	UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(avatarFile, uploadPathPrefix);
	// 	String originUrl = uploadPictureResult.getOriginUrl();
	// 	if (StrUtil.isBlank(originUrl)) {
	// 		throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败");
	// 	}
	// 	// 更新用户头像
	// 	User user = new User();
	// 	user.setId(loginUser.getId());
	// 	user.setUserAvatar(originUrl);
	// 	boolean updated = this.updateById(user);
	// 	if (!updated) {
	// 		throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户头像失败");
	// 	}
	// 	return originUrl;
	// }
}




