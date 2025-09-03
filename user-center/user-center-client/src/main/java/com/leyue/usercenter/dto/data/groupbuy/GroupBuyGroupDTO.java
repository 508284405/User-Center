package com.leyue.usercenter.dto.data.groupbuy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyGroupDTO {
    
    private Long id;
    
    private Long activityId;
    
    private String groupNumber;
    
    private String leaderUserId;
    
    private String status;
    
    private String statusDesc;
    
    private Integer requiredParticipants;
    
    private Integer currentParticipants;
    
    private BigDecimal groupPrice;
    
    private BigDecimal totalAmount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expireTime;
    
    private LocalDateTime completedAt;
}