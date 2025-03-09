package com.baolong.pictures.infrastructure.repository.impl;

import com.baolong.pictures.infrastructure.repository.CategoryRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.category.entity.Category;
import com.baolong.pictures.infrastructure.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
 * 分类表 (category) - 仓储服务接口
 *
 * @author Baolong 2025-03-08 01:39:38
 */
@Service
public class CategoryRepositoryImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryRepository {
}
