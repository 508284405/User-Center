package com.leyue.usercenter.infrastructure.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogStatisticsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 操作日志统计Mapper接口
 */
@Mapper
public interface OperationLogStatisticsMapper extends BaseMapper<OperationLogStatisticsDO> {
    
    /**
     * 获取指定日期范围的模块操作统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计列表
     */
    List<OperationLogStatisticsDO> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取指定模块的操作统计
     *
     * @param module 模块名称
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计列表
     */
    List<OperationLogStatisticsDO> findByModule(@Param("module") String module, 
                                              @Param("startDate") Date startDate, 
                                              @Param("endDate") Date endDate);
    
    /**
     * 获取指定操作类型的统计
     *
     * @param operationType 操作类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计列表
     */
    List<OperationLogStatisticsDO> findByOperationType(@Param("operationType") String operationType, 
                                                     @Param("startDate") Date startDate, 
                                                     @Param("endDate") Date endDate);
    
    /**
     * 获取指定日期的统计记录
     *
     * @param statisticDate 统计日期
     * @param module 模块（可选）
     * @param operationType 操作类型（可选）
     * @return 统计记录
     */
    OperationLogStatisticsDO findByDateAndType(@Param("statisticDate") Date statisticDate, 
                                             @Param("module") String module, 
                                             @Param("operationType") String operationType);
    
    /**
     * 生成每日统计数据
     * 
     * @param statisticDate 统计日期
     * @return 生成的记录数
     */
    int generateDailyStatistics(@Param("statisticDate") Date statisticDate);
    
    /**
     * 删除过期统计数据
     *
     * @param expiryDate 过期日期
     * @return 删除的记录数
     */
    int deleteExpiredStatistics(@Param("expiryDate") Date expiryDate);
}
