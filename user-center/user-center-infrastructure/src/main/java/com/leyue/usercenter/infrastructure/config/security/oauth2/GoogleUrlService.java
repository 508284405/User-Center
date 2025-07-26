package com.leyue.usercenter.infrastructure.config.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;
import java.util.List;

/**
 * Google登录URL服务
 */
@Service
@RequiredArgsConstructor
public class GoogleUrlService {

    private final GoogleOAuth2Properties googleOAuth2Properties;
    
    /**
     * 获取Google登录URL
     * 
     * @param originalRedirectUri 登录成功后重定向的URL
     * @return Google登录URL
     */
    public String getGoogleLoginUrl(String originalRedirectUri) {
        String clientId = googleOAuth2Properties.getRegistration().getGoogle().getClientId();
        String authorizationUri = googleOAuth2Properties.getProvider().getGoogle().getAuthorizationUri();
        
        if (clientId == null || authorizationUri == null) {
            throw new IllegalStateException("Google客户端配置不存在");
        }
        
        String state = UUID.randomUUID().toString();
        List<String> scopes = googleOAuth2Properties.getRegistration().getGoogle().getScope();

        return UriComponentsBuilder
                .fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", googleOAuth2Properties.getRedirectUri())
                .queryParam("state", state + (originalRedirectUri != null ? ":" + originalRedirectUri : ""))
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", scopes))
                .build()
                .toUriString();
    }
}
