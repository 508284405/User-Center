package com.leyue.usercenter.infrastructure.config;

import com.leyue.usercenter.infrastructure.config.security.AccessDeniedHandlerImpl;
import com.leyue.usercenter.infrastructure.config.security.AuthenticationEntryPointImpl;
import com.leyue.usercenter.infrastructure.config.security.JwtAuthenticationFilter;
import com.leyue.usercenter.infrastructure.config.security.oauth2.GoogleOAuth2UserService;
import com.leyue.usercenter.infrastructure.config.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.leyue.usercenter.infrastructure.config.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 开启跨域
            .cors(cors -> {})
            // 关闭csrf
            .csrf(AbstractHttpConfigurer::disable)
            // 不通过Session获取SecurityContext
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // 对于登录接口 允许匿名访问
                .requestMatchers("/api/users/login", "/api/users/register", "/api/users/google-login",
                             "/api/users/wechat-login", "/api/users/wechat-qrcode", "/api/users/wechat-login-status/**",
                             "/api/tokens/refresh","/api/tokens/logout",
                             "/oauth2/callback/*", "/oauth2/success", "/oauth2/error", "/api/users/google-login-url",
                             "/api/alipay/qrcode", "/api/alipay/callback", "/api/alipay/login")
                    .permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
            )
            // 配置OAuth2登录
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(googleOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*")
                )
            )
            // 添加JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 添加自定义未授权和未登录结果返回
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );
                
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}