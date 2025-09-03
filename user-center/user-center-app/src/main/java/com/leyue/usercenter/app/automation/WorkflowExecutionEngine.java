package com.leyue.usercenter.app.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.domain.automation.MarketingWorkflow;
import com.leyue.usercenter.domain.automation.WorkflowExecution;
import com.leyue.usercenter.domain.automation.gateway.WorkflowExecutionGateway;
import com.leyue.usercenter.infrastructure.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutionEngine {
    
    private final WorkflowExecutionGateway executionGateway;
    private final OutboxEventPublisher outboxEventPublisher;
    private final ObjectMapper objectMapper;
    private final Map<String, WorkflowActionExecutor> actionExecutors;
    
    @Async
    @Transactional
    public CompletableFuture<Void> executeWorkflow(MarketingWorkflow workflow, String userId, 
                                                  String triggerEvent, Map<String, Object> triggerData) {
        log.info("开始执行营销工作流: workflowId={}, userId={}, triggerEvent={}", 
                workflow.getId(), userId, triggerEvent);
        
        WorkflowExecution execution = new WorkflowExecution(
            workflow.getId(), workflow.getName(), userId, triggerEvent, triggerData);
        
        try {
            execution.start();
            execution.appendLog("工作流执行开始");
            executionGateway.save(execution);
            
            // 解析工作流配置
            List<MarketingWorkflow.WorkflowAction> actions = parseWorkflowActions(workflow.getActionConfig());
            execution.setTotalSteps(actions.size());
            
            // 执行各个步骤
            for (int i = 0; i < actions.size(); i++) {
                MarketingWorkflow.WorkflowAction action = actions.get(i);
                execution.updateStep(action.getActionName(), i + 1);
                execution.appendLog("执行步骤: " + action.getActionName());
                executionGateway.save(execution);
                
                try {
                    // 检查是否需要延迟执行
                    if (action.getDelayMinutes() != null && action.getDelayMinutes() > 0) {
                        execution.appendLog("延迟执行: " + action.getDelayMinutes() + "分钟");
                        // 这里可以使用延迟队列或定时任务来实现延迟执行
                        // 暂时跳过延迟逻辑
                    }
                    
                    // 检查执行条件
                    if (!evaluateActionConditions(action, execution)) {
                        execution.appendLog("跳过步骤: 条件不满足");
                        continue;
                    }
                    
                    // 执行动作
                    executeAction(action, execution);
                    execution.appendLog("步骤执行成功: " + action.getActionName());
                    
                } catch (Exception e) {
                    log.error("工作流步骤执行失败: workflowId={}, step={}, error={}", 
                            workflow.getId(), action.getActionName(), e.getMessage(), e);
                    execution.appendLog("步骤执行失败: " + action.getActionName() + ", 错误: " + e.getMessage());
                    // 根据配置决定是否继续执行后续步骤
                    continue;
                }
            }
            
            execution.complete();
            execution.appendLog("工作流执行完成");
            executionGateway.save(execution);
            
            // 记录工作流执行成功
            workflow.recordExecution(true);
            
            // 发布工作流执行完成事件
            outboxEventPublisher.publish("WORKFLOW_EXECUTION_COMPLETED", 
                String.format("{\"workflowId\":%d,\"executionId\":%d,\"userId\":\"%s\"}", 
                        workflow.getId(), execution.getId(), userId));
            
            log.info("营销工作流执行完成: workflowId={}, executionId={}", workflow.getId(), execution.getId());
            
        } catch (Exception e) {
            log.error("营销工作流执行失败: workflowId={}, error={}", workflow.getId(), e.getMessage(), e);
            execution.fail(e.getMessage());
            executionGateway.save(execution);
            
            // 记录工作流执行失败
            workflow.recordExecution(false);
            
            // 发布工作流执行失败事件
            outboxEventPublisher.publish("WORKFLOW_EXECUTION_FAILED", 
                String.format("{\"workflowId\":%d,\"executionId\":%d,\"userId\":\"%s\",\"error\":\"%s\"}", 
                        workflow.getId(), execution.getId(), userId, e.getMessage()));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private List<MarketingWorkflow.WorkflowAction> parseWorkflowActions(String actionConfig) {
        try {
            return objectMapper.readValue(actionConfig, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, MarketingWorkflow.WorkflowAction.class));
        } catch (Exception e) {
            log.error("解析工作流动作配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("无效的工作流配置", e);
        }
    }
    
    private boolean evaluateActionConditions(MarketingWorkflow.WorkflowAction action, WorkflowExecution execution) {
        // 简化的条件评估逻辑
        if (action.getConditions() == null || action.getConditions().isEmpty()) {
            return true;
        }
        
        // 这里可以实现复杂的条件评估逻辑
        // 例如基于用户属性、行为数据、时间条件等
        return true;
    }
    
    private void executeAction(MarketingWorkflow.WorkflowAction action, WorkflowExecution execution) {
        String actionType = action.getActionType();
        WorkflowActionExecutor executor = actionExecutors.get(actionType.toLowerCase() + "Executor");
        
        if (executor == null) {
            throw new RuntimeException("不支持的动作类型: " + actionType);
        }
        
        executor.execute(action, execution);
    }
    
    @Async
    public void processScheduledWorkflows() {
        log.info("处理定时工作流");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<WorkflowExecution> scheduledExecutions = executionGateway.getScheduledExecutions(now);
            
            for (WorkflowExecution execution : scheduledExecutions) {
                log.info("执行定时工作流: executionId={}", execution.getId());
                // 重新执行工作流（这里需要获取工作流定义）
                // executeWorkflow(workflow, execution.getUserId(), "SCHEDULED", execution.getTriggerData());
            }
            
        } catch (Exception e) {
            log.error("处理定时工作流失败", e);
        }
    }
    
    @Async
    public void processTimeoutExecutions() {
        log.info("处理超时的工作流执行");
        
        try {
            // 超时时间设置为24小时
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            List<WorkflowExecution> timeoutExecutions = executionGateway.getTimeoutExecutions(cutoffTime);
            
            for (WorkflowExecution execution : timeoutExecutions) {
                log.warn("工作流执行超时: executionId={}", execution.getId());
                execution.fail("执行超时");
                execution.appendLog("执行超时，自动标记为失败");
                executionGateway.save(execution);
            }
            
        } catch (Exception e) {
            log.error("处理超时工作流执行失败", e);
        }
    }
    
    public interface WorkflowActionExecutor {
        void execute(MarketingWorkflow.WorkflowAction action, WorkflowExecution execution);
    }
}