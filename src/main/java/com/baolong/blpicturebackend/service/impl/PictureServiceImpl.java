package com.baolong.blpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.manager.FileManager;
import com.baolong.blpicturebackend.mapper.PictureMapper;
import com.baolong.blpicturebackend.model.dto.picture.PictureQueryRequest;
import com.baolong.blpicturebackend.model.dto.picture.PictureUploadRequest;
import com.baolong.blpicturebackend.model.dto.picture.UploadPictureResult;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.model.entity.Picture;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.enums.CategoryTagEnum;
import com.baolong.blpicturebackend.model.vo.PictureVO;
import com.baolong.blpicturebackend.model.vo.UserVO;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.blpicturebackend.service.PictureService;
import com.baolong.blpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ADMIN
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-02-13 23:18:16
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
		implements PictureService {

	@Resource
	private FileManager fileManager;
	@Resource
	private UserService userService;
	@Resource
	private CategoryTagService categoryTagService;

	/**
	 * 上传图片
	 *
	 * @param multipartFile        上传的图片文件
	 * @param pictureUploadRequest 上传图片的请求对象
	 * @param loginUser            登录的用户
	 * @return PictureVO
	 */
	@Override
	public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 用于判断是新增还是更新图片
		Long pictureId = null;
		if (pictureUploadRequest != null) {
			pictureId = pictureUploadRequest.getId();
		}
		// 如果是更新图片，需要校验图片是否存在
		if (pictureId != null) {
			boolean exists = this.lambdaQuery()
					.eq(Picture::getId, pictureId)
					.exists();
			ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
		}
		// 上传图片，得到信息
		// 按照用户 id 划分目录
		String uploadPathPrefix = String.format("public/%s", loginUser.getId());
		UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
		// 构造要入库的图片信息
		Picture picture = new Picture();
		picture.setUrl(uploadPictureResult.getUrl());
		picture.setName(uploadPictureResult.getPicName());
		picture.setPicSize(uploadPictureResult.getPicSize());
		picture.setPicWidth(uploadPictureResult.getPicWidth());
		picture.setPicHeight(uploadPictureResult.getPicHeight());
		picture.setPicScale(uploadPictureResult.getPicScale());
		picture.setPicFormat(uploadPictureResult.getPicFormat());
		picture.setUserId(loginUser.getId());
		// 如果 pictureId 不为空，表示更新，否则是新增
		if (pictureId != null) {
			// 如果是更新，需要补充 id 和编辑时间
			picture.setId(pictureId);
			picture.setEditTime(new Date());
		}
		boolean result = this.saveOrUpdate(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
		return PictureVO.objToVo(picture);
	}

	/**
	 * 校验图片对象
	 *
	 * @param picture 图片对象
	 */
	@Override
	public void validPicture(Picture picture) {
		ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
		// 从对象中取值
		Long id = picture.getId();
		String url = picture.getUrl();
		String introduction = picture.getIntroduction();
		// 修改数据时，id 不能为空，有参数则校验
		ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
		if (StrUtil.isNotBlank(url)) {
			ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
		}
		if (StrUtil.isNotBlank(introduction)) {
			ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
		}
	}

	/**
	 * 获取图片封装类
	 *
	 * @param picture 图片
	 * @param request HttpServletRequest
	 * @return 图片封装类
	 */
	@Override
	public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
		// 对象转封装类
		PictureVO pictureVO = PictureVO.objToVo(picture);
		// 关联查询用户信息
		Long userId = picture.getUserId();
		if (userId != null && userId > 0) {
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
			pictureVO.setUser(userVO);
		}
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
				pictureVO.setCategory(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getName());
			}
			if (typeMap.get(CategoryTagEnum.TAG.getValue()) != null) {
				pictureVO.setTags(typeMap.get(CategoryTagEnum.TAG.getValue())
						.stream()
						.map(CategoryTag::getName)
						.collect(Collectors.toList()));
			}
		}
		return pictureVO;
	}

	/**
	 * 分页获取图片封装
	 *
	 * @param picturePage 图片分页对象
	 * @param request     HttpServletRequest
	 * @return Page<PictureVO>
	 */
	@Override
	public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
		List<Picture> pictureList = picturePage.getRecords();
		Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
		if (CollUtil.isEmpty(pictureList)) {
			return pictureVOPage;
		}
		// 对象列表 => 封装对象列表
		List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		pictureVOList.forEach(pictureVO -> {
			Long userId = pictureVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			pictureVO.setUser(userService.getUserVO(user));
			// 查询当前图片的分类和标签信息
			List<String> ctIds = new ArrayList<>();
			if (pictureVO.getTags() != null && !pictureVO.getTags().isEmpty()) {
				ctIds.addAll(pictureVO.getTags());
			}
			if (StrUtil.isNotBlank(pictureVO.getCategory())) {
				ctIds.add(pictureVO.getCategory());
			}
			if (!ctIds.isEmpty()) {
				List<CategoryTag> categoryTagList = categoryTagService.list(new LambdaQueryWrapper<CategoryTag>()
						.in(CategoryTag::getId, ctIds)
				);
				Map<Integer, List<CategoryTag>> typeMap = categoryTagList.stream().collect(Collectors.groupingBy(CategoryTag::getType));
				if (typeMap.get(CategoryTagEnum.CATEGORY.getValue()) != null) {
					pictureVO.setCategory(typeMap.get(CategoryTagEnum.CATEGORY.getValue()).get(0).getName());
				}
				if (typeMap.get(CategoryTagEnum.TAG.getValue()) != null) {
					pictureVO.setTags(typeMap.get(CategoryTagEnum.TAG.getValue())
							.stream()
							.map(CategoryTag::getName)
							.collect(Collectors.toList()));
				}
			}
		});
		pictureVOPage.setRecords(pictureVOList);
		return pictureVOPage;
	}

	/**
	 * 获取查询条件
	 *
	 * @param pictureQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	@Override
	public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
		QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
		if (pictureQueryRequest == null) {
			return queryWrapper;
		}
		// 从对象中取值
		Long id = pictureQueryRequest.getId();
		String name = pictureQueryRequest.getName();
		String introduction = pictureQueryRequest.getIntroduction();
		String category = pictureQueryRequest.getCategory();
		List<String> tags = pictureQueryRequest.getTags();
		Long picSize = pictureQueryRequest.getPicSize();
		Integer picWidth = pictureQueryRequest.getPicWidth();
		Integer picHeight = pictureQueryRequest.getPicHeight();
		Double picScale = pictureQueryRequest.getPicScale();
		String picFormat = pictureQueryRequest.getPicFormat();
		String searchText = pictureQueryRequest.getSearchText();
		Long userId = pictureQueryRequest.getUserId();
		String sortField = pictureQueryRequest.getSortField();
		String sortOrder = pictureQueryRequest.getSortOrder();
		// 从多字段中搜索
		if (StrUtil.isNotBlank(searchText)) {
			// 需要拼接查询条件
			queryWrapper.and(qw -> qw.like("name", searchText)
					.or()
					.like("introduction", searchText)
			);
		}
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
		queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
		queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
		queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
		queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
		queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
		queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
		queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
		// // JSON 数组查询
		// if (CollUtil.isNotEmpty(tags)) {
		// 	for (String tag : tags) {
		// 		queryWrapper.like("tags", "\"" + tag + "\"");
		// 	}
		// }

		// 拼接 分类标签列表的SQL
		if (CollUtil.isNotEmpty(tags)) {
			StringBuilder FIND_IN_SET_SQL = new StringBuilder(" (");
			for (String tag : tags) {
				FIND_IN_SET_SQL.append(" FIND_IN_SET (").append(tag).append(", tags) > 0 OR");
			}
			// 去掉字符串 FIND_IN_SET_SQL 末尾的 OR
			if (FIND_IN_SET_SQL.length() > 0 && FIND_IN_SET_SQL.lastIndexOf("OR") != -1) {
				FIND_IN_SET_SQL.setLength(FIND_IN_SET_SQL.lastIndexOf("OR"));
			}
			FIND_IN_SET_SQL.append(") ");
			queryWrapper.apply(FIND_IN_SET_SQL.toString());
		}

		// 排序
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

}




