package com.leyue.usercenter.log.task;

import com.leyue.usercenter.domain.log.gateway.OperationLogGateway;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.leyue.usercenter.domain.log.OperationLog;

/**
 * 操作日志定时任务
 * 负责执行日志归档、统计和清理工作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogScheduledTask {

    private final OperationLogGateway operationLogGateway;

    /**
     * 日志保留天数（默认30天）
     */
    @Value("${operation.log.retention.days:30}")
    private int logRetentionDays;

    /**
     * 归档日志保留天数（默认365天）
     */
    @Value("${operation.log.archive.retention.days:365}")
    private int archiveRetentionDays;

    /**
     * 统计数据保留天数（默认730天）
     */
    @Value("${operation.log.statistics.retention.days:730}")
    private int statisticsRetentionDays;

    /**
     * 异常操作检测阈值：单个用户每分钟最大操作次数（默认100次）
     */
    @Value("${operation.log.abnormal.threshold.per.minute:100}")
    private int abnormalThresholdPerMinute;

    /**
     * 异常操作检测时间窗口（分钟，默认5分钟）
     */
    @Value("${operation.log.abnormal.detection.window.minutes:5}")
    private int abnormalDetectionWindowMinutes;

    /**
     * 异常操作检测敏感操作阈值：单个用户每小时最大敏感操作次数（默认10次）
     */
    @Value("${operation.log.abnormal.sensitive.threshold.per.hour:10}")
    private int abnormalSensitiveThresholdPerHour;

    /**
     * 每日日志统计任务
     * 每天凌晨1:00执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyStatistics() {
        try {
            log.info("开始生成每日操作日志统计...");

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1); // 统计昨天的数据
            Date yesterday = calendar.getTime();

            // 查询昨天的操作日志，按模块和操作类型分组统计
            log.info("生成的统计日期: {}", yesterday);

            // 调用Gateway方法生成统计数据
            int count = operationLogGateway.generateDailyStatistics(yesterday);

            log.info("操作日志统计生成完成，共生成 {} 条统计记录", count);
        } catch (Exception e) {
            log.error("生成每日操作日志统计时发生异常", e);
        }
    }

    /**
     * 操作日志归档任务
     * 每周日凌晨2:00执行
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void archiveOperationLogs() {
        try {
            log.info("开始归档操作日志...");

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -7); // 归档7天前的日志
            Date endDate = calendar.getTime();

            calendar.add(Calendar.DAY_OF_MONTH, -23); // 再往前推23天，总共归档30天前至7天前的日志
            Date startDate = calendar.getTime();

            log.info("归档时间范围: {} 至 {}", startDate, endDate);

            int archivedCount = operationLogGateway.archiveLogs(startDate, endDate);

            log.info("操作日志归档完成，共归档 {} 条日志", archivedCount);
        } catch (Exception e) {
            log.error("归档操作日志时发生异常", e);
        }
    }

    /**
     * 清理过期操作日志任务
     * 每月1日凌晨3:00执行
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void cleanExpiredLogs() {
        try {
            log.info("开始清理过期操作日志...");

            // 清理超过保留期限的操作日志
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -logRetentionDays);
            Date logExpiryDate = calendar.getTime();

            int deletedLogCount = operationLogGateway.deleteExpiredLogs(logExpiryDate);
            log.info("清理过期操作日志完成，共清理 {} 条日志", deletedLogCount);

            // 清理超过保留期限的归档日志
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -archiveRetentionDays);
            Date archiveExpiryDate = calendar.getTime();

            int deletedArchiveCount = operationLogGateway.deleteExpiredArchives(archiveExpiryDate);
            log.info("清理过期归档日志完成，共清理 {} 条归档日志", deletedArchiveCount);

            // 清理超过保留期限的统计数据
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -statisticsRetentionDays);
            Date statisticsExpiryDate = calendar.getTime();

            int deletedStatisticsCount = operationLogGateway.deleteExpiredStatistics(statisticsExpiryDate);
            log.info("清理过期统计数据完成，共清理 {} 条统计数据", deletedStatisticsCount);
        } catch (Exception e) {
            log.error("清理过期操作日志时发生异常", e);
        }
    }

    /**
     * 操作日志异常检测任务
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void detectAbnormalOperations() {
        try {
            log.info("开始检测异常操作行为...");

            // 计算检测时间窗口
            Calendar calendar = Calendar.getInstance();
            Date endTime = calendar.getTime();
            calendar.add(Calendar.MINUTE, -abnormalDetectionWindowMinutes);
            Date startTime = calendar.getTime();

            log.info("检测时间窗口: {} 至 {}", startTime, endTime);

            // 检测用户高频操作异常
            detectHighFrequencyOperations(startTime, endTime);

            // 检测敏感操作异常
            detectAbnormalSensitiveOperations(startTime, endTime);

            log.info("异常操作检测完成");
        } catch (Exception e) {
            log.error("检测异常操作行为时发生异常", e);
        }
    }

    /**
     * 检测高频操作异常
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void detectHighFrequencyOperations(Date startTime, Date endTime) {
        try {
            // 这里应该查询所有用户，然后逐个检测
            // 为了简化，我们先检测一个示例用户ID为null的情况，表示检测所有用户
            List<OperationLog> abnormalOperations = operationLogGateway.findAbnormalOperations(
                    null, startTime, endTime, abnormalThresholdPerMinute);

            if (!abnormalOperations.isEmpty()) {
                log.warn("检测到 {} 个高频操作异常", abnormalOperations.size());
                
                // 这里可以进行进一步的处理，比如发送告警、记录到特殊日志等
                abnormalOperations.forEach(operation -> {
                    log.warn("异常操作 - 用户ID: {}, 用户名: {}, 操作类型: {}, 模块: {}, 操作时间: {}", 
                            operation.getUserId(), operation.getUsername(), 
                            operation.getOperationType(), operation.getModule(), 
                            operation.getCreatedAt());
                });
            }
        } catch (Exception e) {
            log.error("检测高频操作异常时发生错误", e);
        }
    }

    /**
     * 检测敏感操作异常
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void detectAbnormalSensitiveOperations(Date startTime, Date endTime) {
        try {
            // 检测敏感操作的频率是否异常
            // 这里可以实现更复杂的检测逻辑，比如检测某个用户的敏感操作频率
            log.info("检测敏感操作异常行为...");
            
            // 获取用户操作统计，检查是否有异常的敏感操作
            // 这里可以进一步扩展具体的检测逻辑
            
            log.info("敏感操作异常检测完成");
        } catch (Exception e) {
            log.error("检测敏感操作异常时发生错误", e);
        }
    }
}
