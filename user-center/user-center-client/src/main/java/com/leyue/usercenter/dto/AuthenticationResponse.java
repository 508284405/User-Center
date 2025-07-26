package com.leyue.usercenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应，包含用户信息和 Token 信息
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    /**
     * 用户信息
     */
    private UserDTO user;

    /**
     * Token 信息
     */
    private TokenDTO token;
} 