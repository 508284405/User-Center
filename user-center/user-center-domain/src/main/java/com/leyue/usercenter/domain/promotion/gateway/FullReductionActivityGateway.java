package com.leyue.usercenter.domain.promotion.gateway;

import com.leyue.usercenter.domain.promotion.FullReductionActivity;
import com.leyue.usercenter.domain.promotion.FullReductionActivity.ActivityStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FullReductionActivityGateway {
    
    FullReductionActivity save(FullReductionActivity activity);
    
    FullReductionActivity getById(Long id);
    
    List<FullReductionActivity> getByStatus(ActivityStatus status);
    
    List<FullReductionActivity> getActiveActivities(LocalDateTime now);
    
    List<FullReductionActivity> getActivitiesByDateRange(LocalDateTime startTime, LocalDateTime endTime);
    
    List<FullReductionActivity> getActivitiesByPage(int pageNum, int pageSize, String name, 
                                                   ActivityStatus status, LocalDateTime startTime, LocalDateTime endTime);
    
    long countActivitiesByPage(String name, ActivityStatus status, LocalDateTime startTime, LocalDateTime endTime);
    
    void delete(Long id);
    
    boolean existsByName(String name);
    
    List<FullReductionActivity> getExpiredActivities(LocalDateTime now);
    
    // 业务查询
    List<FullReductionActivity> getApplicableActivities(BigDecimal orderAmount, 
                                                       List<Long> productIds, 
                                                       List<Long> categoryIds, 
                                                       String userId, 
                                                       LocalDateTime now);
    
    int getUserUsageCount(Long activityId, String userId);
    
    List<FullReductionActivity> getActivitiesByPriority(LocalDateTime now);
}