package com.leyue.usercenter.infrastructure.menu;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.menu.service.MenuDomainService;
import com.leyue.usercenter.dto.query.MenuPageQuery;
import com.leyue.usercenter.infrastructure.menu.assembler.MenuAssembler;
import com.leyue.usercenter.infrastructure.menu.dataobject.MenuDO;
import com.leyue.usercenter.infrastructure.menu.dataobject.MenuPermissionDO;
import com.leyue.usercenter.infrastructure.menu.mapper.MenuMapper;
import com.leyue.usercenter.infrastructure.menu.mapper.MenuPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuGatewayImpl implements MenuGateway {
    private final MenuMapper menuMapper;
    private final MenuPermissionMapper menuPermissionMapper;
    @Override
    public Menu findById(Long menuId) {
        MenuDO menuDO = menuMapper.selectById(menuId);
        return MenuAssembler.INSTANCE.toDomain(menuDO);
    }
    
    @Override
    public Menu findByCode(String menuCode) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getMenuCode, menuCode);
        MenuDO menuDO = menuMapper.selectOne(wrapper);
        return MenuAssembler.INSTANCE.toDomain(menuDO);
    }
    
    @Override
    public List<Menu> findByRoleId(Long roleId) {
        List<MenuDO> menuDOs = menuMapper.selectByRoleId(roleId);
        List<Menu> menus = menuDOs.stream()
                .map(MenuAssembler.INSTANCE::toDomain)
                .collect(Collectors.toList());
                
        // 加载权限
        menus.forEach(menu -> {
            Set<String> permissions = findPermissionsByMenuId(menu.getId());
            menu.setPermissions(permissions);
        });
        
        // 构建树形结构
        return MenuDomainService.INSTANCE.buildMenuTree(menus);
    }
    
    @Override
    public List<Menu> findAll() {
        List<MenuDO> menuDOs = menuMapper.selectList(null);
        List<Menu> menus = menuDOs.stream()
                .map(MenuAssembler.INSTANCE::toDomain)
                .collect(Collectors.toList());
                
        // 加载权限
        menus.forEach(menu -> {
            Set<String> permissions = findPermissionsByMenuId(menu.getId());
            menu.setPermissions(permissions);
        });
        
        // 构建树形结构
        return MenuDomainService.INSTANCE.buildMenuTree(menus);
    }
    
    @Override
    public void save(Menu menu) {
        MenuDO menuDO = MenuAssembler.INSTANCE.toDO(menu);
        if (menuDO.getId() == null) {
            menuMapper.insert(menuDO);
        } else {
            menuMapper.updateById(menuDO);
        }
    }
    
    @Override
    public void delete(Long menuId) {
        menuMapper.deleteById(menuId);
    }
    
    @Override
    public PageResponse<Menu> pageMenus(MenuPageQuery query) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getMenuName())) {
            wrapper.like(MenuDO::getMenuName, query.getMenuName());
        }
        if (StringUtils.hasText(query.getMenuCode())) {
            wrapper.like(MenuDO::getMenuCode, query.getMenuCode());
        }
        if (query.getType() != null) {
            wrapper.eq(MenuDO::getType, query.getType());
        }
        wrapper.orderByAsc(MenuDO::getSort);
        
        IPage<MenuDO> page = menuMapper.selectPage(
            new Page<>(query.getPageNum(), query.getPageSize()),
            wrapper
        );
        
        List<Menu> menus = page.getRecords().stream()
                .map(MenuAssembler.INSTANCE::toDomain)
                .collect(Collectors.toList());
                
        return PageResponse.of(menus, (int)page.getTotal(), query.getPageNum(), query.getPageSize());
    }
    
    @Override
    public Set<String> findPermissionsByMenuId(Long menuId) {
        List<String> permissions = menuPermissionMapper.selectPermissionsByMenuId(menuId);
        return new HashSet<>(permissions);
    }
    
    @Override
    @Transactional
    public void savePermissions(Long menuId, Set<String> permissions) {
        // 先删除原有权限
        menuPermissionMapper.deleteByMenuId(menuId);
        
        // 保存新的权限
        permissions.forEach(permission -> {
            MenuPermissionDO menuPermissionDO = new MenuPermissionDO();
            menuPermissionDO.setMenuId(menuId);
            menuPermissionDO.setPermission(permission);
            menuPermissionMapper.insert(menuPermissionDO);
        });
    }
    
    @Override
    public void deletePermissions(Long menuId) {
        menuPermissionMapper.deleteByMenuId(menuId);
    }
} 