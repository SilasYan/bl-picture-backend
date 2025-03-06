package com.baolong.picture.shared.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.picture.shared.auth.SpaceUserAuthManager;
import com.baolong.picture.shared.auth.model.SpaceUserPermissionConstant;
import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.domain.space.enums.SpaceTypeEnum;
import com.baolong.picture.application.service.PictureApplicationService;
import com.baolong.picture.application.service.SpaceApplicationService;
import com.baolong.picture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截器
 */
@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {

	@Resource
	private UserApplicationService userApplicationService;

	@Resource
	private PictureApplicationService pictureApplicationService;

	@Resource
	private SpaceApplicationService spaceApplicationService;

	@Resource
	private SpaceUserAuthManager spaceUserAuthManager;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
		if (request instanceof ServletServerHttpRequest) {
			HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
			// 获取请求参数
			String pictureId = servletRequest.getParameter("pictureId");
			if (StrUtil.isBlank(pictureId)) {
				log.error("缺少图片参数，拒绝握手");
				return false;
			}
			User loginUser = userApplicationService.getLoginUser(servletRequest);
			if (ObjUtil.isEmpty(loginUser)) {
				log.error("用户未登录，拒绝握手");
				return false;
			}
			// 校验用户是否有该图片的权限
			Picture picture = pictureApplicationService.getPictureById(Long.valueOf(pictureId));
			if (picture == null) {
				log.error("图片不存在，拒绝握手");
				return false;
			}
			Long spaceId = picture.getSpaceId();
			Space space = null;
			if (spaceId != null) {
				space = spaceApplicationService.getSpaceById(spaceId);
				if (space == null) {
					log.error("空间不存在，拒绝握手");
					return false;
				}
				if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
					log.info("不是团队空间，拒绝握手");
					return false;
				}
			}
			List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
			if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
				log.error("没有图片编辑权限，拒绝握手");
				return false;
			}
			// 设置 attributes
			attributes.put("user", loginUser);
			attributes.put("userId", loginUser.getId());
			attributes.put("pictureId", Long.valueOf(pictureId)); // 记得转换为 Long 类型
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
	}
}
