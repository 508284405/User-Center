package com.leyue.usercenter.domain.benefit;

/**
 * 权益周期枚举
 * 
 * @author Claude Code
 */
public enum BenefitPeriod {
    
    INFINITE("永久", "权益永久有效"),
    DAILY("每日", "每日可使用的权益"),
    WEEKLY("每周", "每周可使用的权益"),
    MONTHLY("每月", "每月可使用的权益"),
    YEARLY("每年", "每年可使用的权益");
    
    private final String description;
    private final String detail;
    
    BenefitPeriod(String description, String detail) {
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