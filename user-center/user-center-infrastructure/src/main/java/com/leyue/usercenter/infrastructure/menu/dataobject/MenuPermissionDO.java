package com.leyue.usercenter.infrastructure.menu.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("menu_permission")
public class MenuPermissionDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long menuId;
    private String permission;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
} 