package com.leyue.usercenter.app.analytics.scheduler;

import com.leyue.usercenter.api.MarketingAnalyticsServiceI;
import com.leyue.usercenter.domain.analytics.gateway.MarketingAnalyticsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.DayOfWeek;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "analytics.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsScheduledTasks {
    
    private final MarketingAnalyticsServiceI analyticsService;
    private final MarketingAnalyticsGateway analyticsGateway;
    
    /**
     * 每天凌晨1点生成前一天的日报
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyReports() {
        log.info("开始生成昨日营销分析日报...");
        
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            analyticsService.generateDailyReport(yesterday);
            log.info("营销分析日报生成完成: {}", yesterday.toLocalDate());
            
        } catch (Exception e) {
            log.error("营销分析日报生成失败", e);
        }
    }
    
    /**
     * 每周一凌晨2点生成上周的周报
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void generateWeeklyReports() {
        log.info("开始生成上周营销分析周报...");
        
        try {
            LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
            // 设置为上周一
            LocalDateTime lastMonday = lastWeek.with(DayOfWeek.MONDAY);
            analyticsService.generateWeeklyReport(lastMonday);
            log.info("营销分析周报生成完成: 第{}周", lastWeek.getYear() * 100 + getWeekOfYear(lastWeek));
            
        } catch (Exception e) {
            log.error("营销分析周报生成失败", e);
        }
    }
    
    /**
     * 每月1号凌晨3点生成上月的月报
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void generateMonthlyReports() {
        log.info("开始生成上月营销分析月报...");
        
        try {
            LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
            // 设置为上月1号
            LocalDateTime firstDayOfLastMonth = lastMonth.withDayOfMonth(1);
            analyticsService.generateMonthlyReport(firstDayOfLastMonth);
            log.info("营销分析月报生成完成: {}-{}", lastMonth.getYear(), lastMonth.getMonthValue());
            
        } catch (Exception e) {
            log.error("营销分析月报生成失败", e);
        }
    }
    
    /**
     * 每天凌晨5点清理3个月前的分析数据（保留聚合数据）
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cleanupOldAnalyticsData() {
        log.info("开始清理旧的营销分析数据...");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(3);
            analyticsGateway.deleteBefore(cutoffDate);
            log.info("营销分析数据清理完成，删除了{}之前的数据", cutoffDate.toLocalDate());
            
        } catch (Exception e) {
            log.error("营销分析数据清理失败", e);
        }
    }
    
    /**
     * 每小时检查活动表现并发送预警（如果表现异常）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkCampaignPerformance() {
        log.info("开始检查活动表现...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            
            var poorPerformingCampaigns = analyticsGateway.getPoorPerformingCampaigns(10, oneHourAgo, now);
            
            if (!poorPerformingCampaigns.isEmpty()) {
                log.warn("发现{}个表现不佳的活动", poorPerformingCampaigns.size());
                
                // 这里可以发送预警通知，例如邮件、钉钉、企微等
                for (var analytics : poorPerformingCampaigns) {
                    log.warn("活动表现预警: 活动[{}]({}) 转化率{} ROI{}",
                        analytics.getCampaignName(),
                        analytics.getCampaignId(),
                        analytics.getConversionRate(),
                        analytics.getReturnOnInvestment());
                }
            }
            
        } catch (Exception e) {
            log.error("活动表现检查失败", e);
        }
    }
    
    /**
     * 每天早上9点生成营销洞察报告（基于AI分析的建议）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateInsightsReport() {
        log.info("开始生成营销洞察报告...");
        
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            var topCampaigns = analyticsGateway.getTopPerformingCampaigns(5, yesterday, LocalDateTime.now());
            var poorCampaigns = analyticsGateway.getPoorPerformingCampaigns(5, yesterday, LocalDateTime.now());
            
            // 生成洞察和建议（这里可以集成AI分析）
            generatePerformanceInsights(topCampaigns, poorCampaigns);
            
            log.info("营销洞察报告生成完成");
            
        } catch (Exception e) {
            log.error("营销洞察报告生成失败", e);
        }
    }
    
    private void generatePerformanceInsights(var topCampaigns, var poorCampaigns) {
        // 分析最佳实践
        if (!topCampaigns.isEmpty()) {
            log.info("=== 最佳实践分析 ===");
            for (var campaign : topCampaigns) {
                log.info("优秀活动[{}]: 转化率{}%, ROI{}%, 建议复制其成功因素",
                    campaign.getCampaignName(),
                    campaign.getConversionRate(),
                    campaign.getReturnOnInvestment());
            }
        }
        
        // 分析改进建议
        if (!poorCampaigns.isEmpty()) {
            log.info("=== 改进建议分析 ===");
            for (var campaign : poorCampaigns) {
                String suggestions = generateOptimizationSuggestions(campaign);
                log.info("待改进活动[{}]: {}", campaign.getCampaignName(), suggestions);
            }
        }
    }
    
    private String generateOptimizationSuggestions(var analytics) {
        StringBuilder suggestions = new StringBuilder();
        
        if (analytics.getConversionRate() != null && analytics.getConversionRate().doubleValue() < 2.0) {
            suggestions.append("转化率过低，建议优化落地页和用户引导流程；");
        }
        
        if (analytics.getReturnOnInvestment() != null && analytics.getReturnOnInvestment().doubleValue() < 50.0) {
            suggestions.append("ROI偏低，建议优化投放策略和预算分配；");
        }
        
        if (analytics.getBounceRate() != null && analytics.getBounceRate().doubleValue() > 70.0) {
            suggestions.append("跳出率过高，建议改进页面内容和用户体验；");
        }
        
        if (suggestions.length() == 0) {
            suggestions.append("整体表现良好，建议继续监控关键指标变化");
        }
        
        return suggestions.toString();
    }
    
    private int getWeekOfYear(LocalDateTime dateTime) {
        return dateTime.getDayOfYear() / 7 + 1;
    }
}