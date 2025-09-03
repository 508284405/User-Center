package com.leyue.usercenter.dto.command.groupbuy;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateGroupBuyActivityCmd extends Command {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String updatedBy;
}