package com.leyue.usercenter.domain.coupon;

/**
 * 优惠券模板状态枚举
 * 
 * @author Claude Code
 */
public enum CouponTemplateStatus {
    
    ACTIVE("激活", "可正常发放"),
    PAUSED("暂停", "暂停发放"),
    FINISHED("完成", "发放完毕");
    
    private final String description;
    private final String detail;
    
    CouponTemplateStatus(String description, String detail) {
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