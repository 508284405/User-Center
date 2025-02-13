package com.yuwang.usercenter.domain.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwang.usercenter.domain.system.dto.SystemPageQuery;
import com.yuwang.usercenter.domain.system.entity.System;
import com.yuwang.usercenter.domain.system.repository.SystemMapper;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class SystemDomainService {
    private final SystemMapper systemMapper;

    public SystemDomainService(SystemMapper systemMapper) {
        this.systemMapper = systemMapper;
    }

    @Transactional
    public System createSystem(System system) {
        if (systemMapper.existsBySystemCode(system.getSystemCode())) {
            throw new IllegalArgumentException("系统编码已存在");
        }
        systemMapper.insert(system);
        return system;
    }

    @Transactional
    public System updateSystem(System system) {
        System existingSystem = systemMapper.selectById(system.getId());
        if (existingSystem == null) {
            throw new IllegalArgumentException("系统不存在");
        }

        if (!existingSystem.getSystemCode().equals(system.getSystemCode())
                && systemMapper.existsBySystemCode(system.getSystemCode())) {
            throw new IllegalArgumentException("系统编码已存在");
        }

        systemMapper.updateById(system);
        return system;
    }

    @Transactional
    public void deleteSystem(Long systemId) {
        if (systemMapper.selectById(systemId) == null) {
            throw new IllegalArgumentException("系统不存在");
        }
        systemMapper.deleteById(systemId);
    }

    public Optional<System> findById(Long systemId) {
        return Optional.ofNullable(systemMapper.selectById(systemId));
    }

    public Optional<System> findBySystemCode(String systemCode) {
        return systemMapper.findBySystemCode(systemCode);
    }

    public List<System> findAllSystems() {
        return systemMapper.selectList(null);
    }

    public BasePageResult<System> findSystemPage(SystemPageQuery query) {
        LambdaQueryWrapper<System> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getSystemCode())) {
            wrapper.like(System::getSystemCode, query.getSystemCode());
        }
        if (StringUtils.hasText(query.getSystemName())) {
            wrapper.like(System::getSystemName, query.getSystemName());
        }

        Page<System> page = new Page<>(query.getPageNum(), query.getPageSize());
        page = systemMapper.selectPage(page, wrapper);

        return BasePageResult.success(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }
}