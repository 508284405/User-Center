package com.leyue.usercenter.infrastructure.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogDO> {
    
    /**
     * 按条件分页查询操作日志
     *
     * @param page 分页参数
     * @param params 查询条件
     * @return 分页结果
     */
    IPage<OperationLogDO> pageQuery(Page<OperationLogDO> page, @Param("params") Map<String, Object> params);
    
    /**
     * 根据关联操作ID查询操作链
     *
     * @param relatedOperationId 关联操作ID
     * @return 操作日志列表
     */
    List<OperationLogDO> findByRelatedOperationId(@Param("relatedOperationId") Long relatedOperationId);
    
    /**
     * 根据模块和目标ID查询操作历史
     *
     * @param module 模块
     * @param targetId 目标ID
     * @return 操作日志列表
     */
    List<OperationLogDO> findByModuleAndTargetId(@Param("module") String module, @Param("targetId") Long targetId);
    
    /**
     * 查询指定时间范围内的日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志列表
     */
    List<OperationLogDO> findBetweenDates(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 根据ID列表批量删除日志
     *
     * @param ids ID列表
     * @return 删除数量
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 删除早于指定日期的日志
     *
     * @param expiryDate 过期日期
     * @return 删除数量
     */
    int deleteExpiredLogs(@Param("expiryDate") Date expiryDate);
    
    /**
     * 获取用户操作统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果Map
     */
    Map<String, Object> getUserOperationStatistics(@Param("userId") Long userId, 
                                                  @Param("startTime") Date startTime, 
                                                  @Param("endTime") Date endTime);
    
    /**
     * 获取指定模块的操作统计
     *
     * @param modules 模块列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getModuleStatistics(@Param("modules") List<String> modules,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime);
    
    /**
     * 获取指定操作类型的统计
     *
     * @param operationTypes 操作类型列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getOperationTypeStatistics(@Param("operationTypes") List<String> operationTypes,
                                                       @Param("startTime") Date startTime,
                                                       @Param("endTime") Date endTime);
    
    /**
     * 查找异常操作行为
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param threshold 操作频率阈值（每分钟）
     * @return 异常操作列表
     */
    List<OperationLogDO> findAbnormalOperations(@Param("userId") Long userId,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime,
                                             @Param("threshold") int threshold);
}
