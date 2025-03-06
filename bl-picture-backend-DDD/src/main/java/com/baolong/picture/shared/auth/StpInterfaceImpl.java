package com.baolong.picture.shared.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.baolong.picture.shared.auth.model.SpaceUserPermissionConstant;
import com.baolong.picture.infrastructure.exception.BusinessException;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.domain.space.enums.SpaceRoleEnum;
import com.baolong.picture.domain.space.enums.SpaceTypeEnum;
import com.baolong.picture.application.service.PictureApplicationService;
import com.baolong.picture.application.service.SpaceApplicationService;
import com.baolong.picture.application.service.SpaceUserApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.baolong.picture.domain.user.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

	@Resource
	private SpaceUserAuthManager spaceUserAuthManager;
	@Resource
	private SpaceUserApplicationService spaceUserApplicationService;
	@Resource
	private PictureApplicationService pictureApplicationService;
	@Resource
	private SpaceApplicationService spaceApplicationService;
	@Resource
	private UserApplicationService userApplicationService;

	/**
	 * 返回一个账号所拥有的权限码集合
	 */
	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		// 判断 loginType，仅对类型为 "space" 进行权限校验
		if (!StpKit.SPACE_TYPE.equals(loginType)) {
			return new ArrayList<>();
		}
		// 管理员权限，表示权限校验通过
		List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
		// 获取上下文对象
		SpaceUserAuthContext authContext = getAuthContextByRequest();
		// 如果所有字段都为空，表示查询公共图库，可以通过
		if (isAllFieldsNull(authContext)) {
			return ADMIN_PERMISSIONS;
		}
		// 获取 userId
		User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
		if (loginUser == null) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
		}
		Long userId = loginUser.getId();
		// 优先从上下文中获取 SpaceUser 对象
		SpaceUser spaceUser = authContext.getSpaceUser();
		if (spaceUser != null) {
			return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
		}
		// 如果有 spaceUserId，必然是团队空间，通过数据库查询 SpaceUser 对象
		Long spaceUserId = authContext.getSpaceUserId();
		if (spaceUserId != null) {
			spaceUser = spaceUserApplicationService.getSpaceUserById(spaceUserId);
			if (spaceUser == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
			}
			// 取出当前登录用户对应的 spaceUser
			QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("spaceId", spaceUser.getSpaceId());
			queryWrapper.eq("userId", userId);
			SpaceUser loginSpaceUser = spaceUserApplicationService.getSpaceUser(queryWrapper);
			if (loginSpaceUser == null) {
				return new ArrayList<>();
			}
			// 这里会导致管理员在私有空间没有权限，可以再查一次库处理
			return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
		}
		// 如果没有 spaceUserId，尝试通过 spaceId 或 pictureId 获取 Space 对象并处理
		Long spaceId = authContext.getSpaceId();
		if (spaceId == null) {
			// 如果没有 spaceId，通过 pictureId 获取 Picture 对象和 Space 对象
			Long pictureId = authContext.getPictureId();
			// 图片 id 也没有，则默认通过权限校验
			if (pictureId == null) {
				return ADMIN_PERMISSIONS;
			}
			Picture picture = pictureApplicationService.getPictureById(pictureId);
			if (picture == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
			}
			spaceId = picture.getSpaceId();
			// 公共图库，仅本人或管理员可操作
			if (spaceId == null) {
				if (picture.getUserId().equals(userId) || loginUser.isAdmin()) {
					return ADMIN_PERMISSIONS;
				} else {
					// 不是自己的图片，仅可查看
					return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
				}
			}
		}
		// 获取 Space 对象
		Space space = spaceApplicationService.getSpaceById(spaceId);
		if (space == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
		}
		// 根据 Space 类型判断权限
		if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
			// 私有空间，仅本人或管理员有权限
			if (space.getUserId().equals(userId) || loginUser.isAdmin()) {
				return ADMIN_PERMISSIONS;
			} else {
				return new ArrayList<>();
			}
		} else {
			// 团队空间，查询 SpaceUser 并获取角色和权限
			QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("spaceId", spaceId);
			queryWrapper.eq("userId", userId);
			spaceUser = spaceUserApplicationService.getSpaceUser(queryWrapper);
			if (spaceUser == null) {
				return new ArrayList<>();
			}
			return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
		}
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

	private boolean isAllFieldsNull(Object object) {
		if (object == null) {
			return true; // 对象本身为空
		}
		// 获取所有字段并判断是否所有字段都为空
		return Arrays.stream(ReflectUtil.getFields(object.getClass()))
				// 获取字段值
				.map(field -> ReflectUtil.getFieldValue(object, field))
				// 检查是否所有字段都为空
				.allMatch(ObjectUtil::isEmpty);
	}

	@Value("${server.servlet.context-path}")
	private String contextPath;

	/**
	 * 从请求中获取上下文对象
	 */
	private SpaceUserAuthContext getAuthContextByRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
		SpaceUserAuthContext authRequest;
		// 兼容 get 和 post 操作
		if (ContentType.JSON.getValue().equals(contentType)) {
			String body = ServletUtil.getBody(request);
			authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
		} else {
			Map<String, String> paramMap = ServletUtil.getParamMap(request);
			authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
		}
		// 根据请求路径区分 id 字段的含义
		Long id = authRequest.getId();
		if (ObjUtil.isNotNull(id)) {
			String requestUri = request.getRequestURI();
			String partUri = requestUri.replace(contextPath + "/", "");
			String moduleName = StrUtil.subBefore(partUri, "/", false);
			switch (moduleName) {
				case "picture":
					authRequest.setPictureId(id);
					break;
				case "spaceUser":
					authRequest.setSpaceUserId(id);
					break;
				case "space":
					authRequest.setSpaceId(id);
					break;
				default:
			}
		}
		return authRequest;
	}

}
