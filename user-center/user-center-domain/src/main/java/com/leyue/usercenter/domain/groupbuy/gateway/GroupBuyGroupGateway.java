package com.leyue.usercenter.domain.groupbuy.gateway;

import com.leyue.usercenter.domain.groupbuy.GroupBuyGroup;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupBuyGroupGateway {
    
    GroupBuyGroup save(GroupBuyGroup group);
    
    GroupBuyGroup getById(Long id);
    
    GroupBuyGroup getByGroupNumber(String groupNumber);
    
    List<GroupBuyGroup> getByActivityId(Long activityId);
    
    List<GroupBuyGroup> getByStatus(GroupBuyGroup.GroupStatus status);
    
    List<GroupBuyGroup> getByLeaderUserId(String leaderUserId);
    
    List<GroupBuyGroup> getByParticipantUserId(String userId);
    
    List<GroupBuyGroup> getExpiredGroups();
    
    List<GroupBuyGroup> getFormingGroups();
    
    List<GroupBuyGroup> getByActivityAndStatus(Long activityId, GroupBuyGroup.GroupStatus status);
    
    List<GroupBuyGroup> getByPage(int pageNum, int pageSize, Long activityId, 
                                GroupBuyGroup.GroupStatus status, String leaderUserId);
    
    long countByPage(Long activityId, GroupBuyGroup.GroupStatus status, String leaderUserId);
    
    void delete(Long id);
    
    int countSuccessfulGroupsByActivity(Long activityId);
    
    int countTotalGroupsByActivity(Long activityId);
    
    List<GroupBuyGroup> getGroupsExpiredBefore(LocalDateTime expireTime);
}