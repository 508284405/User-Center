package com.leyue.usercenter.app.loyalty.event;

import com.alibaba.cola.event.EventBusI;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.leyue.usercenter.domain.loyalty.event.OrderCompletedEvent;
import com.leyue.usercenter.domain.loyalty.event.PaymentCompletedEvent;
import com.leyue.usercenter.domain.loyalty.points.PointAccount;
import com.leyue.usercenter.domain.loyalty.points.gateway.PointAccountGateway;
import com.leyue.usercenter.domain.loyalty.level.Level;
import com.leyue.usercenter.domain.loyalty.level.gateway.LevelGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {
    
    private final PointAccountGateway pointAccountGateway;
    private final LevelGateway levelGateway;
    private final EventBusI eventBus;
    
    @EventHandler
    @Transactional
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Processing order completed event: orderId={}, userId={}, amount={}", 
                event.getOrderId(), event.getUserId(), event.getOrderAmount());
        
        try {
            // 计算积分奖励（每消费1元获得1积分）
            BigDecimal pointsToEarn = event.getOrderAmount().setScale(0, RoundingMode.DOWN);
            
            // 获取用户积分账户
            PointAccount pointAccount = pointAccountGateway.getByUserId(event.getUserId());
            if (pointAccount == null) {
                pointAccount = new PointAccount(event.getUserId());
                pointAccountGateway.save(pointAccount);
            }
            
            // 赚取积分
            pointAccount.earn(pointsToEarn.intValue(), "ORDER_COMPLETED", event.getOrderId());
            pointAccountGateway.save(pointAccount);
            
            // 检查等级升级
            checkAndUpgradeLevel(event.getUserId(), pointAccount.getTotalPoints());
            
            log.info("Successfully processed order completed event: earned {} points", pointsToEarn);
            
        } catch (Exception e) {
            log.error("Failed to process order completed event: orderId={}, error={}", 
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @EventHandler
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Processing payment completed event: paymentId={}, orderId={}, userId={}, amount={}", 
                event.getPaymentId(), event.getOrderId(), event.getUserId(), event.getPaidAmount());
        
        try {
            // 支付完成后额外奖励（根据支付方式给予不同奖励）
            int bonusPoints = calculatePaymentBonus(event.getPaymentMethod(), event.getPaidAmount());
            
            if (bonusPoints > 0) {
                PointAccount pointAccount = pointAccountGateway.getByUserId(event.getUserId());
                if (pointAccount != null) {
                    pointAccount.earn(bonusPoints, "PAYMENT_BONUS", event.getPaymentId());
                    pointAccountGateway.save(pointAccount);
                    log.info("Awarded payment bonus: {} points for payment method: {}", 
                            bonusPoints, event.getPaymentMethod());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to process payment completed event: paymentId={}, error={}", 
                    event.getPaymentId(), e.getMessage(), e);
            throw e;
        }
    }
    
    private void checkAndUpgradeLevel(String userId, int totalPoints) {
        try {
            // 获取所有等级，按积分门槛降序排列
            List<Level> levels = levelGateway.getAllLevels();
            levels.sort((a, b) -> Integer.compare(b.getPointsRequired(), a.getPointsRequired()));
            
            // 找到用户应该达到的等级
            Level targetLevel = null;
            for (Level level : levels) {
                if (totalPoints >= level.getPointsRequired()) {
                    targetLevel = level;
                    break;
                }
            }
            
            if (targetLevel != null) {
                // 这里可以发布等级升级事件，由其他服务处理等级变更
                log.info("User {} should be at level: {} (points: {})", 
                        userId, targetLevel.getName(), totalPoints);
            }
            
        } catch (Exception e) {
            log.error("Failed to check level upgrade for user: {}, error: {}", userId, e.getMessage(), e);
        }
    }
    
    private int calculatePaymentBonus(String paymentMethod, BigDecimal paidAmount) {
        // 根据支付方式计算奖励积分
        switch (paymentMethod.toUpperCase()) {
            case "WECHAT_PAY":
                return paidAmount.multiply(new BigDecimal("0.1")).intValue(); // 微信支付10%额外积分
            case "ALIPAY":
                return paidAmount.multiply(new BigDecimal("0.15")).intValue(); // 支付宝15%额外积分
            case "CREDIT_CARD":
                return paidAmount.multiply(new BigDecimal("0.05")).intValue(); // 信用卡5%额外积分
            default:
                return 0;
        }
    }
}