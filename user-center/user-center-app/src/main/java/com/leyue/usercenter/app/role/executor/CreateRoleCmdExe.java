package com.leyue.usercenter.app.role.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.command.CreateRoleCmd;
import com.leyue.usercenter.infrastructure.role.assembler.RoleAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreateRoleCmdExe {
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;

    public RoleDTO execute(CreateRoleCmd cmd) {
        Role existRole = roleGateway.findByCode(cmd.getRoleCode());
        if (existRole != null) {
            throw new BizException("角色编码已存在");
        }

        Role role = new Role();
        role.setRoleName(cmd.getRoleName());
        role.setRoleCode(cmd.getRoleCode());
        role.setDescription(cmd.getDescription());
        role.setStatus(1);

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