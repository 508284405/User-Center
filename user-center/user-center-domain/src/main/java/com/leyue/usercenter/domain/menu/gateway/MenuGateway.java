package com.leyue.usercenter.domain.menu.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.MenuPermission;
import com.leyue.usercenter.dto.PageResult;
import com.leyue.usercenter.dto.query.MenuPageQuery;
import java.util.List;
import java.util.Set;

public interface MenuGateway {
    Menu findById(Long menuId);
    Menu findByCode(String menuCode);
    List<Menu> findByRoleId(Long roleId);
    List<Menu> findAll();
    void save(Menu menu);
    void delete(Long menuId);
    PageResponse<Menu> pageMenus(MenuPageQuery query);
    Set<String> findPermissionsByMenuId(Long menuId);
    void savePermissions(Long menuId, Set<String> permissions);
    void deletePermissions(Long menuId);
} 