package com.leyue.usercenter.domain.automation.gateway;

import com.leyue.usercenter.domain.automation.WorkflowExecution;
import com.leyue.usercenter.domain.automation.WorkflowExecution.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkflowExecutionGateway {
    
    WorkflowExecution save(WorkflowExecution execution);
    
    WorkflowExecution getById(Long id);
    
    List<WorkflowExecution> getByWorkflowId(Long workflowId);
    
    List<WorkflowExecution> getByUserId(String userId);
    
    List<WorkflowExecution> getByStatus(ExecutionStatus status);
    
    List<WorkflowExecution> getPendingExecutions();
    
    List<WorkflowExecution> getScheduledExecutions(LocalDateTime now);
    
    List<WorkflowExecution> getRunningExecutions();
    
    List<WorkflowExecution> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<WorkflowExecution> getByWorkflowAndUser(Long workflowId, String userId);
    
    List<WorkflowExecution> getTimeoutExecutions(LocalDateTime cutoffTime);
    
    void deleteBefore(LocalDateTime cutoffDate);
    
    // 统计查询
    long countByWorkflowId(Long workflowId);
    
    long countByStatus(ExecutionStatus status);
    
    double getSuccessRateByWorkflow(Long workflowId);
    
    List<WorkflowExecution> getRecentExecutions(int limit);
}