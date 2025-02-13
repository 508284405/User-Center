package com.yuwang.usercenter.domain.user.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;

@Data
public class UserPageQuery extends BasePageQuery {
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{0,30}$", message = "用户名只能包含字母、数字、下划线和连字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^[0-1]$", message = "状态值只能为0或1")
    private Integer status;
}