package com.leyue.usercenter.domain.automation;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkflowExecution {
    
    private Long id;
    private Long workflowId;
    private String workflowName;
    private String userId;
    private ExecutionStatus status;
    private String triggerEvent;
    private Map<String, Object> triggerData;
    private Map<String, Object> executionContext;
    
    private LocalDateTime scheduledAt;  // 计划执行时间
    private LocalDateTime startedAt;    // 开始执行时间
    private LocalDateTime completedAt;  // 完成时间
    private Integer executionDuration; // 执行耗时（秒）
    
    private String currentStepId;       // 当前执行步骤
    private Integer currentStepIndex;   // 当前步骤索引
    private Integer totalSteps;         // 总步骤数
    private String executionResult;     // 执行结果（JSON）
    private String errorMessage;       // 错误信息
    private String executionLog;       // 执行日志
    
    private LocalDateTime createdAt;
    
    public WorkflowExecution() {}
    
    public WorkflowExecution(Long workflowId, String workflowName, String userId, 
                           String triggerEvent, Map<String, Object> triggerData) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.userId = userId;
        this.triggerEvent = triggerEvent;
        this.triggerData = triggerData;
        this.status = ExecutionStatus.PENDING;
        this.currentStepIndex = 0;
        this.scheduledAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        calculateDuration();
    }
    
    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        calculateDuration();
    }
    
    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
        calculateDuration();
    }
    
    public void pause() {
        this.status = ExecutionStatus.PAUSED;
    }
    
    public void resume() {
        if (this.status != ExecutionStatus.PAUSED) {
            throw new IllegalStateException("只有暂停状态的执行才能恢复");
        }
        this.status = ExecutionStatus.RUNNING;
    }
    
    public void updateStep(String stepId, int stepIndex) {
        this.currentStepId = stepId;
        this.currentStepIndex = stepIndex;
    }
    
    public void appendLog(String logMessage) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = String.format("[%s] %s\n", timestamp, logMessage);
        
        if (this.executionLog == null) {
            this.executionLog = logEntry;
        } else {
            this.executionLog += logEntry;
        }
    }
    
    public boolean isCompleted() {
        return this.status == ExecutionStatus.COMPLETED ||
               this.status == ExecutionStatus.FAILED ||
               this.status == ExecutionStatus.CANCELLED;
    }
    
    public boolean isRunning() {
        return this.status == ExecutionStatus.RUNNING;
    }
    
    public double getProgressPercentage() {
        if (this.totalSteps == null || this.totalSteps == 0) {
            return 0.0;
        }
        return (double) this.currentStepIndex / this.totalSteps * 100.0;
    }
    
    private void calculateDuration() {
        if (this.startedAt != null && this.completedAt != null) {
            this.executionDuration = (int) java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }
    
    public enum ExecutionStatus {
        PENDING("等待执行"),
        RUNNING("执行中"),
        PAUSED("已暂停"),
        COMPLETED("已完成"),
        FAILED("执行失败"),
        CANCELLED("已取消"),
        TIMEOUT("执行超时");
        
        private final String description;
        
        ExecutionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}