package com.leyue.usercenter.infrastructure.menu.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("menu")
public class MenuDO extends BaseDO {
    private String menuName;
    private String menuCode;
    private String path;
    private String component;
    private Integer type;
    private Long parentId;
    private Integer sort;
    private String icon;
} 