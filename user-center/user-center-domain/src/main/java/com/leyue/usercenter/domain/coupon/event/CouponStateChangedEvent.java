package com.leyue.usercenter.domain.coupon.event;

import com.leyue.usercenter.domain.common.DomainEvent;
import com.leyue.usercenter.domain.coupon.CouponState;
import lombok.Getter;

import java.util.UUID;

/**
 * 优惠券状态变更事件
 * 
 * @author Claude Code
 */
@Getter
public class CouponStateChangedEvent extends DomainEvent {
    
    private final String couponCode;
    private final String tplCode;
    private final Long userId;
    private final CouponState fromState;
    private final CouponState toState;
    private final String reason;
    
    public CouponStateChangedEvent(String couponCode, String tplCode, Long userId,
                                  CouponState fromState, CouponState toState, String reason) {
        super(UUID.randomUUID().toString());
        this.couponCode = couponCode;
        this.tplCode = tplCode;
        this.userId = userId;
        this.fromState = fromState;
        this.toState = toState;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "coupon.state.changed";
    }
    
    @Override
    public String toString() {
        return "CouponStateChangedEvent{" +
               "eventId='" + getEventId() + '\'' +
               ", couponCode='" + couponCode + '\'' +
               ", userId=" + userId +
               ", fromState=" + fromState +
               ", toState=" + toState +
               ", reason='" + reason + '\'' +
               ", occurredAt=" + getOccurredAt() +
               '}';
    }
}