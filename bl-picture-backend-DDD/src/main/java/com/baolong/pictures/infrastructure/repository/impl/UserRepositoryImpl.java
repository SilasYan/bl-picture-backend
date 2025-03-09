package com.baolong.pictures.infrastructure.repository.impl;

import com.baolong.pictures.infrastructure.repository.UserRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.user.entity.User;
import com.baolong.pictures.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* 用户表 (user) - 仓储服务接口
*
* @author Baolong 2025-03-08 01:39:55
*/
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}
