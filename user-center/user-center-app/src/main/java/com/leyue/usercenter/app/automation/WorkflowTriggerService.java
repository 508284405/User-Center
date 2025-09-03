package com.leyue.usercenter.app.automation;

import com.alibaba.cola.event.EventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.domain.automation.MarketingWorkflow;
import com.leyue.usercenter.domain.automation.gateway.MarketingWorkflowGateway;
import com.leyue.usercenter.domain.loyalty.event.OrderCompletedEvent;
import com.leyue.usercenter.domain.loyalty.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowTriggerService {
    
    private final MarketingWorkflowGateway workflowGateway;
    private final WorkflowExecutionEngine executionEngine;
    private final ObjectMapper objectMapper;
    
    /**
     * 处理用户注册事件，触发欢迎系列工作流
     */
    @Async
    public void handleUserRegistration(String userId, Map<String, Object> userInfo) {
        log.info("处理用户注册事件: userId={}", userId);
        
        try {
            triggerWorkflowsByEvent("USER_REGISTERED", userId, userInfo);
        } catch (Exception e) {
            log.error("处理用户注册事件失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 处理订单完成事件
     */
    @EventHandler
    @Async
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("处理订单完成事件: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        
        try {
            Map<String, Object> triggerData = new HashMap<>();
            triggerData.put("orderId", event.getOrderId());
            triggerData.put("orderAmount", event.getOrderAmount());
            triggerData.put("productType", event.getProductType());
            triggerData.put("completedAt", event.getCompletedAt());
            
            triggerWorkflowsByEvent("ORDER_COMPLETED", event.getUserId(), triggerData);
        } catch (Exception e) {
            log.error("处理订单完成事件失败: orderId={}, error={}", event.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理支付完成事件
     */
    @EventHandler
    @Async
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("处理支付完成事件: paymentId={}, userId={}", event.getPaymentId(), event.getUserId());
        
        try {
            Map<String, Object> triggerData = new HashMap<>();
            triggerData.put("paymentId", event.getPaymentId());
            triggerData.put("orderId", event.getOrderId());
            triggerData.put("paidAmount", event.getPaidAmount());
            triggerData.put("paymentMethod", event.getPaymentMethod());
            triggerData.put("paidAt", event.getPaidAt());
            
            triggerWorkflowsByEvent("PAYMENT_COMPLETED", event.getUserId(), triggerData);
        } catch (Exception e) {
            log.error("处理支付完成事件失败: paymentId={}, error={}", event.getPaymentId(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理购物车遗弃事件
     */
    @Async
    public void handleCartAbandoned(String userId, String cartId, Map<String, Object> cartData) {
        log.info("处理购物车遗弃事件: userId={}, cartId={}", userId, cartId);
        
        try {
            Map<String, Object> triggerData = new HashMap<>();
            triggerData.put("cartId", cartId);
            triggerData.put("abandonedAt", LocalDateTime.now());
            triggerData.putAll(cartData);
            
            triggerWorkflowsByEvent("CART_ABANDONED", userId, triggerData);
        } catch (Exception e) {
            log.error("处理购物车遗弃事件失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户生日事件
     */
    @Async
    public void handleUserBirthday(String userId, Map<String, Object> userInfo) {
        log.info("处理用户生日事件: userId={}", userId);
        
        try {
            Map<String, Object> triggerData = new HashMap<>();
            triggerData.put("birthday", LocalDateTime.now());
            triggerData.putAll(userInfo);
            
            triggerWorkflowsByEvent("USER_BIRTHDAY", userId, triggerData);
        } catch (Exception e) {
            log.error("处理用户生日事件失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户长期未活跃事件
     */
    @Async
    public void handleUserInactive(String userId, int inactiveDays) {
        log.info("处理用户不活跃事件: userId={}, inactiveDays={}", userId, inactiveDays);
        
        try {
            Map<String, Object> triggerData = new HashMap<>();
            triggerData.put("inactiveDays", inactiveDays);
            triggerData.put("lastActiveDate", LocalDateTime.now().minusDays(inactiveDays));
            
            triggerWorkflowsByEvent("USER_INACTIVE", userId, triggerData);
        } catch (Exception e) {
            log.error("处理用户不活跃事件失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 手动触发工作流
     */
    @Async
    public void triggerWorkflowManually(Long workflowId, String userId, Map<String, Object> triggerData) {
        log.info("手动触发工作流: workflowId={}, userId={}", workflowId, userId);
        
        try {
            MarketingWorkflow workflow = workflowGateway.getById(workflowId);
            if (workflow == null) {
                log.warn("工作流不存在: workflowId={}", workflowId);
                return;
            }
            
            if (!workflow.canExecute()) {
                log.warn("工作流无法执行: workflowId={}, status={}", workflowId, workflow.getStatus());
                return;
            }
            
            workflow.recordTrigger();
            executionEngine.executeWorkflow(workflow, userId, "MANUAL_TRIGGER", triggerData);
            
        } catch (Exception e) {
            log.error("手动触发工作流失败: workflowId={}, error={}", workflowId, e.getMessage(), e);
        }
    }
    
    /**
     * 根据事件类型触发相应的工作流
     */
    private void triggerWorkflowsByEvent(String triggerEvent, String userId, Map<String, Object> triggerData) {
        List<MarketingWorkflow> workflows = workflowGateway.getByTriggerEvent(triggerEvent);
        
        for (MarketingWorkflow workflow : workflows) {
            if (!workflow.canExecute()) {
                log.debug("跳过工作流: workflowId={}, status={}", workflow.getId(), workflow.getStatus());
                continue;
            }
            
            // 评估触发条件
            if (!evaluateTriggerConditions(workflow, userId, triggerData)) {
                log.debug("工作流触发条件不满足: workflowId={}", workflow.getId());
                continue;
            }
            
            // 记录触发
            workflow.recordTrigger();
            workflowGateway.save(workflow);
            
            // 执行工作流
            executionEngine.executeWorkflow(workflow, userId, triggerEvent, triggerData);
            
            log.info("工作流已触发: workflowId={}, userId={}, triggerEvent={}", 
                    workflow.getId(), userId, triggerEvent);
        }
    }
    
    /**
     * 评估工作流触发条件
     */
    private boolean evaluateTriggerConditions(MarketingWorkflow workflow, String userId, Map<String, Object> triggerData) {
        try {
            String triggerConfig = workflow.getTriggerConfig();
            if (triggerConfig == null || triggerConfig.trim().isEmpty()) {
                return true; // 没有配置条件，默认触发
            }
            
            // 解析触发条件
            Map<String, Object> conditions = objectMapper.readValue(triggerConfig, Map.class);
            
            // 简化的条件评估逻辑
            // 实际项目中可以实现更复杂的条件评估引擎
            return evaluateConditions(conditions, userId, triggerData);
            
        } catch (Exception e) {
            log.error("评估工作流触发条件失败: workflowId={}, error={}", workflow.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    private boolean evaluateConditions(Map<String, Object> conditions, String userId, Map<String, Object> triggerData) {
        // 实现条件评估逻辑
        // 例如：用户属性条件、数值比较条件、时间条件等
        
        // 用户类型条件
        if (conditions.containsKey("userType")) {
            String expectedType = (String) conditions.get("userType");
            // 这里需要查询用户类型
            // String actualType = getUserType(userId);
            // if (!expectedType.equals(actualType)) return false;
        }
        
        // 订单金额条件
        if (conditions.containsKey("minOrderAmount") && triggerData.containsKey("orderAmount")) {
            Double minAmount = ((Number) conditions.get("minOrderAmount")).doubleValue();
            Double actualAmount = ((Number) triggerData.get("orderAmount")).doubleValue();
            if (actualAmount < minAmount) return false;
        }
        
        // 时间条件
        if (conditions.containsKey("timeRange")) {
            // 实现时间范围条件评估
        }
        
        return true;
    }
}