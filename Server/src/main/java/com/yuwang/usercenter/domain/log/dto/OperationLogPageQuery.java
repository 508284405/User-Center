package com.yuwang.usercenter.domain.log.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogPageQuery extends BasePageQuery {
    private Long userId;
    private String username;
    private String operationType;
    private String module;
    private String description;
    private String ipAddress;
}