package com.baolong.pictures.infrastructure.repository.impl;

import com.baolong.pictures.infrastructure.repository.TagRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.tag.entity.Tag;
import com.baolong.pictures.infrastructure.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
 * 标签表 (tag) - 仓储服务接口
 *
 * @author Baolong 2025-03-08 01:39:52
 */
@Service
public class TagRepositoryImpl extends ServiceImpl<TagMapper, Tag> implements TagRepository {
}
