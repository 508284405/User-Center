package com.leyue.usercenter.dto.data.analytics;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
public class CampaignComparisonDTO extends DTO {
    
    private String campaignId;
    private String campaignName;
    private String campaignType;
    private Integer totalParticipants;
    private Integer conversions;
    private BigDecimal conversionRate;
    private BigDecimal totalRevenue;
    private BigDecimal budgetUsed;
    private BigDecimal returnOnInvestment;
    private String performanceLevel;
    
    // 对比指标（相对于平均值的比较）
    private BigDecimal participantsComparisonRatio; // 参与人数对比比率
    private BigDecimal conversionComparisonRatio;   // 转化率对比比率
    private BigDecimal revenueComparisonRatio;      // 收入对比比率
    private BigDecimal roiComparisonRatio;          // ROI对比比率
}