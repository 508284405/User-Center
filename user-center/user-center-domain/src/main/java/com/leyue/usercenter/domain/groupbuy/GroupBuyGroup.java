package com.leyue.usercenter.domain.groupbuy;

import com.alibaba.cola.domain.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupBuyGroup {
    
    private Long id;
    private Long activityId;
    private String groupNumber; // 团号
    private String leaderUserId; // 团长用户ID
    private GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expireTime; // 过期时间
    private LocalDateTime completedAt; // 成团时间
    
    // 团购信息
    private Integer requiredParticipants; // 需要人数
    private Integer currentParticipants; // 当前人数
    private BigDecimal groupPrice; // 团购价
    private BigDecimal totalAmount; // 总金额
    
    // 参团记录
    private List<GroupParticipant> participants;
    
    public GroupBuyGroup() {
        this.participants = new ArrayList<>();
    }
    
    public GroupBuyGroup(Long activityId, String leaderUserId, Integer requiredParticipants, 
                        BigDecimal groupPrice, int timeoutHours) {
        this.activityId = activityId;
        this.groupNumber = generateGroupNumber();
        this.leaderUserId = leaderUserId;
        this.status = GroupStatus.FORMING;
        this.requiredParticipants = requiredParticipants;
        this.currentParticipants = 0;
        this.groupPrice = groupPrice;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.expireTime = LocalDateTime.now().plusHours(timeoutHours);
        this.participants = new ArrayList<>();
    }
    
    /**
     * 加入团购
     */
    public boolean joinGroup(String userId, Integer quantity) {
        if (!canJoin(userId)) {
            return false;
        }
        
        // 检查是否已经参团
        if (hasParticipated(userId)) {
            throw new IllegalStateException("用户已经参加过此团");
        }
        
        // 检查团购数量
        if (this.currentParticipants + quantity > this.requiredParticipants) {
            return false; // 超过成团人数
        }
        
        // 添加参团记录
        GroupParticipant participant = new GroupParticipant(userId, quantity, this.groupPrice);
        this.participants.add(participant);
        this.currentParticipants += quantity;
        this.totalAmount = this.totalAmount.add(this.groupPrice.multiply(new BigDecimal(quantity)));
        
        // 检查是否成团
        if (this.currentParticipants >= this.requiredParticipants) {
            completeGroup();
        }
        
        return true;
    }
    
    /**
     * 退出团购
     */
    public boolean leaveGroup(String userId) {
        if (this.status != GroupStatus.FORMING) {
            throw new IllegalStateException("团购已结束，无法退出");
        }
        
        GroupParticipant participant = findParticipant(userId);
        if (participant == null) {
            return false;
        }
        
        // 团长不能退团（除非解散）
        if (userId.equals(this.leaderUserId)) {
            throw new IllegalStateException("团长不能退团，只能解散");
        }
        
        // 移除参团记录
        this.participants.remove(participant);
        this.currentParticipants -= participant.getQuantity();
        this.totalAmount = this.totalAmount.subtract(
            this.groupPrice.multiply(new BigDecimal(participant.getQuantity())));
        
        return true;
    }
    
    /**
     * 解散团购（团长操作）
     */
    public void dissolveGroup(String operatorUserId) {
        if (!operatorUserId.equals(this.leaderUserId)) {
            throw new IllegalStateException("只有团长才能解散团购");
        }
        
        if (this.status == GroupStatus.SUCCESS) {
            throw new IllegalStateException("已成团的团购无法解散");
        }
        
        this.status = GroupStatus.DISSOLVED;
    }
    
    /**
     * 团购过期
     */
    public void expire() {
        if (this.status == GroupStatus.FORMING) {
            this.status = GroupStatus.FAILED;
        }
    }
    
    /**
     * 检查是否可以加入
     */
    public boolean canJoin(String userId) {
        LocalDateTime now = LocalDateTime.now();
        
        return this.status == GroupStatus.FORMING
                && now.isBefore(this.expireTime)
                && this.currentParticipants < this.requiredParticipants;
    }
    
    /**
     * 检查用户是否已参团
     */
    public boolean hasParticipated(String userId) {
        return this.participants.stream()
                .anyMatch(participant -> participant.getUserId().equals(userId));
    }
    
    /**
     * 获取剩余时间（分钟）
     */
    public long getRemainingMinutes() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.expireTime)) {
            return 0;
        }
        return java.time.Duration.between(now, this.expireTime).toMinutes();
    }
    
    /**
     * 获取还需要的人数
     */
    public int getRequiredMoreParticipants() {
        return Math.max(0, this.requiredParticipants - this.currentParticipants);
    }
    
    private void completeGroup() {
        this.status = GroupStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }
    
    private GroupParticipant findParticipant(String userId) {
        return this.participants.stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
    
    private String generateGroupNumber() {
        // 生成团号：GB + 时间戳 + 随机数
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("GB%d%03d", timestamp, random);
    }
    
    public enum GroupStatus {
        FORMING("组团中"),
        SUCCESS("已成团"),
        FAILED("团购失败"),
        DISSOLVED("已解散");
        
        private final String description;
        
        GroupStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Data
    public static class GroupParticipant {
        private String userId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private LocalDateTime joinTime;
        
        public GroupParticipant() {}
        
        public GroupParticipant(String userId, Integer quantity, BigDecimal unitPrice) {
            this.userId = userId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
            this.joinTime = LocalDateTime.now();
        }
    }
}