package com.baolong.pictures.application.shared.auth;

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
import com.baolong.pictures.application.service.PictureApplicationService;
import com.baolong.pictures.application.service.SpaceApplicationService;
import com.baolong.pictures.application.service.SpaceUserApplicationService;
import com.baolong.pictures.application.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
		/*// 判断 loginType，仅对类型为 "space" 进行权限校验
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
		TUser loginTUser = (TUser) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
		if (loginTUser == null) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
		}
		Long userId = loginTUser.getId();
		// 优先从上下文中获取 SpaceUser 对象
		TeSpaceUser teSpaceUser = authContext.getTeSpaceUser();
		if (teSpaceUser != null) {
			return spaceUserAuthManager.getPermissionsByRole(teSpaceUser.getSpaceRole());
		}
		// 如果有 spaceUserId，必然是团队空间，通过数据库查询 SpaceUser 对象
		Long spaceUserId = authContext.getSpaceUserId();
		if (spaceUserId != null) {
			teSpaceUser = spaceUserApplicationService.getSpaceUserById(spaceUserId);
			if (teSpaceUser == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
			}
			// 取出当前登录用户对应的 spaceUser
			QueryWrapper<TeSpaceUser> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("spaceId", teSpaceUser.getSpaceId());
			queryWrapper.eq("userId", userId);
			// TODO 修改 TeSpaceUser loginTeSpaceUser = spaceUserApplicationService.getSpaceUser(queryWrapper);
			// if (loginTeSpaceUser == null) {
			// 	return new ArrayList<>();
			// }
			// 这里会导致管理员在私有空间没有权限，可以再查一次库处理
			return spaceUserAuthManager.getPermissionsByRole(loginTeSpaceUser.getSpaceRole());
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
			TePicture tePicture = pictureApplicationService.getPictureById(pictureId);
			if (tePicture == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
			}
			spaceId = tePicture.getSpaceId();
			// 公共图库，仅本人或管理员可操作
			if (spaceId == null) {
				if (tePicture.getUserId().equals(userId) || loginTUser.isAdmin()) {
					return ADMIN_PERMISSIONS;
				} else {
					// 不是自己的图片，仅可查看
					return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
				}
			}
		}
		// 获取 Space 对象
		TeSpace teSpace = spaceApplicationService.getSpaceById(spaceId);
		if (teSpace == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
		}
		// 根据 Space 类型判断权限
		if (teSpace.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
			// 私有空间，仅本人或管理员有权限
			if (teSpace.getUserId().equals(userId) || loginTUser.isAdmin()) {
				return ADMIN_PERMISSIONS;
			} else {
				return new ArrayList<>();
			}
		} else {
			// 团队空间，查询 SpaceUser 并获取角色和权限
			QueryWrapper<TeSpaceUser> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("spaceId", spaceId);
			queryWrapper.eq("userId", userId);
			teSpaceUser = spaceUserApplicationService.getSpaceUser(queryWrapper);
			if (teSpaceUser == null) {
				return new ArrayList<>();
			}
			return spaceUserAuthManager.getPermissionsByRole(teSpaceUser.getSpaceRole());
		}*/
		return new ArrayList<>();
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
