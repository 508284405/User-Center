package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token 刷新命令
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
public class TokenRefreshCmd {
    /**
     * Refresh Token
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
} 