package com.leyue.usercenter.app.menu.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.dto.command.CreateMenuCmd;
import com.leyue.usercenter.infrastructure.menu.assembler.MenuAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateMenuCmdExe {
    private final MenuGateway menuGateway;

    public MenuDTO execute(CreateMenuCmd cmd) {
        Menu existMenu = menuGateway.findByCode(cmd.getMenuCode());
        if (existMenu != null) {
            throw new BizException("菜单编码已存在");
        }

        Menu menu = new Menu();
        menu.setMenuName(cmd.getMenuName());
        menu.setMenuCode(cmd.getMenuCode());
        menu.setPath(cmd.getPath());
        menu.setComponent(cmd.getComponent());
        menu.setType(cmd.getType());
        menu.setParentId(cmd.getParentId());
        menu.setSort(cmd.getSort());
        menu.setIcon(cmd.getIcon());

        if (cmd.getPermissions() != null) {
            cmd.getPermissions().forEach(menu::addPermission);
        }

        menuGateway.save(menu);
        return MenuAssembler.INSTANCE.toDTO(menu);
    }
} 