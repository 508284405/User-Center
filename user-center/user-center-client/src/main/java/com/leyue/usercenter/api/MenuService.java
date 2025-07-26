package com.leyue.usercenter.api;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.dto.command.CreateMenuCmd;
import com.leyue.usercenter.dto.command.UpdateMenuCmd;
import com.leyue.usercenter.dto.query.MenuPageQuery;

import java.util.List;

public interface MenuService {
    MenuDTO createMenu(CreateMenuCmd cmd);

    MenuDTO updateMenu(UpdateMenuCmd cmd);

    void deleteMenu(Long menuId);

    MenuDTO getMenu(Long menuId);

    List<MenuDTO> getRoleMenus(Long roleId);

    List<MenuDTO> getAllMenus();

    PageResponse<MenuDTO> pageMenus(MenuPageQuery query);
} 