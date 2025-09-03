package com.leyue.usercenter.infrastructure.common.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("groupbuy_participants")
public class GroupBuyParticipantDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long groupId;
    
    private String userId;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalPrice;
    
    private LocalDateTime joinTime;
}