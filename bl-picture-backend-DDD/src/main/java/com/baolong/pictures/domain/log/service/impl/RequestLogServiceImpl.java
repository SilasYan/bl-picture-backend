package com.baolong.pictures.domain.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baolong.pictures.domain.log.entity.RequestLog;
import com.baolong.pictures.domain.log.service.RequestLogService;
import com.baolong.pictures.infrastructure.mapper.RequestLogMapper;
import org.springframework.stereotype.Service;

/**
 * 请求日志表 (request_log) - 仓储服务接口
 *
 * @author Baolong 2025-03-08 01:39:44
 */
@Service
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {
}
