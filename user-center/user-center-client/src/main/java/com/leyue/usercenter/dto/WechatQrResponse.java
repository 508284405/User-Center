package com.leyue.usercenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信二维码响应
 * 包含生成的二维码URL和状态码
 *
 * @author Trae
 * @since 2024-01-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatQrResponse {
    /**
     * 二维码URL
     */
    private String qrCodeUrl;

    /**
     * 状态码，用于前端轮询或WebSocket通知时识别会话
     */
    private String state;
}