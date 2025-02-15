package com.yuwang.usercenter.domain.role.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$", message = "角色编码只能包含字母、数字、下划线和连字符，长度不超过50")
    private String roleCode;

    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    private String description;

    @Min(value = 0, message = "角色级别不能小于0")
    @Max(value = 999, message = "角色级别不能大于999")
    private Integer level;

    private Boolean enabled;
}