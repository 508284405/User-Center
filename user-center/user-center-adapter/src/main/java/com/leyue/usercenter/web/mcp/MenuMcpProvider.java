package com.leyue.usercenter.web.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.mcp.MenuToolsService;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class MenuMcpProvider {
    
    /* 菜单MCP传输 */
    @Bean("menuTransport")
    WebMvcSseServerTransportProvider menuTransport(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/menu/message", "/mcp/menu/sse");
    }

    /* 菜单MCP路由 */
    @Bean
    RouterFunction<ServerResponse> menuRouter(
            @Qualifier("menuTransport") WebMvcSseServerTransportProvider t) {
        return t.getRouterFunction();
    }

    /* 菜单MCP服务 */
    @Bean("menuMcpServer")
    public McpSyncServer menuMcpServer(
            @Qualifier("menuTransport") WebMvcSseServerTransportProvider t,
            @Qualifier("menuTools") ToolCallbackProvider tools) {

        return McpServer.sync(t)
                .serverInfo("Menu-MCP", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }

    /* 菜单工具 */
    @Bean("menuTools")
    public ToolCallbackProvider menuTools(MenuToolsService menuToolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(menuToolsService).build();
    }
} 