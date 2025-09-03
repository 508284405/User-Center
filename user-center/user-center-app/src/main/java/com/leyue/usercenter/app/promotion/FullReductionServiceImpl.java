package com.leyue.usercenter.app.promotion;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.FullReductionServiceI;
import com.leyue.usercenter.domain.promotion.FullReductionActivity;
import com.leyue.usercenter.domain.promotion.gateway.FullReductionActivityGateway;
import com.leyue.usercenter.dto.command.promotion.CreateFullReductionActivityCmd;
import com.leyue.usercenter.dto.command.promotion.UpdateFullReductionActivityCmd;
import com.leyue.usercenter.dto.data.promotion.FullReductionActivityDTO;
import com.leyue.usercenter.dto.data.promotion.FullReductionCalculationDTO;
import com.leyue.usercenter.dto.query.promotion.FullReductionActivityPageQry;
import com.leyue.usercenter.dto.query.promotion.FullReductionActivityQry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FullReductionServiceImpl implements FullReductionServiceI {

    @Resource
    private FullReductionActivityGateway fullReductionActivityGateway;

    @Override
    @Transactional
    public SingleResponse<FullReductionActivityDTO> createActivity(CreateFullReductionActivityCmd cmd) {
        log.info("创建满减活动: {}", cmd.getName());
        
        try {
            FullReductionActivity activity = new FullReductionActivity(
                cmd.getName(),
                cmd.getDescription(),
                cmd.getStartTime(),
                cmd.getEndTime(),
                cmd.getCreatedBy()
            );
            
            FullReductionActivity savedActivity = fullReductionActivityGateway.save(activity);
            return SingleResponse.of(convertToDTO(savedActivity));
            
        } catch (Exception e) {
            log.error("创建满减活动失败", e);
            return SingleResponse.buildFailure("创建满减活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public SingleResponse<FullReductionActivityDTO> updateActivity(UpdateFullReductionActivityCmd cmd) {
        log.info("更新满减活动: {}", cmd.getId());
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(cmd.getId());
        if (activity == null) {
            return SingleResponse.buildFailure("满减活动不存在");
        }
        
        if (activity.getStatus() != FullReductionActivity.ActivityStatus.DRAFT) {
            return SingleResponse.buildFailure("只有草稿状态的活动才能编辑");
        }
        
        try {
            if (cmd.getName() != null) {
                activity.setName(cmd.getName());
            }
            if (cmd.getDescription() != null) {
                activity.setDescription(cmd.getDescription());
            }
            if (cmd.getUpdatedBy() != null) {
                activity.setUpdatedBy(cmd.getUpdatedBy());
                activity.setUpdatedAt(LocalDateTime.now());
            }
            
            FullReductionActivity updatedActivity = fullReductionActivityGateway.save(activity);
            return SingleResponse.of(convertToDTO(updatedActivity));
            
        } catch (Exception e) {
            log.error("更新满减活动失败", e);
            return SingleResponse.buildFailure("更新满减活动失败: " + e.getMessage());
        }
    }

    @Override
    public SingleResponse<FullReductionActivityDTO> getActivity(FullReductionActivityQry qry) {
        log.info("查询满减活动, id: {}", qry.getId());
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(qry.getId());
        if (activity == null) {
            return SingleResponse.buildFailure("满减活动不存在");
        }
        
        return SingleResponse.of(convertToDTO(activity));
    }

    @Override
    public PageResponse<FullReductionActivityDTO> getActivityPage(FullReductionActivityPageQry qry) {
        log.info("分页查询满减活动, pageNum: {}, pageSize: {}", qry.getPageIndex(), qry.getPageSize());
        
        List<FullReductionActivityDTO> dtoList = List.of();
        return PageResponse.of(dtoList, 0, qry.getPageIndex(), qry.getPageSize());
    }

    @Override
    public MultiResponse<FullReductionActivityDTO> getActiveActivities() {
        log.info("查询进行中的满减活动");
        
        List<FullReductionActivityDTO> dtoList = List.of();
        return MultiResponse.of(dtoList);
    }

    @Override
    @Transactional
    public Response deleteActivity(Long id) {
        log.info("删除满减活动: {}", id);
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("满减活动不存在");
        }
        
        if (activity.getStatus() != FullReductionActivity.ActivityStatus.DRAFT) {
            return Response.buildFailure("只有草稿状态的活动才能删除");
        }
        
        fullReductionActivityGateway.delete(id);
        return Response.buildSuccess();
    }

    @Override
    @Transactional
    public Response activateActivity(Long id) {
        log.info("激活满减活动: {}", id);
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("满减活动不存在");
        }
        
        try {
            activity.activate();
            fullReductionActivityGateway.save(activity);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("激活满减活动失败", e);
            return Response.buildFailure("激活失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response pauseActivity(Long id) {
        log.info("暂停满减活动: {}", id);
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("满减活动不存在");
        }
        
        try {
            activity.pause();
            fullReductionActivityGateway.save(activity);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("暂停满减活动失败", e);
            return Response.buildFailure("暂停失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response endActivity(Long id) {
        log.info("结束满减活动: {}", id);
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("满减活动不存在");
        }
        
        activity.expire();
        fullReductionActivityGateway.save(activity);
        return Response.buildSuccess();
    }

    @Override
    public SingleResponse<FullReductionCalculationDTO> calculateReduction(String userId, BigDecimal orderAmount,
                                                                         List<Long> productIds, List<Long> categoryIds) {
        log.info("计算满减金额, userId: {}, orderAmount: {}", userId, orderAmount);
        
        FullReductionCalculationDTO result = new FullReductionCalculationDTO();
        result.setCanUse(false);
        result.setOriginalAmount(orderAmount);
        result.setReductionAmount(BigDecimal.ZERO);
        result.setFinalAmount(orderAmount);
        result.setReason("暂无可用的满减活动");
        
        return SingleResponse.of(result);
    }

    @Override
    @Transactional
    public Response useActivity(Long activityId, String userId, String orderId, 
                               BigDecimal orderAmount, BigDecimal reductionAmount) {
        log.info("使用满减活动, activityId: {}, userId: {}, orderId: {}", activityId, userId, orderId);
        
        FullReductionActivity activity = fullReductionActivityGateway.getById(activityId);
        if (activity == null) {
            return Response.buildFailure("满减活动不存在");
        }
        
        try {
            boolean success = activity.use(userId, orderId, orderAmount, reductionAmount);
            if (success) {
                fullReductionActivityGateway.save(activity);
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("使用满减活动失败，可能已达到使用限制");
            }
        } catch (Exception e) {
            log.error("使用满减活动失败", e);
            return Response.buildFailure("使用失败: " + e.getMessage());
        }
    }

    @Override
    public MultiResponse<FullReductionActivityDTO> getUserUsableActivities(String userId, BigDecimal orderAmount) {
        log.info("查询用户可用的满减活动, userId: {}, orderAmount: {}", userId, orderAmount);
        
        List<FullReductionActivityDTO> dtoList = List.of();
        return MultiResponse.of(dtoList);
    }

    private FullReductionActivityDTO convertToDTO(FullReductionActivity activity) {
        FullReductionActivityDTO dto = new FullReductionActivityDTO();
        BeanUtils.copyProperties(activity, dto);
        
        if (activity.getStatus() != null) {
            dto.setStatus(activity.getStatus().name());
            dto.setStatusDesc(activity.getStatus().getDescription());
        }
        
        return dto;
    }
}