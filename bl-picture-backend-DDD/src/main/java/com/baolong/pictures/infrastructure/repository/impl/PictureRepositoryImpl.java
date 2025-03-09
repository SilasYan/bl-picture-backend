package com.baolong.pictures.infrastructure.repository.impl;

import com.baolong.pictures.infrastructure.repository.PictureRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.picture.entity.Picture;
import com.baolong.pictures.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* 图片表 (picture) - 仓储服务接口
*
* @author Baolong 2025-03-08 01:39:41
*/
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
