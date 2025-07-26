package com.leyue.usercenter.infrastructure.wechat;

import com.alibaba.cola.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.WechatLoginStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WechatClient {
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
    private static final String QR_CODE_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect";

    // 存储状态码和回调信息的映射，用于处理微信回调
    private static final Map<String, String> STATE_MAP = new ConcurrentHashMap<>();
    
    // 存储登录状态的映射，用于前端轮询获取登录结果
    private static final Map<String, WechatLoginStatusResponse> LOGIN_STATUS_MAP = new ConcurrentHashMap<>();
    
    // 存储状态码创建时间的映射，用于清理过期的状态码
    private static final Map<String, LocalDateTime> STATE_CREATE_TIME_MAP = new ConcurrentHashMap<>();
    
    // 二维码过期时间（分钟）
    private static final int QR_CODE_EXPIRE_MINUTES = 5;

    private final WechatProperties wechatProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WechatUserInfo getWechatUserInfo(String code) {
        try {
            // 1. 获取access_token
            String accessTokenUrl = String.format(ACCESS_TOKEN_URL,
                    wechatProperties.getAppId(),
                    wechatProperties.getAppSecret(),
                    code);
            String accessTokenResponse = restTemplate.getForObject(accessTokenUrl, String.class);
            JsonNode accessTokenNode = objectMapper.readTree(accessTokenResponse);

            if (accessTokenNode.has("errcode")) {
                throw new BizException("获取微信access_token失败: " + accessTokenNode.get("errmsg").asText());
            }

            String accessToken = accessTokenNode.get("access_token").asText();
            String openId = accessTokenNode.get("openid").asText();

            // 2. 获取用户信息
            String userInfoUrl = String.format(USER_INFO_URL, accessToken, openId);
            String userInfoResponse = restTemplate.getForObject(userInfoUrl, String.class);
            JsonNode userInfoNode = objectMapper.readTree(userInfoResponse);

            if (userInfoNode.has("errcode")) {
                throw new BizException("获取微信用户信息失败: " + userInfoNode.get("errmsg").asText());
            }

            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenId(openId);
            userInfo.setNickname(userInfoNode.get("nickname").asText());
            userInfo.setAvatar(userInfoNode.get("headimgurl").asText());

            return userInfo;
        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            throw new BizException("微信登录失败");
        }
    }

    /**
     * 生成微信扫码登录的二维码URL
     *
     * @param redirectUrl 回调URL
     * @param state       状态码
     * @return 二维码URL
     */
    public String generateQrCodeUrl(String redirectUrl, String state) {
        try {
            // 存储状态码，用于后续回调验证
            STATE_MAP.put(state, redirectUrl);
            
            // 初始化登录状态为等待扫码
            LOGIN_STATUS_MAP.put(state, WechatLoginStatusResponse.pending());
            
            // 记录状态码创建时间
            STATE_CREATE_TIME_MAP.put(state, LocalDateTime.now());

            // 编码回调URL
            String encodedRedirectUrl = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8.name());

            // 生成二维码URL
            return String.format(QR_CODE_URL,
                    wechatProperties.getAppId(),
                    encodedRedirectUrl,
                    state);
        } catch (Exception e) {
            log.error("生成微信二维码URL失败", e);
            throw new BizException("生成微信二维码失败");
        }
    }

    /**
     * 验证状态码是否有效
     *
     * @param state 状态码
     * @return 是否有效
     */
    public boolean validateState(String state) {
        if (!STATE_MAP.containsKey(state)) {
            return false;
        }
        
        // 检查二维码是否过期
        LocalDateTime createTime = STATE_CREATE_TIME_MAP.get(state);
        if (createTime != null && createTime.plusMinutes(QR_CODE_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            // 二维码已过期，更新状态并返回false
            LOGIN_STATUS_MAP.put(state, WechatLoginStatusResponse.expired());
            return false;
        }
        
        return true;
    }

    /**
     * 移除状态码
     *
     * @param state 状态码
     */
    public void removeState(String state) {
        STATE_MAP.remove(state);
        STATE_CREATE_TIME_MAP.remove(state);
    }
    
    /**
     * 获取登录状态
     *
     * @param state 状态码
     * @return 登录状态响应
     */
    public WechatLoginStatusResponse getLoginStatus(String state) {
        // 检查二维码是否过期
        LocalDateTime createTime = STATE_CREATE_TIME_MAP.get(state);
        if (createTime != null && createTime.plusMinutes(QR_CODE_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            return WechatLoginStatusResponse.expired();
        }
        
        // 返回当前登录状态，如果不存在则返回等待状态
        return LOGIN_STATUS_MAP.getOrDefault(state, WechatLoginStatusResponse.pending());
    }
    
    /**
     * 更新登录状态为成功
     *
     * @param state 状态码
     * @param authResponse 认证响应
     */
    public void updateLoginStatusSuccess(String state, AuthenticationResponse authResponse) {
        LOGIN_STATUS_MAP.put(state, WechatLoginStatusResponse.success(authResponse));
    }
    
    /**
     * 更新登录状态为失败
     *
     * @param state 状态码
     * @param errorMessage 错误信息
     */
    public void updateLoginStatusFailed(String state, String errorMessage) {
        LOGIN_STATUS_MAP.put(state, WechatLoginStatusResponse.failed(errorMessage));
    }
}