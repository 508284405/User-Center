package com.leyue.usercenter.domain.log;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 操作日志归档实体
 */
@Data
public class OperationLogArchive {
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
     * 归档时间
     */
    private Date archivedAt;
    
    /**
     * 从OperationLog创建归档实体
     * 
     * @param log 源操作日志
     * @return 归档实体
     */
    public static OperationLogArchive fromOperationLog(OperationLog log) {
        OperationLogArchive archive = new OperationLogArchive();
        archive.setId(log.getId());
        archive.setUserId(log.getUserId());
        archive.setUsername(log.getUsername());
        archive.setOperationType(log.getOperationType());
        archive.setModule(log.getModule());
        archive.setTargetId(log.getTargetId());
        archive.setDescription(log.getDescription());
        archive.setOperationContent(log.getOperationContent());
        archive.setOperationResult(log.getOperationResult());
        archive.setRelatedOperationId(log.getRelatedOperationId());
        archive.setSeverityLevel(log.getSeverityLevel());
        archive.setIsSensitive(log.getIsSensitive());
        archive.setApprovalInfo(log.getApprovalInfo());
        archive.setIpAddress(log.getIpAddress());
        archive.setCreatedAt(log.getCreatedAt());
        archive.setCreatedBy(log.getCreatedBy());
        archive.setUpdatedAt(log.getUpdatedAt());
        archive.setUpdatedBy(log.getUpdatedBy());
        archive.setArchivedAt(new Date());
        return archive;
    }
}
