package com.leyue.usercenter.dto.command.promotion;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateFullReductionActivityCmd extends Command {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String applicableProducts;
    
    private String applicableCategories;
    
    private String excludeProducts;
    
    private Integer totalUsageLimit;
    
    private Integer userUsageLimit;
    
    private BigDecimal minOrderAmount;
    
    private Integer priority;
    
    private Boolean canCombineWithCoupon;
    
    private Boolean canCombineWithMemberDiscount;
    
    private String updatedBy;
    
    private List<FullReductionRuleCmd> rules;
}