package com.leyue.usercenter.dto.data.seckill;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class SeckillActivityDTO extends DTO {
    
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String statusDescription;
    
    // 商品信息
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Double discountRate;
    
    // 库存信息
    private Integer totalStock;
    private Integer availableStock;
    private Integer soldCount;
    private Integer limitPerUser;
    private Double salesProgress;
    private Boolean soldOut;
    
    // 活动状态判断
    private Boolean canParticipate;
    private Long remainingSeconds; // 剩余时间（秒）
    
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}