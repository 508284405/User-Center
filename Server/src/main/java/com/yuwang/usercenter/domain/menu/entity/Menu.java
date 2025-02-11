package com.yuwang.usercenter.domain.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("menu_permissions")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("system_id")
    private Long systemId;  // 关联的系统ID

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_url")
    private String menuUrl;

    @TableField("parent_id")
    private Long parentId;  // 父菜单ID

    @TableField
    private Integer sort = 0;  // 排序

    @TableField
    private String icon;  // 菜单图标

    @TableField
    private String permission;  // 权限标识

    @TableField("menu_type")
    private MenuType menuType = MenuType.MENU;  // 菜单类型

    @TableField
    private Boolean visible = true;  // 是否可见

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


}