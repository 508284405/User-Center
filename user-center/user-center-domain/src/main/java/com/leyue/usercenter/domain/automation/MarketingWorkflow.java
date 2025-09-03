package com.leyue.usercenter.domain.automation;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class MarketingWorkflow {
    
    private Long id;
    private String name;
    private String description;
    private WorkflowType type;
    private WorkflowStatus status;
    private String triggerConfig; // JSON配置触发条件
    private String actionConfig;  // JSON配置执行动作
    private Integer priority;
    private Boolean isActive;
    
    // 执行统计
    private Integer triggerCount;      // 触发次数
    private Integer successCount;      // 成功执行次数
    private Integer failureCount;      // 失败次数
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime lastExecutedAt;
    
    // 条件配置
    private Map<String, Object> conditions; // 触发条件
    private Map<String, Object> filters;    // 筛选条件
    
    // 执行动作配置
    private List<WorkflowAction> actions;   // 执行动作列表
    private Integer delayMinutes;           // 延迟执行时间（分钟）
    private String scheduleConfig;          // 定时执行配置（cron表达式）
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    public MarketingWorkflow() {}
    
    public MarketingWorkflow(String name, String description, WorkflowType type, 
                           String triggerConfig, String actionConfig, String createdBy) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = WorkflowStatus.DRAFT;
        this.triggerConfig = triggerConfig;
        this.actionConfig = actionConfig;
        this.priority = 5;
        this.isActive = false;
        this.triggerCount = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    public void activate() {
        validateForActivation();
        this.status = WorkflowStatus.ACTIVE;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.status = WorkflowStatus.INACTIVE;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void pause() {
        if (this.status != WorkflowStatus.ACTIVE) {
            throw new IllegalStateException("只有激活状态的工作流才能暂停");
        }
        this.status = WorkflowStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resume() {
        if (this.status != WorkflowStatus.PAUSED) {
            throw new IllegalStateException("只有暂停状态的工作流才能恢复");
        }
        this.status = WorkflowStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void recordTrigger() {
        this.triggerCount++;
        this.lastTriggeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void recordExecution(boolean success) {
        if (success) {
            this.successCount++;
        } else {
            this.failureCount++;
        }
        this.lastExecutedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean canExecute() {
        return this.isActive && this.status == WorkflowStatus.ACTIVE;
    }
    
    public double getSuccessRate() {
        int totalExecutions = this.successCount + this.failureCount;
        if (totalExecutions == 0) {
            return 0.0;
        }
        return (double) this.successCount / totalExecutions * 100.0;
    }
    
    private void validateForActivation() {
        if (this.triggerConfig == null || this.triggerConfig.trim().isEmpty()) {
            throw new IllegalStateException("触发配置不能为空");
        }
        
        if (this.actionConfig == null || this.actionConfig.trim().isEmpty()) {
            throw new IllegalStateException("动作配置不能为空");
        }
    }
    
    public enum WorkflowType {
        USER_JOURNEY("用户旅程"),
        ABANDONED_CART("购物车遗弃"),
        RE_ENGAGEMENT("用户重新激活"),
        WELCOME_SERIES("欢迎系列"),
        BIRTHDAY_CAMPAIGN("生日营销"),
        WIN_BACK("流失用户召回"),
        CROSS_SELL("交叉销售"),
        UP_SELL("向上销售"),
        LOYALTY_REWARDS("忠诚度奖励"),
        FEEDBACK_COLLECTION("反馈收集");
        
        private final String description;
        
        WorkflowType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum WorkflowStatus {
        DRAFT("草稿"),
        ACTIVE("激活"),
        INACTIVE("未激活"),
        PAUSED("暂停"),
        ARCHIVED("已归档");
        
        private final String description;
        
        WorkflowStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Data
    public static class WorkflowAction {
        private String actionType; // EMAIL, SMS, PUSH, COUPON, POINTS, WEBHOOK
        private String actionName;
        private Map<String, Object> parameters;
        private Integer delayMinutes;
        private Map<String, Object> conditions;
    }
}