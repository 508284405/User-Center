package com.leyue.usercenter.domain.analytics;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class MarketingAnalytics {
    
    private Long id;
    private String campaignId;
    private String campaignName;
    private String campaignType;
    private LocalDateTime reportDate;
    private String reportPeriod; // DAILY, WEEKLY, MONTHLY
    
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
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public MarketingAnalytics() {}
    
    public MarketingAnalytics(String campaignId, String campaignName, String campaignType, 
                             LocalDateTime reportDate, String reportPeriod) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.reportDate = reportDate;
        this.reportPeriod = reportPeriod;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 计算核心指标
    public void calculateMetrics() {
        // 计算参与率
        if (totalParticipants != null && uniqueVisitors != null && uniqueVisitors > 0) {
            this.participationRate = new BigDecimal(totalParticipants)
                .divide(new BigDecimal(uniqueVisitors), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
        }
        
        // 计算转化率
        if (conversions != null && totalParticipants != null && totalParticipants > 0) {
            this.conversionRate = new BigDecimal(conversions)
                .divide(new BigDecimal(totalParticipants), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
        }
        
        // 计算平均订单价值
        if (totalRevenue != null && conversions != null && conversions > 0) {
            this.averageOrderValue = totalRevenue
                .divide(new BigDecimal(conversions), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // 计算获客成本
        if (budgetUsed != null && newParticipants != null && newParticipants > 0) {
            this.costPerAcquisition = budgetUsed
                .divide(new BigDecimal(newParticipants), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // 计算投资回报率
        if (totalRevenue != null && budgetUsed != null && budgetUsed.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = totalRevenue.subtract(budgetUsed);
            this.returnOnInvestment = profit
                .divide(budgetUsed, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
        }
        
        // 计算跳出率
        if (pageViews != null && uniqueVisitors != null && uniqueVisitors > 0) {
            int singlePageVisits = Math.max(0, uniqueVisitors - (pageViews - uniqueVisitors));
            this.bounceRate = new BigDecimal(singlePageVisits)
                .divide(new BigDecimal(uniqueVisitors), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isPerformanceGood() {
        if (conversionRate == null || returnOnInvestment == null) {
            return false;
        }
        
        // 定义良好表现的阈值
        boolean goodConversion = conversionRate.compareTo(new BigDecimal("5.0")) >= 0; // 转化率>=5%
        boolean goodROI = returnOnInvestment.compareTo(new BigDecimal("50.0")) >= 0; // ROI>=50%
        
        return goodConversion && goodROI;
    }
    
    public String getPerformanceLevel() {
        if (!isPerformanceGood()) {
            return "需要优化";
        }
        
        if (conversionRate != null && conversionRate.compareTo(new BigDecimal("15.0")) >= 0 &&
            returnOnInvestment != null && returnOnInvestment.compareTo(new BigDecimal("200.0")) >= 0) {
            return "表现优秀";
        }
        
        if (conversionRate != null && conversionRate.compareTo(new BigDecimal("10.0")) >= 0 &&
            returnOnInvestment != null && returnOnInvestment.compareTo(new BigDecimal("100.0")) >= 0) {
            return "表现良好";
        }
        
        return "表现一般";
    }
}