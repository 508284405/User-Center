package com.leyue.usercenter.domain.menu.service;

import com.leyue.usercenter.domain.menu.Menu;
import java.util.*;
import java.util.stream.Collectors;

public class MenuDomainService {
    public static MenuDomainService INSTANCE = new MenuDomainService();
    
    public List<Menu> buildMenuTree(List<Menu> menus) {
        // 按父ID分组
        Map<Long, List<Menu>> parentIdMenuMap = menus.stream()
                .collect(Collectors.groupingBy(menu -> 
                    menu.getParentId() == null ? 0L : menu.getParentId()));
                    
        // 构建树形结构
        return buildTree(0L, parentIdMenuMap);
    }
    
    private List<Menu> buildTree(Long parentId, Map<Long, List<Menu>> parentIdMenuMap) {
        List<Menu> children = parentIdMenuMap.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }

        children.forEach(child -> {
            List<Menu> grandChildren = buildTree(child.getId(), parentIdMenuMap);
            grandChildren.forEach(child::addChild);
        });

        return children.stream()
                .sorted(Comparator.comparing(Menu::getSort))
                .collect(Collectors.toList());
    }

    public List<Menu> filterByType(List<Menu> menus, Integer type) {
        return menus.stream()
                .filter(menu -> menu.getType().equals(type))
                .collect(Collectors.toList());
    }

    public List<Menu> filterByPermission(List<Menu> menus, String permission) {
        return menus.stream()
                .filter(menu -> menu.getAllPermissions().contains(permission))
                .collect(Collectors.toList());
    }
} 