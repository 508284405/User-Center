package com.yuwang.usercenter.domain.menu.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
public class MenuPageQuery extends BasePageQuery {
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{0,50}$", message = "菜单编码只能包含字母、数字、下划线和连字符，长度不超过50")
    private String menuCode;

    private Long systemId;
    private Long parentId;
}