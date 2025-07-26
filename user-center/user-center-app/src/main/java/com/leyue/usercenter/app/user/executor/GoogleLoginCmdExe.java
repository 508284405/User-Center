package com.leyue.usercenter.app.user.executor;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.alibaba.cola.exception.BizException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.GoogleLoginCmd;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleLoginCmdExe {
    private final UserGateway userGateway;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;

    private String clientId = "1231";

    public AuthenticationResponse execute(GoogleLoginCmd cmd) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(cmd.getAuthCode());
            if (idToken == null) {
                throw new BizException("Invalid ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();

            // 查找或创建用户
            User user = userGateway.findByGoogleId(googleId);
            if (user == null) {
                user = new User();
                user.setUsername(email);
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setStatus(1);
                userGateway.save(user);
            }
            // 3. 将用户信息转换为 UserDTO
            UserDTO userDTO = userAssembler.toDTO(user);

            // 4. 使用 JwtService 生成 Token
            TokenDTO tokenDTO = jwtService.generateToken(userDTO);

            return AuthenticationResponse.builder().token(tokenDTO).user(userDTO).build();

        } catch (Exception e) {
            throw new BizException("Google登录失败: " + e.getMessage());
        }
    }
}