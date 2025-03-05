package com.baolong.blpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.blpicturebackend.model.entity.CategoryTag;
import com.baolong.blpicturebackend.service.CategoryTagService;
import com.baolong.picture.infrastructure.mapper.CategoryTagMapper;
import org.springframework.stereotype.Service;

/**
* @author ADMIN
* @description 针对表【category_tag(分类标签表)】的数据库操作Service实现
* @createDate 2025-02-14 23:37:44
*/
@Service
public class CategoryTagServiceImpl extends ServiceImpl<CategoryTagMapper, CategoryTag>
    implements CategoryTagService{

}




