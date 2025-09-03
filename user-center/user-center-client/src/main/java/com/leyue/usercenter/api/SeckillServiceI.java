package com.leyue.usercenter.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.command.seckill.CreateSeckillActivityCmd;
import com.leyue.usercenter.dto.command.seckill.SeckillParticipateCmd;
import com.leyue.usercenter.dto.data.seckill.SeckillActivityDTO;
import com.leyue.usercenter.dto.data.seckill.SeckillOrderDTO;
import com.leyue.usercenter.dto.query.seckill.SeckillActivityPageQuery;
import com.leyue.usercenter.dto.query.seckill.SeckillOrderPageQuery;

public interface SeckillServiceI {
    
    SingleResponse<SeckillActivityDTO> createActivity(CreateSeckillActivityCmd cmd);
    
    SingleResponse<SeckillActivityDTO> getActivityById(Long id);
    
    PageResponse<SeckillActivityDTO> getActivityPage(SeckillActivityPageQuery query);
    
    SingleResponse<Void> startActivity(Long id);
    
    SingleResponse<Void> endActivity(Long id);
    
    SingleResponse<Void> pauseActivity(Long id);
    
    SingleResponse<Void> resumeActivity(Long id);
    
    SingleResponse<SeckillOrderDTO> participate(SeckillParticipateCmd cmd);
    
    SingleResponse<Void> payOrder(String orderId);
    
    SingleResponse<Void> cancelOrder(String orderId, String reason);
    
    PageResponse<SeckillOrderDTO> getOrderPage(SeckillOrderPageQuery query);
    
    SingleResponse<SeckillOrderDTO> getOrderById(Long id);
}