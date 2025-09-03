package com.leyue.usercenter.dto.data.promotion;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FullReductionRuleDTO {
    
    private Long id;
    
    private Long activityId;
    
    private BigDecimal thresholdAmount;
    
    private String reductionType;
    
    private String reductionTypeDesc;
    
    private BigDecimal reductionValue;
    
    private BigDecimal maxReduction;
}