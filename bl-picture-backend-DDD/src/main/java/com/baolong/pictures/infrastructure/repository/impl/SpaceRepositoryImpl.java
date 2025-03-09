package com.baolong.pictures.infrastructure.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.space.entity.Space;
import com.baolong.pictures.infrastructure.repository.SpaceRepository;
import com.baolong.pictures.infrastructure.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
* 空间表 (space) - 仓储服务接口
*
* @author Baolong 2025-03-08 01:39:47
*/
@Service
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceRepository {
}
