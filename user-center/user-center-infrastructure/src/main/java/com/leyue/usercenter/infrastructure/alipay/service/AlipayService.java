package com.leyue.usercenter.infrastructure.alipay.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.infrastructure.config.AlipayConfig;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝服务实现
 * 用于处理支付宝授权、获取用户信息等操作
 *
 * @author Cascade
 * @since 2025-05-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlipayService {
    private final AlipayClient alipayClient; // 使用Object类型，避免编译期依赖问题
    private final AlipayConfig alipayConfig;
    private final UserGateway userGateway;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;
    private final PasswordEncoder passwordEncoder;

    private Map<String, String> stateRedirectUrl = new HashMap<>();

    /**
     * 生成支付宝授权URL
     *
     * @param state       状态码，用于防止CSRF攻击
     * @param redirectUri
     * @return 支付宝授权URL
     */
    public String generateAuthUrl(String state, String redirectUri) {
        stateRedirectUrl.put(state, redirectUri);
        try {
            String url = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?" +
                    "app_id=" + alipayConfig.getAppId() +
                    "&scope=auth_user" +
                    "&redirect_uri=" + URLEncoder.encode(alipayConfig.getRedirectUri(), "UTF-8") +
                    "&state=" + state;
            return url;
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 处理支付宝登录
     * 调用支付宝SDK获取用户信息并完成登录
     *
     * @param authCode 授权码
     * @return 认证响应
     */
    public AuthenticationResponse handleAlipayLogin(String authCode) {
        log.info("处理支付宝登录，授权码: {}", authCode);

        try {
            // 1. 获取访问令牌
            AlipaySystemOauthTokenResponse tokenResponse = getAccessToken(authCode);
            if (!tokenResponse.isSuccess()) {
                throw new RuntimeException("获取支付宝访问令牌失败: " + tokenResponse.getSubMsg());
            }

            String accessToken = tokenResponse.getAccessToken();
            String alipayUserId = tokenResponse.getOpenId();
            log.info("获取到支付宝用户ID: {}", alipayUserId);

            // 2. 获取用户信息
            AlipayUserInfoShareResponse userInfoResponse = getUserInfo(accessToken);
            if (!userInfoResponse.isSuccess()) {
                throw new RuntimeException("获取支付宝用户信息失败: " + userInfoResponse.getSubMsg());
            }

            String nickname = userInfoResponse.getNickName();
            String avatar = userInfoResponse.getAvatar();
            log.info("获取到支付宝用户信息: nickname={}, avatar={}", nickname, avatar);

            // 3. 查找或创建用户
            User user = userGateway.findByAlipayId(alipayUserId);
            if (user == null) {
                // 创建新用户
                log.info("用户不存在，创建新用户");
                user = new User();
                user.setUsername(nickname != null ? nickname : "alipay_" + alipayUserId.substring(0, 8));
                // 随机密码
                user.setPassword(passwordEncoder.encode("12345678"));
                user.setAlipayId(alipayUserId);
                user.setAvatar(avatar);
                user.setStatus(1); // 启用状态
                userGateway.save(user);
            } else {
                // 更新用户信息
                log.info("用户已存在，更新用户信息");
                user.updateAlipayInfo(alipayUserId, nickname, avatar);
                userGateway.save(user);
            }

            // 4. 生成JWT令牌
            UserDTO userDTO = userAssembler.toDTO(user);
            TokenDTO tokenDTO = jwtService.generateToken(userDTO);

            // 5. 创建认证响应
            return AuthenticationResponse.builder()
                    .token(tokenDTO)
                    .user(userDTO)
                    .build();
        } catch (Exception e) {
            log.error("支付宝登录处理异常", e);
            throw new RuntimeException("支付宝登录处理异常: " + e.getMessage(), e);
        }
    }

    public AlipaySystemOauthTokenResponse getAccessToken(String authCode) {
        try {
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            request.setCode(authCode);
            request.setGrantType("authorization_code");

            AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.error("获取支付宝访问令牌失败: {}", response.getSubMsg());
                throw new RuntimeException("获取支付宝访问令牌失败: " + response.getSubMsg());
            }
            return response;
        } catch (Exception e) {
            log.error("获取支付宝访问令牌异常", e);
            throw new RuntimeException("获取支付宝访问令牌异常", e);
        }
    }

    public AlipayUserInfoShareResponse getUserInfo(String accessToken) {
        try {
            AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse response = alipayClient.execute(request, accessToken);

            if (!response.isSuccess()) {
                log.error("获取支付宝用户信息失败: {}", response.getSubMsg());
                throw new RuntimeException("获取支付宝用户信息失败: " + response.getSubMsg());
            }

            return response;
        } catch (Exception e) {
            log.error("获取支付宝用户信息异常", e);
            throw new RuntimeException("获取支付宝用户信息异常", e);
        }
    }

    public String determineRedirectUrl(String state) {
        return stateRedirectUrl.remove(state);
    }
}