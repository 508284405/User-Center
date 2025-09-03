package com.leyue.usercenter.dto.data.groupbuy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyActivityDTO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String status;
    
    private String statusDesc;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long productId;
    
    private String productName;
    
    private BigDecimal originalPrice;
    
    private BigDecimal groupPrice;
    
    private Integer requiredParticipants;
    
    private Integer maxParticipants;
    
    private Integer limitPerUser;
    
    private Integer groupTimeoutHours;
    
    private Integer totalStock;
    
    private Integer availableStock;
    
    private Integer soldCount;
    
    private Integer totalGroups;
    
    private Integer successfulGroups;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private String updatedBy;
    
    private LocalDateTime updatedAt;
}