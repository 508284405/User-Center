package com.leyue.usercenter.dto.data.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FullReductionCalculationDTO {
    
    private Boolean canUse;
    
    private BigDecimal originalAmount;
    
    private BigDecimal reductionAmount;
    
    private BigDecimal finalAmount;
    
    private String reductionType;
    
    private String reductionDesc;
    
    private List<FullReductionActivityDTO> usedActivities;
    
    private String reason;
}