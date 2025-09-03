package com.leyue.usercenter.domain.groupbuy.gateway;

import com.leyue.usercenter.domain.groupbuy.GroupBuyActivity;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupBuyActivityGateway {
    
    GroupBuyActivity save(GroupBuyActivity activity);
    
    GroupBuyActivity getById(Long id);
    
    List<GroupBuyActivity> getByStatus(GroupBuyActivity.GroupBuyStatus status);
    
    List<GroupBuyActivity> getActiveActivities();
    
    List<GroupBuyActivity> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    List<GroupBuyActivity> getByProductId(Long productId);
    
    List<GroupBuyActivity> getByPage(int pageNum, int pageSize, String name, 
                                   GroupBuyActivity.GroupBuyStatus status, Long productId);
    
    long countByPage(String name, GroupBuyActivity.GroupBuyStatus status, Long productId);
    
    void delete(Long id);
    
    List<GroupBuyActivity> getExpiredActivities();
    
    List<GroupBuyActivity> getActivitiesByCreator(String createdBy);
}