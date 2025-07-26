package com.leyue.usercenter.infrastructure.log.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 操作日志统计数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_logs_statistics")
public class OperationLogStatisticsDO extends BaseDO {
    
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
}
