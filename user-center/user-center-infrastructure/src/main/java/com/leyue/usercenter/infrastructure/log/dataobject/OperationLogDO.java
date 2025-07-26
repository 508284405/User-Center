package com.leyue.usercenter.infrastructure.log.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 操作日志数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "operation_logs", autoResultMap = true)
public class OperationLogDO extends BaseDO {
    
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
     * 操作详细内容，JSON类型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
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
     * 审批信息，JSON类型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> approvalInfo;
    
    /**
     * IP地址
     */
    private String ipAddress;
}
