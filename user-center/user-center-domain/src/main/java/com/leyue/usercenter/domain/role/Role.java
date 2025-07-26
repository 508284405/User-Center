package com.leyue.usercenter.domain.role;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import com.leyue.usercenter.domain.menu.Menu;

@Data
public class Role {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer roleType; // 0-客户端角色 1-运营端角色
    private Integer status;
    private List<Menu> menus;

    public Role() {
        this.menus = new ArrayList<>();
        this.status = 1;
        this.roleType = 0; // 默认为客户端角色
    }

    public void addMenu(Menu menu) {
        this.menus.add(menu);
    }

    public void removeMenu(Menu menu) {
        this.menus.remove(menu);
    }

    public Set<String> getAllPermissions() {
        Set<String> permissions = new HashSet<>();
        menus.forEach(menu -> permissions.addAll(menu.getAllPermissions()));
        return permissions;
    }

    public void assignMenus(List<Menu> menus) {
        this.menus.clear();
        this.menus.addAll(menus);
    }

    public boolean hasMenu(String menuCode) {
        return menus.stream()
                .anyMatch(menu -> menu.getMenuCode().equals(menuCode));
    }

    public Set<Menu> getAccessibleMenus() {
        Set<Menu> accessibleMenus = new HashSet<>();
        menus.forEach(menu -> {
            accessibleMenus.add(menu);
            accessibleMenus.addAll(menu.getChildren());
        });
        return accessibleMenus;
    }
    
    public boolean isOperationRole() {
        return roleType != null && roleType == 1;
    }
    
    public boolean isClientRole() {
        return roleType == null || roleType == 0;
    }
}