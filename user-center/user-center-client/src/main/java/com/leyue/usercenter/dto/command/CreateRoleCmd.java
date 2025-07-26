package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoleCmd {
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;
    private List<Long> menuIds;
    private Long systemId;
    private Integer level;
} 