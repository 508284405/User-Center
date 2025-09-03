package com.leyue.usercenter.domain.seckill;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SeckillActivity {
    
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SeckillStatus status;
    
    // 商品信息
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    
    // 库存信息
    private Integer totalStock;
    private Integer availableStock;
    private Integer soldCount;
    private Integer limitPerUser;
    
    // 并发控制
    private Integer version;
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    public SeckillActivity() {}
    
    public SeckillActivity(String name, String description, LocalDateTime startTime, 
                          LocalDateTime endTime, Long productId, String productName,
                          BigDecimal originalPrice, BigDecimal seckillPrice, 
                          Integer totalStock, Integer limitPerUser, String createdBy) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.productId = productId;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.seckillPrice = seckillPrice;
        this.totalStock = totalStock;
        this.availableStock = totalStock;
        this.soldCount = 0;
        this.limitPerUser = limitPerUser;
        this.status = SeckillStatus.NOT_STARTED;
        this.version = 0;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 检查活动是否可以参与
     */
    public boolean canParticipate() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == SeckillStatus.ONGOING 
                && now.isAfter(this.startTime) 
                && now.isBefore(this.endTime)
                && this.availableStock > 0;
    }
    
    /**
     * 秒杀商品（原子性扣减库存）
     */
    public boolean seckillProduct(int quantity) {
        if (!canParticipate()) {
            throw new IllegalStateException("活动当前状态不允许秒杀");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }
        
        if (quantity > this.limitPerUser) {
            throw new IllegalArgumentException("购买数量超过单用户限制");
        }
        
        if (this.availableStock < quantity) {
            return false; // 库存不足
        }
        
        // 扣减库存
        this.availableStock -= quantity;
        this.soldCount += quantity;
        this.updatedAt = LocalDateTime.now();
        
        return true;
    }
    
    /**
     * 开始活动
     */
    public void start() {
        validateForStart();
        this.status = SeckillStatus.ONGOING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 结束活动
     */
    public void end() {
        this.status = SeckillStatus.ENDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 暂停活动
     */
    public void pause() {
        if (this.status != SeckillStatus.ONGOING) {
            throw new IllegalStateException("只有进行中的活动才能暂停");
        }
        this.status = SeckillStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复活动
     */
    public void resume() {
        if (this.status != SeckillStatus.PAUSED) {
            throw new IllegalStateException("只有暂停的活动才能恢复");
        }
        this.status = SeckillStatus.ONGOING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消活动
     */
    public void cancel() {
        if (this.status == SeckillStatus.ENDED) {
            throw new IllegalStateException("已结束的活动不能取消");
        }
        this.status = SeckillStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 获取销售进度百分比
     */
    public double getSalesProgress() {
        if (this.totalStock == 0) {
            return 0.0;
        }
        return (double) this.soldCount / this.totalStock * 100.0;
    }
    
    /**
     * 检查是否售罄
     */
    public boolean isSoldOut() {
        return this.availableStock <= 0;
    }
    
    /**
     * 计算折扣率
     */
    public double getDiscountRate() {
        if (this.originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal discount = this.originalPrice.subtract(this.seckillPrice);
        return discount.divide(this.originalPrice, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100.0;
    }
    
    private void validateForStart() {
        LocalDateTime now = LocalDateTime.now();
        
        if (this.status != SeckillStatus.NOT_STARTED) {
            throw new IllegalStateException("只有未开始的活动才能启动");
        }
        
        if (this.endTime.isBefore(now)) {
            throw new IllegalStateException("活动结束时间不能早于当前时间");
        }
        
        if (this.startTime.isAfter(this.endTime)) {
            throw new IllegalStateException("活动开始时间不能晚于结束时间");
        }
        
        if (this.totalStock <= 0) {
            throw new IllegalStateException("活动库存必须大于0");
        }
        
        if (this.seckillPrice.compareTo(this.originalPrice) >= 0) {
            throw new IllegalStateException("秒杀价格必须低于原价");
        }
    }
    
    public enum SeckillStatus {
        NOT_STARTED("未开始"),
        ONGOING("进行中"),
        PAUSED("已暂停"),
        ENDED("已结束"),
        CANCELLED("已取消");
        
        private final String description;
        
        SeckillStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}