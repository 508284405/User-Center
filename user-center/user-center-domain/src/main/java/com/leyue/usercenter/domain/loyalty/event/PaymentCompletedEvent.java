package com.leyue.usercenter.domain.loyalty.event;

import com.alibaba.cola.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class PaymentCompletedEvent extends Event {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal paidAmount;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private String source;
    
    public PaymentCompletedEvent(String paymentId, String orderId, String userId, 
                                BigDecimal paidAmount, String paymentMethod, 
                                LocalDateTime paidAt, String source) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.paidAmount = paidAmount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.source = source;
    }
}