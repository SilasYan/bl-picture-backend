package com.baolong.pictures.infrastructure.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.vip.entity.Vip;
import com.baolong.pictures.infrastructure.repository.VipRepositoryService;
import com.baolong.pictures.infrastructure.mapper.VipMapper;
import org.springframework.stereotype.Service;

/**
 * 会员表 (会员表) - 仓储服务接口
 *
 * @author Baolong 2025-03-08 00:58:22
 */
@Service
public class VipRepositoryServiceImpl extends ServiceImpl<VipMapper, Vip> implements VipRepositoryService {
}
