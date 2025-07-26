package com.leyue.usercenter.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.WechatLoginStatusResponse;
import com.leyue.usercenter.dto.WechatQrResponse;
import com.leyue.usercenter.dto.command.*;
import com.leyue.usercenter.dto.query.UserPageQuery;

import java.util.List;

public interface UserService {
    AuthenticationResponse login(UserLoginCmd cmd);

    AuthenticationResponse googleLogin(GoogleLoginCmd cmd);

    AuthenticationResponse wechatLogin(WechatLoginCmd cmd);

    AuthenticationResponse register(UserRegisterCmd cmd);

    UserDTO createUser(CreateUserCmd cmd);

    UserDTO updateUser(UpdateUserCmd cmd);

    void deleteUser(Long userId);

    UserDTO getUser(Long userId);

    void updateStatus(Long userId, Integer status);

    PageResponse<UserDTO> pageUsers(UserPageQuery query);

    UserDTO getCurrentUserInfo();

    /**
     * 根据角色编码查询用户列表
     *
     * @param roleCode 角色编码
     * @return 用户列表
     */
    List<UserDTO> listUsersByRoleCode(String roleCode);

    /**
     * 生成微信扫码登录的二维码
     *
     * @param cmd 二维码生成命令
     * @return 包含二维码URL和状态码的响应
     */
    SingleResponse<WechatQrResponse> generateWechatQrCode(WechatQrCodeCmd cmd);

    /**
     * 处理微信扫码回调
     *
     * @param code  微信授权码
     * @param state 状态码，用于关联前端请求
     * @return 处理结果
     */
    boolean handleWechatCallback(String code, String state);

    /**
     * 获取微信扫码登录状态
     * 用于前端轮询获取登录结果
     *
     * @param state 状态码，用于关联前端请求
     * @return 登录状态响应
     */
    SingleResponse<WechatLoginStatusResponse> getWechatLoginStatus(String state);
}