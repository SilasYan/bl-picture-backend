package com.baolong.picture.infrastructure.repository;

import com.baolong.picture.domain.space.entity.SpaceUser;
import com.baolong.picture.domain.space.repository.SpaceUserRepository;
import com.baolong.picture.infrastructure.mapper.SpaceUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 空间用户仓储服务实现
 *
 * @author Baolong 2025年03月06 20:38
 * @version 1.0
 * @since 1.8
 */
@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {
}
