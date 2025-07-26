package com.leyue.usercenter.mcp;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.leyue.usercenter.api.MenuService;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.dto.command.CreateMenuCmd;
import com.leyue.usercenter.dto.command.UpdateMenuCmd;
import com.leyue.usercenter.dto.query.MenuPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 菜单MCP工具服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuToolsService {

    private final MenuService menuService;

    @Tool(description = "查询菜单分页列表")
    public PageResponse<MenuDTO> queryMenu(MenuPageQuery qry, ToolContext toolContext) {
        log.info("工具名称{}，查询菜单分页列表，查询参数: {}", "queryMenu", qry);
        return menuService.pageMenus(qry);
    }

    @Tool(description = "查询菜单详情")
    public MenuDTO getMenu(Long menuId, ToolContext toolContext) {
        log.info("工具名称{}，查询菜单详情，菜单ID: {}", "getMenu", menuId);
        return menuService.getMenu(menuId);
    }

    @Tool(description = "创建菜单")
    public MenuDTO createMenu(CreateMenuCmd cmd, ToolContext toolContext) {
        log.info("工具名称{}，创建菜单，命令: {}", "createMenu", cmd);
        return menuService.createMenu(cmd);
    }

    @Tool(description = "更新菜单")
    public MenuDTO updateMenu(UpdateMenuCmd cmd, ToolContext toolContext) {
        log.info("工具名称{}，更新菜单，命令: {}", "updateMenu", cmd);
        return menuService.updateMenu(cmd);
    }

    @Tool(description = "删除菜单")
    public Response deleteMenu(Long menuId, ToolContext toolContext) {
        log.info("工具名称{}，删除菜单，菜单ID: {}", "deleteMenu", menuId);
        menuService.deleteMenu(menuId);
        return Response.buildSuccess();
    }
} 