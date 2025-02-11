package com.yuwang.usercenter.domain.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String username;

    @TableField
    private String email;

    @TableField
    private String phone;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField
    private UserStatus status = UserStatus.ACTIVE;

    @TableField("audit_status")
    private AuditStatus auditStatus = AuditStatus.PENDING;

    @TableField("membership_level")
    private MembershipLevel membershipLevel = MembershipLevel.NORMAL;

    @TableField("membership_expiry")
    private LocalDateTime membershipExpiry;

    @TableField
    private Integer points = 0;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


}