package com.yuwang.usercenter.domain.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "systems")
public class System {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("system_name")
    private String systemName;

    @TableField("system_code")
    private String systemCode;

    @TableField("owner_name")
    private String ownerName;  // 责任人

    @TableField("owner_email")
    private String ownerEmail;

    @TableField("owner_phone")
    private String ownerPhone;

    @TableField("api_key")
    private String apiKey;  // 系统接入密钥

    @TableField("api_secret")
    private String apiSecret;

    @TableField
    private String description;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}