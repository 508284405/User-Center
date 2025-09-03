package com.leyue.usercenter.dto.command.seckill;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateSeckillActivityCmd extends Command {
    
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称长度不能超过100字符")
    private String name;
    
    @Size(max = 500, message = "活动描述长度不能超过500字符")
    private String description;
    
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    
    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;
    
    @NotNull(message = "秒杀价不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价必须大于0")
    private BigDecimal seckillPrice;
    
    @NotNull(message = "总库存不能为空")
    @Min(value = 1, message = "总库存必须大于0")
    private Integer totalStock;
    
    @NotNull(message = "单用户限购数量不能为空")
    @Min(value = 1, message = "单用户限购数量必须大于0")
    private Integer limitPerUser;
}