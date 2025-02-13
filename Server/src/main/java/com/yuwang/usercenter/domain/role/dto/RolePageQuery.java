package com.yuwang.usercenter.domain.role.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RolePageQuery extends BasePageQuery {
    private String roleName;
    private String roleCode;
    private Long systemId;
    private Integer level;
    private Integer status;
}