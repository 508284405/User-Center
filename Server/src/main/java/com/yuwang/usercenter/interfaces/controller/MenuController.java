package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.menu.dto.MenuPageQuery;
import com.yuwang.usercenter.domain.menu.entity.Menu;
import com.yuwang.usercenter.domain.menu.service.MenuDomainService;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import jakarta.servlet.http.HttpServletRequest;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")

public class MenuController {
    private final MenuDomainService menuDomainService;
    private final OperationLogDomainService operationLogDomainService;

    public MenuController(MenuDomainService menuDomainService, OperationLogDomainService operationLogDomainService) {
        this.menuDomainService = menuDomainService;
        this.operationLogDomainService = operationLogDomainService;
    }

    @PostMapping
    public BaseResult<Menu> createMenu(@RequestBody Menu menu, HttpServletRequest request) {
        Menu createdMenu = menuDomainService.createMenu(menu);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("CREATE");
        log.setModule("MENU");
        log.setTargetId(createdMenu.getId());
        log.setDescription("创建菜单：" + createdMenu.getMenuName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);
        return BaseResult.success(createdMenu);
    }

    @PostMapping("/page")
    public BasePageResult<Menu> getMenuPage(@RequestBody MenuPageQuery query) {
        return menuDomainService.findMenuPage(query);
    }

    @PutMapping("/{id}")
    public BaseResult<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu, HttpServletRequest request) {
        menu.setId(id);
        Menu updatedMenu = menuDomainService.updateMenu(menu);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("UPDATE");
        log.setModule("MENU");
        log.setTargetId(updatedMenu.getId());
        log.setDescription("更新菜单：" + updatedMenu.getMenuName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);
        return BaseResult.success(updatedMenu);
    }

    @DeleteMapping("/{id}")
    public BaseResult<Void> deleteMenu(@PathVariable Long id, HttpServletRequest request) {
        Menu menu = menuDomainService.findById(id).orElseThrow(() -> new IllegalArgumentException("菜单不存在"));
        menuDomainService.deleteMenu(id);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("DELETE");
        log.setModule("MENU");
        log.setTargetId(id);
        log.setDescription("删除菜单：" + menu.getMenuName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);
        return BaseResult.success();
    }

    @GetMapping("/{id}")
    public BaseResult<Menu> getMenu(@PathVariable Long id) {
        return menuDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "菜单不存在"));
    }

    @GetMapping("/system/{systemId}")
    public BaseResult<List<Menu>> getMenusBySystemId(@PathVariable Long systemId) {
        return BaseResult.success(menuDomainService.findBySystemId(systemId));
    }

    @GetMapping("/parent/{parentId}")
    public BaseResult<List<Menu>> getMenusByParentId(@PathVariable Long parentId) {
        return BaseResult.success(menuDomainService.findByParentId(parentId));
    }

    @GetMapping
    public BaseResult<List<Menu>> getAllMenus() {
        return BaseResult.success(menuDomainService.findAllMenus());
    }
}