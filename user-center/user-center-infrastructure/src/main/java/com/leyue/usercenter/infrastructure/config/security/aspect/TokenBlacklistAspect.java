package com.leyue.usercenter.infrastructure.config.security.aspect;

import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.infrastructure.config.security.annotation.RevokeCurrentToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Token 黑名单处理切面
 * 用于处理标记有 @RevokeCurrentToken 注解的方法，自动将当前 Token 加入黑名单
 *
 * @author Admin
 * @since 2025-03-15
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TokenBlacklistAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistAspect.class);
    
    private final JwtService jwtService;
    
    /**
     * 环绕通知处理 @RevokeCurrentToken 注解的方法
     *
     * @param joinPoint 连接点
     * @param annotation 注解实例
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(annotation)")
    public Object handleTokenRevocation(ProceedingJoinPoint joinPoint, RevokeCurrentToken annotation) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = extractToken(request);
        
        if (!annotation.onlyOnSuccess() && StringUtils.isNotBlank(token)) {
            // 如果不要求方法成功执行才撤销Token，则先撤销
            revokeToken(token);
        }
        
        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            
            // 方法成功执行，且配置要求成功时撤销Token，则撤销Token
            if (annotation.onlyOnSuccess() && StringUtils.isNotBlank(token)) {
                revokeToken(token);
            }
        } catch (Throwable e) {
            // 方法执行出现异常，不撤销Token
            logger.error("方法执行异常，Token不会被撤销", e);
            throw e;
        }
        
        return result;
    }
    
    /**
     * 从请求中提取 Token
     *
     * @param request HTTP 请求
     * @return Token 字符串，如果没有则返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 撤销 Token（加入黑名单）
     *
     * @param token Token 字符串
     */
    private void revokeToken(String token) {
        try {
            jwtService.addToBlacklist(token);
            logger.debug("已将 Token 加入黑名单");
        } catch (Exception e) {
            logger.error("将 Token 加入黑名单时发生错误", e);
        }
    }
}
