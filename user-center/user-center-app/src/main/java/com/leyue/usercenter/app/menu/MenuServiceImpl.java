package com.leyue.usercenter.app.menu;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.api.MenuService;
import com.leyue.usercenter.app.menu.executor.CreateMenuCmdExe;
import com.leyue.usercenter.app.menu.executor.UpdateMenuCmdExe;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.menu.service.MenuDomainService;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.dto.command.CreateMenuCmd;
import com.leyue.usercenter.dto.command.UpdateMenuCmd;
import com.leyue.usercenter.dto.query.MenuPageQuery;
import com.leyue.usercenter.infrastructure.menu.assembler.MenuAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuGateway menuGateway;
    private final CreateMenuCmdExe createMenuCmdExe;
    private final UpdateMenuCmdExe updateMenuCmdExe;

    @Override
    public MenuDTO createMenu(CreateMenuCmd cmd) {
        return createMenuCmdExe.execute(cmd);
    }

    @Override
    public MenuDTO updateMenu(UpdateMenuCmd cmd) {
        return updateMenuCmdExe.execute(cmd);
    }

    @Override
    public void deleteMenu(Long menuId) {
        menuGateway.delete(menuId);
    }

    @Override
    public MenuDTO getMenu(Long menuId) {
        Menu menu = menuGateway.findById(menuId);
        return MenuAssembler.INSTANCE.toDTO(menu);
    }

    @Override
    public List<MenuDTO> getRoleMenus(Long roleId) {
        List<Menu> menus = menuGateway.findByRoleId(roleId);
        return menus.stream()
                .map(MenuAssembler.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuDTO> getAllMenus() {
        List<Menu> menus = menuGateway.findAll();
        List<Menu> menuTree = MenuDomainService.INSTANCE.buildMenuTree(menus);
        return menuTree.stream()
                .map(MenuAssembler.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<MenuDTO> pageMenus(MenuPageQuery query) {
        PageResponse<Menu> pageResponse = menuGateway.pageMenus(query);
        return PageResponse.of(pageResponse.getData().stream().map(MenuAssembler.INSTANCE::toDTO).collect(Collectors.toList())
                , pageResponse.getTotalCount(), pageResponse.getPageIndex(), pageResponse.getPageSize());
    }
} 