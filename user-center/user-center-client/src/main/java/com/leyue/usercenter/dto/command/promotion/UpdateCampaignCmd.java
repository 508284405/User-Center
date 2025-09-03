package com.leyue.usercenter.dto.command.promotion;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateCampaignCmd extends Command {
    
    @NotNull(message = "活动ID不能为空")
    private Long id;
    
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称长度不能超过100字符")
    private String name;
    
    @Size(max = 500, message = "活动描述长度不能超过500字符")
    private String description;
    
    @NotBlank(message = "活动类型不能为空")
    private String type;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    private String targetAudience;
    
    private String rules;
    
    @Min(value = 0, message = "预算不能小于0")
    private Integer budget;
    
    @Min(value = 1, message = "参与人数限制不能小于1")
    private Integer participantLimit;
}