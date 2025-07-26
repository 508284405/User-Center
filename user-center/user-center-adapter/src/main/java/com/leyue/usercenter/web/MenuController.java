package com.leyue.usercenter.web;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.MenuService;
import com.leyue.usercenter.dto.*;
import com.leyue.usercenter.dto.command.*;
import com.leyue.usercenter.dto.query.MenuPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 菜单管理控制器
 * 提供菜单相关的RESTful API，包括菜单的创建、更新、删除、查询等功能
 *
 * @author Trae
 * @since 2024-01-20
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    
    /**
     * 创建新菜单
     * 
     * @param cmd 创建菜单命令，包含菜单基本信息
     * @return 创建的菜单信息
     */
    @PostMapping("/create")
    public SingleResponse<MenuDTO> createMenu(@Valid @RequestBody CreateMenuCmd cmd) {
        return SingleResponse.of(menuService.createMenu(cmd));
    }
    
    /**
     * 更新菜单信息
     * 
     * @param menuId 菜单ID
     * @param cmd 更新菜单命令，包含需要更新的菜单信息
     * @return 更新后的菜单信息
     */
    @PutMapping("/{menuId}")
    public SingleResponse<MenuDTO> updateMenu(@PathVariable Long menuId,
                                      @Valid @RequestBody UpdateMenuCmd cmd) {
        cmd.setMenuId(menuId);
        return SingleResponse.of(menuService.updateMenu(cmd));
    }
    
    /**
     * 删除菜单
     * 
     * @param menuId 要删除的菜单ID
     * @return 无返回内容
     */
    @DeleteMapping("/{menuId}")
    public SingleResponse<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return SingleResponse.of(null);
    }
    
    /**
     * 获取菜单信息
     * 
     * @param menuId 菜单ID
     * @return 菜单详细信息
     */
    @GetMapping("/{menuId}")
    public SingleResponse<MenuDTO> getMenu(@PathVariable Long menuId) {
        return SingleResponse.of(menuService.getMenu(menuId));
    }
    
    /**
     * 获取角色的所有菜单
     * 
     * @param roleId 角色ID
     * @return 角色拥有的菜单列表
     */
    @GetMapping("/role/{roleId}")
    public SingleResponse<List<MenuDTO>> getRoleMenus(@PathVariable Long roleId) {
        return SingleResponse.of(menuService.getRoleMenus(roleId));
    }
    
    /**
     * 获取所有菜单
     * 
     * @return 所有菜单列表
     */
    @GetMapping
    public SingleResponse<List<MenuDTO>> getAllMenus() {
        return SingleResponse.of(menuService.getAllMenus());
    }
    
    /**
     * 分页查询菜单列表
     * 
     * @param query 分页查询参数
     * @return 分页的菜单列表
     */
    @PostMapping("/page")
    public PageResponse<MenuDTO> pageMenus(@RequestBody @Valid MenuPageQuery query) {
        return menuService.pageMenus(query);
    }
}