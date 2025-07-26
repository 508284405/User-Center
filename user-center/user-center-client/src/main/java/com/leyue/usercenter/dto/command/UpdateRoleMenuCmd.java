package com.leyue.usercenter.dto.command;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleMenuCmd {
    private List<Long> menuIds;
} 