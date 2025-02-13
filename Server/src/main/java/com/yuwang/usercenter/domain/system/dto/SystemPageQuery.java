package com.yuwang.usercenter.domain.system.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
public class SystemPageQuery extends BasePageQuery {
    @Pattern(regexp = "^[a-zA-Z0-9_-]{0,30}$", message = "系统编码只能包含字母、数字、下划线和连字符，长度不超过30")
    private String systemCode;

    @Size(max = 50, message = "系统名称长度不能超过50个字符")
    private String systemName;
}