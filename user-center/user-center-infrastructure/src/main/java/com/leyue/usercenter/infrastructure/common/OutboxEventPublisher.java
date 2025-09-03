package com.leyue.usercenter.infrastructure.common;

import com.leyue.usercenter.domain.common.gateway.OutboxEventGateway;
import com.leyue.usercenter.domain.common.gateway.OutboxEventGateway.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbox事件发布器 - 负责将事件发送到外部消息系统
 * 
 * @author Claude Code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    
    private final OutboxEventGateway outboxEventGateway;
    // private final MessageProducer messageProducer; // 实际项目中注入消息生产者
    
    /**
     * 定时扫描并发送Outbox事件
     */
    @Scheduled(fixedDelay = 30000) // 每30秒执行一次
    public void publishPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxEventGateway.findPendingEvents(100);
            
            if (pendingEvents.isEmpty()) {
                return;
            }
            
            log.info("Found {} pending outbox events to publish", pendingEvents.size());
            
            for (OutboxEvent event : pendingEvents) {
                publishEvent(event);
            }
            
        } catch (Exception e) {
            log.error("Failed to publish pending outbox events", e);
        }
    }
    
    private void publishEvent(OutboxEvent event) {
        try {
            // 模拟发送到消息队列/事件总线
            // messageProducer.send(event.getTopic(), event.getPayloadJson());
            
            // 这里简化为打印日志，实际项目中替换为真实的消息发送
            log.info("Publishing outbox event: eventId={}, topic={}, payload={}", 
                    event.getEventId(), event.getTopic(), event.getPayloadJson());
            
            // 标记为已发送
            outboxEventGateway.markAsSent(event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to publish outbox event: eventId={}", event.getEventId(), e);
            
            // 标记为发送失败
            outboxEventGateway.markAsFailed(event.getEventId(), e.getMessage());
        }
    }
}