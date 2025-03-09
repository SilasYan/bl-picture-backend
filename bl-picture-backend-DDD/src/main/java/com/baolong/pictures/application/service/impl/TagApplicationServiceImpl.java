package com.baolong.pictures.application.service.impl;

import com.baolong.pictures.application.service.TagApplicationService;
import com.baolong.pictures.application.service.UserApplicationService;
import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.domain.tag.service.TagDomainService;
import com.baolong.pictures.domain.user.entity.User;
import com.baolong.pictures.infrastructure.common.DeleteRequest;
import com.baolong.pictures.infrastructure.common.page.PageVO;
import com.baolong.pictures.infrastructure.exception.ErrorCode;
import com.baolong.pictures.infrastructure.exception.ThrowUtils;
import com.baolong.pictures.interfaces.assembler.TagAssembler;
import com.baolong.pictures.interfaces.dto.tag.TagAddRequest;
import com.baolong.pictures.interfaces.dto.tag.TagQueryRequest;
import com.baolong.pictures.interfaces.dto.tag.TagUpdateRequest;
import com.baolong.pictures.interfaces.vo.tag.TagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签应用服务实现
 *
 * @author Baolong 2025年03月09 22:02
 * @version 1.0
 * @since 1.8
 */
@Service
@RequiredArgsConstructor
public class TagApplicationServiceImpl implements TagApplicationService {

	private final TagDomainService tagDomainService;
	private final UserApplicationService userApplicationService;

	// region 增删改相关

	/**
	 * 新增标签
	 *
	 * @param tagAddRequest 标签新增请求
	 * @return 是否成功
	 */
	@Override
	public Boolean addTag(TagAddRequest tagAddRequest) {
		Tag tag = TagAssembler.toTagEntity(tagAddRequest);
		User loginUser = userApplicationService.getLoginUser();
		tag.setUserId(loginUser.getId());
		// 查询当前标签是否已经存在于数据库
		Tag oldTag = tagDomainService.getTagByUserIdAndName(tag.getUserId(), tag.getName());
		if (oldTag == null) return false;
		return tagDomainService.addTag(tag);
	}

	/**
	 * 删除标签
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteTag(DeleteRequest deleteRequest) {
		return tagDomainService.deleteTag(deleteRequest.getId());
	}

	/**
	 * 更新标签
	 *
	 * @param tagUpdateRequest 标签更新请求
	 * @return 是否成功
	 */
	@Override
	public Boolean updateTag(TagUpdateRequest tagUpdateRequest) {
		Tag tag = TagAssembler.toTagEntity(tagUpdateRequest);
		return tagDomainService.updateTag(tag);
	}

	// endregion 增删改相关

	// region 查询相关

	/**
	 * 获取首页标签列表
	 *
	 * @return 标签列表
	 */
	@Override
	public List<TagVO> getTagListAsUser() {
		List<Tag> categories = tagDomainService.getTagListAsUser();
		return categories.stream().map(TagAssembler::toTagVO).collect(Collectors.toList());
	}

	/**
	 * 获取标签分页列表（管理员）
	 *
	 * @param tagQueryRequest 标签查询请求
	 * @return 标签分页列表
	 */
	@Override
	public PageVO<Tag> getTagPageListAsAdmin(TagQueryRequest tagQueryRequest) {
		Page<Tag> picturePage = tagDomainService.getPicturePageListAsAdmin(
				tagQueryRequest.getPage(Tag.class), this.getLambdaQueryWrapper(tagQueryRequest)
		);
		return PageVO.from(picturePage);
	}

	// endregion 查询相关

	// region 其他方法

	/**
	 * 获取查询条件对象（Lambda）
	 *
	 * @param tagQueryRequest 标签查询请求
	 * @return 查询条件对象（Lambda）
	 */
	@Override
	public LambdaQueryWrapper<Tag> getLambdaQueryWrapper(TagQueryRequest tagQueryRequest) {
		ThrowUtils.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
		return tagDomainService.getLambdaQueryWrapper(tagQueryRequest);
	}

	// endregion 其他方法
}
