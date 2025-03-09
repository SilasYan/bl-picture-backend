package com.baolong.pictures.application.service;

import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.infrastructure.common.DeleteRequest;
import com.baolong.pictures.infrastructure.common.page.PageVO;
import com.baolong.pictures.interfaces.dto.tag.TagAddRequest;
import com.baolong.pictures.interfaces.dto.tag.TagQueryRequest;
import com.baolong.pictures.interfaces.dto.tag.TagUpdateRequest;
import com.baolong.pictures.interfaces.vo.tag.TagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

/**
 * 标签应用服务接口
 *
 * @author Baolong 2025年03月09 22:01
 * @version 1.0
 * @since 1.8
 */
public interface TagApplicationService {

	// region 增删改相关

	/**
	 * 新增标签
	 *
	 * @param tagAddRequest 标签新增请求
	 * @return 是否成功
	 */
	Boolean addTag(TagAddRequest tagAddRequest);

	/**
	 * 删除标签
	 *
	 * @param deleteRequest 删除请求
	 * @return 是否成功
	 */
	Boolean deleteTag(DeleteRequest deleteRequest);

	/**
	 * 更新标签
	 *
	 * @param tagUpdateRequest 标签更新请求
	 * @return 是否成功
	 */
	Boolean updateTag(TagUpdateRequest tagUpdateRequest);

	// endregion 增删改相关

	// region 查询相关

	/**
	 * 获取首页标签列表
	 *
	 * @return 标签列表
	 */
	List<TagVO> getTagListAsUser();

	/**
	 * 获取标签分页列表（管理员）
	 *
	 * @param tagQueryRequest 标签查询请求
	 * @return 标签分页列表
	 */
	PageVO<Tag> getTagPageListAsAdmin(TagQueryRequest tagQueryRequest);

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
