package com.yuwang.usercenter.domain.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("roles")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("role_name")
    private String roleName;

    @TableField
    private String description;

    @TableField
    private Integer level = 0;  // 权限等级，0: 普通管理员，1: 超级管理员

    @TableField("system_id")
    private Long systemId;  // 关联的系统ID

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


}