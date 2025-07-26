package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginCmd {
    @NotBlank(message = "微信code不能为空")
    private String code;
} 