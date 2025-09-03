package com.leyue.usercenter.app.seckill;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.SeckillServiceI;
import com.leyue.usercenter.dto.command.seckill.CreateSeckillActivityCmd;
import com.leyue.usercenter.dto.command.seckill.SeckillParticipateCmd;
import com.leyue.usercenter.dto.data.seckill.SeckillActivityDTO;
import com.leyue.usercenter.dto.data.seckill.SeckillOrderDTO;
import com.leyue.usercenter.domain.seckill.SeckillActivity;
import com.leyue.usercenter.domain.seckill.SeckillOrder;
import com.leyue.usercenter.domain.seckill.gateway.SeckillActivityGateway;
import com.leyue.usercenter.domain.seckill.gateway.SeckillOrderGateway;
import com.leyue.usercenter.infrastructure.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillServiceI {
    
    private final SeckillActivityGateway activityGateway;
    private final SeckillOrderGateway orderGateway;
    private final OutboxEventPublisher outboxEventPublisher;
    
    private static final int DEFAULT_PAYMENT_TIMEOUT_MINUTES = 15; // 默认支付超时15分钟
    
    @Override
    @Transactional
    public SingleResponse<SeckillActivityDTO> createActivity(CreateSeckillActivityCmd cmd) {
        log.info("创建秒杀活动: {}", cmd.getName());
        
        // 验证活动时间
        if (cmd.getStartTime().isAfter(cmd.getEndTime())) {
            return SingleResponse.buildFailure("INVALID_TIME", "开始时间不能晚于结束时间");
        }
        
        // 验证价格
        if (cmd.getSeckillPrice().compareTo(cmd.getOriginalPrice()) >= 0) {
            return SingleResponse.buildFailure("INVALID_PRICE", "秒杀价格必须低于原价");
        }
        
        // 检查活动名称是否已存在
        if (activityGateway.existsByName(cmd.getName())) {
            return SingleResponse.buildFailure("NAME_EXISTS", "活动名称已存在");
        }
        
        try {
            SeckillActivity activity = new SeckillActivity(
                cmd.getName(),
                cmd.getDescription(),
                cmd.getStartTime(),
                cmd.getEndTime(),
                cmd.getProductId(),
                cmd.getProductName(),
                cmd.getOriginalPrice(),
                cmd.getSeckillPrice(),
                cmd.getTotalStock(),
                cmd.getLimitPerUser(),
                getCurrentUser()
            );
            
            SeckillActivity saved = activityGateway.save(activity);
            
            // 发布活动创建事件
            outboxEventPublisher.publish("SECKILL_ACTIVITY_CREATED", 
                String.format("{\"activityId\":%d,\"name\":\"%s\"}", saved.getId(), saved.getName()));
            
            return SingleResponse.of(convertToActivityDTO(saved));
            
        } catch (Exception e) {
            log.error("创建秒杀活动失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_FAILED", "创建活动失败");
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<SeckillOrderDTO> participate(SeckillParticipateCmd cmd) {
        log.info("参与秒杀活动: userId={}, activityId={}, quantity={}", 
                cmd.getUserId(), cmd.getActivityId(), cmd.getQuantity());
        
        try {
            // 获取活动（使用悲观锁）
            SeckillActivity activity = activityGateway.getByIdWithLock(cmd.getActivityId());
            if (activity == null) {
                return SingleResponse.buildFailure("ACTIVITY_NOT_FOUND", "秒杀活动不存在");
            }
            
            // 检查活动是否可参与
            if (!activity.canParticipate()) {
                return SingleResponse.buildFailure("ACTIVITY_NOT_AVAILABLE", "活动当前不可参与");
            }
            
            // 检查用户购买限制
            int userOrderCount = orderGateway.countUserOrdersInActivity(cmd.getUserId(), cmd.getActivityId());
            if (userOrderCount + cmd.getQuantity() > activity.getLimitPerUser()) {
                return SingleResponse.buildFailure("EXCEED_USER_LIMIT", "超过单用户购买限制");
            }
            
            // 扣减库存
            if (!activity.seckillProduct(cmd.getQuantity())) {
                return SingleResponse.buildFailure("INSUFFICIENT_STOCK", "库存不足");
            }
            
            // 更新活动库存
            activityGateway.save(activity);
            
            // 创建订单
            SeckillOrder order = new SeckillOrder(
                activity.getId(),
                activity.getName(),
                cmd.getUserId(),
                activity.getProductId(),
                activity.getProductName(),
                cmd.getQuantity(),
                activity.getSeckillPrice(),
                DEFAULT_PAYMENT_TIMEOUT_MINUTES
            );
            
            SeckillOrder savedOrder = orderGateway.save(order);
            
            // 发布秒杀成功事件
            outboxEventPublisher.publish("SECKILL_SUCCESS", 
                String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"activityId\":%d}", 
                        savedOrder.getOrderId(), cmd.getUserId(), cmd.getActivityId()));
            
            log.info("秒杀成功: orderId={}, userId={}, activityId={}", 
                    savedOrder.getOrderId(), cmd.getUserId(), cmd.getActivityId());
            
            return SingleResponse.of(convertToOrderDTO(savedOrder));
            
        } catch (IllegalStateException e) {
            log.warn("秒杀失败: {}", e.getMessage());
            return SingleResponse.buildFailure("SECKILL_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("秒杀异常: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("SYSTEM_ERROR", "系统异常");
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<Void> payOrder(String orderId) {
        log.info("支付秒杀订单: orderId={}", orderId);
        
        try {
            SeckillOrder order = orderGateway.getByOrderId(orderId);
            if (order == null) {
                return SingleResponse.buildFailure("ORDER_NOT_FOUND", "订单不存在");
            }
            
            if (!order.canPay()) {
                return SingleResponse.buildFailure("CANNOT_PAY", "订单当前状态不允许支付");
            }
            
            // 支付订单
            order.pay();
            orderGateway.save(order);
            
            // 发布支付成功事件
            outboxEventPublisher.publish("SECKILL_ORDER_PAID", 
                String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"amount\":%s}", 
                        order.getOrderId(), order.getUserId(), order.getTotalAmount()));
            
            log.info("秒杀订单支付成功: orderId={}", orderId);
            
            return SingleResponse.buildSuccess();
            
        } catch (IllegalStateException e) {
            return SingleResponse.buildFailure("PAY_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("支付秒杀订单失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("SYSTEM_ERROR", "系统异常");
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<Void> cancelOrder(String orderId, String reason) {
        log.info("取消秒杀订单: orderId={}, reason={}", orderId, reason);
        
        try {
            SeckillOrder order = orderGateway.getByOrderId(orderId);
            if (order == null) {
                return SingleResponse.buildFailure("ORDER_NOT_FOUND", "订单不存在");
            }
            
            // 取消订单
            order.cancel(reason);
            orderGateway.save(order);
            
            // 恢复库存
            activityGateway.increaseStock(order.getActivityId(), order.getQuantity());
            
            // 发布订单取消事件
            outboxEventPublisher.publish("SECKILL_ORDER_CANCELLED", 
                String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"reason\":\"%s\"}", 
                        order.getOrderId(), order.getUserId(), reason));
            
            log.info("秒杀订单取消成功: orderId={}", orderId);
            
            return SingleResponse.buildSuccess();
            
        } catch (IllegalStateException e) {
            return SingleResponse.buildFailure("CANCEL_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("取消秒杀订单失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("SYSTEM_ERROR", "系统异常");
        }
    }
    
    @Override
    public SingleResponse<SeckillActivityDTO> getActivityById(Long id) {
        SeckillActivity activity = activityGateway.getById(id);
        if (activity == null) {
            return SingleResponse.buildFailure("ACTIVITY_NOT_FOUND", "活动不存在");
        }
        
        return SingleResponse.of(convertToActivityDTO(activity));
    }
    
    private SeckillActivityDTO convertToActivityDTO(SeckillActivity activity) {
        SeckillActivityDTO dto = new SeckillActivityDTO();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setDescription(activity.getDescription());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setStatus(activity.getStatus().name());
        dto.setStatusDescription(activity.getStatus().getDescription());
        dto.setProductId(activity.getProductId());
        dto.setProductName(activity.getProductName());
        dto.setOriginalPrice(activity.getOriginalPrice());
        dto.setSeckillPrice(activity.getSeckillPrice());
        dto.setDiscountRate(activity.getDiscountRate());
        dto.setTotalStock(activity.getTotalStock());
        dto.setAvailableStock(activity.getAvailableStock());
        dto.setSoldCount(activity.getSoldCount());
        dto.setLimitPerUser(activity.getLimitPerUser());
        dto.setSalesProgress(activity.getSalesProgress());
        dto.setSoldOut(activity.isSoldOut());
        dto.setCanParticipate(activity.canParticipate());
        
        // 计算剩余时间
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStatus() == SeckillActivity.SeckillStatus.NOT_STARTED) {
            dto.setRemainingSeconds(ChronoUnit.SECONDS.between(now, activity.getStartTime()));
        } else if (activity.getStatus() == SeckillActivity.SeckillStatus.ONGOING) {
            dto.setRemainingSeconds(ChronoUnit.SECONDS.between(now, activity.getEndTime()));
        } else {
            dto.setRemainingSeconds(0L);
        }
        
        dto.setCreatedBy(activity.getCreatedBy());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedBy(activity.getUpdatedBy());
        dto.setUpdatedAt(activity.getUpdatedAt());
        
        return dto;
    }
    
    private SeckillOrderDTO convertToOrderDTO(SeckillOrder order) {
        SeckillOrderDTO dto = new SeckillOrderDTO();
        dto.setId(order.getId());
        dto.setActivityId(order.getActivityId());
        dto.setActivityName(order.getActivityName());
        dto.setUserId(order.getUserId());
        dto.setOrderId(order.getOrderId());
        dto.setProductId(order.getProductId());
        dto.setProductName(order.getProductName());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setStatusDescription(order.getStatus().getDescription());
        dto.setExpireTime(order.getExpireTime());
        dto.setRemainingPaymentMinutes(order.getRemainingPaymentMinutes());
        dto.setCanPay(order.canPay());
        dto.setExpired(order.isExpired());
        dto.setCancelReason(order.getCancelReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        return dto;
    }
    
    private String getCurrentUser() {
        // TODO: 从安全上下文获取当前用户
        return "system";
    }
}