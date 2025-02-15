package com.baolong.blpicturebackend.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baolong.blpicturebackend.annotation.AuthCheck;
import com.baolong.blpicturebackend.comment.BaseResponse;
import com.baolong.blpicturebackend.comment.DeleteRequest;
import com.baolong.blpicturebackend.comment.ResultUtils;
import com.baolong.blpicturebackend.constant.UserConstant;
import com.baolong.blpicturebackend.exception.BusinessException;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.model.dto.picture.PictureEditRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureQueryRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadRequest;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.model.entity.Picture;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.CategoryTagEnum;
import com.baolong.blpicturebackend.model.vo.PictureTagCategory;
import com.baolong.blpicturebackend.model.vo.PictureVO;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.blpicturebackend.service.PictureService;
import com.baolong.blpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

	@Resource
	private UserService userService;
	@Resource
	private PictureService pictureService;
	@Resource
	private CategoryTagService categoryTagService;

	/**
	 * 上传图片（可重新上传）
	 */
	@PostMapping("/upload")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<PictureVO> uploadPicture(
			@RequestPart("file") MultipartFile multipartFile,
			PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 删除图片
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = pictureService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 更新图片（仅管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
		if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureUpdateRequest, picture);

		/*// 注意将 list 转为 string
		picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));*/

		User loginUser = userService.getLoginUser(request);
		List<String> inputTagList = pictureUpdateRequest.getTags();
		// 使用分类标签表的方式
		if (pictureUpdateRequest.getInputTagList() != null && !pictureUpdateRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureUpdateRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginUser.getId());
				categoryTagService.save(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		picture.setTags(inputTagListStr);

		// 数据校验
		pictureService.validPicture(picture);
		// 判断是否存在
		long id = pictureUpdateRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取图片（仅管理员可用）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(picture);
	}

	/**
	 * 根据 id 获取图片（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVO(picture, request));
	}

	/**
	 * 分页获取图片列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 处理分类和标签
		for (Picture picture : picturePage.getRecords()) {
			// 查询当前图片的分类和标签的信息
			List<String> ctIds = new ArrayList<>();
			if (StrUtil.isNotBlank(picture.getTags())) {
				ctIds.addAll(Arrays.stream(picture.getTags().split(",")).collect(Collectors.toList()));
			}
			if (StrUtil.isNotBlank(picture.getCategory())) {
				ctIds.add(picture.getCategory());
			}
			if (!ctIds.isEmpty()) {
				List<CategoryTag> categoryTagList = categoryTagService.list(new LambdaQueryWrapper<CategoryTag>()
						.in(CategoryTag::getId, ctIds)
				);
				Map<Integer, List<CategoryTag>> typeMap = categoryTagList.stream().collect(Collectors.groupingBy(CategoryTag::getType));
				if (typeMap.get(CategoryTagEnum.CATEGORY.getValue()) != null) {
					picture.setCategory(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getName());
				}
				if (typeMap.get(CategoryTagEnum.TAG.getValue()) != null) {
					picture.setTags(JSONUtil.toJsonStr(
							typeMap.get(CategoryTagEnum.TAG.getValue())
									.stream()
									.map(CategoryTag::getName)
									.collect(Collectors.toList())
					));
				}
			}
		}

		return ResultUtils.success(picturePage);
	}

	/**
	 * 分页获取图片列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
															 HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
	}

	/**
	 * 编辑图片（给用户使用）
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
		if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureEditRequest, picture);

		/*// 注意将 list 转为 string
		picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));*/

		User loginUser = userService.getLoginUser(request);
		List<String> inputTagList = pictureEditRequest.getTags();
		// 使用分类标签表的方式
		if (pictureEditRequest.getInputTagList() != null && !pictureEditRequest.getInputTagList().isEmpty()) {
			// 需要把这个里面的标签新增到数据库中
			for (String tag : pictureEditRequest.getInputTagList()) {
				CategoryTag categoryTag = new CategoryTag();
				categoryTag.setName(tag);
				categoryTag.setType(CategoryTagEnum.TAG.getValue());
				categoryTag.setUserId(loginUser.getId());
				categoryTagService.save(categoryTag);
				// 把新增的id放到 inputTagList 中
				inputTagList.add(String.valueOf(categoryTag.getId()));
			}
		}
		// 把 inputTagList 转为逗号分隔的字符串
		String inputTagListStr = String.join(",", inputTagList);
		picture.setTags(inputTagListStr);

		// 设置编辑时间
		picture.setEditTime(new Date());
		// 数据校验
		pictureService.validPicture(picture);
		// 判断是否存在
		long id = pictureEditRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	@GetMapping("/tag_category")
	public BaseResponse<PictureTagCategory> listPictureTagCategory() {
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
		List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
		pictureTagCategory.setTagList(tagList);
		pictureTagCategory.setCategoryList(categoryList);
		return ResultUtils.success(pictureTagCategory);
	}

}
