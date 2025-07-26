package com.leyue.usercenter.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageQuery extends PageQuery {
    private String roleName;
    private String roleCode;
    private Integer status;
} 