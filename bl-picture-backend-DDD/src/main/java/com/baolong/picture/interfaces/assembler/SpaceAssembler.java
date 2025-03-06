package com.baolong.picture.interfaces.assembler;

import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.interfaces.dto.space.SpaceAddRequest;
import com.baolong.picture.interfaces.dto.space.SpaceEditRequest;
import com.baolong.picture.interfaces.dto.space.SpaceUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 空间转换类
 *
 * @author Baolong 2025年03月05 23:26
 * @version 1.0
 * @since 1.8
 */
public class SpaceAssembler {
	/**
	 * 将空间新增请求转换为空间实体
	 */
	public static Space toPictureEntity(SpaceAddRequest spaceAddRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceAddRequest, space);
		return space;
	}

	/**
	 * 将空间修改请求转换为空间实体
	 */
	public static Space toPictureEntity(SpaceUpdateRequest spaceUpdateRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceUpdateRequest, space);
		return space;
	}

	/**
	 * 将空间编辑请求转换为空间实体
	 */
	public static Space toPictureEntity(SpaceEditRequest spaceEditRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceEditRequest, space);
		return space;
	}
}
