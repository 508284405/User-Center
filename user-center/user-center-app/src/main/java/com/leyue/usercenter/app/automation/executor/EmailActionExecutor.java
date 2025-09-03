package com.leyue.usercenter.app.automation.executor;

import com.leyue.usercenter.app.automation.WorkflowExecutionEngine.WorkflowActionExecutor;
import com.leyue.usercenter.domain.automation.MarketingWorkflow;
import com.leyue.usercenter.domain.automation.WorkflowExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("emailExecutor")
@RequiredArgsConstructor
public class EmailActionExecutor implements WorkflowActionExecutor {
    
    // 这里可以注入邮件服务
    // private final EmailService emailService;
    
    @Override
    public void execute(MarketingWorkflow.WorkflowAction action, WorkflowExecution execution) {
        log.info("执行邮件发送动作: userId={}, actionName={}", 
                execution.getUserId(), action.getActionName());
        
        try {
            Map<String, Object> parameters = action.getParameters();
            String template = (String) parameters.get("template");
            String subject = (String) parameters.get("subject");
            String content = (String) parameters.get("content");
            
            // 个性化内容替换
            content = personalizeContent(content, execution);
            subject = personalizeContent(subject, execution);
            
            // 发送邮件
            sendEmail(execution.getUserId(), subject, content, template);
            
            execution.appendLog("邮件发送成功: " + subject);
            
        } catch (Exception e) {
            log.error("邮件发送失败: userId={}, error={}", execution.getUserId(), e.getMessage(), e);
            execution.appendLog("邮件发送失败: " + e.getMessage());
            throw e;
        }
    }
    
    private String personalizeContent(String content, WorkflowExecution execution) {
        if (content == null) return "";
        
        // 替换用户相关的占位符
        content = content.replace("{{userId}}", execution.getUserId());
        content = content.replace("{{workflowName}}", execution.getWorkflowName());
        
        // 从触发数据中获取更多个性化信息
        if (execution.getTriggerData() != null) {
            for (Map.Entry<String, Object> entry : execution.getTriggerData().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                content = content.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        
        return content;
    }
    
    private void sendEmail(String userId, String subject, String content, String template) {
        // 模拟邮件发送
        log.info("发送邮件 - 收件人: {}, 主题: {}, 模板: {}", userId, subject, template);
        
        // 实际实现中应该调用邮件服务
        // emailService.sendEmail(userId, subject, content, template);
        
        // 这里可以记录邮件发送记录到数据库
        // emailSendLogService.recordEmailSent(userId, subject, template);
    }
}