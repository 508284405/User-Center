package com.yuwang.usercenter.domain;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class BasePageQuery {
    @Min(value = 1, message = "页码必须大于等于1")
    private long pageNum = 1;
    
    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private long pageSize = 10;
}