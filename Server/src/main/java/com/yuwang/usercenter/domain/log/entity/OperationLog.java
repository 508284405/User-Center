package com.yuwang.usercenter.domain.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_logs")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;  // 操作用户ID

    @TableField("username")
    private String username;  // 操作用户名

    @TableField("operation_type")
    private String operationType;  // 操作类型（CREATE, UPDATE, DELETE）

    @TableField("module")
    private String module;  // 操作模块（USER, ROLE, MENU, SYSTEM）

    @TableField("target_id")
    private Long targetId;  // 操作目标ID

    @TableField("description")
    private String description;  // 操作描述

    @TableField("ip_address")
    private String ipAddress;  // 操作者IP地址

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;  // 操作时间
}