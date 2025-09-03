package com.leyue.usercenter.dto.query.analytics;

import com.alibaba.cola.dto.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class AnalyticsQuery extends Query {
    
    private String campaignId;
    private String campaignType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportPeriod; // DAILY, WEEKLY, MONTHLY
    private Integer limit;
    private String sortBy;
    private String sortOrder; // ASC, DESC
}