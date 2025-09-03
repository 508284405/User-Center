package com.leyue.usercenter.dto.command.promotion;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FullReductionRuleCmd {
    
    private BigDecimal thresholdAmount;
    
    private String reductionType;
    
    private BigDecimal reductionValue;
    
    private BigDecimal maxReduction;
}