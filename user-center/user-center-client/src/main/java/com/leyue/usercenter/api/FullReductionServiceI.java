package com.leyue.usercenter.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.command.promotion.CreateFullReductionActivityCmd;
import com.leyue.usercenter.dto.command.promotion.UpdateFullReductionActivityCmd;
import com.leyue.usercenter.dto.data.promotion.FullReductionActivityDTO;
import com.leyue.usercenter.dto.data.promotion.FullReductionCalculationDTO;
import com.leyue.usercenter.dto.query.promotion.FullReductionActivityPageQry;
import com.leyue.usercenter.dto.query.promotion.FullReductionActivityQry;

import java.math.BigDecimal;
import java.util.List;

public interface FullReductionServiceI {
    
    SingleResponse<FullReductionActivityDTO> createActivity(CreateFullReductionActivityCmd cmd);
    
    SingleResponse<FullReductionActivityDTO> updateActivity(UpdateFullReductionActivityCmd cmd);
    
    SingleResponse<FullReductionActivityDTO> getActivity(FullReductionActivityQry qry);
    
    PageResponse<FullReductionActivityDTO> getActivityPage(FullReductionActivityPageQry qry);
    
    MultiResponse<FullReductionActivityDTO> getActiveActivities();
    
    Response deleteActivity(Long id);
    
    Response activateActivity(Long id);
    
    Response pauseActivity(Long id);
    
    Response endActivity(Long id);
    
    SingleResponse<FullReductionCalculationDTO> calculateReduction(String userId, BigDecimal orderAmount, 
                                                                  List<Long> productIds, List<Long> categoryIds);
    
    Response useActivity(Long activityId, String userId, String orderId, BigDecimal orderAmount, 
                        BigDecimal reductionAmount);
    
    MultiResponse<FullReductionActivityDTO> getUserUsableActivities(String userId, BigDecimal orderAmount);
}