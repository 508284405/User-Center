package com.yuwang.usercenter.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    @TableField(value = "created_by")
    private String createdBy;  // 创建人

    @TableField(value = "updated_by")
    private String updatedBy;  // 更新人

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;  // 创建时间

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;  // 更新时间
}