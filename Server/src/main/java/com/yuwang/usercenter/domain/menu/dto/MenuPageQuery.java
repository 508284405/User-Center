package com.yuwang.usercenter.domain.menu.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MenuPageQuery extends BasePageQuery {
    private String menuName;
    private String menuCode;
    private Long systemId;
    private Long parentId;
}