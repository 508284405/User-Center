package com.leyue.usercenter.dto.query.member;

import com.alibaba.cola.dto.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberPricingQry extends Query {
    
    private Long id;
    
    private Long productId;
    
    private String memberLevel;
}