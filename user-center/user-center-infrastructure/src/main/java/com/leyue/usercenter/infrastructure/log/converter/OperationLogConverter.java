package com.leyue.usercenter.infrastructure.log.converter;

import com.leyue.usercenter.domain.log.OperationLog;
import com.leyue.usercenter.domain.log.OperationLogArchive;
import com.leyue.usercenter.domain.log.OperationLogStatistics;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogArchiveDO;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogDO;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogStatisticsDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 操作日志转换器
 * 负责领域对象和数据对象之间的转换
 */
@Component
public class OperationLogConverter {

    /**
     * 将领域对象转换为数据对象
     *
     * @param operationLog 操作日志领域对象
     * @return 操作日志数据对象
     */
    public OperationLogDO toDataObject(OperationLog operationLog) {
        if (operationLog == null) {
            return null;
        }
        
        OperationLogDO operationLogDO = new OperationLogDO();
        BeanUtils.copyProperties(operationLog, operationLogDO);
        return operationLogDO;
    }

    /**
     * 将数据对象转换为领域对象
     *
     * @param operationLogDO 操作日志数据对象
     * @return 操作日志领域对象
     */
    public OperationLog toDomainObject(OperationLogDO operationLogDO) {
        if (operationLogDO == null) {
            return null;
        }
        
        OperationLog operationLog = new OperationLog();
        BeanUtils.copyProperties(operationLogDO, operationLog);
        return operationLog;
    }

    /**
     * 将领域对象转换为归档数据对象
     *
     * @param operationLog 操作日志领域对象
     * @return 操作日志归档数据对象
     */
    public OperationLogArchiveDO toArchiveDataObject(OperationLog operationLog) {
        if (operationLog == null) {
            return null;
        }
        
        OperationLogArchiveDO archiveDO = new OperationLogArchiveDO();
        BeanUtils.copyProperties(operationLog, archiveDO);
        archiveDO.setId(null); // 清空ID，避免主键冲突
        return archiveDO;
    }

    /**
     * 将操作日志数据对象转换为归档数据对象
     *
     * @param operationLogDO 操作日志数据对象
     * @return 操作日志归档数据对象
     */
    public OperationLogArchiveDO toArchiveDataObject(OperationLogDO operationLogDO) {
        if (operationLogDO == null) {
            return null;
        }
        
        OperationLogArchiveDO archiveDO = new OperationLogArchiveDO();
        BeanUtils.copyProperties(operationLogDO, archiveDO);
        archiveDO.setId(null); // 清空ID，避免主键冲突
        return archiveDO;
    }

    /**
     * 将归档数据对象转换为领域对象
     *
     * @param archiveDO 操作日志归档数据对象
     * @return 操作日志归档领域对象
     */
    public OperationLogArchive toArchiveDomainObject(OperationLogArchiveDO archiveDO) {
        if (archiveDO == null) {
            return null;
        }
        
        OperationLogArchive archive = new OperationLogArchive();
        BeanUtils.copyProperties(archiveDO, archive);
        return archive;
    }

    /**
     * 将归档领域对象转换为归档数据对象
     *
     * @param archive 操作日志归档领域对象
     * @return 操作日志归档数据对象
     */
    public OperationLogArchiveDO toArchiveDataObject(OperationLogArchive archive) {
        if (archive == null) {
            return null;
        }
        
        OperationLogArchiveDO archiveDO = new OperationLogArchiveDO();
        BeanUtils.copyProperties(archive, archiveDO);
        return archiveDO;
    }

    /**
     * 将统计领域对象转换为统计数据对象
     *
     * @param statistics 操作日志统计领域对象
     * @return 操作日志统计数据对象
     */
    public OperationLogStatisticsDO toStatisticsDataObject(OperationLogStatistics statistics) {
        if (statistics == null) {
            return null;
        }
        
        OperationLogStatisticsDO statisticsDO = new OperationLogStatisticsDO();
        BeanUtils.copyProperties(statistics, statisticsDO);
        return statisticsDO;
    }

    /**
     * 将统计数据对象转换为统计领域对象
     *
     * @param statisticsDO 操作日志统计数据对象
     * @return 操作日志统计领域对象
     */
    public OperationLogStatistics toStatisticsDomainObject(OperationLogStatisticsDO statisticsDO) {
        if (statisticsDO == null) {
            return null;
        }
        
        OperationLogStatistics statistics = new OperationLogStatistics();
        BeanUtils.copyProperties(statisticsDO, statistics);
        return statistics;
    }
}
