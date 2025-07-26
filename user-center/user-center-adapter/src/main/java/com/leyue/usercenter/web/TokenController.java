package com.leyue.usercenter.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.command.LogoutCmd;
import com.leyue.usercenter.dto.command.TokenRefreshCmd;
import com.leyue.usercenter.infrastructure.config.security.annotation.RevokeCurrentToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Token 控制器
 * 提供 Token 相关的操作，包括刷新、撤销（登出）和黑名单查询等
 *
 * @author Trae & Admin
 * @since 2025-03-15
 */
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {
    private final JwtService jwtService;
    
    /**
     * 刷新 Token
     *
     * @param cmd 刷新 Token 命令
     * @return 新的 Token 信息
     */
    @PostMapping("/refresh")
    public SingleResponse<TokenDTO> refreshToken(@Valid @RequestBody TokenRefreshCmd cmd) {
        TokenDTO tokenDTO = jwtService.refreshToken(cmd.getRefreshToken());
        return SingleResponse.of(tokenDTO);
    }
    
    /**
     * 登出
     * 将当前用户的 Token 加入黑名单
     *
     * @param cmd 登出命令
     * @param request HTTP 请求
     * @return 无返回内容
     */
    @PostMapping("/logout")
    @RevokeCurrentToken
    public SingleResponse<Void> logout(@RequestBody(required = false) LogoutCmd cmd, HttpServletRequest request) {
        // 获取 Token
        String token = null;
        
        // 优先从请求体中获取 Token
        if (cmd != null && cmd.getAccessToken() != null) {
            token = cmd.getAccessToken();
        } else {
            // 从请求头中获取 Token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        // 如果 Token 不为空，则加入黑名单
        if (token != null) {
            jwtService.addToBlacklist(token);
        }
        
        return SingleResponse.of(null);
    }
    
    /**
     * 检查 Token 是否在黑名单中
     * 仅用于调试和测试目的
     *
     * @param jti JWT ID
     * @return 检查结果
     */
    @GetMapping("/check-blacklist")
    public SingleResponse<Boolean> checkBlacklist(@RequestParam String jti) {
        boolean isBlacklisted = jwtService.isInBlacklist(jti);
        return SingleResponse.of(isBlacklisted);
    }
}