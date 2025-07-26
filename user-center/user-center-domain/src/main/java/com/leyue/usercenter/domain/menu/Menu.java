package com.leyue.usercenter.domain.menu;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Menu {
    private Long id;
    private String menuName;
    private String menuCode;
    private String path;
    private String component;
    private Integer type; // 1-目录 2-菜单 3-按钮
    private Long parentId;
    private Integer sort;
    private String icon;
    private String target; // 打开方式(_self/_blank)
    private Integer visible; // 0-不可见 1-可见
    private Integer menuType; // 0-客户端菜单 1-运营端菜单
    private Set<String> permissions;
    private List<Menu> children = new ArrayList<>();

    public Menu() {
        this.permissions = new HashSet<>();
        this.visible = 1;
        this.menuType = 0; // 默认为客户端菜单
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public void addChild(Menu child) {
        if (child.getParentId() == null || !child.getParentId().equals(this.getId())) {
            child.setParentId(this.getId());
        }
        this.children.add(child);
    }

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

    public void removeChild(Menu child) {
        this.children.remove(child);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public boolean isVisible() {
        return visible != null && visible == 1;
    }

    public boolean isButton() {
        return type != null && type == 3;
    }

    public boolean isMenu() {
        return type != null && type == 2;
    }

    public boolean isDirectory() {
        return type != null && type == 1;
    }

    public boolean isOperationMenu() {
        return menuType != null && menuType == 1;
    }

    public boolean isClientMenu() {
        return menuType == null || menuType == 0;
    }

    public Set<Menu> getAllChildren() {
        Set<Menu> allChildren = new HashSet<>();
        children.forEach(child -> {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        });
        return allChildren;
    }

    public Set<String> getAllPermissions() {
        Set<String> allPermissions = new HashSet<>(permissions);
        children.forEach(child -> 
            allPermissions.addAll(child.getAllPermissions())
        );
        return allPermissions;
    }

    public boolean hasPermission(String permission) {
        return getAllPermissions().contains(permission);
    }

    public boolean isAccessibleBy(Set<String> userPermissions) {
        return !Collections.disjoint(getAllPermissions(), userPermissions);
    }
}