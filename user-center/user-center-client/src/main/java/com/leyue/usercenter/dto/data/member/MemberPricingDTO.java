package com.leyue.usercenter.dto.data.member;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberPricingDTO {
    
    private Long id;
    
    private Long productId;
    
    private String memberLevel;
    
    private String discountType;
    
    private String discountTypeDesc;
    
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