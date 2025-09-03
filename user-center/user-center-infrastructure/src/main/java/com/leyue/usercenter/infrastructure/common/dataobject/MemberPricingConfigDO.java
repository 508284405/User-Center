package com.leyue.usercenter.infrastructure.common.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.usercenter.domain.member.MemberPricing;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_pricing_configs")
public class MemberPricingConfigDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private String memberLevel;
    
    private MemberPricing.DiscountType discountType;
    
    private BigDecimal discountValue;
    
    private Integer minPurchaseQuantity;
    
    private Integer maxPurchaseQuantity;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Boolean isActive;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private String updatedBy;
    
    private LocalDateTime updatedAt;
}