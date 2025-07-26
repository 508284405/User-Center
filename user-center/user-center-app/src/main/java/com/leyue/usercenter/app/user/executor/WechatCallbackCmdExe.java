package com.leyue.usercenter.app.user.executor;

import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import com.leyue.usercenter.infrastructure.wechat.WechatClient;
import com.leyue.usercenter.infrastructure.wechat.WechatUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 微信回调命令执行器
 * 用于处理微信扫码登录的回调
 *
 * @author Trae
 * @since 2024-01-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatCallbackCmdExe {
    private final WechatClient wechatClient;
    private final UserGateway userGateway;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;

    /**
     * 执行微信回调处理
     *
     * @param code  微信授权码
     * @param state 状态码
     * @return 处理结果
     */
    public boolean execute(String code, String state) {
        try {
            // 1. 验证状态码是否有效
            if (!wechatClient.validateState(state)) {
                log.error("无效的状态码: {}", state);
                return false;
            }

            // 2. 获取微信用户信息
            WechatUserInfo wechatUser = wechatClient.getWechatUserInfo(code);

            // 3. 查找或创建用户
            User user = userGateway.findByWechatOpenId(wechatUser.getOpenId());
            if (user == null) {
                user = new User();
                user.setStatus(1);
                user.updateWechatInfo(wechatUser.getOpenId(),
                        wechatUser.getNickname(),
                        wechatUser.getAvatar());
                userGateway.save(user);
            }

            // 4. 将用户信息转换为 UserDTO
            UserDTO userDTO = userAssembler.toDTO(user);

            // 5. 使用 JwtService 生成 Token
            TokenDTO tokenDTO = jwtService.generateToken(userDTO);

            // 6. 构建认证响应
            AuthenticationResponse authResponse = AuthenticationResponse.builder()
                    .user(userDTO)
                    .token(tokenDTO)
                    .build();

            // 7. 更新登录状态为成功
            wechatClient.updateLoginStatusSuccess(state, authResponse);

            return true;
        } catch (Exception e) {
            log.error("处理微信回调失败", e);
            // 更新登录状态为失败
            wechatClient.updateLoginStatusFailed(state, e.getMessage());
            return false;
        }
    }
}