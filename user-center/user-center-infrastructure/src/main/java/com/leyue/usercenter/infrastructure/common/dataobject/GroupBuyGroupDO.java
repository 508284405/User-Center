package com.leyue.usercenter.infrastructure.common.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.usercenter.domain.groupbuy.GroupBuyGroup;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("groupbuy_groups")
public class GroupBuyGroupDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long activityId;
    
    private String groupNumber;
    
    private String leaderUserId;
    
    private GroupBuyGroup.GroupStatus status;
    
    private Integer requiredParticipants;
    
    private Integer currentParticipants;
    
    private BigDecimal groupPrice;
    
    private BigDecimal totalAmount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expireTime;
    
    private LocalDateTime completedAt;
}