package com.leyue.usercenter.dto.command;

import lombok.Data;

import java.util.List;

@Data
public class UpdateMenuCmd {
    private Long menuId;
    private String menuName;
    private String menuCode;
    private String path;
    private String component;
    private Integer type;
    private Long parentId;
    private Integer sort;
    private String icon;
    private List<String> permissions;
} 