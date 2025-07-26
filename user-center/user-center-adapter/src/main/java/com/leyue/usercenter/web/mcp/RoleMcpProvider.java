package com.leyue.usercenter.web.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.mcp.RoleToolsService;
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
public class RoleMcpProvider {
    
    /* 角色MCP传输 */
    @Bean("roleTransport")
    WebMvcSseServerTransportProvider roleTransport(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/role/message", "/mcp/role/sse");
    }

    /* 角色MCP路由 */
    @Bean
    RouterFunction<ServerResponse> roleRouter(
            @Qualifier("roleTransport") WebMvcSseServerTransportProvider t) {
        return t.getRouterFunction();
    }

    /* 角色MCP服务 */
    @Bean("roleMcpServer")
    public McpSyncServer roleMcpServer(
            @Qualifier("roleTransport") WebMvcSseServerTransportProvider t,
            @Qualifier("roleTools") ToolCallbackProvider tools) {

        return McpServer.sync(t)
                .serverInfo("Role-MCP", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }

    /* 角色工具 */
    @Bean("roleTools")
    public ToolCallbackProvider roleTools(RoleToolsService roleToolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(roleToolsService).build();
    }
} 