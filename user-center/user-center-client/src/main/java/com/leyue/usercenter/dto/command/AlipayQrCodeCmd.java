package com.leyue.usercenter.dto.command;

import lombok.Data;

/**
 * 支付宝二维码生成命令
 * 用于生成支付宝登录二维码
 *
 * @author Cascade
 * @since 2025-05-06
 */
@Data
public class AlipayQrCodeCmd {
    /**
     * 重定向URI，登录成功后跳转的地址
     */
    private String redirectUri;

    private String state;
}
