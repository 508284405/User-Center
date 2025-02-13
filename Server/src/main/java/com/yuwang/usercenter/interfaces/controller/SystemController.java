package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.system.dto.SystemCreateRequest;
import com.yuwang.usercenter.domain.system.dto.SystemPageQuery;
import com.yuwang.usercenter.domain.system.dto.SystemUpdateRequest;
import com.yuwang.usercenter.domain.system.entity.System;
import com.yuwang.usercenter.domain.system.service.SystemDomainService;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/systems")
public class SystemController {
    private final SystemDomainService systemDomainService;
    private final OperationLogDomainService operationLogDomainService;

    public SystemController(SystemDomainService systemDomainService, OperationLogDomainService operationLogDomainService) {
        this.systemDomainService = systemDomainService;
        this.operationLogDomainService = operationLogDomainService;
    }

    /**
     * 创建新系统
     * @param createRequest 系统信息
     * @return 创建成功的系统信息
     */
    @PostMapping
    public BaseResult<System> createSystem(@RequestBody @Validated SystemCreateRequest createRequest, HttpServletRequest request) {
        System system = new System();
        BeanUtils.copyProperties(createRequest, system);
        System createdSystem = systemDomainService.createSystem(system);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("CREATE");
        log.setModule("SYSTEM");
        log.setTargetId(createdSystem.getId());
        log.setDescription("创建系统：" + createdSystem.getSystemName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success(createdSystem);
    }

    /**
     * 更新系统信息
     * @param id 系统ID
     * @param updateRequest 更新的系统信息
     * @return 更新后的系统信息
     */
    @PutMapping("/{id}")
    public BaseResult<System> updateSystem(@PathVariable Long id, @RequestBody @Validated SystemUpdateRequest updateRequest, HttpServletRequest request) {
        System system = new System();
        BeanUtils.copyProperties(updateRequest, system);
        system.setId(id);
        System updatedSystem = systemDomainService.updateSystem(system);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("UPDATE");
        log.setModule("SYSTEM");
        log.setTargetId(updatedSystem.getId());
        log.setDescription("更新系统：" + updatedSystem.getSystemName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success(updatedSystem);
    }

    /**
     * 删除系统
     * @param id 要删除的系统ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public BaseResult<Void> deleteSystem(@PathVariable Long id, HttpServletRequest request) {
        System system = systemDomainService.findById(id).orElseThrow(() -> new IllegalArgumentException("系统不存在"));
        systemDomainService.deleteSystem(id);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("DELETE");
        log.setModule("SYSTEM");
        log.setTargetId(id);
        log.setDescription("删除系统：" + system.getSystemName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success();
    }

    /**
     * 根据ID获取系统信息
     * @param id 系统ID
     * @return 系统信息
     */
    @GetMapping("/{id}")
    public BaseResult<System> getSystem(@PathVariable Long id) {
        return systemDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "系统不存在"));
    }

    /**
     * 根据系统编码获取系统信息
     * @param systemCode 系统编码
     * @return 系统信息
     */
    @GetMapping("/code/{systemCode}")
    public BaseResult<System> getSystemByCode(@PathVariable String systemCode) {
        return systemDomainService.findBySystemCode(systemCode)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "系统不存在"));
    }

    /**
     * 获取所有系统
     * @return 所有系统列表
     */
    @GetMapping
    public BaseResult<List<System>> getAllSystems() {
        return BaseResult.success(systemDomainService.findAllSystems());
    }

    /**
     * 分页查询系统列表
     * @param query 分页查询参数
     * @return 分页系统数据
     */
    @PostMapping("/page")
    public BasePageResult<System> getSystemPage(@RequestBody SystemPageQuery query) {
        return systemDomainService.findSystemPage(query);
    }
}