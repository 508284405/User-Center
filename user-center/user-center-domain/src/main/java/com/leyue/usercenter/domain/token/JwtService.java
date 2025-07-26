package com.leyue.usercenter.domain.token;

import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;

/**
 * JWT 服务接口
 * 负责 JWT Token 的生成、验证和管理
 *
 * @author Trae
 * @since 2024-01-20
 */
public interface JwtService {
    
    /**
     * 生成 Token（包含 Access Token 和 Refresh Token）
     *
     * @param userDTO 用户信息
     * @return Token 信息
     */
    TokenDTO generateToken(UserDTO userDTO);
    
    /**
     * 验证 Access Token
     *
     * @param token Access Token
     * @return 如果 Token 有效，返回 true；否则返回 false
     */
    boolean validateAccessToken(String token);
    
    /**
     * 验证 Refresh Token
     *
     * @param token Refresh Token
     * @return 如果 Token 有效，返回 true；否则返回 false
     */
    boolean validateRefreshToken(String token);
    
    /**
     * 从 Access Token 中提取用户 ID
     *
     * @param token Access Token
     * @return 用户 ID
     */
    Long getUserIdFromAccessToken(String token);
    
    /**
     * 从 Access Token 中提取 JTI（JWT ID）
     *
     * @param token Access Token
     * @return JTI
     */
    String getJtiFromAccessToken(String token);
    
    /**
     * 将 Access Token 加入黑名单
     *
     * @param token Access Token
     */
    void addToBlacklist(String token);
    
    /**
     * 检查 Access Token 是否在黑名单中
     *
     * @param jti JWT ID
     * @return 如果在黑名单中，返回 true；否则返回 false
     */
    boolean isInBlacklist(String jti);
    
    /**
     * 使用 Refresh Token 刷新 Access Token
     *
     * @param refreshToken Refresh Token
     * @return 新的 Token 信息
     */
    TokenDTO refreshToken(String refreshToken);
} 