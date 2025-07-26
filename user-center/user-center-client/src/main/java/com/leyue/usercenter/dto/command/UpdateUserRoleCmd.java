package com.leyue.usercenter.dto.command;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRoleCmd {
    private Long userId;
    private List<Long> roleIds;
} 