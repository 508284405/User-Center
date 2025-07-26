package com.leyue.usercenter.dto;

import lombok.Data;

import java.util.Date;

/**
 * 操作日志统计数据传输对象
 */
@Data
public class OperationLogStatisticsDTO {
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
}
