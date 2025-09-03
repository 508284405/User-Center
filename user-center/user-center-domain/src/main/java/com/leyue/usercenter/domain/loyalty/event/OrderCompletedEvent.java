package com.leyue.usercenter.domain.loyalty.event;

import com.alibaba.cola.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrderCompletedEvent extends Event {
    private String orderId;
    private String userId;
    private BigDecimal orderAmount;
    private String productType;
    private LocalDateTime completedAt;
    private String source;
    
    public OrderCompletedEvent(String orderId, String userId, BigDecimal orderAmount, 
                              String productType, LocalDateTime completedAt, String source) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderAmount = orderAmount;
        this.productType = productType;
        this.completedAt = completedAt;
        this.source = source;
    }
}