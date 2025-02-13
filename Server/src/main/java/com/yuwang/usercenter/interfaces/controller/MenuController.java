package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.menu.dto.MenuCreateRequest;
import com.yuwang.usercenter.domain.menu.dto.MenuPageQuery;
import com.yuwang.usercenter.domain.menu.dto.MenuUpdateRequest;
import com.yuwang.usercenter.domain.menu.entity.Menu;
import com.yuwang.usercenter.domain.menu.service.MenuDomainService;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import jakarta.servlet.http.HttpServletRequest;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
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

    /**
     * 创建新菜单
     * @param request HTTP请求对象，用于获取IP地址
     * @param createRequest 菜单创建请求
     * @return 创建成功的菜单信息
     */
    @PostMapping
    public BaseResult<Menu> createMenu(@RequestBody @Validated MenuCreateRequest createRequest, HttpServletRequest request) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(createRequest, menu);
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

    /**
     * 更新菜单信息
     * @param id 菜单ID
     * @param updateRequest 更新的菜单信息
     * @param request HTTP请求对象，用于获取IP地址
     * @return 更新后的菜单信息
     */
    @PutMapping("/{id}")
    public BaseResult<Menu> updateMenu(@PathVariable Long id, @RequestBody @Validated MenuUpdateRequest updateRequest, HttpServletRequest request) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(updateRequest, menu);
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

    /**
     * 分页查询菜单列表
     * @param query 分页查询参数
     * @return 分页菜单数据
     */
    @PostMapping("/page")
    public BasePageResult<Menu> getMenuPage(@RequestBody @Validated MenuPageQuery query) {
        return menuDomainService.findMenuPage(query);
    }

    /**
     * 删除菜单
     * @param id 要删除的菜单ID
     * @param request HTTP请求对象，用于获取IP地址
     * @return 删除结果
     */
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

    /**
     * 根据ID获取菜单信息
     * @param id 菜单ID
     * @return 菜单信息
     */
    @GetMapping("/{id}")
    public BaseResult<Menu> getMenu(@PathVariable Long id) {
        return menuDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "菜单不存在"));
    }

    /**
     * 获取指定系统的所有菜单
     * @param systemId 系统ID
     * @return 菜单列表
     */
    @GetMapping("/system/{systemId}")
    public BaseResult<List<Menu>> getMenusBySystemId(@PathVariable Long systemId) {
        return BaseResult.success(menuDomainService.findBySystemId(systemId));
    }

    /**
     * 获取指定父菜单的所有子菜单
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    @GetMapping("/parent/{parentId}")
    public BaseResult<List<Menu>> getMenusByParentId(@PathVariable Long parentId) {
        return BaseResult.success(menuDomainService.findByParentId(parentId));
    }

    /**
     * 获取所有菜单
     * @return 所有菜单列表
     */
    @GetMapping
    public BaseResult<List<Menu>> getAllMenus() {
        return BaseResult.success(menuDomainService.findAllMenus());
    }
}