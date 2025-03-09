package com.baolong.pictures.domain.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.pictures.application.shared.auth.StpKit;
import com.baolong.pictures.domain.user.constant.UserConstant;
import com.baolong.pictures.domain.user.entity.User;
import com.baolong.pictures.domain.user.service.UserDomainService;
import com.baolong.pictures.infrastructure.common.constant.CacheKeyConstant;
import com.baolong.pictures.infrastructure.common.page.PageRequest;
import com.baolong.pictures.infrastructure.exception.BusinessException;
import com.baolong.pictures.infrastructure.exception.ErrorCode;
import com.baolong.pictures.infrastructure.exception.ThrowUtils;
import com.baolong.pictures.infrastructure.manager.message.EmailManager;
import com.baolong.pictures.infrastructure.manager.redis.RedisCache;
import com.baolong.pictures.infrastructure.repository.UserRepository;
import com.baolong.pictures.infrastructure.utils.SFLambdaUtil;
import com.baolong.pictures.infrastructure.utils.ServletUtils;
import com.baolong.pictures.interfaces.dto.user.UserQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户领域服务实现类
 */
@Slf4j
@Service
public class UserDomainServiceImpl implements UserDomainService {
	@Resource
	private UserRepository userRepository;
	@Resource
	private EmailManager emailManager;
	@Resource
	private RedisCache redisCache;

	// region 登录注册

	/**
	 * 发送邮箱验证码
	 *
	 * @param userEmail 用户邮箱
	 * @return 验证码 key
	 */
	@Override
	public String sendEmailCode(String userEmail) {
		Long count = userRepository.getBaseMapper().selectCount(new QueryWrapper<User>().eq("user_email", userEmail));
		ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在, 请直接登录!");
		// 发送验证码
		String code = RandomUtil.randomNumbers(4);
		Map<String, Object> contentMap = new HashMap<>();
		contentMap.put("code", code);
		emailManager.sendEmail(userEmail, "注册验证码 - 暴龙图库", contentMap);
		// 生成一个唯一 ID, 后面注册前端需要带过来
		String key = UUID.randomUUID().toString();
		// 存入 Redis, 5 分钟过期
		redisCache.set(String.format(CacheKeyConstant.EMAIL_CODE_KEY, key, userEmail), code, 5, TimeUnit.MINUTES);
		return key;
	}

	/**
	 * 用户注册
	 *
	 * @param userEmail 用户邮箱
	 * @param codeKey   验证码 key
	 * @param codeValue 验证码 value
	 * @return 用户ID
	 */
	@Override
	public Long userRegister(String userEmail, String codeKey, String codeValue) {
		String KEY = String.format(CacheKeyConstant.EMAIL_CODE_KEY, codeKey, userEmail);
		// 获取 Redis 中的验证码
		String code = redisCache.get(KEY);
		if (StrUtil.isEmpty(code) || !code.equals(codeValue)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
		}
		Long count = userRepository.getBaseMapper().selectCount(new QueryWrapper<User>().eq("user_email", userEmail));
		ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在, 请直接登录!");
		// 构建参数
		User user = new User();
		// 默认值填充
		user.fillDefaultValue();
		user.setUserEmail(userEmail);
		boolean result = userRepository.save(user);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
		}
		// 删除验证码
		redisCache.delete(KEY);
		return user.getId();
	}

	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param captchaKey   图形验证码 key
	 * @param captchaCode  图形验证码 验证码
	 * @return 用户信息
	 */
	@Override
	public User userLogin(String userAccount, String userPassword, String captchaKey, String captchaCode) {
		String KEY = String.format(CacheKeyConstant.CAPTCHA_CODE_KEY, captchaKey);
		// 获取 Redis 中的验证码
		String code = redisCache.get(KEY);
		if (StrUtil.isEmpty(code) || !code.equals(captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
		}
		// 构建账号/邮箱登录的请求条件
		LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
		if (ReUtil.isMatch(RegexPool.EMAIL, userAccount)) {
			// 邮箱登录
			queryWrapper.eq(User::getUserEmail, userAccount);
		} else {
			// 账号登录
			queryWrapper.eq(User::getUserAccount, userAccount);
		}
		User user = userRepository.getOne(queryWrapper);
		if (user == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 校验密码
		String encryptPassword = user.getEncryptPassword(userPassword);
		if (!user.getUserPassword().equals(encryptPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 记录用户的登录态到 Session, 已经开启缓存到 Redis 中的配置
		ServletUtils.getRequest().getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
		// 记录用户登录态到 Sa-token，注意保证该用户信息与 SpringSession 中的信息过期时间一致
		StpKit.SPACE.login(user.getId());
		StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
		// 删除验证码
		redisCache.delete(KEY);
		return user;
	}

	/**
	 * 用户退出（退出登录）
	 *
	 * @return 是否成功
	 */
	@Override
	public Boolean userLogout() {
		// 先判断是否已登录
		HttpServletRequest request = ServletUtils.getRequest();
		Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		if (userObj == null) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
		}
		// 移除登录态
		request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
		return true;
	}

	// endregion 登录注册

	// region 增删改相关

	/**
	 * 新增用户
	 *
	 * @param user 用户
	 * @return 是否成功
	 */
	@Override
	public Boolean addUser(User user) {
		user.fillDefaultValue();
		return userRepository.save(user);
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
	 * 更新用户
	 *
	 * @param user 用户
	 * @return 是否成功
	 */
	@Override
	public Boolean updateUser(User user) {
		return userRepository.updateById(user);
	}

	/**
	 * 编辑用户
	 *
	 * @param user 用户
	 * @return 是否成功
	 */
	@Override
	public Boolean editUser(User user) {
		return userRepository.updateById(user);
	}

	// endregion 增删改相关

	// region 查询相关

	/**
	 * 获取登录用户信息
	 *
	 * @return 登录用户信息
	 */
	@Override
	public User getLoginUser() {
		// 先判断是否已登录
		Object userObj = ServletUtils.getRequest().getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		User TUser = (User) userObj;
		if (TUser == null || TUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return TUser;
	}

	/**
	 * 根据用户 ID 获取用户信息
	 *
	 * @param userId 用户ID
	 * @return 用户
	 */
	@Override
	public User getUserById(Long userId) {
		User user = userRepository.getById(userId);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}
		return user;
	}

	/**
	 * 获取分页用户列表（管理员, 条件查询）
	 *
	 * @param page               分页对象
	 * @param lambdaQueryWrapper 查询条件
	 * @return 用户分页列表
	 */
	@Override
	public Page<User> getUserPageListAsAdmin(Page<User> page, LambdaQueryWrapper<User> lambdaQueryWrapper) {
		return userRepository.page(page, lambdaQueryWrapper);
	}

	/**
	 * 根据用户 ID 判断用户是否存在
	 *
	 * @param userId 用户 ID
	 * @return 是否存在
	 */
	@Override
	public Boolean existUserById(Long userId) {
		return userRepository.getBaseMapper().exists(new LambdaQueryWrapper<User>().eq(User::getId, userId));
	}

	/**
	 * 根据用户邮箱判断用户是否存在
	 *
	 * @param userEmail 用户邮箱
	 * @return 是否存在
	 */
	@Override
	public Boolean existUserByEmail(String userEmail) {
		return userRepository.getBaseMapper().exists(new LambdaQueryWrapper<User>().eq(User::getUserEmail, userEmail));
	}

	/**
	 * 根据用户 ID 集合获取用户列表
	 *
	 * @param userIds 用户 ID 集合
	 * @return 用户列表
	 */
	@Override
	public List<User> getUserListByIds(Set<Long> userIds) {
		return userRepository.listByIds(userIds);
	}

	// endregion 查询相关

	// region 其他方法

	/**
	 * 获取查询条件对象
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		Long id = userQueryRequest.getId();
		String userName = userQueryRequest.getUserName();
		String userAccount = userQueryRequest.getUserAccount();
		String userEmail = userQueryRequest.getUserEmail();
		String userPhone = userQueryRequest.getUserPhone();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
		queryWrapper.like(StrUtil.isNotEmpty(userName), "user_name", userName);
		queryWrapper.eq(StrUtil.isNotEmpty(userAccount), "user_account", userAccount);
		queryWrapper.eq(StrUtil.isNotEmpty(userEmail), "user_email", userEmail);
		queryWrapper.eq(StrUtil.isNotEmpty(userPhone), "user_phone", userPhone);
		queryWrapper.like(StrUtil.isNotEmpty(userProfile), "user_profile", userProfile);
		queryWrapper.eq(StrUtil.isNotEmpty(userRole), "user_role", userRole);
		if (userQueryRequest.isMultipleSort()) {
			List<PageRequest.Sort> sorts = userQueryRequest.getSorts();
			if (CollUtil.isNotEmpty(sorts)) {
				sorts.forEach(sort -> {
					String sortField = sort.getField();
					boolean sortAsc = sort.isAsc();
					queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortAsc, sortField);
				});
			}
		} else {
			PageRequest.Sort sort = userQueryRequest.getSort();
			if (sort != null) {
				String sortField = sort.getField();
				boolean sortAsc = sort.isAsc();
				queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortAsc, sortField);
			} else {
				queryWrapper.orderByDesc("create_time");
			}
		}
		return queryWrapper;
	}

	/**
	 * 获取查询条件对象（Lambda）
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return 查询条件对象（Lambda）
	 */
	@Override
	public LambdaQueryWrapper<User> getLambdaQueryWrapper(UserQueryRequest userQueryRequest) {
		LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
		Long id = userQueryRequest.getId();
		String userName = userQueryRequest.getUserName();
		String userAccount = userQueryRequest.getUserAccount();
		String userEmail = userQueryRequest.getUserEmail();
		String userPhone = userQueryRequest.getUserPhone();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(id), User::getId, id);
		lambdaQueryWrapper.like(StrUtil.isNotEmpty(userName), User::getUserName, userName);
		lambdaQueryWrapper.eq(StrUtil.isNotEmpty(userAccount), User::getUserAccount, userAccount);
		lambdaQueryWrapper.eq(StrUtil.isNotEmpty(userEmail), User::getUserEmail, userEmail);
		lambdaQueryWrapper.eq(StrUtil.isNotEmpty(userPhone), User::getUserPhone, userPhone);
		lambdaQueryWrapper.like(StrUtil.isNotEmpty(userProfile), User::getUserProfile, userProfile);
		lambdaQueryWrapper.eq(StrUtil.isNotEmpty(userRole), User::getUserRole, userRole);
		// 处理排序规则
		if (userQueryRequest.isMultipleSort()) {
			List<PageRequest.Sort> sorts = userQueryRequest.getSorts();
			if (CollUtil.isNotEmpty(sorts)) {
				sorts.forEach(sort -> {
					String sortField = sort.getField();
					boolean sortAsc = sort.isAsc();
					lambdaQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortAsc, SFLambdaUtil.getSFunction(User.class, sortField));
				});
			}
		} else {
			PageRequest.Sort sort = userQueryRequest.getSort();
			if (sort != null) {
				String sortField = sort.getField();
				boolean sortAsc = sort.isAsc();
				lambdaQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortAsc, SFLambdaUtil.getSFunction(User.class, sortField));
			} else {
				lambdaQueryWrapper.orderByDesc(User::getCreateTime);
			}
		}
		return lambdaQueryWrapper;
	}

	// endregion 其他方法

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




