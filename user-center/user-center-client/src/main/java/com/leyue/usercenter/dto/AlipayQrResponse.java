package com.leyue.usercenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付宝二维码响应
 * 包含二维码URL和状态码
 *
 * @author Cascade
 * @since 2025-05-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayQrResponse {
    /**
     * 授权URL，用于生成二维码
     */
    private String authUrl;

    /**
     * 状态码，用于防止CSRF攻击，同时用于前端轮询
     */
    private String state;
}
