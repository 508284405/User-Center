package com.leyue.usercenter.domain.groupbuy;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupBuyActivity {
    
    private Long id;
    private String name;
    private String description;
    private GroupBuyStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // 商品信息
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal groupPrice;
    
    // 团购配置
    private Integer requiredParticipants; // 成团人数
    private Integer maxParticipants; // 最大参团人数
    private Integer limitPerUser; // 单用户限购数量
    private Integer groupTimeoutHours; // 成团超时时间(小时)
    
    // 活动限制
    private Integer totalStock;
    private Integer availableStock;
    private Integer soldCount;
    private Integer totalGroups; // 总开团数
    private Integer successfulGroups; // 成功成团数
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    public GroupBuyActivity() {}
    
    public GroupBuyActivity(String name, String description, LocalDateTime startTime, LocalDateTime endTime,
                           Long productId, String productName, BigDecimal originalPrice, BigDecimal groupPrice,
                           Integer requiredParticipants, Integer totalStock, Integer groupTimeoutHours, String createdBy) {
        this.name = name;
        this.description = description;
        this.status = GroupBuyStatus.NOT_STARTED;
        this.startTime = startTime;
        this.endTime = endTime;
        this.productId = productId;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.groupPrice = groupPrice;
        this.requiredParticipants = requiredParticipants;
        this.maxParticipants = requiredParticipants; // 默认等于成团人数
        this.limitPerUser = 1;
        this.groupTimeoutHours = groupTimeoutHours;
        this.totalStock = totalStock;
        this.availableStock = totalStock;
        this.soldCount = 0;
        this.totalGroups = 0;
        this.successfulGroups = 0;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 开始活动
     */
    public void start() {
        validateForStart();
        this.status = GroupBuyStatus.ONGOING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 暂停活动
     */
    public void pause() {
        if (this.status != GroupBuyStatus.ONGOING) {
            throw new IllegalStateException("只有进行中的活动才能暂停");
        }
        this.status = GroupBuyStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 结束活动
     */
    public void end() {
        this.status = GroupBuyStatus.ENDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查活动是否可参与
     */
    public boolean canParticipate() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == GroupBuyStatus.ONGOING
                && now.isAfter(this.startTime)
                && now.isBefore(this.endTime)
                && this.availableStock > 0;
    }
    
    /**
     * 扣减库存
     */
    public boolean decreaseStock(int quantity) {
        if (this.availableStock < quantity) {
            return false;
        }
        
        this.availableStock -= quantity;
        this.soldCount += quantity;
        this.updatedAt = LocalDateTime.now();
        return true;
    }
    
    /**
     * 恢复库存（团购失败时）
     */
    public void restoreStock(int quantity) {
        this.availableStock += quantity;
        this.soldCount -= quantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加开团数
     */
    public void increaseGroupCount() {
        this.totalGroups++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加成功成团数
     */
    public void increaseSuccessfulGroupCount() {
        this.successfulGroups++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 计算折扣率
     */
    public double getDiscountRate() {
        if (this.originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal discount = this.originalPrice.subtract(this.groupPrice);
        return discount.divide(this.originalPrice, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100.0;
    }
    
    /**
     * 计算成团成功率
     */
    public double getSuccessRate() {
        if (this.totalGroups == 0) {
            return 0.0;
        }
        return (double) this.successfulGroups / this.totalGroups * 100.0;
    }
    
    private void validateForStart() {
        if (this.status != GroupBuyStatus.NOT_STARTED) {
            throw new IllegalStateException("只有未开始的活动才能启动");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (this.endTime.isBefore(now)) {
            throw new IllegalStateException("活动结束时间不能早于当前时间");
        }
        
        if (this.requiredParticipants < 2) {
            throw new IllegalStateException("成团人数不能少于2人");
        }
        
        if (this.groupPrice.compareTo(this.originalPrice) >= 0) {
            throw new IllegalStateException("团购价必须低于原价");
        }
    }
    
    public enum GroupBuyStatus {
        NOT_STARTED("未开始"),
        ONGOING("进行中"),
        PAUSED("已暂停"),
        ENDED("已结束"),
        CANCELLED("已取消");
        
        private final String description;
        
        GroupBuyStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}