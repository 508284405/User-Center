package com.leyue.usercenter.infrastructure.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogArchiveDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作日志归档Mapper接口
 */
@Mapper
public interface OperationLogArchiveMapper extends BaseMapper<OperationLogArchiveDO> {
    
    /**
     * 按条件分页查询归档日志
     *
     * @param page 分页参数
     * @param params 查询条件
     * @return 分页结果
     */
    IPage<OperationLogArchiveDO> pageQuery(Page<OperationLogArchiveDO> page, @Param("params") Map<String, Object> params);
    
    /**
     * 根据归档时间范围查询
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 归档日志列表
     */
    List<OperationLogArchiveDO> findByArchiveTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 根据模块和目标ID查询归档操作历史
     *
     * @param module 模块
     * @param targetId 目标ID
     * @return 归档日志列表
     */
    List<OperationLogArchiveDO> findByModuleAndTargetId(@Param("module") String module, @Param("targetId") Long targetId);
    
    /**
     * 批量插入归档记录
     *
     * @param archiveList 归档记录列表
     * @return 插入数量
     */
    int batchInsert(@Param("archiveList") List<OperationLogArchiveDO> archiveList);
    
    /**
     * 删除早于指定日期的归档日志
     *
     * @param expiryDate 过期日期
     * @return 删除数量
     */
    int deleteExpiredArchives(@Param("expiryDate") Date expiryDate);
}
