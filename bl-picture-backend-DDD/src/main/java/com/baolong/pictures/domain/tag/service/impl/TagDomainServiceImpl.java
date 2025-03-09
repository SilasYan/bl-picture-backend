package com.baolong.pictures.domain.tag.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.domain.tag.service.TagDomainService;
import com.baolong.pictures.infrastructure.common.page.PageRequest;
import com.baolong.pictures.infrastructure.repository.TagRepository;
import com.baolong.pictures.infrastructure.utils.SFLambdaUtil;
import com.baolong.pictures.interfaces.dto.tag.TagQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 标签领域服务实现
 *
 * @author Baolong 2025年03月09 22:03
 * @version 1.0
 * @since 1.8
 */
@Service
public class TagDomainServiceImpl implements TagDomainService {
	@Resource
	private TagRepository tagRepository;

	// region 增删改相关

	/**
	 * 新增标签
	 *
	 * @param tag 标签对象
	 * @return 是否成功
	 */
	@Override
	public Boolean addTag(Tag tag) {
		return tagRepository.save(tag);
	}

	/**
	 * 删除标签
	 *
	 * @param tagId 标签 ID
	 * @return 是否成功
	 */
	@Override
	public Boolean deleteTag(Long tagId) {
		return tagRepository.removeById(tagId);
	}

	/**
	 * 更新标签
	 *
	 * @param tag 标签对象
	 * @return 是否成功
	 */
	@Override
	public Boolean updateTag(Tag tag) {
		tag.fillEditTime();
		return tagRepository.updateById(tag);
	}

	// endregion 增删改相关

	// region 查询相关

	/**
	 * 根据用户 ID 和标签名称获取标签
	 *
	 * @param userId 用户 ID
	 * @param name   标签名称
	 * @return 标签对象
	 */
	@Override
	public Tag getTagByUserIdAndName(Long userId, String name) {
		return tagRepository.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId).eq(Tag::getName, name));
	}

	/**
	 * 获取首页标签列表
	 *
	 * @return 标签列表
	 */
	@Override
	public List<Tag> getTagListAsUser() {
		return tagRepository.list();
	}

	/**
	 * 获取标签分页列表（管理员）
	 *
	 * @param page               分页对象
	 * @param lambdaQueryWrapper 查询条件
	 * @return 标签分页列表
	 */
	@Override
	public Page<Tag> getPicturePageListAsAdmin(Page<Tag> page, LambdaQueryWrapper<Tag> lambdaQueryWrapper) {
		return tagRepository.page(page, lambdaQueryWrapper);
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
		LambdaQueryWrapper<Tag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
		Long id = tagQueryRequest.getId();
		String name = tagQueryRequest.getName();
		Integer useNum = tagQueryRequest.getUseNum();
		Long userId = tagQueryRequest.getUserId();
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(id), Tag::getId, id);
		lambdaQueryWrapper.like(StrUtil.isNotEmpty(name), Tag::getName, name);
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(useNum), Tag::getUseNum, useNum);
		lambdaQueryWrapper.eq(ObjUtil.isNotNull(userId), Tag::getUserId, userId);
		// 处理排序规则
		if (tagQueryRequest.isMultipleSort()) {
			List<PageRequest.Sort> sorts = tagQueryRequest.getSorts();
			if (CollUtil.isNotEmpty(sorts)) {
				sorts.forEach(sort -> {
					String sortField = sort.getField();
					boolean sortAsc = sort.isAsc();
					lambdaQueryWrapper.orderBy(
							StrUtil.isNotEmpty(sortField), sortAsc, SFLambdaUtil.getSFunction(Tag.class, sortField)
					);
				});
			}
		} else {
			PageRequest.Sort sort = tagQueryRequest.getSort();
			if (sort != null) {
				String sortField = sort.getField();
				boolean sortAsc = sort.isAsc();
				lambdaQueryWrapper.orderBy(
						StrUtil.isNotEmpty(sortField), sortAsc, SFLambdaUtil.getSFunction(Tag.class, sortField)
				);
			} else {
				lambdaQueryWrapper.orderByDesc(Tag::getCreateTime);
			}
		}
		return null;
	}

	// endregion 其他方法
}
