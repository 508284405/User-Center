package com.leyue.usercenter.dto.query.member;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberPricingPageQry extends PageQuery {
    
    private Long productId;
    
    private String memberLevel;
    
    private Boolean isActive;
}