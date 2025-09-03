package com.leyue.usercenter.domain.coupon;

import com.leyue.usercenter.domain.common.BizType;
import com.leyue.usercenter.domain.common.DomainEvent;
import com.leyue.usercenter.domain.common.IdempotencyKey;
import com.leyue.usercenter.domain.coupon.event.CouponStateChangedEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券聚合根
 * 
 * @author Claude Code
 */
@Getter
public class Coupon {
    
    private final Long id;
    private final String tplCode;
    private final String couponCode;
    private final Long userId;
    private CouponState state;
    private LocalDateTime lockedUntil;
    private final LocalDateTime assignedAt;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private LocalDateTime updatedAt;
    
    // 领域事件
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    public Coupon(Long id, String tplCode, String couponCode, Long userId,
                 CouponState state, LocalDateTime lockedUntil,
                 LocalDateTime assignedAt, LocalDateTime validFrom, LocalDateTime validTo,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.tplCode = Objects.requireNonNull(tplCode, "Template code cannot be null");
        this.couponCode = Objects.requireNonNull(couponCode, "Coupon code cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.state = state != null ? state : CouponState.CREATED;
        this.lockedUntil = lockedUntil;
        this.assignedAt = assignedAt != null ? assignedAt : LocalDateTime.now();
        this.validFrom = Objects.requireNonNull(validFrom, "Valid from cannot be null");
        this.validTo = Objects.requireNonNull(validTo, "Valid to cannot be null");
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
    
    /**
     * 创建优惠券实例
     */
    public static Coupon create(String tplCode, String couponCode, Long userId,
                               LocalDateTime validFrom, LocalDateTime validTo) {
        return new Coupon(null, tplCode, couponCode, userId, CouponState.CREATED, null,
                         null, validFrom, validTo, null);
    }
    
    /**
     * 发放优惠券
     */
    public void issue() {
        if (!state.canTransitionTo(CouponState.ISSUED)) {
            throw new IllegalStateException("Cannot issue coupon in current state: " + state);
        }
        
        changeState(CouponState.ISSUED, "优惠券发放");
    }
    
    /**
     * 激活为可用状态
     */
    public void activate() {
        if (!state.canTransitionTo(CouponState.AVAILABLE)) {
            throw new IllegalStateException("Cannot activate coupon in current state: " + state);
        }
        
        changeState(CouponState.AVAILABLE, "优惠券激活");
    }
    
    /**
     * 预留优惠券（两阶段操作第一阶段）
     */
    public void reserve(String orderId, int ttlSeconds, IdempotencyKey idempotencyKey) {
        if (!state.canReserve()) {
            throw new IllegalStateException("Cannot reserve coupon in current state: " + state);
        }
        
        if (isExpired()) {
            throw new IllegalStateException("Cannot reserve expired coupon");
        }
        
        this.lockedUntil = LocalDateTime.now().plusSeconds(ttlSeconds);
        changeState(CouponState.RESERVED, "优惠券预留，订单：" + orderId);
    }
    
    /**
     * 确认核销（两阶段操作第二阶段）
     */
    public void redeem(String orderId, IdempotencyKey idempotencyKey) {
        if (!state.canTransitionTo(CouponState.REDEEMED)) {
            throw new IllegalStateException("Cannot redeem coupon in current state: " + state);
        }
        
        if (state == CouponState.RESERVED && isReservationExpired()) {
            throw new IllegalStateException("Coupon reservation has expired");
        }
        
        this.lockedUntil = null;
        changeState(CouponState.REDEEMED, "优惠券核销，订单：" + orderId);
    }
    
    /**
     * 释放预留（取消预留）
     */
    public void release(String reason) {
        if (state == CouponState.RESERVED) {
            this.lockedUntil = null;
            changeState(CouponState.AVAILABLE, "释放预留：" + reason);
        }
    }
    
    /**
     * 退款回滚
     */
    public void refund(String orderId, IdempotencyKey idempotencyKey) {
        if (!state.canTransitionTo(CouponState.REFUNDED)) {
            throw new IllegalStateException("Cannot refund coupon in current state: " + state);
        }
        
        changeState(CouponState.REFUNDED, "优惠券退款，订单：" + orderId);
    }
    
    /**
     * 过期
     */
    public void expire() {
        if (!state.isFinalState()) {
            changeState(CouponState.EXPIRED, "优惠券过期");
        }
    }
    
    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(validTo);
    }
    
    /**
     * 检查预留是否过期
     */
    public boolean isReservationExpired() {
        return lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil);
    }
    
    /**
     * 检查是否可用
     */
    public boolean isAvailable() {
        return state == CouponState.AVAILABLE && !isExpired();
    }
    
    /**
     * 检查是否已使用
     */
    public boolean isUsed() {
        return state.isUsed();
    }
    
    /**
     * 获取并清空领域事件
     */
    public List<DomainEvent> getAndClearDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
    
    private void changeState(CouponState newState, String reason) {
        CouponState oldState = this.state;
        this.state = newState;
        this.updatedAt = LocalDateTime.now();
        
        // 发布状态变更事件
        addDomainEvent(new CouponStateChangedEvent(
                couponCode, tplCode, userId, oldState, newState, reason
        ));
    }
    
    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coupon coupon = (Coupon) obj;
        return Objects.equals(couponCode, coupon.couponCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(couponCode);
    }
    
    @Override
    public String toString() {
        return "Coupon{" +
               "couponCode='" + couponCode + '\'' +
               ", userId=" + userId +
               ", state=" + state +
               ", validFrom=" + validFrom +
               ", validTo=" + validTo +
               ", expired=" + isExpired() +
               '}';
    }
}