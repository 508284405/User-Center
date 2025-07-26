package com.leyue.usercenter.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuDTO {
    private Long id;
    private String menuName;
    private String menuCode;
    private String path;
    private String component;
    private Integer type;
    private Long parentId;
    private Integer sort;
    private String icon;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> permissions;
    private List<MenuDTO> children;
} 