package com.leyue.usercenter.domain.seckill;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SeckillOrder {
    
    private Long id;
    private Long activityId;
    private String activityName;
    private String userId;
    private String orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime expireTime; // 支付超时时间
    private String cancelReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    public SeckillOrder() {}
    
    public SeckillOrder(Long activityId, String activityName, String userId, 
                       Long productId, String productName, Integer quantity, 
                       BigDecimal unitPrice, int paymentTimeoutMinutes) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(new BigDecimal(quantity));
        this.status = OrderStatus.PENDING_PAYMENT;
        this.orderId = generateOrderId();
        this.expireTime = LocalDateTime.now().plusMinutes(paymentTimeoutMinutes);
        this.createdAt = LocalDateTime.now();
        this.createdBy = userId;
    }
    
    /**
     * 支付订单
     */
    public void pay() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("只有待支付订单才能支付");
        }
        
        if (LocalDateTime.now().isAfter(this.expireTime)) {
            throw new IllegalStateException("订单已过期，无法支付");
        }
        
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消订单
     */
    public void cancel(String reason) {
        if (this.status == OrderStatus.PAID) {
            throw new IllegalStateException("已支付订单无法取消");
        }
        
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("订单已经是取消状态");
        }
        
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 订单过期处理
     */
    public void expire() {
        if (this.status == OrderStatus.PENDING_PAYMENT) {
            this.status = OrderStatus.EXPIRED;
            this.cancelReason = "支付超时自动取消";
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 检查订单是否已过期
     */
    public boolean isExpired() {
        return this.status == OrderStatus.PENDING_PAYMENT 
               && LocalDateTime.now().isAfter(this.expireTime);
    }
    
    /**
     * 检查订单是否可以支付
     */
    public boolean canPay() {
        return this.status == OrderStatus.PENDING_PAYMENT 
               && LocalDateTime.now().isBefore(this.expireTime);
    }
    
    /**
     * 获取剩余支付时间（分钟）
     */
    public long getRemainingPaymentMinutes() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.expireTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, this.expireTime).toMinutes();
    }
    
    private String generateOrderId() {
        // 生成订单号：SK + 时间戳 + 随机数
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("SK%d%03d", timestamp, random);
    }
    
    public enum OrderStatus {
        PENDING_PAYMENT("待支付"),
        PAID("已支付"),
        CANCELLED("已取消"),
        EXPIRED("已过期");
        
        private final String description;
        
        OrderStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}