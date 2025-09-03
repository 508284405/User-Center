package com.leyue.usercenter.domain.coupon;

import com.leyue.usercenter.domain.common.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 优惠券模板聚合根
 * 
 * @author Claude Code
 */
@Getter
public class CouponTemplate {
    
    private final Long id;
    private final String tplCode;
    private final String name;
    private final CouponType type;
    private final Money faceValue;
    private final BigDecimal discountRate;
    private final Money thresholdAmount;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final LocalDateTime issueStart;
    private final LocalDateTime issueEnd;
    private final Integer total;
    private Integer issued;
    private final String constraintsJson;
    private CouponTemplateStatus status;
    private LocalDateTime updatedAt;
    
    public CouponTemplate(Long id, String tplCode, String name, CouponType type,
                         Money faceValue, BigDecimal discountRate, Money thresholdAmount,
                         LocalDateTime validFrom, LocalDateTime validTo,
                         LocalDateTime issueStart, LocalDateTime issueEnd,
                         Integer total, Integer issued, String constraintsJson,
                         CouponTemplateStatus status, LocalDateTime updatedAt) {
        this.id = id;
        this.tplCode = Objects.requireNonNull(tplCode, "Template code cannot be null");
        this.name = Objects.requireNonNull(name, "Template name cannot be null");
        this.type = Objects.requireNonNull(type, "Coupon type cannot be null");
        this.faceValue = faceValue;
        this.discountRate = discountRate;
        this.thresholdAmount = thresholdAmount;
        this.validFrom = Objects.requireNonNull(validFrom, "Valid from cannot be null");
        this.validTo = Objects.requireNonNull(validTo, "Valid to cannot be null");
        this.issueStart = Objects.requireNonNull(issueStart, "Issue start cannot be null");
        this.issueEnd = Objects.requireNonNull(issueEnd, "Issue end cannot be null");
        this.total = Objects.requireNonNull(total, "Total cannot be null");
        this.issued = issued != null ? issued : 0;
        this.constraintsJson = constraintsJson;
        this.status = status != null ? status : CouponTemplateStatus.ACTIVE;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        
        validateTemplate();
    }
    
    /**
     * 创建优惠券模板
     */
    public static CouponTemplate create(String tplCode, String name, CouponType type,
                                       Money faceValue, BigDecimal discountRate, Money thresholdAmount,
                                       LocalDateTime validFrom, LocalDateTime validTo,
                                       LocalDateTime issueStart, LocalDateTime issueEnd,
                                       Integer total, String constraintsJson) {
        return new CouponTemplate(null, tplCode, name, type, faceValue, discountRate, thresholdAmount,
                                 validFrom, validTo, issueStart, issueEnd, total, 0, 
                                 constraintsJson, CouponTemplateStatus.ACTIVE, null);
    }
    
    /**
     * 检查是否可以发放
     */
    public boolean canIssue(int quantity) {
        if (status != CouponTemplateStatus.ACTIVE) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(issueStart) || now.isAfter(issueEnd)) {
            return false;
        }
        
        return issued + quantity <= total;
    }
    
    /**
     * 发放优惠券
     */
    public void issueCoupons(int quantity) {
        if (!canIssue(quantity)) {
            throw new IllegalStateException("Cannot issue coupons: template constraints not met");
        }
        
        this.issued += quantity;
        this.updatedAt = LocalDateTime.now();
        
        // 检查是否发放完毕
        if (this.issued >= this.total) {
            this.status = CouponTemplateStatus.FINISHED;
        }
    }
    
    /**
     * 暂停发放
     */
    public void pause() {
        if (status == CouponTemplateStatus.ACTIVE) {
            this.status = CouponTemplateStatus.PAUSED;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 恢复发放
     */
    public void resume() {
        if (status == CouponTemplateStatus.PAUSED) {
            this.status = CouponTemplateStatus.ACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 获取剩余发放数量
     */
    public int getRemainingQuantity() {
        return total - issued;
    }
    
    /**
     * 检查是否在发放期内
     */
    public boolean isInIssueRange() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(issueStart) && !now.isAfter(issueEnd);
    }
    
    /**
     * 检查是否在有效期内
     */
    public boolean isInValidRange() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(validFrom) && !now.isAfter(validTo);
    }
    
    private void validateTemplate() {
        if (validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("Valid from must be before valid to");
        }
        if (issueStart.isAfter(issueEnd)) {
            throw new IllegalArgumentException("Issue start must be before issue end");
        }
        if (total <= 0) {
            throw new IllegalArgumentException("Total quantity must be positive");
        }
        if (type == CouponType.CASH && (faceValue == null || !faceValue.isPositive())) {
            throw new IllegalArgumentException("Cash coupon must have positive face value");
        }
        if (type == CouponType.DISCOUNT && (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) <= 0 || discountRate.compareTo(BigDecimal.ONE) >= 0)) {
            throw new IllegalArgumentException("Discount coupon must have rate between 0 and 1");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CouponTemplate template = (CouponTemplate) obj;
        return Objects.equals(tplCode, template.tplCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tplCode);
    }
}