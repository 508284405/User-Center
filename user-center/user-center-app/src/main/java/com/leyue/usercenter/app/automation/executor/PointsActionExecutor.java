package com.leyue.usercenter.app.automation.executor;

import com.leyue.usercenter.app.automation.WorkflowExecutionEngine.WorkflowActionExecutor;
import com.leyue.usercenter.domain.automation.MarketingWorkflow;
import com.leyue.usercenter.domain.automation.WorkflowExecution;
import com.leyue.usercenter.domain.loyalty.points.PointAccount;
import com.leyue.usercenter.domain.loyalty.points.gateway.PointAccountGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("pointsExecutor")
@RequiredArgsConstructor
public class PointsActionExecutor implements WorkflowActionExecutor {
    
    private final PointAccountGateway pointAccountGateway;
    
    @Override
    public void execute(MarketingWorkflow.WorkflowAction action, WorkflowExecution execution) {
        log.info("执行积分奖励动作: userId={}, actionName={}", 
                execution.getUserId(), action.getActionName());
        
        try {
            Map<String, Object> parameters = action.getParameters();
            Integer points = (Integer) parameters.get("points");
            String reason = (String) parameters.getOrDefault("reason", "营销活动奖励");
            String actionType = (String) parameters.getOrDefault("actionType", "REWARD");
            
            if (points == null || points <= 0) {
                throw new RuntimeException("无效的积分数量: " + points);
            }
            
            // 获取或创建用户积分账户
            PointAccount pointAccount = pointAccountGateway.getByUserId(execution.getUserId());
            if (pointAccount == null) {
                pointAccount = new PointAccount(execution.getUserId());
                pointAccountGateway.save(pointAccount);
            }
            
            // 执行积分操作
            if ("REWARD".equals(actionType)) {
                // 奖励积分
                pointAccount.earn(points, reason, execution.getId().toString());
                execution.appendLog(String.format("积分奖励成功: +%d积分, 原因: %s", points, reason));
            } else if ("DEDUCT".equals(actionType)) {
                // 扣减积分
                pointAccount.deduct(points, reason, execution.getId().toString());
                execution.appendLog(String.format("积分扣减成功: -%d积分, 原因: %s", points, reason));
            } else {
                throw new RuntimeException("不支持的积分操作类型: " + actionType);
            }
            
            pointAccountGateway.save(pointAccount);
            
            log.info("积分操作完成: userId={}, actionType={}, points={}, reason={}", 
                    execution.getUserId(), actionType, points, reason);
            
        } catch (Exception e) {
            log.error("积分操作失败: userId={}, error={}", execution.getUserId(), e.getMessage(), e);
            execution.appendLog("积分操作失败: " + e.getMessage());
            throw e;
        }
    }
}