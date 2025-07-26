package com.leyue.usercenter.dto.command;

import lombok.Data;

/**
 * 登出命令
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
public class LogoutCmd {
    /**
     * 客户端传入的 Access Token
     * 这是可选的，因为在大多数情况下 Token 会从请求头中获取
     */
    private String accessToken;
} 