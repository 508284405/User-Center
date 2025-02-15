package com.yuwang.usercenter.domain.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yuwang.usercenter.domain.BaseEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("roles")
public class Role extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("role_name")
    private String roleName;

    private Integer status;

    @TableField
    private String description;

    @TableField
    private Integer level = 0;  // 权限等级，0: 普通管理员，1: 超级管理员

    @TableField("system_id")
    private Long systemId;  // 关联的系统ID
}