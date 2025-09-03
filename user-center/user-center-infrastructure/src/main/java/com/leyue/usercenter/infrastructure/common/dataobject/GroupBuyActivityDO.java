package com.leyue.usercenter.infrastructure.common.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.usercenter.domain.groupbuy.GroupBuyActivity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("groupbuy_activities")
public class GroupBuyActivityDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private GroupBuyActivity.GroupBuyStatus status;
    
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