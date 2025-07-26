package com.leyue.usercenter.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RoleDTO {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private List<MenuDTO> menus;
} 