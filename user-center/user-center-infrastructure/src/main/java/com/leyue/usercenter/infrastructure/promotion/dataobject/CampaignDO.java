package com.leyue.usercenter.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("marketing_campaigns")
public class CampaignDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private String type;
    private String status;
    
    @TableField("start_time")
    private LocalDateTime startTime;
    
    @TableField("end_time")
    private LocalDateTime endTime;
    
    @TableField("target_audience")
    private String targetAudience;
    
    private String rules;
    private Integer budget;
    
    @TableField("used_budget")
    private Integer usedBudget;
    
    @TableField("participant_limit")
    private Integer participantLimit;
    
    @TableField("current_participants")
    private Integer currentParticipants;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField("updated_by")
    private String updatedBy;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}