package com.leyue.usercenter.dto.command.groupbuy;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateGroupCmd extends Command {
    
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    @NotBlank(message = "团长用户ID不能为空")
    private String leaderUserId;
    
    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须为正数")
    private Integer quantity;
}