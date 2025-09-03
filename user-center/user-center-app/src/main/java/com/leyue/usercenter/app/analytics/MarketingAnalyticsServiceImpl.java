package com.leyue.usercenter.app.analytics;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.MarketingAnalyticsServiceI;
import com.leyue.usercenter.dto.data.analytics.MarketingAnalyticsDTO;
import com.leyue.usercenter.dto.data.analytics.CampaignComparisonDTO;
import com.leyue.usercenter.dto.query.analytics.AnalyticsQuery;
import com.leyue.usercenter.domain.analytics.MarketingAnalytics;
import com.leyue.usercenter.domain.analytics.gateway.MarketingAnalyticsGateway;
import com.leyue.usercenter.domain.promotion.gateway.CampaignGateway;
import com.leyue.usercenter.infrastructure.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingAnalyticsServiceImpl implements MarketingAnalyticsServiceI {
    
    private final MarketingAnalyticsGateway analyticsGateway;
    private final CampaignGateway campaignGateway;
    private final OutboxEventPublisher outboxEventPublisher;
    
    @Override
    public SingleResponse<MarketingAnalyticsDTO> getAnalyticsSummary(AnalyticsQuery query) {
        log.info("获取营销分析汇总数据: {}", query);
        
        try {
            MarketingAnalytics aggregated = analyticsGateway.getAggregatedAnalytics(
                query.getStartDate(), query.getEndDate());
            
            if (aggregated == null) {
                // 如果没有聚合数据，创建一个空的响应
                MarketingAnalyticsDTO dto = new MarketingAnalyticsDTO();
                dto.setReportPeriod("CUSTOM");
                dto.setStartDate(query.getStartDate());
                dto.setEndDate(query.getEndDate());
                return SingleResponse.of(dto);
            }
            
            MarketingAnalyticsDTO dto = convertToDTO(aggregated);
            return SingleResponse.of(dto);
            
        } catch (Exception e) {
            log.error("获取营销分析汇总数据失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("QUERY_FAILED", "查询失败");
        }
    }
    
    @Override
    public MultiResponse<MarketingAnalyticsDTO> getCampaignAnalytics(AnalyticsQuery query) {
        log.info("获取活动分析数据: {}", query);
        
        try {
            List<MarketingAnalytics> analyticsList;
            
            if (query.getCampaignId() != null) {
                analyticsList = analyticsGateway.getByCampaignId(query.getCampaignId());
            } else if (query.getCampaignType() != null) {
                analyticsList = analyticsGateway.getByCampaignType(
                    query.getCampaignType(), query.getStartDate(), query.getEndDate());
            } else {
                analyticsList = analyticsGateway.getByDateRange(
                    query.getStartDate(), query.getEndDate());
            }
            
            List<MarketingAnalyticsDTO> dtoList = analyticsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return MultiResponse.of(dtoList);
            
        } catch (Exception e) {
            log.error("获取活动分析数据失败: {}", e.getMessage(), e);
            return MultiResponse.buildFailure("QUERY_FAILED", "查询失败");
        }
    }
    
    @Override
    public MultiResponse<MarketingAnalyticsDTO> getTopPerformingCampaigns(AnalyticsQuery query) {
        log.info("获取表现最佳活动: {}", query);
        
        try {
            int limit = query.getLimit() != null ? query.getLimit() : 10;
            List<MarketingAnalytics> analyticsList = analyticsGateway.getTopPerformingCampaigns(
                limit, query.getStartDate(), query.getEndDate());
            
            List<MarketingAnalyticsDTO> dtoList = analyticsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return MultiResponse.of(dtoList);
            
        } catch (Exception e) {
            log.error("获取表现最佳活动失败: {}", e.getMessage(), e);
            return MultiResponse.buildFailure("QUERY_FAILED", "查询失败");
        }
    }
    
    @Override
    public MultiResponse<CampaignComparisonDTO> compareCampaigns(List<String> campaignIds, 
                                                                 LocalDateTime startDate, 
                                                                 LocalDateTime endDate) {
        log.info("对比活动数据: campaignIds={}", campaignIds);
        
        try {
            List<MarketingAnalytics> analyticsList = analyticsGateway.getCampaignComparison(
                campaignIds, startDate, endDate);
            
            List<CampaignComparisonDTO> comparisonList = analyticsList.stream()
                .map(this::convertToComparisonDTO)
                .collect(Collectors.toList());
            
            return MultiResponse.of(comparisonList);
            
        } catch (Exception e) {
            log.error("对比活动数据失败: {}", e.getMessage(), e);
            return MultiResponse.buildFailure("QUERY_FAILED", "查询失败");
        }
    }
    
    @Override
    @Async
    @Transactional
    public void generateDailyReport(LocalDateTime reportDate) {
        log.info("生成日报: {}", reportDate);
        
        try {
            // 获取所有活跃的活动
            LocalDateTime now = LocalDateTime.now();
            var activeCampaigns = campaignGateway.getActiveCampaigns(now);
            
            for (var campaign : activeCampaigns) {
                String reportKey = campaign.getId() + "_" + reportDate.toLocalDate() + "_DAILY";
                
                // 检查是否已经生成过报告
                if (analyticsGateway.existsByReportKey(campaign.getId().toString(), reportDate, "DAILY")) {
                    continue;
                }
                
                // 生成分析数据（这里使用模拟数据，实际应该从数据仓库查询）
                MarketingAnalytics analytics = generateAnalyticsData(campaign, reportDate, "DAILY");
                analytics.calculateMetrics();
                
                analyticsGateway.save(analytics);
                
                // 发布分析报告生成事件
                outboxEventPublisher.publish("DAILY_REPORT_GENERATED", 
                    String.format("{\"campaignId\":\"%s\",\"reportDate\":\"%s\",\"performanceLevel\":\"%s\"}", 
                        campaign.getId(), reportDate, analytics.getPerformanceLevel()));
            }
            
            log.info("日报生成完成: {}", reportDate);
            
        } catch (Exception e) {
            log.error("生成日报失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Async
    @Transactional
    public void generateWeeklyReport(LocalDateTime reportDate) {
        log.info("生成周报: {}", reportDate);
        
        try {
            var activeCampaigns = campaignGateway.getActiveCampaigns(LocalDateTime.now());
            
            for (var campaign : activeCampaigns) {
                if (analyticsGateway.existsByReportKey(campaign.getId().toString(), reportDate, "WEEKLY")) {
                    continue;
                }
                
                MarketingAnalytics analytics = generateAnalyticsData(campaign, reportDate, "WEEKLY");
                analytics.calculateMetrics();
                
                analyticsGateway.save(analytics);
                
                outboxEventPublisher.publish("WEEKLY_REPORT_GENERATED", 
                    String.format("{\"campaignId\":\"%s\",\"reportDate\":\"%s\",\"performanceLevel\":\"%s\"}", 
                        campaign.getId(), reportDate, analytics.getPerformanceLevel()));
            }
            
            log.info("周报生成完成: {}", reportDate);
            
        } catch (Exception e) {
            log.error("生成周报失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Async
    @Transactional
    public void generateMonthlyReport(LocalDateTime reportDate) {
        log.info("生成月报: {}", reportDate);
        
        try {
            var activeCampaigns = campaignGateway.getActiveCampaigns(LocalDateTime.now());
            
            for (var campaign : activeCampaigns) {
                if (analyticsGateway.existsByReportKey(campaign.getId().toString(), reportDate, "MONTHLY")) {
                    continue;
                }
                
                MarketingAnalytics analytics = generateAnalyticsData(campaign, reportDate, "MONTHLY");
                analytics.calculateMetrics();
                
                analyticsGateway.save(analytics);
                
                outboxEventPublisher.publish("MONTHLY_REPORT_GENERATED", 
                    String.format("{\"campaignId\":\"%s\",\"reportDate\":\"%s\",\"performanceLevel\":\"%s\"}", 
                        campaign.getId(), reportDate, analytics.getPerformanceLevel()));
            }
            
            log.info("月报生成完成: {}", reportDate);
            
        } catch (Exception e) {
            log.error("生成月报失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private MarketingAnalytics generateAnalyticsData(var campaign, LocalDateTime reportDate, String period) {
        MarketingAnalytics analytics = new MarketingAnalytics(
            campaign.getId().toString(),
            campaign.getName(),
            campaign.getType().name(),
            reportDate,
            period
        );
        
        // 这里应该从实际的数据源（如数据仓库、点击流数据、订单数据等）获取指标
        // 以下使用模拟数据
        Random random = new Random();
        int baseParticipants = campaign.getCurrentParticipants() != null ? campaign.getCurrentParticipants() : 0;
        
        analytics.setTotalParticipants(baseParticipants + random.nextInt(100));
        analytics.setNewParticipants(random.nextInt(50));
        analytics.setReturningParticipants(analytics.getTotalParticipants() - analytics.getNewParticipants());
        analytics.setConversions(random.nextInt(analytics.getTotalParticipants() / 5 + 1));
        analytics.setTotalRevenue(new BigDecimal(random.nextInt(10000) + 1000));
        analytics.setBudgetUsed(new BigDecimal(campaign.getUsedBudget() != null ? campaign.getUsedBudget() : 0));
        analytics.setPageViews(random.nextInt(1000) + 500);
        analytics.setUniqueVisitors(random.nextInt(500) + 200);
        
        // 渠道分布
        Map<String, Integer> channelDistribution = new HashMap<>();
        channelDistribution.put("搜索引擎", random.nextInt(40) + 20);
        channelDistribution.put("社交媒体", random.nextInt(30) + 15);
        channelDistribution.put("直接访问", random.nextInt(25) + 10);
        channelDistribution.put("邮件营销", random.nextInt(15) + 5);
        analytics.setChannelDistribution(channelDistribution);
        
        // 设备分布
        Map<String, Integer> deviceDistribution = new HashMap<>();
        deviceDistribution.put("移动端", random.nextInt(60) + 50);
        deviceDistribution.put("桌面端", random.nextInt(30) + 20);
        deviceDistribution.put("平板", random.nextInt(20) + 5);
        analytics.setDeviceDistribution(deviceDistribution);
        
        return analytics;
    }
    
    private MarketingAnalyticsDTO convertToDTO(MarketingAnalytics analytics) {
        MarketingAnalyticsDTO dto = new MarketingAnalyticsDTO();
        dto.setId(analytics.getId());
        dto.setCampaignId(analytics.getCampaignId());
        dto.setCampaignName(analytics.getCampaignName());
        dto.setCampaignType(analytics.getCampaignType());
        dto.setReportDate(analytics.getReportDate());
        dto.setReportPeriod(analytics.getReportPeriod());
        dto.setTotalParticipants(analytics.getTotalParticipants());
        dto.setNewParticipants(analytics.getNewParticipants());
        dto.setReturningParticipants(analytics.getReturningParticipants());
        dto.setParticipationRate(analytics.getParticipationRate());
        dto.setConversions(analytics.getConversions());
        dto.setConversionRate(analytics.getConversionRate());
        dto.setAverageOrderValue(analytics.getAverageOrderValue());
        dto.setTotalRevenue(analytics.getTotalRevenue());
        dto.setBudgetUsed(analytics.getBudgetUsed());
        dto.setCostPerAcquisition(analytics.getCostPerAcquisition());
        dto.setReturnOnInvestment(analytics.getReturnOnInvestment());
        dto.setAverageSessionDuration(analytics.getAverageSessionDuration());
        dto.setPageViews(analytics.getPageViews());
        dto.setUniqueVisitors(analytics.getUniqueVisitors());
        dto.setBounceRate(analytics.getBounceRate());
        dto.setChannelDistribution(analytics.getChannelDistribution());
        dto.setChannelConversionRates(analytics.getChannelConversionRates());
        dto.setDeviceDistribution(analytics.getDeviceDistribution());
        dto.setRegionDistribution(analytics.getRegionDistribution());
        dto.setHourlyDistribution(analytics.getHourlyDistribution());
        dto.setPerformanceLevel(analytics.getPerformanceLevel());
        dto.setCreatedAt(analytics.getCreatedAt());
        dto.setUpdatedAt(analytics.getUpdatedAt());
        return dto;
    }
    
    private CampaignComparisonDTO convertToComparisonDTO(MarketingAnalytics analytics) {
        CampaignComparisonDTO dto = new CampaignComparisonDTO();
        dto.setCampaignId(analytics.getCampaignId());
        dto.setCampaignName(analytics.getCampaignName());
        dto.setCampaignType(analytics.getCampaignType());
        dto.setTotalParticipants(analytics.getTotalParticipants());
        dto.setConversions(analytics.getConversions());
        dto.setConversionRate(analytics.getConversionRate());
        dto.setTotalRevenue(analytics.getTotalRevenue());
        dto.setBudgetUsed(analytics.getBudgetUsed());
        dto.setReturnOnInvestment(analytics.getReturnOnInvestment());
        dto.setPerformanceLevel(analytics.getPerformanceLevel());
        return dto;
    }
}