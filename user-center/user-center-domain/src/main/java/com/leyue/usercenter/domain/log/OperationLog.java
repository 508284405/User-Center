package com.leyue.usercenter.domain.log;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 操作日志领域实体
 */
@Data
public class OperationLog {
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 模块
     */
    private String module;
    
    /**
     * 操作目标ID
     */
    private Long targetId;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 操作详细内容，包括修改前后的值
     */
    private Map<String, Object> operationContent;
    
    /**
     * 操作结果：成功、失败、部分成功等
     */
    private String operationResult;
    
    /**
     * 关联操作ID
     */
    private Long relatedOperationId;
    
    /**
     * 操作级别：普通、重要、关键
     */
    private String severityLevel;
    
    /**
     * 是否敏感操作：0-否，1-是
     */
    private Integer isSensitive;
    
    /**
     * 审批信息
     */
    private Map<String, Object> approvalInfo;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 检查是否为敏感操作
     */
    public boolean isSensitiveOperation() {
        return this.isSensitive != null && this.isSensitive == 1;
    }
    
    /**
     * 检查操作是否成功
     */
    public boolean isSuccessful() {
        return "成功".equals(this.operationResult);
    }
    
    /**
     * 检查操作是否为指定级别
     */
    public boolean isOfSeverity(String level) {
        return level != null && level.equals(this.severityLevel);
    }
}
