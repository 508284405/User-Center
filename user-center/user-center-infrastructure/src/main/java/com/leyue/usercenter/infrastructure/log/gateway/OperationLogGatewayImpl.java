package com.leyue.usercenter.infrastructure.log.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.log.OperationLog;
import com.leyue.usercenter.domain.log.OperationLogArchive;
import com.leyue.usercenter.domain.log.OperationLogStatistics;
import com.leyue.usercenter.domain.log.gateway.OperationLogGateway;
import com.leyue.usercenter.dto.query.OperationLogPageQuery;
import com.leyue.usercenter.infrastructure.log.converter.OperationLogConverter;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogArchiveDO;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogDO;
import com.leyue.usercenter.infrastructure.log.dataobject.OperationLogStatisticsDO;
import com.leyue.usercenter.infrastructure.log.mapper.OperationLogArchiveMapper;
import com.leyue.usercenter.infrastructure.log.mapper.OperationLogMapper;
import com.leyue.usercenter.infrastructure.log.mapper.OperationLogStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作日志网关实现类
 */
@Component
@RequiredArgsConstructor
public class OperationLogGatewayImpl implements OperationLogGateway {

    private final OperationLogMapper operationLogMapper;
    private final OperationLogArchiveMapper operationLogArchiveMapper;
    private final OperationLogStatisticsMapper operationLogStatisticsMapper;
    private final OperationLogConverter operationLogConverter;

    @Override
    public void save(OperationLog log) {
        OperationLogDO operationLogDO = operationLogConverter.toDataObject(log);
        operationLogMapper.insert(operationLogDO);
        // 回写ID
        log.setId(operationLogDO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OperationLog> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        List<OperationLogDO> operationLogDOs = logs.stream()
                .map(operationLogConverter::toDataObject)
                .collect(Collectors.toList());
        operationLogDOs.forEach(operationLogMapper::insert);
        
        // 回写ID
        for (int i = 0; i < logs.size(); i++) {
            logs.get(i).setId(operationLogDOs.get(i).getId());
        }
    }

    @Override
    public OperationLog findById(Long id) {
        OperationLogDO operationLogDO = operationLogMapper.selectById(id);
        return operationLogConverter.toDomainObject(operationLogDO);
    }

    @Override
    public PageResponse<OperationLog> pageQuery(OperationLogPageQuery query) {
        Page<OperationLogDO> page = new Page<>(query.getPageIndex(), query.getPageSize());
        
        // 构建查询参数Map
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(query.getUsername())) {
            params.put("username", query.getUsername());
        }
        if (StringUtils.hasText(query.getModule())) {
            params.put("module", query.getModule());
        }
        if (StringUtils.hasText(query.getOperationType())) {
            params.put("operationType", query.getOperationType());
        }
        if (query.getTargetId() != null) {
            params.put("targetId", query.getTargetId());
        }
        if (query.getUserId() != null) {
            params.put("userId", query.getUserId());
        }
        if (StringUtils.hasText(query.getOperationResult())) {
            params.put("operationResult", query.getOperationResult());
        }
        if (StringUtils.hasText(query.getSeverityLevel())) {
            params.put("severityLevel", query.getSeverityLevel());
        }
        if (query.getSeverityLevel() != null) {
            params.put("isSensitive", query.getSeverityLevel());
        }
        if (query.getRelatedOperationId() != null) {
            params.put("relatedOperationId", query.getRelatedOperationId());
        }
        if (query.getStartTime() != null) {
            params.put("startTime", query.getStartTime());
        }
        if (query.getEndTime() != null) {
            params.put("endTime", query.getEndTime());
        }
        
        // 调用自定义分页查询
        IPage<OperationLogDO> resultPage = operationLogMapper.pageQuery(page, params);
        
        // 转换为领域对象并构建分页响应
        List<OperationLog> operationLogs = resultPage.getRecords().stream()
                .map(operationLogConverter::toDomainObject)
                .collect(Collectors.toList());
        
        return PageResponse.of(operationLogs, (int)resultPage.getTotal(), query.getPageSize(), query.getPageIndex());
    }

    @Override
    public List<OperationLog> findByRelatedOperationId(Long relatedOperationId) {
        List<OperationLogDO> operationLogDOs = operationLogMapper.findByRelatedOperationId(relatedOperationId);
        return operationLogDOs.stream()
                .map(operationLogConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public List<OperationLog> findByModuleAndTargetId(String module, Long targetId) {
        List<OperationLogDO> operationLogDOs = operationLogMapper.findByModuleAndTargetId(module, targetId);
        return operationLogDOs.stream()
                .map(operationLogConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveLogs(Date startTime, Date endTime) {
        // 1. 查询需要归档的日志
        List<OperationLogDO> operationLogDOs = operationLogMapper.findBetweenDates(startTime, endTime);
        if (CollectionUtils.isEmpty(operationLogDOs)) {
            return 0;
        }
        
        // 2. 转换为归档对象
        List<OperationLogArchiveDO> archiveDOs = operationLogDOs.stream()
                .map(operationLogConverter::toArchiveDataObject)
                .peek(archive -> archive.setArchivedAt(new Date()))
                .collect(Collectors.toList());
        
        // 3. 批量插入归档表
        int count = operationLogArchiveMapper.batchInsert(archiveDOs);
        
        // 4. 删除已归档的日志
        if (count > 0) {
            List<Long> archivedIds = operationLogDOs.stream()
                    .map(OperationLogDO::getId)
                    .collect(Collectors.toList());
            operationLogMapper.deleteByIds(archivedIds);
        }
        
        return count;
    }

    @Override
    public void saveArchive(OperationLogArchive archive) {
        OperationLogArchiveDO archiveDO = operationLogConverter.toArchiveDataObject(archive);
        operationLogArchiveMapper.insert(archiveDO);
        // 回写ID
        archive.setId(archiveDO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveArchive(List<OperationLogArchive> archives) {
        if (CollectionUtils.isEmpty(archives)) {
            return;
        }
        List<OperationLogArchiveDO> archiveDOs = archives.stream()
                .map(operationLogConverter::toArchiveDataObject)
                .collect(Collectors.toList());
        
        operationLogArchiveMapper.batchInsert(archiveDOs);
        
        // 回写ID
        for (int i = 0; i < archives.size(); i++) {
            archives.get(i).setId(archiveDOs.get(i).getId());
        }
    }

    @Override
    public int deleteArchivedLogs(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return operationLogMapper.deleteByIds(ids);
    }

    @Override
    public int deleteExpiredLogs(Date expiryDate) {
        return operationLogMapper.deleteExpiredLogs(expiryDate);
    }

    @Override
    public Map<String, Object> getUserOperationStatistics(Long userId, Date startTime, Date endTime) {
        return operationLogMapper.getUserOperationStatistics(userId, startTime, endTime);
    }

    @Override
    public List<OperationLogStatistics> getModuleStatistics(List<String> modules, Date startTime, Date endTime) {
        List<Map<String, Object>> statisticsMapList = operationLogMapper.getModuleStatistics(modules, startTime, endTime);
        
        List<OperationLogStatistics> statisticsList = new ArrayList<>();
        for (Map<String, Object> map : statisticsMapList) {
            OperationLogStatistics statistics = new OperationLogStatistics();
            statistics.setModule((String) map.get("module"));
            statistics.setOperationType((String) map.getOrDefault("operationType", ""));
            statistics.setUserCount(((Number) map.getOrDefault("userCount", 0)).intValue());
            statistics.setOperationCount(((Number) map.getOrDefault("operationCount", 0)).intValue());
            statistics.setSuccessCount(((Number) map.getOrDefault("successCount", 0)).intValue());
            statistics.setFailCount(((Number) map.getOrDefault("failCount", 0)).intValue());
            statistics.setStatisticDate(new Date());
            statisticsList.add(statistics);
        }
        
        return statisticsList;
    }

    @Override
    public List<OperationLogStatistics> getOperationTypeStatistics(List<String> operationTypes, Date startTime, Date endTime) {
        List<Map<String, Object>> statisticsMapList = operationLogMapper.getOperationTypeStatistics(operationTypes, startTime, endTime);
        
        List<OperationLogStatistics> statisticsList = new ArrayList<>();
        for (Map<String, Object> map : statisticsMapList) {
            OperationLogStatistics statistics = new OperationLogStatistics();
            statistics.setModule((String) map.getOrDefault("module", ""));
            statistics.setOperationType((String) map.get("operationType"));
            statistics.setUserCount(((Number) map.getOrDefault("userCount", 0)).intValue());
            statistics.setOperationCount(((Number) map.getOrDefault("operationCount", 0)).intValue());
            statistics.setSuccessCount(((Number) map.getOrDefault("successCount", 0)).intValue());
            statistics.setFailCount(((Number) map.getOrDefault("failCount", 0)).intValue());
            statistics.setStatisticDate(new Date());
            statisticsList.add(statistics);
        }
        
        return statisticsList;
    }

    @Override
    public void saveStatistics(OperationLogStatistics statistics) {
        OperationLogStatisticsDO statisticsDO = operationLogConverter.toStatisticsDataObject(statistics);
        operationLogStatisticsMapper.insert(statisticsDO);
        // 回写ID
        statistics.setId(statisticsDO.getId());
    }

    @Override
    public void batchSaveStatistics(List<OperationLogStatistics> statisticsList) {
        if (CollectionUtils.isEmpty(statisticsList)) {
            return;
        }
        
        statisticsList.forEach(statistics -> {
            OperationLogStatisticsDO statisticsDO = operationLogConverter.toStatisticsDataObject(statistics);
            operationLogStatisticsMapper.insert(statisticsDO);
            // 回写ID
            statistics.setId(statisticsDO.getId());
        });
    }

    @Override
    public List<OperationLog> findAbnormalOperations(Long userId, Date startTime, Date endTime, int threshold) {
        List<OperationLogDO> operationLogDOs = operationLogMapper.findAbnormalOperations(userId, startTime, endTime, threshold);
        return operationLogDOs.stream()
                .map(operationLogConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public int generateDailyStatistics(Date statisticDate) {
        return operationLogStatisticsMapper.generateDailyStatistics(statisticDate);
    }

    @Override
    public int deleteExpiredArchives(Date expiryDate) {
        return operationLogArchiveMapper.deleteExpiredArchives(expiryDate);
    }

    @Override
    public int deleteExpiredStatistics(Date expiryDate) {
        return operationLogStatisticsMapper.deleteExpiredStatistics(expiryDate);
    }
}
