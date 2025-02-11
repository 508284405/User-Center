package com.yuwang.usercenter.domain.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwang.usercenter.domain.role.entity.Role;
import com.yuwang.usercenter.domain.role.repository.RoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleDomainService {
    private final RoleMapper roleMapper;

    public RoleDomainService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Transactional
    public Role createRole(Role role) {
        if (roleMapper.exists(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, role.getRoleName()))) {
            throw new IllegalArgumentException("角色名称已存在");
        }
        roleMapper.insert(role);
        return role;
    }

    @Transactional
    public Role updateRole(Role role) {
        roleMapper.updateById(role);
        return role;
    }

    @Transactional
    public void deleteRole(Long roleId) {
        roleMapper.deleteById(roleId);
    }

    public Optional<Role> findById(Long roleId) {
        return Optional.ofNullable(roleMapper.selectById(roleId));
    }

    public Optional<Role> findByRoleName(String roleName) {
        return Optional.ofNullable(roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName)));
    }

    public List<Role> findBySystemId(Long systemId) {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().eq(Role::getSystemId, systemId));
    }

    public List<Role> findByLevel(Integer level) {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().eq(Role::getLevel, level));
    }

    public List<Role> findAllRoles() {
        return roleMapper.selectList(null);
    }
}