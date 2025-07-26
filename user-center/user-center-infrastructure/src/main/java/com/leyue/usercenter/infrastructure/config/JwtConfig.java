package com.leyue.usercenter.infrastructure.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置类
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    // Getters and Setters
    /**
     * JWT 秘钥（已废弃，使用privateKey）
     */
    @Deprecated
    private String secret;
    
    /**
     * RSA 私钥（用于签名）
     */
    private String privateKey;
    
    /**
     * RSA 公钥（用于验签）
     */
    private String publicKey;
    
    /**
     * Access Token 有效期（分钟）
     */
    private long accessTokenExpiration = 30;
    
    /**
     * Refresh Token 有效期（天）
     */
    private long refreshTokenExpiration = 7;
    
    /**
     * 发行者
     */
    private String issuer = "user-center";
    
    /**
     * 时间漂移容忍度（秒）
     */
    private long clockSkew = 60;
} 