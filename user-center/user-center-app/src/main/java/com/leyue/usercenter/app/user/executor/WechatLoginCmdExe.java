package com.leyue.usercenter.app.user.executor;

import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.WechatLoginCmd;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import com.leyue.usercenter.infrastructure.wechat.WechatClient;
import com.leyue.usercenter.infrastructure.wechat.WechatUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WechatLoginCmdExe {
    private final WechatClient wechatClient;
    private final UserGateway userGateway;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;

    public AuthenticationResponse execute(WechatLoginCmd cmd) {
        // 1. 获取微信用户信息
        WechatUserInfo wechatUser = wechatClient.getWechatUserInfo(cmd.getCode());

        // 2. 查找或创建用户
        User user = userGateway.findByWechatOpenId(wechatUser.getOpenId());
        if (user == null) {
            user = new User();
            user.setStatus(1);
            user.updateWechatInfo(wechatUser.getOpenId(),
                    wechatUser.getNickname(),
                    wechatUser.getAvatar());
            userGateway.save(user);
        }

        // 3. 将用户信息转换为 UserDTO
        UserDTO userDTO = userAssembler.toDTO(user);

        // 4. 使用 JwtService 生成 Token
        TokenDTO tokenDTO = jwtService.generateToken(userDTO);

        // 5. 构建并返回认证响应
        return AuthenticationResponse.builder()
                .user(userDTO)
                .token(tokenDTO)
                .build();
    }
}