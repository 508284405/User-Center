package com.leyue.usercenter.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.UserService;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.WechatLoginStatusResponse;
import com.leyue.usercenter.dto.WechatQrResponse;
import com.leyue.usercenter.dto.command.*;
import com.leyue.usercenter.dto.query.UserPageQuery;
import com.leyue.usercenter.infrastructure.config.security.oauth2.GoogleUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户相关的RESTful API，包括用户认证、注册、信息管理等功能
 *
 * @author Trae
 * @since 2024-01-20
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final GoogleUrlService googleUrlService;
    
    /**
     * 用户登录
     * 
     * @param cmd 登录命令，包含用户名和密码
     * @return 认证响应，包含JWT token和用户信息
     */
    @PostMapping("/login")
    public SingleResponse<AuthenticationResponse> login(@Valid @RequestBody UserLoginCmd cmd) {
        return SingleResponse.of(userService.login(cmd));
    }
    
    /**
     * Google账号登录
     * 
     * @param cmd Google登录命令，包含Google认证码
     * @return 认证响应，包含JWT token和用户信息
     */
    @PostMapping("/google-login")
    public SingleResponse<AuthenticationResponse> googleLogin(@Valid @RequestBody GoogleLoginCmd cmd) {
        return SingleResponse.of(userService.googleLogin(cmd));
    }
    
    /**
     * 获取Google登录URL
     * 
     * @param redirectUri 登录成功后的重定向URL
     * @return Google登录URL
     */
    @GetMapping("/google-login-url")
    public SingleResponse<Map<String, String>> getGoogleLoginUrl(@RequestParam(required = false) String redirectUri) {
        String googleLoginUrl = googleUrlService.getGoogleLoginUrl(redirectUri);
        Map<String, String> result = new HashMap<>();
        result.put("url", googleLoginUrl);
        return SingleResponse.of(result);
    }
    
    /**
     * 创建新用户
     * 
     * @param cmd 创建用户命令，包含用户基本信息
     * @return 创建的用户信息
     */
    @PostMapping
    public SingleResponse<UserDTO> createUser(@Valid @RequestBody CreateUserCmd cmd) {
        return SingleResponse.of(userService.createUser(cmd));
    }
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param cmd 更新用户命令，包含需要更新的用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}")
    public SingleResponse<UserDTO> updateUser(@PathVariable Long userId,
                                      @Valid @RequestBody UpdateUserCmd cmd) {
        cmd.setId(userId);
        return SingleResponse.of(userService.updateUser(cmd));
    }
    
    /**
     * 删除用户
     * 
     * @param userId 要删除的用户ID
     * @return 无返回内容
     */
    @DeleteMapping("/{userId}")
    public SingleResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return SingleResponse.of(null);
    }
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{userId}")
    public SingleResponse<UserDTO> getUser(@PathVariable Long userId) {
        return SingleResponse.of(userService.getUser(userId));
    }
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新的状态值
     * @return 无返回内容
     */
    @PutMapping("/{userId}/status/{status}")
    public SingleResponse<Void> updateStatus(@PathVariable Long userId,
                                     @PathVariable Integer status) {
        userService.updateStatus(userId, status);
        return SingleResponse.of(null);
    }
    
    /**
     * 分页查询用户列表
     * 
     * @param query 分页查询参数
     * @return 分页的用户列表
     */
    @PostMapping("/page")
    public PageResponse<UserDTO> pageUsers(@RequestBody @Valid UserPageQuery query) {
        return userService.pageUsers(query);
    }
    
    /**
     * 微信登录
     * 
     * @param cmd 微信登录命令，包含微信认证信息
     * @return 认证响应，包含JWT token和用户信息
     */
    @PostMapping("/wechat-login")
    public SingleResponse<AuthenticationResponse> wechatLogin(@Valid @RequestBody WechatLoginCmd cmd) {
        return SingleResponse.of(userService.wechatLogin(cmd));
    }
    
    /**
     * 生成微信扫码登录的二维码
     * 根据时序图的第1步和第2步，前端请求生成二维码，后端返回二维码信息
     * 
     * @param cmd 二维码生成命令，包含回调URL和状态码
     * @return 包含二维码URL和状态码的响应
     */
    @PostMapping("/wechat-qrcode")
    public SingleResponse<WechatQrResponse> generateWechatQrCode(@Valid @RequestBody WechatQrCodeCmd cmd) {
        return userService.generateWechatQrCode(cmd);
    }
    
    /**
     * 获取微信扫码登录状态
     * 用于前端轮询获取登录结果
     * 
     * @param state 状态码，用于关联前端请求
     * @return 登录状态响应
     */
    @GetMapping("/wechat-login-status/{state}")
    public SingleResponse<WechatLoginStatusResponse> getWechatLoginStatus(@PathVariable String state) {
        return userService.getWechatLoginStatus(state);
    }
    
    /**
     * 用户注册
     * 
     * @param cmd 注册命令，包含用户注册信息
     * @return 认证响应，包含JWT token和用户信息
     */
    @PostMapping("/register")
    public SingleResponse<AuthenticationResponse> register(@Valid @RequestBody UserRegisterCmd cmd) {
        return SingleResponse.of(userService.register(cmd));
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 当前登录用户的详细信息
     */
    @GetMapping("/current/info")
    public SingleResponse<UserDTO> getCurrentUserInfo() {
        return SingleResponse.of(userService.getCurrentUserInfo());
    }

    /**
     * 根据角色编码查询用户列表
     * 
     * @param roleCode 角色编码
     * @return 用户列表
     */
    @GetMapping("/by-role/{roleCode}")
    public MultiResponse<UserDTO> listUsersByRoleCode(@PathVariable String roleCode) {
        return MultiResponse.of(userService.listUsersByRoleCode(roleCode));
    }
}