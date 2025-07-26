package com.leyue.usercenter.infrastructure.config.security.oauth2;

import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Google OAuth2用户服务实现
 */
@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    @Resource
    private UserGateway userGateway;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // 提取Google用户信息
        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        // 可能用于显示名称，目前未使用
        // String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");
        
        if(!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("邮箱不能为空");
        }
        
        // 查找或创建用户
        User user = userGateway.findByGoogleId(googleId);
        if (user == null) {
            user = new User();
            user.setUsername(email);
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setAvatar(pictureUrl);
            user.setStatus(1); // 启用状态
            userGateway.save(user);
        } else {
            // 更新用户信息
            if (!email.equals(user.getEmail()) || (pictureUrl != null && !pictureUrl.equals(user.getAvatar()))) {
                user.setEmail(email);
                if (pictureUrl != null) {
                    user.setAvatar(pictureUrl);
                }
                userGateway.save(user);
            }
        }
        
        // 创建用户认证主体
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("user_id", user.getId());
        
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }
}
