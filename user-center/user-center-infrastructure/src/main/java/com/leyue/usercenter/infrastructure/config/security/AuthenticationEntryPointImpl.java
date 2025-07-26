package com.leyue.usercenter.infrastructure.config.security;

import com.alibaba.cola.dto.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 当用户未通过认证时调用此方法处理
     *
     * @param request       当前的HTTP请求对象，提供请求的相关信息
     * @param response      当前的HTTP响应对象，用于向客户端发送响应
     * @param authException 认证异常，表示认证过程中发生的错误
     * @throws IOException      如果在处理过程中发生I/O错误
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 设置HTTP响应状态码为401未授权
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 设置响应内容类型为JSON格式
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 设置响应字符编码为UTF-8
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 构建一个表示失败响应的对象，包含错误代码和消息
        Response errorResponse = Response.buildFailure("UNAUTHORIZED", "认证失败");
        // 将错误响应对象序列化为JSON字符串，并写入响应体中
        log.error("认证失败", authException);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 