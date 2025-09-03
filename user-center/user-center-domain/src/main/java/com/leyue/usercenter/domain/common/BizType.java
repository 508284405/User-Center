package com.leyue.usercenter.domain.common;

/**
 * 业务类型枚举
 * 
 * @author Claude Code
 */
public enum BizType {
    
    ORDER("订单"),
    PAYMENT("支付"),
    REFUND("退款"),
    PROMOTION("促销活动"),
    MANUAL("手动操作"),
    LEVEL_UPGRADE("等级升级"),
    BENEFIT_GRANT("权益发放"),
    SYSTEM("系统操作");
    
    private final String description;
    
    BizType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}