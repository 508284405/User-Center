package com.leyue.usercenter.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.OperationLogDTO;
import com.leyue.usercenter.dto.OperationLogStatisticsDTO;
import com.leyue.usercenter.dto.query.OperationLogPageQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 分页查询操作日志
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResponse<OperationLogDTO> pageOperationLogs(OperationLogPageQuery query);

    /**
     * 根据ID查询操作日志
     *
     * @param id 日志ID
     * @return 操作日志
     */
    SingleResponse<OperationLogDTO> getOperationLog(Long id);

    /**
     * 根据关联ID查询操作链
     *
     * @param relatedOperationId 关联操作ID
     * @return 操作日志列表
     */
    SingleResponse<List<OperationLogDTO>> getOperationChain(Long relatedOperationId);

    /**
     * 获取用户操作统计
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 操作统计结果
     */
    SingleResponse<Map<String, Object>> getUserOperationStatistics(Long userId, Date startTime, Date endTime);

    /**
     * 获取模块操作统计
     *
     * @param modules   模块列表
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 模块操作统计结果
     */
    SingleResponse<List<OperationLogStatisticsDTO>> getModuleOperationStatistics(List<String> modules, Date startTime, Date endTime);

    /**
     * 获取操作类型统计
     *
     * @param operationTypes 操作类型列表
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return 操作类型统计结果
     */
    SingleResponse<List<OperationLogStatisticsDTO>> getOperationTypeStatistics(List<String> operationTypes, Date startTime, Date endTime);

    /**
     * 导出操作日志
     *
     * @param query  查询参数
     * @param format 导出格式（CSV, EXCEL, PDF）
     * @return 导出文件路径
     */
    SingleResponse<String> exportOperationLogs(OperationLogPageQuery query, String format);

    /**
     * 手动归档指定时间范围的日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 归档结果
     */
    Response archiveOperationLogs(Date startTime, Date endTime);

    /**
     * 清理过期操作日志
     *
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    Response cleanExpiredOperationLogs(Integer retentionDays);

    /**
     * 获取特定资源的操作历史
     *
     * @param module   模块
     * @param targetId 目标ID
     * @return 操作历史列表
     */
    SingleResponse<List<OperationLogDTO>> getResourceOperationHistory(String module, Long targetId);

    /**
     * 检测异常操作行为
     *
     * @param userId           用户ID
     * @param timeRangeMinutes 检测时间范围（分钟）
     * @return 异常操作列表
     */
    SingleResponse<List<OperationLogDTO>> detectAbnormalOperations(Long userId, Integer timeRangeMinutes);
}
