package com.yuwang.usercenter.domain.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yuwang.usercenter.domain.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_admins")
public class UserAdmin extends BaseEntity {
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
}