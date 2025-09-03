package com.leyue.usercenter.domain.seckill.gateway;

import com.leyue.usercenter.domain.seckill.SeckillOrder;
import com.leyue.usercenter.domain.seckill.SeckillOrder.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillOrderGateway {
    
    SeckillOrder save(SeckillOrder order);
    
    SeckillOrder getById(Long id);
    
    SeckillOrder getByOrderId(String orderId);
    
    List<SeckillOrder> getByActivityId(Long activityId);
    
    List<SeckillOrder> getByUserId(String userId);
    
    List<SeckillOrder> getByUserAndActivity(String userId, Long activityId);
    
    List<SeckillOrder> getByStatus(OrderStatus status);
    
    List<SeckillOrder> getExpiredOrders(LocalDateTime now);
    
    List<SeckillOrder> getOrdersByPage(int pageNum, int pageSize, Long activityId, 
                                      String userId, OrderStatus status);
    
    long countOrdersByPage(Long activityId, String userId, OrderStatus status);
    
    // 统计相关
    int countUserOrdersInActivity(String userId, Long activityId);
    
    int countActivityOrders(Long activityId, OrderStatus status);
    
    List<SeckillOrder> getRecentOrders(int limit);
}