package com.leyue.usercenter.dto.command;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleCmd {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private List<Long> menuIds;
} 