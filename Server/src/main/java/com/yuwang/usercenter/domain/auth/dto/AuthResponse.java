package com.yuwang.usercenter.domain.auth.dto;

import com.yuwang.usercenter.domain.user.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private User user;

    public static AuthResponse of(String token, User user) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(user);
        return response;
    }
}