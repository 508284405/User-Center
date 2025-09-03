package com.leyue.usercenter.domain.promotion;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class FullReductionActivity {
    
    private Long id;
    private String name;
    private String description;
    private ActivityStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // 满减规则配置
    private List<FullReductionRule> rules;
    private String applicableProducts; // JSON配置适用商品
    private String applicableCategories; // JSON配置适用分类
    private String excludeProducts; // JSON配置排除商品
    
    // 使用限制
    private Integer totalUsageLimit; // 总使用次数限制
    private Integer currentUsage; // 当前使用次数
    private Integer userUsageLimit; // 单用户使用次数限制
    private BigDecimal minOrderAmount; // 最小订单金额
    
    // 活动优先级
    private Integer priority;
    private Boolean canCombineWithCoupon; // 是否可与优惠券叠加
    private Boolean canCombineWithMemberDiscount; // 是否可与会员折扣叠加
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    public FullReductionActivity() {}
    
    public FullReductionActivity(String name, String description, LocalDateTime startTime, 
                                LocalDateTime endTime, List<FullReductionRule> rules, String createdBy) {
        this.name = name;
        this.description = description;
        this.status = ActivityStatus.DRAFT;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rules = rules;
        this.currentUsage = 0;
        this.priority = 5;
        this.canCombineWithCoupon = true;
        this.canCombineWithMemberDiscount = true;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 激活活动
     */
    public void activate() {
        validateForActivation();
        this.status = ActivityStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 暂停活动
     */
    public void pause() {
        if (this.status != ActivityStatus.ACTIVE) {
            throw new IllegalStateException("只有活跃状态的活动才能暂停");
        }
        this.status = ActivityStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查活动是否可用
     */
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == ActivityStatus.ACTIVE
                && now.isAfter(this.startTime)
                && now.isBefore(this.endTime)
                && (this.totalUsageLimit == null || this.currentUsage < this.totalUsageLimit);
    }
    
    /**
     * 计算满减金额
     */
    public BigDecimal calculateReduction(BigDecimal orderAmount) {
        if (!isAvailable()) {
            return BigDecimal.ZERO;
        }
        
        if (this.minOrderAmount != null && orderAmount.compareTo(this.minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        // 找到最大的满足条件的满减规则
        BigDecimal maxReduction = BigDecimal.ZERO;
        for (FullReductionRule rule : this.rules) {
            if (orderAmount.compareTo(rule.getThresholdAmount()) >= 0) {
                BigDecimal reduction = rule.calculateReduction(orderAmount);
                if (reduction.compareTo(maxReduction) > 0) {
                    maxReduction = reduction;
                }
            }
        }
        
        return maxReduction;
    }
    
    /**
     * 应用满减（增加使用次数）
     */
    public void applyReduction() {
        if (!isAvailable()) {
            throw new IllegalStateException("活动当前状态不可用");
        }
        
        this.currentUsage++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 获取适用的满减规则
     */
    public FullReductionRule getApplicableRule(BigDecimal orderAmount) {
        if (!isAvailable()) {
            return null;
        }
        
        FullReductionRule bestRule = null;
        for (FullReductionRule rule : this.rules) {
            if (orderAmount.compareTo(rule.getThresholdAmount()) >= 0) {
                if (bestRule == null || rule.getThresholdAmount().compareTo(bestRule.getThresholdAmount()) > 0) {
                    bestRule = rule;
                }
            }
        }
        
        return bestRule;
    }
    
    private void validateForActivation() {
        if (this.status != ActivityStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的活动才能激活");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (this.endTime.isBefore(now)) {
            throw new IllegalStateException("活动结束时间不能早于当前时间");
        }
        
        if (this.startTime.isAfter(this.endTime)) {
            throw new IllegalStateException("活动开始时间不能晚于结束时间");
        }
        
        if (this.rules == null || this.rules.isEmpty()) {
            throw new IllegalStateException("满减规则不能为空");
        }
    }
    
    public enum ActivityStatus {
        DRAFT("草稿"),
        ACTIVE("活跃"),
        PAUSED("暂停"),
        EXPIRED("已过期"),
        CANCELLED("已取消");
        
        private final String description;
        
        ActivityStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Data
    public static class FullReductionRule {
        private BigDecimal thresholdAmount; // 门槛金额
        private ReductionType type; // 减免类型
        private BigDecimal reductionValue; // 减免值
        private BigDecimal maxReduction; // 最大减免金额
        
        public FullReductionRule() {}
        
        public FullReductionRule(BigDecimal thresholdAmount, ReductionType type, 
                               BigDecimal reductionValue, BigDecimal maxReduction) {
            this.thresholdAmount = thresholdAmount;
            this.type = type;
            this.reductionValue = reductionValue;
            this.maxReduction = maxReduction;
        }
        
        /**
         * 计算减免金额
         */
        public BigDecimal calculateReduction(BigDecimal orderAmount) {
            if (orderAmount.compareTo(thresholdAmount) < 0) {
                return BigDecimal.ZERO;
            }
            
            BigDecimal reduction;
            
            switch (type) {
                case FIXED_AMOUNT:
                    // 固定金额减免
                    reduction = reductionValue;
                    break;
                case PERCENTAGE:
                    // 百分比减免
                    reduction = orderAmount.multiply(reductionValue).divide(new BigDecimal("100"));
                    break;
                case TIERED_FIXED:
                    // 阶梯式固定减免（每满X元减Y元）
                    int tiers = orderAmount.divide(thresholdAmount).intValue();
                    reduction = reductionValue.multiply(new BigDecimal(tiers));
                    break;
                default:
                    reduction = BigDecimal.ZERO;
            }
            
            // 应用最大减免限制
            if (maxReduction != null && reduction.compareTo(maxReduction) > 0) {
                reduction = maxReduction;
            }
            
            // 减免金额不能超过订单金额
            if (reduction.compareTo(orderAmount) > 0) {
                reduction = orderAmount;
            }
            
            return reduction;
        }
        
        public enum ReductionType {
            FIXED_AMOUNT("固定金额"),
            PERCENTAGE("百分比"),
            TIERED_FIXED("阶梯式固定");
            
            private final String description;
            
            ReductionType(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
}