package com.baolong.blpicturebackend.controller;

import com.baolong.blpicturebackend.comment.BaseResponse;
import com.baolong.blpicturebackend.comment.ResultUtils;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.model.enums.CategoryTagEnum;
import com.baolong.blpicturebackend.model.vo.CategoryTagVO;
import com.baolong.blpicturebackend.model.vo.PictureTagCategory;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.blpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/categoryTag")
public class CategoryTagController {

	@Resource
	private UserService userService;
	@Resource
	private CategoryTagService categoryTagService;

	/**
	 * 获取分类标签数据
	 */
	@GetMapping("/data/vo")
	public BaseResponse<PictureTagCategory> pictureCategoryTagData() {
		List<CategoryTag> categoryTagList = categoryTagService.list();
		List<CategoryTagVO> voList = categoryTagList.stream().map(CategoryTagVO::objToVo).collect(Collectors.toList());
		Map<Integer, List<CategoryTagVO>> typeMap = voList.stream().collect(Collectors.groupingBy(CategoryTagVO::getType));
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		pictureTagCategory.setCategoryVOList(typeMap.get(CategoryTagEnum.CATEGORY.getValue()));
		pictureTagCategory.setTagVOList(typeMap.get(CategoryTagEnum.TAG.getValue()));
		return ResultUtils.success(pictureTagCategory);
	}

}
