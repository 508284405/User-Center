package com.leyue.usercenter.dto.command.seckill;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class SeckillParticipateCmd extends Command {
    
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer quantity;
}