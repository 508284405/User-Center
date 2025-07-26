package com.leyue.usercenter.infrastructure.config.security;

import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.infrastructure.config.security.context.UserContext;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT 认证过滤器
 * 用于验证请求中的 JWT Token
 *
 * @author Trae
 * @since 2024-01-20
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;
    private final UserAssembler userAssembler;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        
        // 如果没有 Authorization 头或者不是以 Bearer 开头，则直接放行
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 提取 Token
        String token = authHeader.substring(7);
        
        try {
            // 验证 Token，包括检查是否在黑名单中
            if (jwtService.validateAccessToken(token)) {
                // 从 Token 中获取用户 ID
                Long userId = jwtService.getUserIdFromAccessToken(token);
                User user = userGateway.findById(userId);
                // 补充角色信息
                List<Role> roles = roleGateway.findByUserId(userId);
                // 补充菜单信息
                roles.forEach(role->{
                    Long roleId = role.getId();
                    List<Menu> menuList = menuGateway.findByRoleId(roleId);
                    role.setMenus(menuList);
                });
                user.setRoles(new HashSet<>(roles));
                // 将用户信息设置到上下文中
                UserContext.UserInfo userInfo = userAssembler.toUserInfo(user);
                UserContext.setCurrentUser(userInfo);

                // ✅ 告诉 Spring Security：这个请求的用户已认证
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userInfo, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token 验证异常，不设置认证上下文
            logger.error("无法验证 JWT Token", e);
        }
        
        chain.doFilter(request, response);
    }
}