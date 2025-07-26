package com.leyue.usercenter.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuPageQuery extends PageQuery {
    private String menuName;
    private String menuCode;
    private Integer type;
} 