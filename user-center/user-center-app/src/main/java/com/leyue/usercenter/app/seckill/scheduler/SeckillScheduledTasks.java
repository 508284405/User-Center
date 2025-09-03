package com.leyue.usercenter.app.seckill.scheduler;

import com.leyue.usercenter.domain.seckill.SeckillActivity;
import com.leyue.usercenter.domain.seckill.SeckillOrder;
import com.leyue.usercenter.domain.seckill.gateway.SeckillActivityGateway;
import com.leyue.usercenter.domain.seckill.gateway.SeckillOrderGateway;
import com.leyue.usercenter.infrastructure.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "seckill.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SeckillScheduledTasks {
    
    private final SeckillActivityGateway activityGateway;
    private final SeckillOrderGateway orderGateway;
    private final OutboxEventPublisher outboxEventPublisher;
    
    /**
     * 每分钟检查需要开始的秒杀活动
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void startActivities() {
        log.debug("检查需要开始的秒杀活动...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<SeckillActivity> activitiesToStart = activityGateway.getActivitiesNeedToStart(now);
            
            for (SeckillActivity activity : activitiesToStart) {
                try {
                    activity.start();
                    activityGateway.save(activity);
                    
                    // 发布活动开始事件
                    outboxEventPublisher.publish("SECKILL_ACTIVITY_STARTED", 
                        String.format("{\"activityId\":%d,\"name\":\"%s\",\"startTime\":\"%s\"}", 
                                activity.getId(), activity.getName(), activity.getStartTime()));
                    
                    log.info("秒杀活动自动开始: activityId={}, name={}", 
                            activity.getId(), activity.getName());
                    
                } catch (Exception e) {
                    log.error("启动秒杀活动失败: activityId={}, error={}", 
                            activity.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("检查需要开始的秒杀活动失败", e);
        }
    }
    
    /**
     * 每分钟检查需要结束的秒杀活动
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void endActivities() {
        log.debug("检查需要结束的秒杀活动...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<SeckillActivity> activitiesToEnd = activityGateway.getActivitiesNeedToEnd(now);
            
            for (SeckillActivity activity : activitiesToEnd) {
                try {
                    activity.end();
                    activityGateway.save(activity);
                    
                    // 发布活动结束事件
                    outboxEventPublisher.publish("SECKILL_ACTIVITY_ENDED", 
                        String.format("{\"activityId\":%d,\"name\":\"%s\",\"endTime\":\"%s\",\"soldCount\":%d}", 
                                activity.getId(), activity.getName(), activity.getEndTime(), activity.getSoldCount()));
                    
                    log.info("秒杀活动自动结束: activityId={}, name={}, 销售数量={}", 
                            activity.getId(), activity.getName(), activity.getSoldCount());
                    
                } catch (Exception e) {
                    log.error("结束秒杀活动失败: activityId={}, error={}", 
                            activity.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("检查需要结束的秒杀活动失败", e);
        }
    }
    
    /**
     * 每分钟处理过期订单
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processExpiredOrders() {
        log.debug("处理过期的秒杀订单...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<SeckillOrder> expiredOrders = orderGateway.getExpiredOrders(now);
            
            for (SeckillOrder order : expiredOrders) {
                try {
                    // 标记订单为过期
                    order.expire();
                    orderGateway.save(order);
                    
                    // 恢复库存
                    activityGateway.increaseStock(order.getActivityId(), order.getQuantity());
                    
                    // 发布订单过期事件
                    outboxEventPublisher.publish("SECKILL_ORDER_EXPIRED", 
                        String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"activityId\":%d,\"quantity\":%d}", 
                                order.getOrderId(), order.getUserId(), order.getActivityId(), order.getQuantity()));
                    
                    log.info("秒杀订单自动过期: orderId={}, userId={}, activityId={}", 
                            order.getOrderId(), order.getUserId(), order.getActivityId());
                    
                } catch (Exception e) {
                    log.error("处理过期订单失败: orderId={}, error={}", 
                            order.getOrderId(), e.getMessage(), e);
                }
            }
            
            if (expiredOrders.size() > 0) {
                log.info("处理过期秒杀订单完成，共处理 {} 个订单", expiredOrders.size());
            }
            
        } catch (Exception e) {
            log.error("处理过期秒杀订单失败", e);
        }
    }
    
    /**
     * 每天凌晨3点清理历史数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupHistoryData() {
        log.info("开始清理秒杀历史数据...");
        
        try {
            // 清理30天前的已完成活动数据（根据业务需要调整）
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            
            // 这里可以实现具体的清理逻辑
            // 比如归档已完成的活动、清理过期的订单等
            
            log.info("秒杀历史数据清理完成");
            
        } catch (Exception e) {
            log.error("清理秒杀历史数据失败", e);
        }
    }
    
    /**
     * 每小时生成秒杀统计报表
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void generateStatistics() {
        log.info("开始生成秒杀统计数据...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            
            // 获取进行中的活动
            List<SeckillActivity> ongoingActivities = activityGateway.getByStatus(
                    SeckillActivity.SeckillStatus.ONGOING);
            
            for (SeckillActivity activity : ongoingActivities) {
                try {
                    generateActivityStatistics(activity, oneHourAgo, now);
                } catch (Exception e) {
                    log.error("生成活动统计失败: activityId={}, error={}", 
                            activity.getId(), e.getMessage(), e);
                }
            }
            
            log.info("秒杀统计数据生成完成");
            
        } catch (Exception e) {
            log.error("生成秒杀统计数据失败", e);
        }
    }
    
    /**
     * 每10分钟检查库存告警
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void checkStockAlert() {
        log.debug("检查秒杀活动库存告警...");
        
        try {
            List<SeckillActivity> ongoingActivities = activityGateway.getByStatus(
                    SeckillActivity.SeckillStatus.ONGOING);
            
            for (SeckillActivity activity : ongoingActivities) {
                double stockPercentage = (double) activity.getAvailableStock() / activity.getTotalStock() * 100;
                
                // 库存低于10%时发送告警
                if (stockPercentage <= 10.0 && stockPercentage > 0) {
                    log.warn("秒杀活动库存不足告警: activityId={}, name={}, 剩余库存={}({}%)", 
                            activity.getId(), activity.getName(), 
                            activity.getAvailableStock(), String.format("%.1f", stockPercentage));
                    
                    // 发布库存告警事件
                    outboxEventPublisher.publish("SECKILL_LOW_STOCK_ALERT", 
                        String.format("{\"activityId\":%d,\"name\":\"%s\",\"availableStock\":%d,\"stockPercentage\":%.2f}", 
                                activity.getId(), activity.getName(), activity.getAvailableStock(), stockPercentage));
                }
                
                // 库存售罄时发送通知
                if (activity.isSoldOut()) {
                    log.info("秒杀活动已售罄: activityId={}, name={}", 
                            activity.getId(), activity.getName());
                    
                    // 发布售罄事件
                    outboxEventPublisher.publish("SECKILL_SOLD_OUT", 
                        String.format("{\"activityId\":%d,\"name\":\"%s\",\"soldCount\":%d}", 
                                activity.getId(), activity.getName(), activity.getSoldCount()));
                }
            }
            
        } catch (Exception e) {
            log.error("检查秒杀活动库存告警失败", e);
        }
    }
    
    private void generateActivityStatistics(SeckillActivity activity, LocalDateTime startTime, LocalDateTime endTime) {
        // 统计订单数据
        int totalOrders = orderGateway.countActivityOrders(activity.getId(), null);
        int paidOrders = orderGateway.countActivityOrders(activity.getId(), SeckillOrder.OrderStatus.PAID);
        int cancelledOrders = orderGateway.countActivityOrders(activity.getId(), SeckillOrder.OrderStatus.CANCELLED);
        int expiredOrders = orderGateway.countActivityOrders(activity.getId(), SeckillOrder.OrderStatus.EXPIRED);
        
        // 计算转化率和支付率
        double conversionRate = activity.getTotalStock() > 0 ? 
                (double) totalOrders / activity.getTotalStock() * 100 : 0;
        double paymentRate = totalOrders > 0 ? 
                (double) paidOrders / totalOrders * 100 : 0;
        
        log.info("=== 秒杀活动统计 [{}] ===", activity.getName());
        log.info("活动ID: {}", activity.getId());
        log.info("总库存: {}, 剩余库存: {}, 已售: {}", 
                activity.getTotalStock(), activity.getAvailableStock(), activity.getSoldCount());
        log.info("总订单: {}, 已支付: {}, 已取消: {}, 已过期: {}", 
                totalOrders, paidOrders, cancelledOrders, expiredOrders);
        log.info("转化率: {:.2f}%, 支付率: {:.2f}%", conversionRate, paymentRate);
        
        // 这里可以将统计数据保存到数据库的统计表中
        // statisticsService.saveActivityStatistics(activity.getId(), statistics);
    }
}