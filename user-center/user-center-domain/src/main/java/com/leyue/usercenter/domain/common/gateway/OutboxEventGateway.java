package com.leyue.usercenter.domain.common.gateway;

import com.leyue.usercenter.domain.common.DomainEvent;

import java.util.List;

/**
 * Outbox事件网关接口
 * 
 * @author Claude Code
 */
public interface OutboxEventGateway {
    
    /**
     * 保存事件到Outbox表
     * 
     * @param event 领域事件
     */
    void save(DomainEvent event);
    
    /**
     * 批量保存事件到Outbox表
     * 
     * @param events 领域事件列表
     */
    void saveAll(List<DomainEvent> events);
    
    /**
     * 获取待发送的事件
     * 
     * @param limit 限制数量
     * @return 待发送事件列表
     */
    List<OutboxEvent> findPendingEvents(int limit);
    
    /**
     * 标记事件为已发送
     * 
     * @param eventId 事件ID
     */
    void markAsSent(String eventId);
    
    /**
     * 标记事件为发送失败
     * 
     * @param eventId 事件ID
     * @param errorMsg 错误信息
     */
    void markAsFailed(String eventId, String errorMsg);
    
    /**
     * Outbox事件数据对象
     */
    public static class OutboxEvent {
        private String eventId;
        private String topic;
        private String payloadJson;
        private String status;
        private int retryCnt;
        
        public OutboxEvent() {}
        
        public OutboxEvent(String eventId, String topic, String payloadJson, String status, int retryCnt) {
            this.eventId = eventId;
            this.topic = topic;
            this.payloadJson = payloadJson;
            this.status = status;
            this.retryCnt = retryCnt;
        }
        
        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public String getPayloadJson() { return payloadJson; }
        public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getRetryCnt() { return retryCnt; }
        public void setRetryCnt(int retryCnt) { this.retryCnt = retryCnt; }
    }
}