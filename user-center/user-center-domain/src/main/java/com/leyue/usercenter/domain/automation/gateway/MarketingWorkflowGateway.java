package com.leyue.usercenter.domain.automation.gateway;

import com.leyue.usercenter.domain.automation.MarketingWorkflow;
import com.leyue.usercenter.domain.automation.MarketingWorkflow.WorkflowStatus;
import com.leyue.usercenter.domain.automation.MarketingWorkflow.WorkflowType;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketingWorkflowGateway {
    
    MarketingWorkflow save(MarketingWorkflow workflow);
    
    MarketingWorkflow getById(Long id);
    
    List<MarketingWorkflow> getByStatus(WorkflowStatus status);
    
    List<MarketingWorkflow> getByType(WorkflowType type);
    
    List<MarketingWorkflow> getActiveWorkflows();
    
    List<MarketingWorkflow> getByTriggerEvent(String triggerEvent);
    
    List<MarketingWorkflow> getScheduledWorkflows(LocalDateTime now);
    
    List<MarketingWorkflow> getByCreator(String createdBy);
    
    List<MarketingWorkflow> getByPage(int pageNum, int pageSize, String name, 
                                     WorkflowStatus status, WorkflowType type);
    
    long countByPage(String name, WorkflowStatus status, WorkflowType type);
    
    void delete(Long id);
    
    boolean existsByName(String name);
    
    List<MarketingWorkflow> getByPriority(int minPriority);
}