package com.leyue.usercenter.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.RoleService;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.command.CreateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateRoleMenuCmd;
import com.leyue.usercenter.dto.command.UpdateUserRoleCmd;
import com.leyue.usercenter.dto.query.RolePageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 * 提供角色相关的RESTful API，包括角色的创建、更新、删除、查询等功能
 *
 * @author Trae
 * @since 2024-01-20
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    /**
     * 创建新角色
     *
     * @param cmd 创建角色命令，包含角色基本信息
     * @return 创建的角色信息
     */
    @PostMapping("create")
    public SingleResponse<RoleDTO> createRole(@RequestBody @Valid CreateRoleCmd cmd) {
        return SingleResponse.of(roleService.createRole(cmd));
    }

    /**
     * 更新角色信息
     *
     * @param cmd 更新角色命令，包含需要更新的角色信息
     * @return 更新后的角色信息
     */
    @PutMapping("/update")
    public SingleResponse<RoleDTO> updateRole(@RequestBody @Valid UpdateRoleCmd cmd) {
        return SingleResponse.of(roleService.updateRole(cmd));
    }

    /**
     * 删除角色
     *
     * @param roleId 要删除的角色ID
     * @return 无返回内容
     */
    @DeleteMapping("/{roleId}")
    public SingleResponse<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return SingleResponse.of(null);
    }

    /**
     * 获取角色信息
     *
     * @param roleId 角色ID
     * @return 角色详细信息
     */
    @GetMapping("/{roleId}")
    public SingleResponse<RoleDTO> getRole(@PathVariable Long roleId) {
        return SingleResponse.of(roleService.getRole(roleId));
    }

    /**
     * 获取用户的所有角色
     *
     * @param userId 用户ID
     * @return 用户拥有的角色列表
     */
    @GetMapping("/user/{userId}")
    public SingleResponse<List<RoleDTO>> getUserRoles(@PathVariable Long userId) {
        return SingleResponse.of(roleService.getUserRoles(userId));
    }

    /**
     * 更新用户的角色
     */
    @PutMapping("/user/{userId}")
    public SingleResponse<Void> updateUserRole(@PathVariable Long userId, @RequestBody @Valid UpdateUserRoleCmd cmd) {
        roleService.updateUserRole(userId, cmd);
        return SingleResponse.of(null);
    }

    /**
     * 为角色分配菜单权限
     *
     * @param roleId 角色ID
     * @param cmd    菜单ID列表
     * @return 无返回内容
     */
    @PostMapping("/{roleId}/menus")
    public SingleResponse<Void> assignMenus(@PathVariable Long roleId,
                                            @RequestBody @Valid UpdateRoleMenuCmd cmd) {
        roleService.assignMenus(roleId, cmd.getMenuIds());
        return SingleResponse.of(null);
    }

    /**
     * 分页查询角色列表
     *
     * @param query 分页查询参数
     * @return 分页的角色列表
     */
    @PostMapping("/page")
    public PageResponse<RoleDTO> pageRoles(@RequestBody @Valid RolePageQuery query) {
        return roleService.pageRoles(query);
    }

    @PostMapping("/list")
    public MultiResponse<RoleDTO> pageRoles() {
        return roleService.list();
    }
}