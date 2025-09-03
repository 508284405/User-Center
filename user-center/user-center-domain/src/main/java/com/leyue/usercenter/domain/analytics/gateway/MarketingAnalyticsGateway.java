package com.leyue.usercenter.domain.analytics.gateway;

import com.leyue.usercenter.domain.analytics.MarketingAnalytics;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketingAnalyticsGateway {
    
    MarketingAnalytics save(MarketingAnalytics analytics);
    
    MarketingAnalytics getById(Long id);
    
    List<MarketingAnalytics> getByCampaignId(String campaignId);
    
    List<MarketingAnalytics> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<MarketingAnalytics> getByPeriod(String reportPeriod, LocalDateTime startDate, LocalDateTime endDate);
    
    List<MarketingAnalytics> getByCampaignType(String campaignType, LocalDateTime startDate, LocalDateTime endDate);
    
    MarketingAnalytics getLatestByCampaign(String campaignId);
    
    List<MarketingAnalytics> getTopPerformingCampaigns(int limit, LocalDateTime startDate, LocalDateTime endDate);
    
    List<MarketingAnalytics> getPoorPerformingCampaigns(int limit, LocalDateTime startDate, LocalDateTime endDate);
    
    boolean existsByReportKey(String campaignId, LocalDateTime reportDate, String reportPeriod);
    
    void deleteBefore(LocalDateTime cutoffDate);
    
    // 聚合查询
    MarketingAnalytics getAggregatedAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    List<MarketingAnalytics> getCampaignComparison(List<String> campaignIds, LocalDateTime startDate, LocalDateTime endDate);
}