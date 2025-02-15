package com.yuwang.usercenter.domain.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwang.usercenter.domain.log.dto.OperationLogPageQuery;
import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.repository.OperationLogMapper;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OperationLogDomainService {
    private final OperationLogMapper operationLogMapper;

    public OperationLogDomainService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional
    public OperationLog createLog(OperationLog log) {
        operationLogMapper.insert(log);
        return log;
    }

    public Optional<OperationLog> findById(Long id) {
        return Optional.ofNullable(operationLogMapper.selectById(id));
    }

    public List<OperationLog> findByUserId(Long userId) {
        return operationLogMapper.selectList(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getUserId, userId)
                .orderByDesc(OperationLog::getCreatedAt)
        );
    }

    public List<OperationLog> findByModule(String module) {
        return operationLogMapper.selectList(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getModule, module)
                .orderByDesc(OperationLog::getCreatedAt)
        );
    }

    @Transactional
    public void recordOperation(String operationType, String module, Long targetId, String description) {
        OperationLog log = new OperationLog();
        log.setOperationType(operationType);
        log.setModule(module);
        log.setTargetId(targetId);
        log.setDescription(description);
        createLog(log);
    }

    public List<OperationLog> findByOperationType(String operationType) {
        return operationLogMapper.selectList(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getOperationType, operationType)
                .orderByDesc(OperationLog::getCreatedAt)
        );
    }

    public List<OperationLog> findAll() {
        return operationLogMapper.selectList(
            new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreatedAt)
        );
    }

    public BasePageResult<OperationLog> findLogPage(OperationLogPageQuery query) {
        Page<OperationLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        wrapper.eq(query.getUserId() != null, OperationLog::getUserId, query.getUserId())
               .like(StringUtils.isNotBlank(query.getUsername()), OperationLog::getUsername, query.getUsername())
               .eq(StringUtils.isNotBlank(query.getOperationType()), OperationLog::getOperationType, query.getOperationType())
               .eq(StringUtils.isNotBlank(query.getModule()), OperationLog::getModule, query.getModule())
               .like(StringUtils.isNotBlank(query.getDescription()), OperationLog::getDescription, query.getDescription())
               .eq(StringUtils.isNotBlank(query.getIpAddress()), OperationLog::getIpAddress, query.getIpAddress())
               .orderByDesc(OperationLog::getCreatedAt);
        
        Page<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);
        return BasePageResult.success(logPage.getRecords(), query.getPageNum(), query.getPageSize(), logPage.getTotal());
    }
}