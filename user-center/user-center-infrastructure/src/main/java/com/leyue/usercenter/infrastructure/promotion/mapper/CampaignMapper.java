package com.leyue.usercenter.infrastructure.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.promotion.dataobject.CampaignDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CampaignMapper extends BaseMapper<CampaignDO> {
    
    List<CampaignDO> selectByStatus(@Param("status") String status);
    
    List<CampaignDO> selectByType(@Param("type") String type);
    
    List<CampaignDO> selectActiveCampaigns(@Param("now") LocalDateTime now);
    
    List<CampaignDO> selectByDateRange(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
    
    List<CampaignDO> selectByCreator(@Param("createdBy") String createdBy);
    
    List<CampaignDO> selectByPage(@Param("offset") int offset, 
                                 @Param("pageSize") int pageSize,
                                 @Param("name") String name,
                                 @Param("status") String status,
                                 @Param("type") String type);
    
    long countByPage(@Param("name") String name,
                     @Param("status") String status,
                     @Param("type") String type);
    
    int existsByName(@Param("name") String name);
    
    List<CampaignDO> selectExpiredCampaigns(@Param("now") LocalDateTime now);
}