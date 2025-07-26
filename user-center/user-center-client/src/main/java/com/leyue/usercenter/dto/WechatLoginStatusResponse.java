package com.leyue.usercenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信登录状态响应
 * 用于前端轮询获取微信扫码登录的状态
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginStatusResponse {
    /**
     * 登录状态
     * pending: 等待扫码
     * success: 登录成功
     * failed: 登录失败
     * expired: 二维码已过期
     */
    private String status;

    /**
     * 认证响应，登录成功时返回
     */
    private AuthenticationResponse authResponse;

    /**
     * 错误信息，登录失败时返回
     */
    private String errorMessage;

    /**
     * 创建等待状态的响应
     */
    public static WechatLoginStatusResponse pending() {
        return new WechatLoginStatusResponse("pending", null, null);
    }

    /**
     * 创建成功状态的响应
     */
    public static WechatLoginStatusResponse success(AuthenticationResponse authResponse) {
        return new WechatLoginStatusResponse("success", authResponse, null);
    }

    /**
     * 创建失败状态的响应
     */
    public static WechatLoginStatusResponse failed(String errorMessage) {
        return new WechatLoginStatusResponse("failed", null, errorMessage);
    }

    /**
     * 创建过期状态的响应
     */
    public static WechatLoginStatusResponse expired() {
        return new WechatLoginStatusResponse("expired", null, "二维码已过期");
    }
}