package com.leyue.usercenter.domain.promotion.gateway;

import com.leyue.usercenter.domain.promotion.Campaign;
import com.leyue.usercenter.domain.promotion.Campaign.CampaignStatus;
import com.leyue.usercenter.domain.promotion.Campaign.CampaignType;

import java.time.LocalDateTime;
import java.util.List;

public interface CampaignGateway {
    
    Campaign save(Campaign campaign);
    
    Campaign getById(Long id);
    
    List<Campaign> getByStatus(CampaignStatus status);
    
    List<Campaign> getByType(CampaignType type);
    
    List<Campaign> getActiveCampaigns(LocalDateTime now);
    
    List<Campaign> getCampaignsByDateRange(LocalDateTime startTime, LocalDateTime endTime);
    
    List<Campaign> getCampaignsByCreator(String createdBy);
    
    List<Campaign> getCampaignsByPage(int pageNum, int pageSize, String name, 
                                     CampaignStatus status, CampaignType type);
    
    long countCampaignsByPage(String name, CampaignStatus status, CampaignType type);
    
    void delete(Long id);
    
    boolean existsByName(String name);
    
    List<Campaign> getExpiredCampaigns(LocalDateTime now);
}