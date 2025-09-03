package com.leyue.usercenter.dto.data.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FullReductionActivityDTO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String status;
    
    private String statusDesc;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String applicableProducts;
    
    private String applicableCategories;
    
    private String excludeProducts;
    
    private Integer totalUsageLimit;
    
    private Integer currentUsage;
    
    private Integer userUsageLimit;
    
    private BigDecimal minOrderAmount;
    
    private Integer priority;
    
    private Boolean canCombineWithCoupon;
    
    private Boolean canCombineWithMemberDiscount;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private String updatedBy;
    
    private LocalDateTime updatedAt;
    
    private List<FullReductionRuleDTO> rules;
}