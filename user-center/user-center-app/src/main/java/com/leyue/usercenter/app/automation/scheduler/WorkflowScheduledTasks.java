package com.leyue.usercenter.app.automation.scheduler;

import com.leyue.usercenter.app.automation.WorkflowExecutionEngine;
import com.leyue.usercenter.app.automation.WorkflowTriggerService;
import com.leyue.usercenter.domain.automation.gateway.MarketingWorkflowGateway;
import com.leyue.usercenter.domain.automation.gateway.WorkflowExecutionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "workflow.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class WorkflowScheduledTasks {
    
    private final WorkflowExecutionEngine executionEngine;
    private final WorkflowTriggerService triggerService;
    private final MarketingWorkflowGateway workflowGateway;
    private final WorkflowExecutionGateway executionGateway;
    
    /**
     * 每分钟处理一次定时工作流
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processScheduledWorkflows() {
        log.debug("处理定时工作流...");
        
        try {
            executionEngine.processScheduledWorkflows();
        } catch (Exception e) {
            log.error("处理定时工作流失败", e);
        }
    }
    
    /**
     * 每10分钟处理一次超时的工作流执行
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void processTimeoutExecutions() {
        log.debug("处理超时的工作流执行...");
        
        try {
            executionEngine.processTimeoutExecutions();
        } catch (Exception e) {
            log.error("处理超时工作流执行失败", e);
        }
    }
    
    /**
     * 每天凌晨检测用户不活跃情况，触发召回工作流
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkInactiveUsers() {
        log.info("开始检测不活跃用户...");
        
        try {
            // 这里应该查询用户活跃度数据
            // 示例：查询7天、30天、90天未活跃的用户
            
            // 7天未活跃用户
            checkAndTriggerInactiveUsers(7);
            // 30天未活跃用户  
            checkAndTriggerInactiveUsers(30);
            // 90天未活跃用户
            checkAndTriggerInactiveUsers(90);
            
            log.info("不活跃用户检测完成");
            
        } catch (Exception e) {
            log.error("检测不活跃用户失败", e);
        }
    }
    
    /**
     * 每天早上检测用户生日，触发生日营销工作流
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkUserBirthdays() {
        log.info("开始检测用户生日...");
        
        try {
            // 这里应该查询今天生日的用户
            // List<User> birthdayUsers = userService.getTodayBirthdayUsers();
            
            // 模拟处理
            // for (User user : birthdayUsers) {
            //     Map<String, Object> userInfo = getUserInfo(user);
            //     triggerService.handleUserBirthday(user.getId(), userInfo);
            // }
            
            log.info("用户生日检测完成");
            
        } catch (Exception e) {
            log.error("检测用户生日失败", e);
        }
    }
    
    /**
     * 每小时检测购物车遗弃情况
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkAbandonedCarts() {
        log.info("开始检测遗弃购物车...");
        
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            
            // 这里应该查询遗弃的购物车
            // List<Cart> abandonedCarts = cartService.getAbandonedCarts(oneHourAgo, twentyFourHoursAgo);
            
            // 模拟处理
            // for (Cart cart : abandonedCarts) {
            //     Map<String, Object> cartData = getCartData(cart);
            //     triggerService.handleCartAbandoned(cart.getUserId(), cart.getId(), cartData);
            // }
            
            log.info("遗弃购物车检测完成");
            
        } catch (Exception e) {
            log.error("检测遗弃购物车失败", e);
        }
    }
    
    /**
     * 每天凌晨清理旧的工作流执行记录（保留30天）
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupOldExecutions() {
        log.info("开始清理旧的工作流执行记录...");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            executionGateway.deleteBefore(cutoffDate);
            log.info("工作流执行记录清理完成，删除了{}之前的记录", cutoffDate.toLocalDate());
            
        } catch (Exception e) {
            log.error("清理旧的工作流执行记录失败", e);
        }
    }
    
    /**
     * 每天早上生成工作流执行报告
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateWorkflowReport() {
        log.info("开始生成工作流执行报告...");
        
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            
            var recentExecutions = executionGateway.getByDateRange(yesterday, today);
            
            // 统计数据
            long totalExecutions = recentExecutions.size();
            long successfulExecutions = recentExecutions.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus().name()))
                .count();
            long failedExecutions = recentExecutions.stream()
                .filter(e -> "FAILED".equals(e.getStatus().name()))
                .count();
            
            double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
            
            log.info("=== 工作流执行报告 (昨日) ===");
            log.info("总执行次数: {}", totalExecutions);
            log.info("成功执行: {}", successfulExecutions);
            log.info("失败执行: {}", failedExecutions);
            log.info("成功率: {:.2f}%", successRate);
            
            if (failedExecutions > 0) {
                log.warn("发现{}个失败的工作流执行，请检查日志", failedExecutions);
            }
            
        } catch (Exception e) {
            log.error("生成工作流执行报告失败", e);
        }
    }
    
    private void checkAndTriggerInactiveUsers(int inactiveDays) {
        // 这里应该查询指定天数未活跃的用户
        // List<String> inactiveUserIds = userActivityService.getInactiveUsers(inactiveDays);
        
        // 模拟处理
        log.info("检测{}天未活跃用户", inactiveDays);
        
        // for (String userId : inactiveUserIds) {
        //     triggerService.handleUserInactive(userId, inactiveDays);
        // }
    }
}