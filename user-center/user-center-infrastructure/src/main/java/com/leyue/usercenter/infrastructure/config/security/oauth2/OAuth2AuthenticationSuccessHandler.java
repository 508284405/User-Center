package com.leyue.usercenter.infrastructure.config.security.oauth2;

import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OAuth2认证成功处理器
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserGateway userGateway;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;
    private final ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");
        
        // 获取用户信息
        User user = userGateway.findByGoogleId(googleId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        
        // 生成JWT令牌
        UserDTO userDTO = userAssembler.toDTO(user);
        TokenDTO tokenDTO = jwtService.generateToken(userDTO);
        
        // 构建认证响应 (用于日志记录或其他目的)
        // 注: 我们不直接使用authResponse对象，而是直接将token添加到URL
        // AuthenticationResponse authResponse = AuthenticationResponse.builder()
        //        .token(tokenDTO)
        //        .user(userDTO)
        //        .build();
        
        // 重定向到前端应用
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        // 将令牌附加到重定向URL
        if (response.isCommitted()) {
            logger.debug("响应已提交，无法重定向到" + targetUrl);
            return;
        }
        
        // 将身份验证信息添加到重定向URL
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", tokenDTO.getAccessToken())
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && !redirectUri.isEmpty()) {
            return redirectUri;
        }
        return "/oauth2/success"; // 默认重定向URL
    }
}
