package com.leyue.usercenter.infrastructure.promotion;

import com.leyue.usercenter.domain.promotion.Campaign;
import com.leyue.usercenter.domain.promotion.Campaign.CampaignStatus;
import com.leyue.usercenter.domain.promotion.Campaign.CampaignType;
import com.leyue.usercenter.domain.promotion.gateway.CampaignGateway;
import com.leyue.usercenter.infrastructure.promotion.mapper.CampaignMapper;
import com.leyue.usercenter.infrastructure.promotion.dataobject.CampaignDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CampaignGatewayImpl implements CampaignGateway {
    
    private final CampaignMapper campaignMapper;
    
    @Override
    public Campaign save(Campaign campaign) {
        CampaignDO campaignDO = convertToDO(campaign);
        
        if (campaign.getId() == null) {
            campaignMapper.insert(campaignDO);
            campaign.setId(campaignDO.getId());
        } else {
            campaignMapper.updateById(campaignDO);
        }
        
        return campaign;
    }
    
    @Override
    public Campaign getById(Long id) {
        CampaignDO campaignDO = campaignMapper.selectById(id);
        return campaignDO != null ? convertToEntity(campaignDO) : null;
    }
    
    @Override
    public List<Campaign> getByStatus(CampaignStatus status) {
        List<CampaignDO> campaignDOs = campaignMapper.selectByStatus(status.name());
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Campaign> getByType(CampaignType type) {
        List<CampaignDO> campaignDOs = campaignMapper.selectByType(type.name());
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Campaign> getActiveCampaigns(LocalDateTime now) {
        List<CampaignDO> campaignDOs = campaignMapper.selectActiveCampaigns(now);
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Campaign> getCampaignsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<CampaignDO> campaignDOs = campaignMapper.selectByDateRange(startTime, endTime);
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Campaign> getCampaignsByCreator(String createdBy) {
        List<CampaignDO> campaignDOs = campaignMapper.selectByCreator(createdBy);
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Campaign> getCampaignsByPage(int pageNum, int pageSize, String name, 
                                           CampaignStatus status, CampaignType type) {
        int offset = (pageNum - 1) * pageSize;
        List<CampaignDO> campaignDOs = campaignMapper.selectByPage(
                offset, pageSize, name, 
                status != null ? status.name() : null,
                type != null ? type.name() : null);
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countCampaignsByPage(String name, CampaignStatus status, CampaignType type) {
        return campaignMapper.countByPage(name, 
                status != null ? status.name() : null,
                type != null ? type.name() : null);
    }
    
    @Override
    public void delete(Long id) {
        campaignMapper.deleteById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return campaignMapper.existsByName(name) > 0;
    }
    
    @Override
    public List<Campaign> getExpiredCampaigns(LocalDateTime now) {
        List<CampaignDO> campaignDOs = campaignMapper.selectExpiredCampaigns(now);
        return campaignDOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    private CampaignDO convertToDO(Campaign campaign) {
        CampaignDO campaignDO = new CampaignDO();
        campaignDO.setId(campaign.getId());
        campaignDO.setName(campaign.getName());
        campaignDO.setDescription(campaign.getDescription());
        campaignDO.setType(campaign.getType().name());
        campaignDO.setStatus(campaign.getStatus().name());
        campaignDO.setStartTime(campaign.getStartTime());
        campaignDO.setEndTime(campaign.getEndTime());
        campaignDO.setTargetAudience(campaign.getTargetAudience());
        campaignDO.setRules(campaign.getRules());
        campaignDO.setBudget(campaign.getBudget());
        campaignDO.setUsedBudget(campaign.getUsedBudget());
        campaignDO.setParticipantLimit(campaign.getParticipantLimit());
        campaignDO.setCurrentParticipants(campaign.getCurrentParticipants());
        campaignDO.setCreatedBy(campaign.getCreatedBy());
        campaignDO.setCreatedAt(campaign.getCreatedAt());
        campaignDO.setUpdatedBy(campaign.getUpdatedBy());
        campaignDO.setUpdatedAt(campaign.getUpdatedAt());
        return campaignDO;
    }
    
    private Campaign convertToEntity(CampaignDO campaignDO) {
        Campaign campaign = new Campaign();
        campaign.setId(campaignDO.getId());
        campaign.setName(campaignDO.getName());
        campaign.setDescription(campaignDO.getDescription());
        campaign.setType(CampaignType.valueOf(campaignDO.getType()));
        campaign.setStatus(CampaignStatus.valueOf(campaignDO.getStatus()));
        campaign.setStartTime(campaignDO.getStartTime());
        campaign.setEndTime(campaignDO.getEndTime());
        campaign.setTargetAudience(campaignDO.getTargetAudience());
        campaign.setRules(campaignDO.getRules());
        campaign.setBudget(campaignDO.getBudget());
        campaign.setUsedBudget(campaignDO.getUsedBudget());
        campaign.setParticipantLimit(campaignDO.getParticipantLimit());
        campaign.setCurrentParticipants(campaignDO.getCurrentParticipants());
        campaign.setCreatedBy(campaignDO.getCreatedBy());
        campaign.setCreatedAt(campaignDO.getCreatedAt());
        campaign.setUpdatedBy(campaignDO.getUpdatedBy());
        campaign.setUpdatedAt(campaignDO.getUpdatedAt());
        return campaign;
    }
}