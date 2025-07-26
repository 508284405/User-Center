package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginCmd {
    @NotBlank(message = "授权码不能为空")
    private String authCode;
} 