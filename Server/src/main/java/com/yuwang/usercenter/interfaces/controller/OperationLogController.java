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

    /**
     * 根据ID获取操作日志
     * @param id 日志ID
     * @return 操作日志信息
     */
    @GetMapping("/{id}")
    public BaseResult<OperationLog> getLog(@PathVariable Long id) {
        return operationLogDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "操作日志不存在"));
    }

    /**
     * 获取指定用户的操作日志
     * @param userId 用户ID
     * @return 用户的操作日志列表
     */
    @GetMapping("/user/{userId}")
    public BaseResult<List<OperationLog>> getLogsByUserId(@PathVariable Long userId) {
        return BaseResult.success(operationLogDomainService.findByUserId(userId));
    }

    /**
     * 获取指定模块的操作日志
     * @param module 模块名称
     * @return 模块的操作日志列表
     */
    @GetMapping("/module/{module}")
    public BaseResult<List<OperationLog>> getLogsByModule(@PathVariable String module) {
        return BaseResult.success(operationLogDomainService.findByModule(module));
    }

    /**
     * 获取指定操作类型的日志
     * @param operationType 操作类型
     * @return 指定操作类型的日志列表
     */
    @GetMapping("/type/{operationType}")
    public BaseResult<List<OperationLog>> getLogsByOperationType(@PathVariable String operationType) {
        return BaseResult.success(operationLogDomainService.findByOperationType(operationType));
    }

    /**
     * 分页查询操作日志
     * @param query 分页查询参数
     * @return 分页操作日志数据
     */
    @PostMapping("/page")
    public BasePageResult<OperationLog> getLogPage(@RequestBody @Validated OperationLogPageQuery query) {
        return operationLogDomainService.findLogPage(query);
    }
}