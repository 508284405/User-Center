package com.leyue.usercenter.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.data.analytics.MarketingAnalyticsDTO;
import com.leyue.usercenter.dto.data.analytics.CampaignComparisonDTO;
import com.leyue.usercenter.dto.query.analytics.AnalyticsQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketingAnalyticsServiceI {
    
    SingleResponse<MarketingAnalyticsDTO> getAnalyticsSummary(AnalyticsQuery query);
    
    MultiResponse<MarketingAnalyticsDTO> getCampaignAnalytics(AnalyticsQuery query);
    
    MultiResponse<MarketingAnalyticsDTO> getTopPerformingCampaigns(AnalyticsQuery query);
    
    MultiResponse<CampaignComparisonDTO> compareCampaigns(List<String> campaignIds, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate);
    
    void generateDailyReport(LocalDateTime reportDate);
    
    void generateWeeklyReport(LocalDateTime reportDate);
    
    void generateMonthlyReport(LocalDateTime reportDate);
}