package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import com.yuwang.usercenter.domain.log.dto.OperationLogPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class OperationLogController {
    private final OperationLogDomainService operationLogDomainService;

    public OperationLogController(OperationLogDomainService operationLogDomainService) {
        this.operationLogDomainService = operationLogDomainService;
    }

    @GetMapping("/{id}")
    public BaseResult<OperationLog> getLog(@PathVariable Long id) {
        return operationLogDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "操作日志不存在"));
    }

    @GetMapping("/user/{userId}")
    public BaseResult<List<OperationLog>> getLogsByUserId(@PathVariable Long userId) {
        return BaseResult.success(operationLogDomainService.findByUserId(userId));
    }

    @GetMapping("/module/{module}")
    public BaseResult<List<OperationLog>> getLogsByModule(@PathVariable String module) {
        return BaseResult.success(operationLogDomainService.findByModule(module));
    }

    @GetMapping("/type/{operationType}")
    public BaseResult<List<OperationLog>> getLogsByOperationType(@PathVariable String operationType) {
        return BaseResult.success(operationLogDomainService.findByOperationType(operationType));
    }

    @PostMapping("/page")
    public BasePageResult<OperationLog> getLogPage(@RequestBody @Validated OperationLogPageQuery query) {
        return operationLogDomainService.findLogPage(query);
    }
}