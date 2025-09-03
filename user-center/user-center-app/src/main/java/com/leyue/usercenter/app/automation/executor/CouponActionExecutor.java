package com.leyue.usercenter.app.automation.executor;

import com.leyue.usercenter.app.automation.WorkflowExecutionEngine.WorkflowActionExecutor;
import com.leyue.usercenter.domain.automation.MarketingWorkflow;
import com.leyue.usercenter.domain.automation.WorkflowExecution;
import com.leyue.usercenter.domain.loyalty.coupon.gateway.CouponGateway;
import com.leyue.usercenter.domain.loyalty.coupon.gateway.CouponTemplateGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("couponExecutor")
@RequiredArgsConstructor
public class CouponActionExecutor implements WorkflowActionExecutor {
    
    private final CouponGateway couponGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    
    @Override
    public void execute(MarketingWorkflow.WorkflowAction action, WorkflowExecution execution) {
        log.info("执行优惠券发放动作: userId={}, actionName={}", 
                execution.getUserId(), action.getActionName());
        
        try {
            Map<String, Object> parameters = action.getParameters();
            String templateId = (String) parameters.get("templateId");
            Integer quantity = (Integer) parameters.getOrDefault("quantity", 1);
            String reason = (String) parameters.getOrDefault("reason", "营销活动奖励");
            
            // 获取优惠券模板
            var couponTemplate = couponTemplateGateway.getById(Long.parseLong(templateId));
            if (couponTemplate == null) {
                throw new RuntimeException("优惠券模板不存在: " + templateId);
            }
            
            // 发放优惠券
            for (int i = 0; i < quantity; i++) {
                var coupon = couponTemplate.issueCoupon(execution.getUserId(), reason);
                couponGateway.save(coupon);
            }
            
            execution.appendLog(String.format("优惠券发放成功: 模板[%s] 数量[%d]", 
                    couponTemplate.getName(), quantity));
            
            log.info("优惠券发放完成: userId={}, templateId={}, quantity={}", 
                    execution.getUserId(), templateId, quantity);
            
        } catch (Exception e) {
            log.error("优惠券发放失败: userId={}, error={}", execution.getUserId(), e.getMessage(), e);
            execution.appendLog("优惠券发放失败: " + e.getMessage());
            throw e;
        }
    }
}