package com.leyue.usercenter.dto.data.promotion;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CampaignDTO extends DTO {
    
    private Long id;
    private String name;
    private String description;
    private String type;
    private String typeDescription;
    private String status;
    private String statusDescription;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String targetAudience;
    private String rules;
    private Integer budget;
    private Integer usedBudget;
    private Integer participantLimit;
    private Integer currentParticipants;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    // 计算字段
    private Double budgetUsageRate;
    private Double participationRate;
    private Boolean canParticipate;
}