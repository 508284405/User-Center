package com.yuwang.usercenter.domain.menu.dto;

import com.yuwang.usercenter.domain.menu.entity.MenuType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuCreateRequest {
    @NotNull(message = "系统ID不能为空")
    private Long systemId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    @NotBlank(message = "菜单编码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$", message = "菜单编码只能包含字母、数字、下划线和连字符，长度不超过50")
    private String menuCode;

    @Size(max = 200, message = "菜单URL长度不能超过200个字符")
    private String menuUrl;

    private Long parentId;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 999, message = "排序值不能大于999")
    private Integer sort = 0;

    @Size(max = 50, message = "图标长度不能超过50个字符")
    private String icon;

    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permission;

    @NotNull(message = "菜单类型不能为空")
    private MenuType menuType;

    private Boolean visible = true;
}