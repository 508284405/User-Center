package com.leyue.usercenter.web;

import com.leyue.usercenter.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信回调控制器
 * 处理微信扫码登录的回调请求
 *
 * @author Trae
 * @since 2024-01-20
 */
@RestController
@RequestMapping("/api/wechat")
@RequiredArgsConstructor
public class WechatCallbackController {
    private final UserService userService;

    /**
     * 处理微信扫码登录回调
     * 接收微信服务器的回调请求，获取授权码并处理登录逻辑
     *
     * @param code 微信授权码
     * @param state 状态码，用于关联前端请求
     * @return 回调结果页面
     */
    @GetMapping("/callback")
    public String handleCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        // 处理微信回调，获取用户信息并更新登录状态
        boolean success = userService.handleWechatCallback(code, state);
        return success ? "success" : "failure";
    }
}