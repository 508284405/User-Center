package com.leyue.usercenter.dto.command;

import lombok.Data;

/**
 * 微信二维码生成命令
 * 用于请求生成微信扫码登录的二维码
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
public class WechatQrCodeCmd {
    /**
     * 回调URL，用于接收微信服务器的回调
     */
    private String redirectUrl;

    /**
     * 状态码，用于防止CSRF攻击
     */
    private String state;
}