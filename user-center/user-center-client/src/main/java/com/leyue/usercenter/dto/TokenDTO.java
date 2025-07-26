package com.leyue.usercenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token DTO，表示 JWT Token 的结构
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    /**
     * Access Token
     */
    private String accessToken;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Access Token 过期时间（毫秒时间戳）
     */
    private long accessTokenExpiresAt;

    /**
     * Refresh Token 过期时间（毫秒时间戳）
     */
    private long refreshTokenExpiresAt;

    /**
     * Token 类型
     */
    private String tokenType = "Bearer";
} 