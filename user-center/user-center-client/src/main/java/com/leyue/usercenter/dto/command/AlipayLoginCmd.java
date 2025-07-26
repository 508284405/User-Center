package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付宝登录命令
 * 用于通过授权码进行支付宝登录
 *
 * @author Cascade
 * @since 2025-05-06
 */
@Data
public class AlipayLoginCmd {
    /**
     * 支付宝授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String authCode;
}
