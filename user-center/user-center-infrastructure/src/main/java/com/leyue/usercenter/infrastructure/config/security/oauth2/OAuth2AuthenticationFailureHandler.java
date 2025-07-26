package com.leyue.usercenter.infrastructure.config.security.oauth2;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OAuth2认证失败处理器
 */
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String redirectUri = request.getParameter("redirect_uri");
        String targetUrl;
        
        if (redirectUri != null && !redirectUri.isEmpty()) {
            targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", exception.getMessage())
                    .build().toUriString();
        } else {
            targetUrl = UriComponentsBuilder.fromUriString("/oauth2/error")
                    .queryParam("error", exception.getMessage())
                    .build().toUriString();
        }
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
