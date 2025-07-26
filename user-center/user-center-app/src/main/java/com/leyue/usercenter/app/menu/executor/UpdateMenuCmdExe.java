package com.leyue.usercenter.app.menu.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.dto.command.UpdateMenuCmd;
import com.leyue.usercenter.infrastructure.menu.assembler.MenuAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UpdateMenuCmdExe {
    private final MenuGateway menuGateway;

    public MenuDTO execute(UpdateMenuCmd cmd) {
        Menu menu = menuGateway.findById(cmd.getMenuId());
        if (menu == null) {
            throw new BizException("菜单不存在");
        }

        if (StringUtils.hasText(cmd.getMenuName())) {
            menu.setMenuName(cmd.getMenuName());
        }
        if (StringUtils.hasText(cmd.getMenuCode())) {
            Menu existMenu = menuGateway.findByCode(cmd.getMenuCode());
            if (existMenu != null && !existMenu.getId().equals(cmd.getMenuId())) {
                throw new BizException("菜单编码已存在");
            }
            menu.setMenuCode(cmd.getMenuCode());
        }
        if (StringUtils.hasText(cmd.getPath())) {
            menu.setPath(cmd.getPath());
        }
        if (StringUtils.hasText(cmd.getComponent())) {
            menu.setComponent(cmd.getComponent());
        }
        if (cmd.getType() != null) {
            menu.setType(cmd.getType());
        }
        if (cmd.getParentId() != null) {
            menu.setParentId(cmd.getParentId());
        }
        if (cmd.getSort() != null) {
            menu.setSort(cmd.getSort());
        }
        if (StringUtils.hasText(cmd.getIcon())) {
            menu.setIcon(cmd.getIcon());
        }

        if (cmd.getPermissions() != null) {
            menu.getPermissions().clear();
            cmd.getPermissions().forEach(menu::addPermission);
        }

        menuGateway.save(menu);
        return MenuAssembler.INSTANCE.toDTO(menu);
    }
} 