package com.leyue.usercenter.app.role.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.command.UpdateRoleCmd;
import com.leyue.usercenter.infrastructure.role.assembler.RoleAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpdateRoleCmdExe {
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;

    public RoleDTO execute(UpdateRoleCmd cmd) {
        Role role = roleGateway.findById(cmd.getRoleId());
        if (role == null) {
            throw new BizException("角色不存在");
        }

        if (StringUtils.hasText(cmd.getRoleName())) {
            role.setRoleName(cmd.getRoleName());
        }
        if (StringUtils.hasText(cmd.getRoleCode())) {
            Role existRole = roleGateway.findByCode(cmd.getRoleCode());
            if (existRole != null && !existRole.getId().equals(cmd.getRoleId())) {
                throw new BizException("角色编码已存在");
            }
            role.setRoleCode(cmd.getRoleCode());
        }
        if (StringUtils.hasText(cmd.getDescription())) {
            role.setDescription(cmd.getDescription());
        }
        if (cmd.getStatus() != null) {
            role.setStatus(cmd.getStatus());
        }

        if (cmd.getMenuIds() != null) {
            List<Menu> menus = cmd.getMenuIds().stream()
                    .map(menuGateway::findById)
                    .collect(Collectors.toList());
            role.setMenus(menus);
        }

        roleGateway.save(role);
        return RoleAssembler.INSTANCE.toDTO(role);
    }
}