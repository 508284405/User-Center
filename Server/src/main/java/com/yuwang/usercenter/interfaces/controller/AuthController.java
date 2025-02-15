package com.yuwang.usercenter.interfaces.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // 登录处理已由Spring Security接管，无需额外的控制器方法
}
