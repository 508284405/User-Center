package com.yuwang.usercenter.domain;

import lombok.Data;

@Data
public class BasePageQuery {
    private long pageNum = 1;
    private long pageSize = 10;
}