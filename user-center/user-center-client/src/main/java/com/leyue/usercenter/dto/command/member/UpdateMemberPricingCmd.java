package com.leyue.usercenter.dto.command.member;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateMemberPricingCmd extends Command {
    
    @NotNull(message = "配置ID不能为空")
    private Long id;
    
    private String discountType;
    
    @Positive(message = "折扣值必须为正数")
    private BigDecimal discountValue;
    
    private Integer minPurchaseQuantity;
    
    private Integer maxPurchaseQuantity;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Boolean isActive;
    
    private String updatedBy;
}