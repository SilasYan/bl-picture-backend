package com.baolong.picture.infrastructure.repository;

import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.space.repository.SpaceRepository;
import com.baolong.picture.infrastructure.mapper.SpaceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 空间仓库服务实现
 *
 * @author Baolong 2025年03月05 23:48
 * @version 1.0
 * @since 1.8
 */
@Service
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceRepository {
}
