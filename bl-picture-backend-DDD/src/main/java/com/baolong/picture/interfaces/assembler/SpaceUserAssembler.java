package com.baolong.picture.interfaces.assembler;

import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserAddRequest;
import com.baolong.picture.interfaces.dto.space.spaceUser.SpaceUserEditRequest;
import org.springframework.beans.BeanUtils;

/**
 * 空间用户转换类
 *
 * @author Baolong 2025年03月06 20:49
 * @version 1.0
 * @since 1.8
 */
public class SpaceUserAssembler {
	/**
	 * 将空间用户新增请求转换为空间用户实体
	 */
	public static SpaceUser toSpaceUserEntity(SpaceUserAddRequest spaceUserAddRequest) {
		SpaceUser spaceUser = new SpaceUser();
		BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
		return spaceUser;
	}

	/**
	 * 将空间用户编辑请求转换为空间用户实体
	 */
	public static SpaceUser toSpaceUserEntity(SpaceUserEditRequest spaceUserEditRequest) {
		SpaceUser spaceUser = new SpaceUser();
		BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
		return spaceUser;
	}
}
