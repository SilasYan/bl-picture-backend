package com.baolong.picture.infrastructure.repository;

import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.domain.user.repository.UserRepository;
import com.baolong.picture.infrastructure.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户仓库服务实现类
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}
