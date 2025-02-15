package com.yuwang.usercenter.domain.user.dto;

import com.yuwang.usercenter.domain.user.entity.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Size(max = 200, message = "头像URL长度不能超过200个字符")
    private String avatar;

    private Boolean enabled;

    private UserStatus status = UserStatus.ACTIVE;
}