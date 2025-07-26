package com.leyue.usercenter.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.AlipayQrResponse;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.command.AlipayLoginCmd;
import com.leyue.usercenter.dto.command.AlipayQrCodeCmd;
import com.leyue.usercenter.infrastructure.alipay.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

/**
 * 支付宝控制器
 * 处理支付宝登录相关接口
 *
 * @author Cascade
 * @since 2025-05-06
 */
@RestController
@RequestMapping("/api/alipay")
@RequiredArgsConstructor
@Slf4j
public class AlipayController {
    private final AlipayService alipayService;

    /**
     * 生成支付宝登录二维码链接
     *
     * @param cmd 支付宝二维码生成命令
     * @return 包含授权URL和状态码的响应
     */
    @PostMapping("/qrcode")
    public SingleResponse<AlipayQrResponse> generateAlipayQrCode(@Valid @RequestBody AlipayQrCodeCmd cmd) {
        String state = cmd.getState();
        String authUrl = alipayService.generateAuthUrl(state, cmd.getRedirectUri());

        AlipayQrResponse response = AlipayQrResponse.builder()
                .authUrl(authUrl)
                .state(state)
                .build();

        return SingleResponse.of(response);
    }

    /**
     * 支付宝授权回调接口
     * 处理支付宝重定向，获取授权码
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param authCode 授权码
     * @param state    状态码
     * @throws IOException 如果重定向失败
     */
    @GetMapping("/callback")
    public void alipayCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("auth_code") String authCode,
            @RequestParam("state") String state) throws IOException {

        try {
            // 处理支付宝授权
            AuthenticationResponse authResponse = alipayService.handleAlipayLogin(authCode);

            // 构建重定向URL（可以根据state参数判断前端来源）
            String redirectUrl = alipayService.determineRedirectUrl(state);
            redirectUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("token", authResponse.getToken().getAccessToken())
                    .build().toUriString();

            // 重定向到前端页面
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("支付宝授权回调处理失败", e);
            response.sendRedirect("/oauth2/error?error=alipay_login_failed");
        }
    }

    /**
     * 直接使用授权码登录
     * 提供给前端，如果授权码是前端获取的
     *
     * @param cmd 支付宝登录命令
     * @return 认证响应
     */
    @PostMapping("/login")
    public SingleResponse<AuthenticationResponse> alipayLogin(@Valid @RequestBody AlipayLoginCmd cmd) {
        return SingleResponse.of(alipayService.handleAlipayLogin(cmd.getAuthCode()));
    }
}
