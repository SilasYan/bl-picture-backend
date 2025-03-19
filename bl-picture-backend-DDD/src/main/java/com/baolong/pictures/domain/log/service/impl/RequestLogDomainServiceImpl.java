package com.baolong.pictures.domain.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.log.entity.RequestLog;
import com.baolong.pictures.domain.log.service.RequestLogDomainService;
import com.baolong.pictures.infrastructure.repository.RequestLogRepository;
import org.springframework.stereotype.Service;

/**
 * 请求日志表 (request_log) - 领域服务实现
 *
 * @author Baolong 2025-03-08 01:39:44
 */
@Service
public class RequestLogDomainServiceImpl extends ServiceImpl<RequestLogRepository, RequestLog> implements RequestLogDomainService {
}
