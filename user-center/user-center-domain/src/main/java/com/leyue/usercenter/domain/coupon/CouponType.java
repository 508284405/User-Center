package com.leyue.usercenter.domain.coupon;

/**
 * 优惠券类型枚举
 * 
 * @author Claude Code
 */
public enum CouponType {
    
    CASH("现金券", "固定金额减免"),
    DISCOUNT("折扣券", "按比例打折"),
    FREEBIE("赠品券", "免费商品赠送");
    
    private final String description;
    private final String detail;
    
    CouponType(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDetail() {
        return detail;
    }
}