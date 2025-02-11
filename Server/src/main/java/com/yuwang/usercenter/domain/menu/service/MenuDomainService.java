package com.yuwang.usercenter.domain.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwang.usercenter.domain.menu.entity.Menu;
import com.yuwang.usercenter.domain.menu.repository.MenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuDomainService {
    private final com.yuwang.usercenter.domain.menu.repository.MenuMapper menuMapper;

    public MenuDomainService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        if (menuMapper.exists(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getMenuName, menu.getMenuName())
                .eq(Menu::getSystemId, menu.getSystemId()))) {
            throw new IllegalArgumentException("该系统下已存在相同名称的菜单");
        }
        menuMapper.insert(menu);
        return menu;
    }

    @Transactional
    public Menu updateMenu(Menu menu) {
        menuMapper.updateById(menu);
        return menu;
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        menuMapper.deleteById(menuId);
    }

    public Optional<Menu> findById(Long menuId) {
        return Optional.ofNullable(menuMapper.selectById(menuId));
    }

    public List<Menu> findBySystemId(Long systemId) {
        return menuMapper.selectList(new LambdaQueryWrapper<Menu>().eq(Menu::getSystemId, systemId));
    }

    public List<Menu> findByParentId(Long parentId) {
        return menuMapper.selectList(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, parentId));
    }

    public List<Menu> findBySystemIdAndParentId(Long systemId, Long parentId) {
        return menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getSystemId, systemId)
                .eq(Menu::getParentId, parentId)
                .orderByAsc(Menu::getSort));
    }

    public List<Menu> findAllMenus() {
        return menuMapper.selectList(null);
    }
}