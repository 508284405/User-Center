package com.leyue.usercenter.domain.seckill.gateway;

import com.leyue.usercenter.domain.seckill.SeckillActivity;
import com.leyue.usercenter.domain.seckill.SeckillActivity.SeckillStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillActivityGateway {
    
    SeckillActivity save(SeckillActivity activity);
    
    SeckillActivity getById(Long id);
    
    SeckillActivity getByIdWithLock(Long id); // 悲观锁查询，用于库存扣减
    
    List<SeckillActivity> getByStatus(SeckillStatus status);
    
    List<SeckillActivity> getActiveActivities(LocalDateTime now);
    
    List<SeckillActivity> getActivitiesByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    List<SeckillActivity> getActivitiesByPage(int pageNum, int pageSize, String name, 
                                             SeckillStatus status, LocalDateTime startTime, LocalDateTime endTime);
    
    long countActivitiesByPage(String name, SeckillStatus status, LocalDateTime startTime, LocalDateTime endTime);
    
    void delete(Long id);
    
    boolean existsByName(String name);
    
    List<SeckillActivity> getExpiredActivities(LocalDateTime now);
    
    List<SeckillActivity> getActivitiesNeedToStart(LocalDateTime now);
    
    List<SeckillActivity> getActivitiesNeedToEnd(LocalDateTime now);
    
    // 库存相关操作
    boolean decreaseStock(Long activityId, int quantity);
    
    void increaseStock(Long activityId, int quantity); // 取消订单时恢复库存
    
    int getAvailableStock(Long activityId);
}