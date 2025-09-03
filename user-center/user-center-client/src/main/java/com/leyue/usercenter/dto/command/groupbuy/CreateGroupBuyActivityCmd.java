package com.leyue.usercenter.dto.command.groupbuy;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateGroupBuyActivityCmd extends Command {
    
    @NotBlank(message = "活动名称不能为空")
    private String name;
    
    private String description;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    
    @NotNull(message = "原价不能为空")
    @Positive(message = "原价必须为正数")
    private BigDecimal originalPrice;
    
    @NotNull(message = "团购价不能为空")
    @Positive(message = "团购价必须为正数")
    private BigDecimal groupPrice;
    
    @NotNull(message = "成团人数不能为空")
    @Positive(message = "成团人数必须为正数")
    private Integer requiredParticipants;
    
    private Integer maxParticipants;
    
    private Integer limitPerUser = 1;
    
    private Integer groupTimeoutHours = 24;
    
    @NotNull(message = "总库存不能为空")
    @Positive(message = "总库存必须为正数")
    private Integer totalStock;
    
    @NotBlank(message = "创建者不能为空")
    private String createdBy;
}