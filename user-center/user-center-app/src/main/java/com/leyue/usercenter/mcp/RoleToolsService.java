package com.leyue.usercenter.mcp;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.leyue.usercenter.api.RoleService;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.command.CreateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateRoleMenuCmd;
import com.leyue.usercenter.dto.command.UpdateUserRoleCmd;
import com.leyue.usercenter.dto.query.RolePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 角色MCP工具服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleToolsService {

    private final RoleService roleService;

    @Tool(description = "查询角色分页列表")
    public PageResponse<RoleDTO> queryRole(RolePageQuery qry, ToolContext toolContext) {
        log.info("工具名称{}，查询角色分页列表，查询参数: {}", "queryRole", qry);
        return roleService.pageRoles(qry);
    }

    @Tool(description = "查询角色详情")
    public RoleDTO getRole(Long roleId, ToolContext toolContext) {
        log.info("工具名称{}，查询角色详情，角色ID: {}", "getRole", roleId);
        return roleService.getRole(roleId);
    }

    @Tool(description = "创建角色")
    public RoleDTO createRole(CreateRoleCmd cmd, ToolContext toolContext) {
        log.info("工具名称{}，创建角色，命令: {}", "createRole", cmd);
        return roleService.createRole(cmd);
    }

    @Tool(description = "更新角色")
    public RoleDTO updateRole(UpdateRoleCmd cmd, ToolContext toolContext) {
        log.info("工具名称{}，更新角色，命令: {}", "updateRole", cmd);
        return roleService.updateRole(cmd);
    }

    @Tool(description = "删除角色")
    public Response deleteRole(Long roleId, ToolContext toolContext) {
        log.info("工具名称{}，删除角色，角色ID: {}", "deleteRole", roleId);
        roleService.deleteRole(roleId);
        return Response.buildSuccess();
    }

    @Tool(description = "为角色分配菜单")
    public Response assignMenus(Long roleId, UpdateRoleMenuCmd cmd, ToolContext toolContext) {
        log.info("工具名称{}，为角色分配菜单，角色ID: {}，命令: {}", "assignMenus", roleId, cmd);
        roleService.assignMenus(roleId, cmd.getMenuIds());
        return Response.buildSuccess();
    }

    @Tool(description = "更新用户角色")
    public Response updateUserRole(Long userId, UpdateUserRoleCmd cmd, ToolContext toolContext) {
        log.info("工具名称{}，更新用户角色，用户ID: {}，命令: {}", "updateUserRole", userId, cmd);
        roleService.updateUserRole(userId, cmd);
        return Response.buildSuccess();
    }
} 