package com.baolong.pictures.interfaces.assembler;

import com.baolong.pictures.domain.space.entity.Space;
import com.baolong.pictures.interfaces.dto.space.SpaceActivateRequest;
import com.baolong.pictures.interfaces.dto.space.SpaceAddRequest;
import com.baolong.pictures.interfaces.dto.space.SpaceEditRequest;
import com.baolong.pictures.interfaces.dto.space.SpaceUpdateRequest;
import com.baolong.pictures.interfaces.vo.space.SpaceVO;
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
	 * 空间激活请求 转为 空间实体
	 */
	public static Space toSpaceEntity(SpaceActivateRequest spaceActivateRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceActivateRequest, space);
		return space;
	}

	/**
	 * 空间新增请求 转为 空间实体
	 */
	public static Space toSpaceEntity(SpaceAddRequest spaceAddRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceAddRequest, space);
		return space;
	}

	/**
	 * 空间更新请求 转为 空间实体
	 */
	public static Space toSpaceEntity(SpaceUpdateRequest spaceUpdateRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceUpdateRequest, space);
		return space;
	}

	/**
	 * 空间编辑请求 转为 空间实体
	 */
	public static Space toSpaceEntity(SpaceEditRequest spaceEditRequest) {
		Space space = new Space();
		BeanUtils.copyProperties(spaceEditRequest, space);
		return space;
	}

	/**
	 * 空间实体 转为 空间 VO
	 */
	public static SpaceVO toSpaceVO(Space space) {
		SpaceVO spaceVO = new SpaceVO();
		BeanUtils.copyProperties(space, spaceVO);
		return spaceVO;
	}
}
