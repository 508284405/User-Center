package com.leyue.usercenter.dto.data.analytics;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class MarketingAnalyticsDTO extends DTO {
    
    private Long id;
    private String campaignId;
    private String campaignName;
    private String campaignType;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportPeriod;
    
    // 参与度指标
    private Integer totalParticipants;
    private Integer newParticipants;
    private Integer returningParticipants;
    private BigDecimal participationRate;
    
    // 转化指标
    private Integer conversions;
    private BigDecimal conversionRate;
    private BigDecimal averageOrderValue;
    private BigDecimal totalRevenue;
    
    // 成本效益指标
    private BigDecimal budgetUsed;
    private BigDecimal costPerAcquisition;
    private BigDecimal returnOnInvestment;
    
    // 用户行为指标
    private BigDecimal averageSessionDuration;
    private Integer pageViews;
    private Integer uniqueVisitors;
    private BigDecimal bounceRate;
    
    // 渠道分析
    private Map<String, Integer> channelDistribution;
    private Map<String, BigDecimal> channelConversionRates;
    
    // 设备和地域分析
    private Map<String, Integer> deviceDistribution;
    private Map<String, Integer> regionDistribution;
    
    // 时段分析
    private Map<String, Integer> hourlyDistribution;
    
    // 综合评估
    private String performanceLevel;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}