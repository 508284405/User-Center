package com.leyue.usercenter.domain.log.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.domain.log.OperationLog;
import com.leyue.usercenter.domain.log.OperationLogArchive;
import com.leyue.usercenter.domain.log.OperationLogStatistics;
import com.leyue.usercenter.dto.query.OperationLogPageQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作日志网关接口
 */
public interface OperationLogGateway {
    
    /**
     * 保存操作日志
     *
     * @param log 操作日志
     */
    void save(OperationLog log);
    
    /**
     * 批量保存操作日志
     *
     * @param logs 操作日志列表
     */
    void batchSave(List<OperationLog> logs);
    
    /**
     * 根据ID查找操作日志
     *
     * @param id 日志ID
     * @return 操作日志
     */
    OperationLog findById(Long id);
    
    /**
     * 分页查询操作日志
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResponse<OperationLog> pageQuery(OperationLogPageQuery query);
    
    /**
     * 根据关联操作ID查询操作链
     *
     * @param relatedOperationId 关联操作ID
     * @return 操作日志列表
     */
    List<OperationLog> findByRelatedOperationId(Long relatedOperationId);
    
    /**
     * 根据模块和目标ID查询操作历史
     *
     * @param module 模块
     * @param targetId 目标ID
     * @return 操作日志列表
     */
    List<OperationLog> findByModuleAndTargetId(String module, Long targetId);
    
    /**
     * 归档操作日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 归档数量
     */
    int archiveLogs(Date startTime, Date endTime);
    
    /**
     * 保存归档日志
     *
     * @param archive 归档日志
     */
    void saveArchive(OperationLogArchive archive);
    
    /**
     * 批量保存归档日志
     *
     * @param archives 归档日志列表
     */
    void batchSaveArchive(List<OperationLogArchive> archives);
    
    /**
     * 删除已归档的日志
     *
     * @param ids 日志ID列表
     * @return 删除数量
     */
    int deleteArchivedLogs(List<Long> ids);
    
    /**
     * 删除过期日志
     *
     * @param expiryDate 过期日期
     * @return 删除数量
     */
    int deleteExpiredLogs(Date expiryDate);
    
    /**
     * 获取用户操作统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getUserOperationStatistics(Long userId, Date startTime, Date endTime);
    
    /**
     * 获取模块操作统计
     *
     * @param modules 模块列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 模块操作统计结果
     */
    List<OperationLogStatistics> getModuleStatistics(List<String> modules, Date startTime, Date endTime);
    
    /**
     * 获取操作类型统计
     *
     * @param operationTypes 操作类型列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作类型统计结果
     */
    List<OperationLogStatistics> getOperationTypeStatistics(List<String> operationTypes, Date startTime, Date endTime);
    
    /**
     * 保存操作日志统计
     *
     * @param statistics 操作日志统计
     */
    void saveStatistics(OperationLogStatistics statistics);
    
    /**
     * 批量保存操作日志统计
     *
     * @param statisticsList 操作日志统计列表
     */
    void batchSaveStatistics(List<OperationLogStatistics> statisticsList);
    
    /**
     * 根据时间范围和用户ID查找异常操作
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param threshold 操作频率阈值（每分钟）
     * @return 异常操作列表
     */
    List<OperationLog> findAbnormalOperations(Long userId, Date startTime, Date endTime, int threshold);

    /**
     * 生成每日统计数据
     *
     * @param statisticDate 统计日期
     * @return 生成的统计记录数
     */
    int generateDailyStatistics(Date statisticDate);

    /**
     * 删除过期归档日志
     *
     * @param expiryDate 过期日期
     * @return 删除数量
     */
    int deleteExpiredArchives(Date expiryDate);

    /**
     * 删除过期统计数据
     *
     * @param expiryDate 过期日期
     * @return 删除数量
     */
    int deleteExpiredStatistics(Date expiryDate);
}
