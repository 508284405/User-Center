package com.leyue.usercenter.infrastructure.role;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.query.RolePageQuery;
import com.leyue.usercenter.infrastructure.role.assembler.RoleAssembler;
import com.leyue.usercenter.infrastructure.role.dataobject.RoleDO;
import com.leyue.usercenter.infrastructure.role.dataobject.RoleMenuDO;
import com.leyue.usercenter.infrastructure.role.mapper.RoleMapper;
import com.leyue.usercenter.infrastructure.role.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    @Override
    public Role findById(Long id) {
        RoleDO roleDO = roleMapper.selectById(id);
        return RoleAssembler.INSTANCE.toDomain(roleDO);
    }

    @Override
    public Role findByRoleCode(String roleCode) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleDO::getRoleCode, roleCode);
        RoleDO roleDO = roleMapper.selectOne(wrapper);
        return RoleAssembler.INSTANCE.toDomain(roleDO);
    }

    @Override
    public List<Role> findByUserId(Long userId) {
        List<RoleDO> roleDOs = roleMapper.selectByUserId(userId);
        return roleDOs.stream()
                .map(RoleAssembler.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Role role) {
        RoleDO roleDO = RoleAssembler.INSTANCE.toDO(role);
        if (roleDO.getId() == null) {
            roleDO.setCreatedAt(new Date());
            roleDO.setUpdatedAt(new Date());
            roleMapper.insert(roleDO);
        } else {
            roleDO.setUpdatedAt(new Date());
            roleMapper.updateById(roleDO);
        }
        // 更近角色和菜单的绑定关系
        roleMenuMapper.delete(Wrappers.<RoleMenuDO>lambdaQuery()
                .eq(RoleMenuDO::getRoleId, roleDO.getId()));
        role.getMenus().forEach(menu -> {
            RoleMenuDO roleMenuDO = new RoleMenuDO();
            roleMenuDO.setRoleId(roleDO.getId());
            roleMenuDO.setMenuId(menu.getId());
            roleMenuMapper.insert(roleMenuDO);
        });
    }

    @Override
    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    @Override
    public Role findByCode(String roleCode) {
        RoleDO roleDO = roleMapper.selectOne(Wrappers.<RoleDO>lambdaQuery()
                .eq(RoleDO::getRoleCode, roleCode));
        return RoleAssembler.INSTANCE.toDomain(roleDO);
    }

    @Override
    public PageResponse<Role> pageRoles(RolePageQuery query) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getRoleName())) {
            wrapper.like(RoleDO::getRoleName, query.getRoleName());
        }
        if (StringUtils.hasText(query.getRoleCode())) {
            wrapper.like(RoleDO::getRoleCode, query.getRoleCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(RoleDO::getStatus, query.getStatus());
        }

        IPage<RoleDO> page = roleMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );

        List<Role> roles = page.getRecords().stream()
                .map(RoleAssembler.INSTANCE::toDomain)
                .collect(Collectors.toList());

        return PageResponse.of(roles, (int) page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public MultiResponse<RoleDTO> list() {
        List<RoleDTO> roleDTOS = roleMapper.selectList(null).stream()
                .map(RoleAssembler.INSTANCE::toDTO)
                .collect(Collectors.toList());
        return MultiResponse.of(roleDTOS);
    }
}