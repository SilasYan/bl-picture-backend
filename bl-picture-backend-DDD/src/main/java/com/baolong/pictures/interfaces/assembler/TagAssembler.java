package com.baolong.pictures.interfaces.assembler;

import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.interfaces.dto.tag.TagAddRequest;
import com.baolong.pictures.interfaces.dto.tag.TagUpdateRequest;
import com.baolong.pictures.interfaces.vo.tag.TagVO;
import org.springframework.beans.BeanUtils;

/**
 * 标签转换类
 *
 * @author Baolong 2025年03月09 21:12
 * @version 1.0
 * @since 1.8
 */
public class TagAssembler {

	/**
	 * 标签新增请求 转为 标签实体
	 */
	public static Tag toTagEntity(TagAddRequest tagAddRequest) {
		Tag tag = new Tag();
		BeanUtils.copyProperties(tagAddRequest, tag);
		return tag;
	}

	/**
	 * 标签更新请求 转为 标签实体
	 */
	public static Tag toTagEntity(TagUpdateRequest tagUpdateRequest) {
		Tag tag = new Tag();
		BeanUtils.copyProperties(tagUpdateRequest, tag);
		return tag;
	}

	/**
	 * 标签实体 转为 标签 VO
	 */
	public static TagVO toTagVO(Tag tag) {
		TagVO tagVO = new TagVO();
		BeanUtils.copyProperties(tag, tagVO);
		return tagVO;
	}
}
