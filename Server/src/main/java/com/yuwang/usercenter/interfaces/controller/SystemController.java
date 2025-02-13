package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.system.dto.SystemPageQuery;
import com.yuwang.usercenter.domain.system.entity.System;
import com.yuwang.usercenter.domain.system.service.SystemDomainService;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/systems")
public class SystemController {
    private final SystemDomainService systemDomainService;

    public SystemController(SystemDomainService systemDomainService) {
        this.systemDomainService = systemDomainService;
    }

    @PostMapping

    public BaseResult<System> createSystem(@RequestBody System system) {
        return BaseResult.success(systemDomainService.createSystem(system));
    }

    @PutMapping("/{id}")
    public BaseResult<System> updateSystem(@PathVariable Long id, @RequestBody System system) {
        system.setId(id);
        return BaseResult.success(systemDomainService.updateSystem(system));
    }

    @DeleteMapping("/{id}")
    public BaseResult<Void> deleteSystem(@PathVariable Long id) {
        systemDomainService.deleteSystem(id);
        return BaseResult.success();
    }

    @GetMapping("/{id}")

    public BaseResult<System> getSystem(@PathVariable Long id) {
        return systemDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "系统不存在"));
    }

    @GetMapping("/code/{systemCode}")

    public BaseResult<System> getSystemByCode(@PathVariable String systemCode) {
        return systemDomainService.findBySystemCode(systemCode)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "系统不存在"));
    }

    @GetMapping
    public BaseResult<List<System>> getAllSystems() {
        return BaseResult.success(systemDomainService.findAllSystems());
    }

    @PostMapping("/page")
    public BasePageResult<System> getSystemPage(@RequestBody SystemPageQuery query) {
        return systemDomainService.findSystemPage(query);
    }
}