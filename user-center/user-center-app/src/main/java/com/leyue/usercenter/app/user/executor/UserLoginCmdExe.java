package com.leyue.usercenter.app.user.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.UserLoginCmd;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLoginCmdExe {
    private final UserGateway userGateway;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;

    public AuthenticationResponse execute(UserLoginCmd cmd) {
        User user = userGateway.findByUsername(cmd.getUsername());
        if (user == null || !passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (!user.isEnabled()) {
            throw new BizException("用户已被禁用");
        }

        // 3. 将用户信息转换为 UserDTO
        UserDTO userDTO = userAssembler.toDTO(user);

        // 4. 使用 JwtService 生成 Token
        TokenDTO tokenDTO = jwtService.generateToken(userDTO);

        return AuthenticationResponse.builder().token(tokenDTO).user(userDTO).build();
    }
} 