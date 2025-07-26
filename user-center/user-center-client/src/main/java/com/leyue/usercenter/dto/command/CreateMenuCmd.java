package com.leyue.usercenter.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateMenuCmd {
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotBlank(message = "菜单编码不能为空")
    private String menuCode;

    private String path;
    private String component;
    /**
     * 1-目录 2-菜单 3-按钮
     */
    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    /**
     * 0-客户端菜单 1-运营端菜单
     */
    @NotNull(message = "菜单客户端类型不能为空")
    private Integer menuType;

    private Long parentId;
    private Integer sort;
    private String icon;
    private List<String> permissions;
} 