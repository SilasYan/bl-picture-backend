package com.baolong.picture.domain.user.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.blpicturebackend.auth.StpKit;
import com.baolong.picture.domain.user.constant.UserConstant;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.domain.user.enums.UserRoleEnum;
import com.baolong.picture.domain.user.repository.UserRepository;
import com.baolong.picture.domain.user.service.UserDomainService;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import com.baolong.picture.interfaces.dto.user.UserQueryRequest;
import com.baolong.picture.interfaces.vo.user.LoginUserVO;
import com.baolong.picture.interfaces.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户领域服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {

	private final UserRepository userRepository;

	/**
	 * 获取加密密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	@Override
	public String getEncryptPassword(String userPassword) {
		// 盐值，混淆密码
		final String SALT = "baolong";
		return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
	}

	// @Autowired
	// private FilePictureUpload filePictureUpload;

	/**
	 * 用户注册
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @return 新用户 id
	 */
	@Override
	public Long userRegister(String userAccount, String userPassword) {
		// 查询是否已经存在该用户
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		long count = userRepository.getBaseMapper().selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在!");
		}
		// 插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(this.getEncryptPassword(userPassword));
		user.setUserName(userAccount + "_" + RandomUtil.randomString(6));
		user.setUserRole(UserRoleEnum.USER.getValue());
		boolean saveResult = userRepository.save(user);
		if (!saveResult) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
		}
		return user.getId();
	}

	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request      HttpServletRequest
	 * @return 脱敏后的用户信息
	 */
	@Override
	public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 查询用户是否存在
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", this.getEncryptPassword(userPassword));
		User user = userRepository.getBaseMapper().selectOne(queryWrapper);
		if (user == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 记录用户的登录态到 Session, 已经配置到 Redis 中
		request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
		// 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
		StpKit.SPACE.login(user.getId());
		StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);

		return this.getLoginUserVO(user);
	}

	/**
	 * 用户退出（退出登录）
	 *
	 * @param request HttpServletRequest
	 * @return 是否成功
	 */
	@Override
	public Boolean userLogout(HttpServletRequest request) {
		// 移除登录态
		request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
		return true;
	}

	/**
	 * 获取登录用户信息
	 *
	 * @param user 用户
	 * @return 当前登录用户信息
	 */
	@Override
	public User getLoginUser(User user) {
		user = userRepository.getById(user.getId());
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return user;
	}

	/**
	 * 获取登录用户信息（脱敏）
	 *
	 * @param user 用户
	 * @return 当前登录用户脱敏信息
	 */
	@Override
	public LoginUserVO getLoginUserVO(User user) {
		LoginUserVO loginUserVO = new LoginUserVO();
		BeanUtils.copyProperties(user, loginUserVO);
		return loginUserVO;
	}

	/**
	 * 获取用户信息（脱敏）
	 *
	 * @param user 用户
	 * @return 用户脱敏信息
	 */
	@Override
	public UserVO getUserVO(User user) {
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return userVO;
	}

	/**
	 * 获取用户信息列表（脱敏）
	 *
	 * @param userList 用户列表
	 * @return 用户脱敏信息列表
	 */
	@Override
	public List<UserVO> getUserVOList(List<User> userList) {
		return userList.stream().map(this::getUserVO).collect(Collectors.toList());
	}

	/**
	 * 获取查询条件对象
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		Long id = userQueryRequest.getId();
		String userAccount = userQueryRequest.getUserAccount();
		String userName = userQueryRequest.getUserName();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
		queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
		queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
		queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
		queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

	/**
	 * 新增用户
	 *
	 * @param user 用户
	 * @return 用户ID
	 */
	@Override
	public Long addUser(User user) {
		// 默认密码 12345678
		final String DEFAULT_PASSWORD = "12345678";
		String encryptPassword = this.getEncryptPassword(DEFAULT_PASSWORD);
		user.setUserPassword(encryptPassword);
		boolean result = userRepository.save(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return user.getId();
	}

	/**
	 * 删除用户
	 *
	 * @param userId 用户ID
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteUser(Long userId) {
		return userRepository.removeById(userId);
	}

	/**
	 * 修改用户
	 *
	 * @param user 用户
	 * @return 是否成功
	 */
	@Override
	public Boolean updateUserById(User user) {
		return userRepository.updateById(user);
	}

	/**
	 * 根据用户ID获取用户
	 *
	 * @param userId 用户ID
	 * @return 用户
	 */
	@Override
	public User getUserById(Long userId) {
		return userRepository.getById(userId);
	}

	/**
	 * 分页查询用户
	 *
	 * @param userPage     分页对象
	 * @param queryWrapper 查询条件
	 * @return 用户分页列表
	 */
	@Override
	public Page<User> listUserVOByPage(Page<User> userPage, QueryWrapper<User> queryWrapper) {
		return userRepository.page(userPage, queryWrapper);
	}

	/**
	 * 根据用户ID集合获取用户列表
	 *
	 * @param userIdSet 用户ID集合
	 * @return 用户列表
	 */
	@Override
	public List<User> listUserByIds(Set<Long> userIdSet) {
		return userRepository.listByIds(userIdSet);
	}

	// // region ------- 以下代码为用户兑换会员功能 --------
	//
	// @Autowired
	// private ResourceLoader resourceLoader;
	//
	// // 文件读写锁（确保并发安全）
	// private final ReentrantLock fileLock = new ReentrantLock();
	//
	// /**
	//  * 用户兑换会员（会员码兑换）
	//  *
	//  * @param user    登录的用户
	//  * @param vipCode 会员码
	//  * @return 是否兑换成功
	//  */
	// @Override
	// public boolean exchangeVip(User user, String vipCode) {
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




