package com.leyue.usercenter.dto.query;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 操作日志分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogPageQuery extends PageQuery {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（支持模糊查询）
     */
    private String username;

    /**
     * 操作类型列表
     */
    private String operationType;

    /**
     * 模块
     */
    private String module;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * IP地址（支持模糊查询）
     */
    private String ipAddress;

    /**
     * 操作结果列表
     */
    private String operationResult;

    /**
     * 操作级别列表
     */
    private String severityLevel;

    /**
     * 是否只查询敏感操作
     */
    private Boolean onlySensitive;

    /**
     * 关联操作ID
     */
    private Long relatedOperationId;

    /**
     * 操作描述（支持模糊查询）
     */
    private String description;

    /**
     * 操作目标ID
     */
    private Long targetId;
}