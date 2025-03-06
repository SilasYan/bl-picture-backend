package com.baolong.picture.infrastructure.repository;

import com.baolong.picture.domain.picture.entity.Picture;
import com.baolong.picture.domain.picture.repository.PictureRepository;
import com.baolong.picture.infrastructure.mapper.PictureMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 图片仓库服务实现类
 */
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
