package com.leyue.usercenter.domain.log;

import lombok.Data;

import java.util.Date;

/**
 * 操作日志统计实体
 */
@Data
public class OperationLogStatistics {
    /**
     * 统计ID
     */
    private Long id;
    
    /**
     * 统计日期
     */
    private Date statisticDate;
    
    /**
     * 模块
     */
    private String module;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作用户数
     */
    private Integer userCount;
    
    /**
     * 操作次数
     */
    private Integer operationCount;
    
    /**
     * 成功操作数
     */
    private Integer successCount;
    
    /**
     * 失败操作数
     */
    private Integer failCount;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 计算成功率
     * 
     * @return 成功率百分比
     */
    public double getSuccessRate() {
        if (operationCount == null || operationCount == 0) {
            return 0.0;
        }
        return (double) successCount / operationCount * 100;
    }
    
    /**
     * 检查是否存在异常情况（成功率过低）
     * 
     * @param threshold 阈值，低于该值视为异常
     * @return 是否异常
     */
    public boolean isAbnormal(double threshold) {
        return getSuccessRate() < threshold;
    }
}
