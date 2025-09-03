package com.leyue.usercenter.web;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.SeckillServiceI;
import com.leyue.usercenter.dto.command.seckill.CreateSeckillActivityCmd;
import com.leyue.usercenter.dto.command.seckill.SeckillParticipateCmd;
import com.leyue.usercenter.dto.data.seckill.SeckillActivityDTO;
import com.leyue.usercenter.dto.data.seckill.SeckillOrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "秒杀系统管理", description = "Seckill System Management APIs")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@Validated
public class SeckillController {
    
    private final SeckillServiceI seckillService;
    
    @Operation(summary = "创建秒杀活动", description = "Create a new seckill activity")
    @PostMapping("/activities")
    public SingleResponse<SeckillActivityDTO> createActivity(@Valid @RequestBody CreateSeckillActivityCmd cmd) {
        return seckillService.createActivity(cmd);
    }
    
    @Operation(summary = "获取秒杀活动详情", description = "Get seckill activity details by ID")
    @GetMapping("/activities/{id}")
    public SingleResponse<SeckillActivityDTO> getActivity(
            @Parameter(description = "Activity ID") @PathVariable Long id) {
        return seckillService.getActivityById(id);
    }
    
    @Operation(summary = "开始秒杀活动", description = "Start a seckill activity")
    @PostMapping("/activities/{id}/start")
    public SingleResponse<Void> startActivity(
            @Parameter(description = "Activity ID") @PathVariable Long id) {
        return seckillService.startActivity(id);
    }
    
    @Operation(summary = "结束秒杀活动", description = "End a seckill activity")
    @PostMapping("/activities/{id}/end")
    public SingleResponse<Void> endActivity(
            @Parameter(description = "Activity ID") @PathVariable Long id) {
        return seckillService.endActivity(id);
    }
    
    @Operation(summary = "暂停秒杀活动", description = "Pause a seckill activity")
    @PostMapping("/activities/{id}/pause")
    public SingleResponse<Void> pauseActivity(
            @Parameter(description = "Activity ID") @PathVariable Long id) {
        return seckillService.pauseActivity(id);
    }
    
    @Operation(summary = "恢复秒杀活动", description = "Resume a paused seckill activity")
    @PostMapping("/activities/{id}/resume")
    public SingleResponse<Void> resumeActivity(
            @Parameter(description = "Activity ID") @PathVariable Long id) {
        return seckillService.resumeActivity(id);
    }
    
    @Operation(summary = "参与秒杀", description = "Participate in seckill activity")
    @PostMapping("/participate")
    public SingleResponse<SeckillOrderDTO> participate(@Valid @RequestBody SeckillParticipateCmd cmd) {
        return seckillService.participate(cmd);
    }
    
    @Operation(summary = "支付秒杀订单", description = "Pay for seckill order")
    @PostMapping("/orders/{orderId}/pay")
    public SingleResponse<Void> payOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        return seckillService.payOrder(orderId);
    }
    
    @Operation(summary = "取消秒杀订单", description = "Cancel seckill order")
    @PostMapping("/orders/{orderId}/cancel")
    public SingleResponse<Void> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId,
            @RequestParam(defaultValue = "用户主动取消") String reason) {
        return seckillService.cancelOrder(orderId, reason);
    }
    
    @Operation(summary = "获取秒杀订单详情", description = "Get seckill order details by ID")
    @GetMapping("/orders/{id}")
    public SingleResponse<SeckillOrderDTO> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return seckillService.getOrderById(id);
    }
}