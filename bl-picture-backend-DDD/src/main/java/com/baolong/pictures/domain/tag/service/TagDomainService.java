package com.baolong.pictures.domain.tag.service;

import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.interfaces.dto.tag.TagQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 标签领域服务接口
 *
 * @author Baolong 2025年03月09 22:02
 * @version 1.0
 * @since 1.8
 */
public interface TagDomainService {

	// region 增删改相关

	/**
	 * 新增标签
	 *
	 * @param tag 标签对象
	 * @return 是否成功
	 */
	Boolean addTag(Tag tag);

	/**
	 * 删除标签
	 *
	 * @param tagId 标签 ID
	 * @return 是否成功
	 */
	Boolean deleteTag(Long tagId);

	/**
	 * 更新标签
	 *
	 * @param tag 标签对象
	 * @return 是否成功
	 */
	Boolean updateTag(Tag tag);

	// endregion 增删改相关

	// region 查询相关

	/**
	 * 根据用户 ID 和标签名称获取标签
	 *
	 * @param userId 用户 ID
	 * @param name   标签名称
	 * @return 标签对象
	 */
	Tag getTagByUserIdAndName(Long userId, String name);


	/**
	 * 获取首页标签列表
	 *
	 * @return 标签列表
	 */
	List<Tag> getTagListAsUser();

	/**
	 * 获取标签分页列表（管理员）
	 *
	 * @param page               分页对象
	 * @param lambdaQueryWrapper 查询条件
	 * @return 标签分页列表
	 */
	Page<Tag> getPicturePageListAsAdmin(Page<Tag> page, LambdaQueryWrapper<Tag> lambdaQueryWrapper);

	// endregion 查询相关

	// region 其他方法

	/**
	 * 获取查询条件对象（Lambda）
	 *
	 * @param tagQueryRequest 标签查询请求
	 * @return 查询条件对象（Lambda）
	 */
	LambdaQueryWrapper<Tag> getLambdaQueryWrapper(TagQueryRequest tagQueryRequest);

	// endregion 其他方法
}
