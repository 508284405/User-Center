package com.leyue.usercenter.domain.promotion;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Campaign {
    
    private Long id;
    private String name;
    private String description;
    private CampaignType type;
    private CampaignStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String targetAudience; // JSON配置目标用户群体
    private String rules; // JSON配置活动规则
    private Integer budget; // 预算（分）
    private Integer usedBudget; // 已使用预算（分）
    private Integer participantLimit; // 参与人数限制
    private Integer currentParticipants; // 当前参与人数
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    public Campaign() {}
    
    public Campaign(String name, String description, CampaignType type, 
                   LocalDateTime startTime, LocalDateTime endTime, 
                   String targetAudience, String rules, Integer budget, 
                   Integer participantLimit, String createdBy) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = CampaignStatus.DRAFT;
        this.startTime = startTime;
        this.endTime = endTime;
        this.targetAudience = targetAudience;
        this.rules = rules;
        this.budget = budget;
        this.usedBudget = 0;
        this.participantLimit = participantLimit;
        this.currentParticipants = 0;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    public void activate() {
        validateForActivation();
        this.status = CampaignStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void pause() {
        if (this.status != CampaignStatus.ACTIVE) {
            throw new IllegalStateException("只有活跃状态的活动才能暂停");
        }
        this.status = CampaignStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resume() {
        if (this.status != CampaignStatus.PAUSED) {
            throw new IllegalStateException("只有暂停状态的活动才能恢复");
        }
        this.status = CampaignStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void complete() {
        this.status = CampaignStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean canParticipate() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == CampaignStatus.ACTIVE 
                && now.isAfter(this.startTime) 
                && now.isBefore(this.endTime)
                && (this.participantLimit == null || this.currentParticipants < this.participantLimit)
                && (this.budget == null || this.usedBudget < this.budget);
    }
    
    public void addParticipant(Integer cost) {
        if (!canParticipate()) {
            throw new IllegalStateException("活动当前状态不允许参与");
        }
        
        this.currentParticipants++;
        if (cost != null && cost > 0) {
            this.usedBudget += cost;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    private void validateForActivation() {
        LocalDateTime now = LocalDateTime.now();
        
        if (this.status != CampaignStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的活动才能激活");
        }
        
        if (this.endTime.isBefore(now)) {
            throw new IllegalStateException("活动结束时间不能早于当前时间");
        }
        
        if (this.startTime.isAfter(this.endTime)) {
            throw new IllegalStateException("活动开始时间不能晚于结束时间");
        }
    }
    
    public enum CampaignType {
        DISCOUNT("折扣活动"),
        POINTS_MULTIPLY("积分翻倍"),
        COUPON_RAIN("优惠券雨"),
        LUCKY_DRAW("抽奖活动"),
        LIMITED_TIME_OFFER("限时优惠");
        
        private final String description;
        
        CampaignType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum CampaignStatus {
        DRAFT("草稿"),
        ACTIVE("活跃"),
        PAUSED("暂停"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String description;
        
        CampaignStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}