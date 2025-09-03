package com.leyue.usercenter.infrastructure.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Outbox事件数据对象
 * 
 * @author Claude Code
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("uc_outbox_event")
public class OutboxEventDO extends BaseDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String eventId;
    
    private String topic;
    
    private String payloadJson;
    
    private String status;
    
    private Integer retryCnt;
    
    private LocalDateTime nextRetryAt;
    
    private String errorMsg;
}