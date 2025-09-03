package com.leyue.usercenter.app.promotion;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.CampaignServiceI;
import com.leyue.usercenter.dto.command.promotion.CreateCampaignCmd;
import com.leyue.usercenter.dto.command.promotion.UpdateCampaignCmd;
import com.leyue.usercenter.dto.query.promotion.CampaignPageQuery;
import com.leyue.usercenter.dto.data.promotion.CampaignDTO;
import com.leyue.usercenter.domain.promotion.Campaign;
import com.leyue.usercenter.domain.promotion.Campaign.CampaignStatus;
import com.leyue.usercenter.domain.promotion.Campaign.CampaignType;
import com.leyue.usercenter.domain.promotion.gateway.CampaignGateway;
import com.leyue.usercenter.infrastructure.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignServiceI {
    
    private final CampaignGateway campaignGateway;
    private final OutboxEventPublisher outboxEventPublisher;
    
    @Override
    @Transactional
    public SingleResponse<CampaignDTO> createCampaign(CreateCampaignCmd cmd) {
        log.info("创建营销活动: {}", cmd.getName());
        
        // 检查活动名称是否已存在
        if (campaignGateway.existsByName(cmd.getName())) {
            return SingleResponse.buildFailure("CAMPAIGN_NAME_EXISTS", "活动名称已存在");
        }
        
        // 验证时间
        if (cmd.getStartTime().isAfter(cmd.getEndTime())) {
            return SingleResponse.buildFailure("INVALID_TIME", "开始时间不能晚于结束时间");
        }
        
        try {
            Campaign campaign = new Campaign(
                cmd.getName(),
                cmd.getDescription(),
                CampaignType.valueOf(cmd.getType()),
                cmd.getStartTime(),
                cmd.getEndTime(),
                cmd.getTargetAudience(),
                cmd.getRules(),
                cmd.getBudget(),
                cmd.getParticipantLimit(),
                getCurrentUser()
            );
            
            Campaign saved = campaignGateway.save(campaign);
            
            // 发布活动创建事件
            outboxEventPublisher.publish("CAMPAIGN_CREATED", 
                String.format("{\"campaignId\":%d,\"name\":\"%s\"}", saved.getId(), saved.getName()));
            
            return SingleResponse.of(convertToDTO(saved));
            
        } catch (Exception e) {
            log.error("创建营销活动失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_FAILED", "创建活动失败");
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<CampaignDTO> updateCampaign(UpdateCampaignCmd cmd) {
        log.info("更新营销活动: ID={}", cmd.getId());
        
        Campaign campaign = campaignGateway.getById(cmd.getId());
        if (campaign == null) {
            return SingleResponse.buildFailure("CAMPAIGN_NOT_FOUND", "活动不存在");
        }
        
        // 只有草稿状态的活动才能编辑
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            return SingleResponse.buildFailure("INVALID_STATUS", "只有草稿状态的活动才能编辑");
        }
        
        try {
            // 更新字段
            campaign.setName(cmd.getName());
            campaign.setDescription(cmd.getDescription());
            campaign.setType(CampaignType.valueOf(cmd.getType()));
            campaign.setStartTime(cmd.getStartTime());
            campaign.setEndTime(cmd.getEndTime());
            campaign.setTargetAudience(cmd.getTargetAudience());
            campaign.setRules(cmd.getRules());
            campaign.setBudget(cmd.getBudget());
            campaign.setParticipantLimit(cmd.getParticipantLimit());
            campaign.setUpdatedBy(getCurrentUser());
            campaign.setUpdatedAt(LocalDateTime.now());
            
            Campaign saved = campaignGateway.save(campaign);
            
            // 发布活动更新事件
            outboxEventPublisher.publish("CAMPAIGN_UPDATED", 
                String.format("{\"campaignId\":%d,\"name\":\"%s\"}", saved.getId(), saved.getName()));
            
            return SingleResponse.of(convertToDTO(saved));
            
        } catch (Exception e) {
            log.error("更新营销活动失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("UPDATE_FAILED", "更新活动失败");
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<Void> activateCampaign(Long id) {
        log.info("激活营销活动: ID={}", id);
        
        Campaign campaign = campaignGateway.getById(id);
        if (campaign == null) {
            return SingleResponse.buildFailure("CAMPAIGN_NOT_FOUND", "活动不存在");
        }
        
        try {
            campaign.activate();
            campaignGateway.save(campaign);
            
            // 发布活动激活事件
            outboxEventPublisher.publish("CAMPAIGN_ACTIVATED", 
                String.format("{\"campaignId\":%d,\"name\":\"%s\"}", campaign.getId(), campaign.getName()));
            
            return SingleResponse.buildSuccess();
            
        } catch (IllegalStateException e) {
            return SingleResponse.buildFailure("INVALID_STATUS", e.getMessage());
        } catch (Exception e) {
            log.error("激活营销活动失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("ACTIVATE_FAILED", "激活活动失败");
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<Void> pauseCampaign(Long id) {
        log.info("暂停营销活动: ID={}", id);
        
        Campaign campaign = campaignGateway.getById(id);
        if (campaign == null) {
            return SingleResponse.buildFailure("CAMPAIGN_NOT_FOUND", "活动不存在");
        }
        
        try {
            campaign.pause();
            campaignGateway.save(campaign);
            
            // 发布活动暂停事件
            outboxEventPublisher.publish("CAMPAIGN_PAUSED", 
                String.format("{\"campaignId\":%d,\"name\":\"%s\"}", campaign.getId(), campaign.getName()));
            
            return SingleResponse.buildSuccess();
            
        } catch (IllegalStateException e) {
            return SingleResponse.buildFailure("INVALID_STATUS", e.getMessage());
        } catch (Exception e) {
            log.error("暂停营销活动失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("PAUSE_FAILED", "暂停活动失败");
        }
    }
    
    @Override
    public PageResponse<CampaignDTO> getCampaignPage(CampaignPageQuery query) {
        log.info("分页查询营销活动");
        
        try {
            CampaignStatus status = query.getStatus() != null ? 
                CampaignStatus.valueOf(query.getStatus()) : null;
            CampaignType type = query.getType() != null ? 
                CampaignType.valueOf(query.getType()) : null;
            
            List<Campaign> campaigns = campaignGateway.getCampaignsByPage(
                query.getPageIndex(), query.getPageSize(), 
                query.getName(), status, type);
            
            long total = campaignGateway.countCampaignsByPage(
                query.getName(), status, type);
            
            List<CampaignDTO> dtoList = campaigns.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return PageResponse.of(dtoList, (int) total, query.getPageSize(), query.getPageIndex());
            
        } catch (Exception e) {
            log.error("分页查询营销活动失败: {}", e.getMessage(), e);
            return PageResponse.buildFailure("QUERY_FAILED", "查询失败");
        }
    }
    
    @Override
    public SingleResponse<CampaignDTO> getCampaignById(Long id) {
        log.info("查询营销活动详情: ID={}", id);
        
        try {
            Campaign campaign = campaignGateway.getById(id);
            if (campaign == null) {
                return SingleResponse.buildFailure("CAMPAIGN_NOT_FOUND", "活动不存在");
            }
            
            return SingleResponse.of(convertToDTO(campaign));
            
        } catch (Exception e) {
            log.error("查询营销活动详情失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("QUERY_FAILED", "查询失败");
        }
    }
    
    private CampaignDTO convertToDTO(Campaign campaign) {
        CampaignDTO dto = new CampaignDTO();
        dto.setId(campaign.getId());
        dto.setName(campaign.getName());
        dto.setDescription(campaign.getDescription());
        dto.setType(campaign.getType().name());
        dto.setStatus(campaign.getStatus().name());
        dto.setStartTime(campaign.getStartTime());
        dto.setEndTime(campaign.getEndTime());
        dto.setTargetAudience(campaign.getTargetAudience());
        dto.setRules(campaign.getRules());
        dto.setBudget(campaign.getBudget());
        dto.setUsedBudget(campaign.getUsedBudget());
        dto.setParticipantLimit(campaign.getParticipantLimit());
        dto.setCurrentParticipants(campaign.getCurrentParticipants());
        dto.setCreatedBy(campaign.getCreatedBy());
        dto.setCreatedAt(campaign.getCreatedAt());
        dto.setUpdatedBy(campaign.getUpdatedBy());
        dto.setUpdatedAt(campaign.getUpdatedAt());
        return dto;
    }
    
    private String getCurrentUser() {
        // TODO: 从安全上下文获取当前用户
        return "system";
    }
}