package com.baolong.picture.interfaces.assembler;

import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.interfaces.dto.picture.PictureEditRequest;
import com.baolong.picture.interfaces.dto.picture.PictureUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 图片转换类
 *
 * @author Baolong 2025年03月05 20:50
 * @version 1.0
 * @since 1.8
 */
public class PictureAssembler {

	/**
	 * 将图片修改请求转换为图片实体
	 */
	public static Picture toPictureEntity(PictureEditRequest request) {
		Picture picture = new Picture();
		BeanUtils.copyProperties(request, picture);
		return picture;
	}

	/**
	 * 将图片更新请求转换为图片实体
	 */
	public static Picture toPictureEntity(PictureUpdateRequest request) {
		Picture picture = new Picture();
		BeanUtils.copyProperties(request, picture);
		return picture;
	}
}
