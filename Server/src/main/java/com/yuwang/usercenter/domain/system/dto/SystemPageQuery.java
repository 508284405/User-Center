package com.yuwang.usercenter.domain.system.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;

@Data
public class SystemPageQuery extends BasePageQuery {
    private String systemCode;
    private String systemName;
}