package com.baolong.picture.infrastructure.repository;

import com.baolong.picture.domain.categoryTag.entity.CategoryTag;
import com.baolong.picture.domain.categoryTag.repository.CategoryTagRepository;
import com.baolong.picture.infrastructure.mapper.CategoryTagMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 分类标签仓库服务实现
 *
 * @author Baolong 2025年03月05 21:14
 * @version 1.0
 * @since 1.8
 */
@Service
public class CategoryTagRepositoryImpl extends ServiceImpl<CategoryTagMapper, CategoryTag> implements CategoryTagRepository {
}
