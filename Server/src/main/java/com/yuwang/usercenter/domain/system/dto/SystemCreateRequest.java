package com.yuwang.usercenter.domain.system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SystemCreateRequest {
    @NotBlank(message = "系统名称不能为空")
    @Size(max = 50, message = "系统名称长度不能超过50个字符")
    private String systemName;

    @NotBlank(message = "系统编码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$", message = "系统编码只能包含字母、数字、下划线和连字符，长度不超过50")
    private String systemCode;

    @Size(max = 200, message = "系统描述长度不能超过200个字符")
    private String description;

    @Size(max = 200, message = "系统访问地址长度不能超过200个字符")
    private String accessUrl;

    private Boolean enabled = true;
}