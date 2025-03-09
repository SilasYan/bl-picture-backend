package com.baolong.pictures.infrastructure.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.space.entity.SpaceUser;
import com.baolong.pictures.infrastructure.repository.SpaceUserRepository;
import com.baolong.pictures.infrastructure.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
* 空间用户表 (space_user) - 仓储服务接口
*
* @author Baolong 2025-03-08 01:39:50
*/
@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {
}
