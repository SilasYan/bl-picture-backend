package com.baolong.blpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baolong.blpicturebackend.auth.StpKit;
import com.baolong.blpicturebackend.constant.UserConstant;
import com.baolong.blpicturebackend.exception.BusinessException;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.manager.upload.FilePictureUpload;
import com.baolong.blpicturebackend.manager.upload.PictureUploadTemplate;
import com.baolong.blpicturebackend.mapper.UserMapper;
import com.baolong.blpicturebackend.model.dto.picture.UploadPictureResult;
import com.baolong.blpicturebackend.model.dto.user.UserQueryRequest;
import com.baolong.blpicturebackend.model.dto.user.UserVipCode;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.UserRoleEnum;
import com.baolong.blpicturebackend.model.enums.UserVipEnum;
import com.baolong.blpicturebackend.model.vo.LoginUserVO;
import com.baolong.blpicturebackend.model.vo.UserVO;
import com.baolong.blpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author ADMIN
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-02-11 22:32:08
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
		implements UserService {

	@Autowired
	private FilePictureUpload filePictureUpload;

	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return 新用户 id
	 */
	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		// 1. 校验
		if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		// 2. 检查是否重复
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		long count = this.baseMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
		}
		// 3. 加密
		String encryptPassword = getEncryptPassword(userPassword);
		// 4. 插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setUserName("无名");
		user.setUserRole(UserRoleEnum.USER.getValue());
		boolean saveResult = this.save(user);
		if (!saveResult) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
		}
		return user.getId();
	}

	/**
	 * 获取加密密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	@Override
	public String getEncryptPassword(String userPassword) {
		// 盐值，混淆密码
		final String SALT = "yupi";
		return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
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
		// 1. 校验
		if (StrUtil.hasBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 2. 加密
		String encryptPassword = getEncryptPassword(userPassword);
		// 查询用户是否存在
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", encryptPassword);
		User user = this.baseMapper.selectOne(queryWrapper);
		// 用户不存在
		if (user == null) {
			log.info("user login failed, userAccount cannot match userPassword");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 3. 记录用户的登录态
		request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

		// 4. 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
		StpKit.SPACE.login(user.getId());
		StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);

		return this.getLoginUserVO(user);
	}

	/**
	 * 获取脱敏的已登录用户信息
	 *
	 * @return 脱敏后的用户信息
	 */
	@Override
	public LoginUserVO getLoginUserVO(User user) {
		if (user == null) {
			return null;
		}
		LoginUserVO loginUserVO = new LoginUserVO();
		BeanUtils.copyProperties(user, loginUserVO);
		return loginUserVO;
	}

	/**
	 * 获取当前登录用户
	 *
	 * @param request HttpServletRequest
	 * @return 当前登录用户
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		// 先判断是否已登录
		Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if (currentUser == null || currentUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		// 从数据库查询（追求性能的话可以注释，直接返回上述结果）
		long userId = currentUser.getId();
		currentUser = this.getById(userId);
		if (currentUser == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return currentUser;
	}

	/**
	 * 用户注销
	 *
	 * @param request HttpServletRequest
	 * @return 注销成功与否
	 */
	@Override
	public boolean userLogout(HttpServletRequest request) {
		// 先判断是否已登录
		Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		if (userObj == null) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
		}
		// 移除登录态
		request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
		return true;
	}

	/**
	 * 获取脱敏的用户信息
	 *
	 * @param user 用户
	 * @return 脱敏的用户信息
	 */
	@Override
	public UserVO getUserVO(User user) {
		if (user == null) {
			return null;
		}
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return userVO;
	}

	/**
	 * 获取脱敏的用户信息列表
	 *
	 * @param userList 用户列表
	 * @return 脱敏的用户信息列表
	 */
	@Override
	public List<UserVO> getUserVOList(List<User> userList) {
		if (CollUtil.isEmpty(userList)) {
			return new ArrayList<>();
		}
		return userList.stream().map(this::getUserVO).collect(Collectors.toList());
	}

	/**
	 * 获取查询条件对象
	 *
	 * @param userQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
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
	 * 是否为管理员
	 *
	 * @param user 用户
	 * @return 是否为管理员
	 */
	@Override
	public boolean isAdmin(User user) {
		return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
	}

	// region ------- 以下代码为用户兑换会员功能 --------

	@Autowired
	private ResourceLoader resourceLoader;

	// 文件读写锁（确保并发安全）
	private final ReentrantLock fileLock = new ReentrantLock();

	/**
	 * 用户兑换会员（会员码兑换）
	 *
	 * @param user    登录的用户
	 * @param vipCode 会员码
	 * @return 是否兑换成功
	 */
	@Override
	public boolean exchangeVip(User user, String vipCode) {
		// 1. 参数校验
		if (user == null || StrUtil.isBlank(vipCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 2. 读取并校验兑换码
		UserVipCode targetCode = validateAndMarkVipCode(vipCode);
		// 3. 更新用户信息
		updateUserVipInfo(user, targetCode.getCode());
		return true;
	}

	/**
	 * 校验兑换码并标记为已使用
	 */
	private UserVipCode validateAndMarkVipCode(String vipCode) {
		fileLock.lock(); // 加锁保证文件操作原子性
		try {
			// 读取 JSON 文件
			JSONArray jsonArray = readVipCodeFile();

			// 查找匹配的未使用兑换码
			List<UserVipCode> codes = JSONUtil.toList(jsonArray, UserVipCode.class);
			UserVipCode target = codes.stream()
					.filter(code -> code.getCode().equals(vipCode) && !code.isHasUsed())
					.findFirst()
					.orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "无效的兑换码"));

			// 标记为已使用
			target.setHasUsed(true);

			// 写回文件
			writeVipCodeFile(JSONUtil.parseArray(codes));
			return target;
		} finally {
			fileLock.unlock();
		}
	}

	/**
	 * 读取兑换码文件
	 */
	private JSONArray readVipCodeFile() {
		try {
			// Resource resource = resourceLoader.getResource("classpath:biz/userVipCode.json");
			Resource resource = resourceLoader.getResource("/userVipCode.json");
			String content = FileUtil.readString(resource.getFile(), StandardCharsets.UTF_8);
			return JSONUtil.parseArray(content);
		} catch (IOException e) {
			log.error("读取兑换码文件失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
		}
	}

	/**
	 * 写入兑换码文件
	 */
	private void writeVipCodeFile(JSONArray jsonArray) {
		try {
			// Resource resource = resourceLoader.getResource("classpath:biz/userVipCode.json");
			Resource resource = resourceLoader.getResource("/userVipCode.json");
			FileUtil.writeString(jsonArray.toStringPretty(), resource.getFile(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("更新兑换码文件失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
		}
	}

	/**
	 * 更新用户会员信息
	 */
	private void updateUserVipInfo(User user, String usedVipCode) {
		User currentUser = this.lambdaQuery().eq(User::getId, user.getId()).one();
		// 如果当前用户已经有了会员, 并且会员未过期, 则在该日期基础上加上365天, 否则为当前时间加365天
		Date expireTime;
		if (currentUser.getVipExpireTime() != null && currentUser.getVipExpireTime().after(new Date())) {
			expireTime = DateUtil.offsetDay(currentUser.getVipExpireTime(), 365); // 计算当前时间加 365 天后的时间
		} else {
			expireTime = DateUtil.offsetDay(new Date(), 365); // 计算当前时间加 365 天后的时间
		}
		// 构建更新对象
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setVipExpireTime(expireTime); // 设置过期时间
		updateUser.setVipCode(usedVipCode);     // 记录使用的兑换码
		updateUser.setVipSign(UserVipEnum.VIP.getValue());       // 修改用户会员角色
		if (currentUser.getVipNumber() == null) {
			// 查询用户表中 vipNumber 最大的那一条数据
			User maxVipNumberUser = this.lambdaQuery().select(User::getVipNumber).orderByDesc(User::getVipNumber).last("limit 1").one();
			if (maxVipNumberUser == null) {
				updateUser.setVipNumber(10000L); // 如果没有数据，则设置会员编号为 1
			} else {
				updateUser.setVipNumber(maxVipNumberUser.getVipNumber() + 1); // 修改用户会员编号
			}
		}
		// 执行更新
		boolean updated = this.updateById(updateUser);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "开通会员失败，操作数据库失败");
		}
	}

	// endregion ------- 以下代码为用户兑换会员功能 --------

	/**
	 * 上传头像
	 *
	 * @param avatarFile 头像文件
	 * @param request    HttpServletRequest
	 * @param loginUser  登录的用户
	 * @return 头像地址
	 */
	@Override
	public String uploadAvatar(MultipartFile avatarFile, HttpServletRequest request, User loginUser) {
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 上传头像, 头像统一管理
		String uploadPathPrefix = String.format("avatar/%s", loginUser.getId());
		PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
		UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(avatarFile, uploadPathPrefix);
		String originUrl = uploadPictureResult.getOriginUrl();
		if (StrUtil.isBlank(originUrl)) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败");
		}
		// 更新用户头像
		User user = new User();
		user.setId(loginUser.getId());
		user.setUserAvatar(originUrl);
		boolean updated = this.updateById(user);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户头像失败");
		}
		return originUrl;
	}
}




