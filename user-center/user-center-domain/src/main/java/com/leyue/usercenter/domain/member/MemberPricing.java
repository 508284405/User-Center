package com.leyue.usercenter.domain.member;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberPricing {
    
    private Long id;
    private Long productId;
    private String memberLevel;
    private DiscountType discountType;
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
    
    public MemberPricing() {}
    
    public MemberPricing(Long productId, String memberLevel, DiscountType discountType, 
                        BigDecimal discountValue, String createdBy) {
        this.productId = productId;
        this.memberLevel = memberLevel;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minPurchaseQuantity = 1;
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 计算会员价格
     */
    public BigDecimal calculateMemberPrice(BigDecimal originalPrice, Integer quantity) {
        if (!isApplicable(quantity)) {
            return originalPrice;
        }
        
        switch (discountType) {
            case PERCENTAGE:
                // 百分比折扣
                BigDecimal discountRate = BigDecimal.ONE.subtract(discountValue.divide(new BigDecimal("100")));
                return originalPrice.multiply(discountRate);
                
            case FIXED_AMOUNT:
                // 固定金额减免
                BigDecimal reducedPrice = originalPrice.subtract(discountValue);
                return reducedPrice.compareTo(BigDecimal.ZERO) > 0 ? reducedPrice : BigDecimal.ZERO;
                
            case FIXED_PRICE:
                // 固定价格
                return discountValue;
                
            default:
                return originalPrice;
        }
    }
    
    /**
     * 检查是否适用
     */
    public boolean isApplicable(Integer quantity) {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查激活状态
        if (!isActive) {
            return false;
        }
        
        // 检查时间范围
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        
        if (endTime != null && now.isAfter(endTime)) {
            return false;
        }
        
        // 检查购买数量
        if (minPurchaseQuantity != null && quantity < minPurchaseQuantity) {
            return false;
        }
        
        if (maxPurchaseQuantity != null && quantity > maxPurchaseQuantity) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 激活配置
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用配置
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置有效期
     */
    public void setValidityPeriod(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        this.startTime = startTime;
        this.endTime = endTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置购买数量限制
     */
    public void setPurchaseQuantityLimit(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && minQuantity < 1) {
            throw new IllegalArgumentException("最小购买数量不能小于1");
        }
        
        if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) {
            throw new IllegalArgumentException("最小购买数量不能大于最大购买数量");
        }
        
        this.minPurchaseQuantity = minQuantity;
        this.maxPurchaseQuantity = maxQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum DiscountType {
        PERCENTAGE("百分比折扣"),
        FIXED_AMOUNT("固定金额减免"),
        FIXED_PRICE("固定价格");
        
        private final String description;
        
        DiscountType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}