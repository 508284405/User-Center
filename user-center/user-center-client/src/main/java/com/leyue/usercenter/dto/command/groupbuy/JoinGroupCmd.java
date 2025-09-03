package com.leyue.usercenter.dto.command.groupbuy;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@EqualsAndHashCode(callSuper = false)
public class JoinGroupCmd extends Command {
    
    @NotNull(message = "团组ID不能为空")
    private Long groupId;
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须为正数")
    private Integer quantity;
}