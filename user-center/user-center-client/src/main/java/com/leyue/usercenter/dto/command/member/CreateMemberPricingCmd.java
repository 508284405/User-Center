package com.leyue.usercenter.dto.command.member;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateMemberPricingCmd extends Command {
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotBlank(message = "会员等级不能为空")
    private String memberLevel;
    
    @NotBlank(message = "折扣类型不能为空")
    private String discountType;
    
    @NotNull(message = "折扣值不能为空")
    @Positive(message = "折扣值必须为正数")
    private BigDecimal discountValue;
    
    private Integer minPurchaseQuantity;
    
    private Integer maxPurchaseQuantity;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Boolean isActive = true;
    
    @NotBlank(message = "创建者不能为空")
    private String createdBy;
}