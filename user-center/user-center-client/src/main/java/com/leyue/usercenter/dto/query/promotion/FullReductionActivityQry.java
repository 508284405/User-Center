package com.leyue.usercenter.dto.query.promotion;

import com.alibaba.cola.dto.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FullReductionActivityQry extends Query {
    
    private Long id;
    
    private String name;
}