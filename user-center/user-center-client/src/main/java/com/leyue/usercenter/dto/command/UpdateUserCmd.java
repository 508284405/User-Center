package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserCmd {
    private Long id;
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;
    private Integer status;
    private List<Long> roleIds;
} 