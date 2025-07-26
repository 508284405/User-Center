package com.leyue.usercenter.app.user.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.WechatQrResponse;
import com.leyue.usercenter.dto.command.WechatQrCodeCmd;
import com.leyue.usercenter.infrastructure.wechat.WechatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 微信二维码生成命令执行器
 * 用于生成微信扫码登录的二维码
 *
 * @author Trae
 * @since 2024-01-20
 */
@Component
@RequiredArgsConstructor
public class WechatQrCodeCmdExe {
    private final WechatClient wechatClient;

    /**
     * 执行微信二维码生成命令
     *
     * @param cmd 二维码生成命令
     * @return 包含二维码URL和状态码的响应
     */
    public SingleResponse<WechatQrResponse> execute(WechatQrCodeCmd cmd) {
        // 如果没有提供状态码，则生成一个随机状态码
        String state = cmd.getState();
        if (state == null || state.isEmpty()) {
            state = UUID.randomUUID().toString();
        }

        // 生成二维码URL
        String qrCodeUrl = wechatClient.generateQrCodeUrl(cmd.getRedirectUrl(), state);

        // 构建响应
        WechatQrResponse response = new WechatQrResponse(qrCodeUrl, state);
        return SingleResponse.of(response);
    }
}