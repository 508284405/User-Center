package com.leyue.usercenter.domain.common;

import com.leyue.usercenter.domain.common.gateway.OutboxEventGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 领域事件发布器
 * 
 * @author Claude Code
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DomainEventPublisher {
    
    private final OutboxEventGateway outboxEventGateway;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 发布单个领域事件
     * 
     * @param event 领域事件
     */
    public void publish(DomainEvent event) {
        try {
            // 保存到Outbox表实现事务性发布
            outboxEventGateway.save(event);
            
            // 同时发布Spring事件供本地消费
            applicationEventPublisher.publishEvent(event);
            
            log.debug("Domain event published: eventId={}, type={}", 
                     event.getEventId(), event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish domain event: eventId={}, type={}", 
                     event.getEventId(), event.getEventType(), e);
            throw new RuntimeException("Failed to publish domain event", e);
        }
    }
    
    /**
     * 批量发布领域事件
     * 
     * @param events 领域事件列表
     */
    public void publishAll(List<DomainEvent> events) {
        try {
            // 批量保存到Outbox表
            outboxEventGateway.saveAll(events);
            
            // 逐个发布Spring事件
            events.forEach(applicationEventPublisher::publishEvent);
            
            log.debug("Batch domain events published: count={}", events.size());
        } catch (Exception e) {
            log.error("Failed to publish batch domain events: count={}", events.size(), e);
            throw new RuntimeException("Failed to publish batch domain events", e);
        }
    }
}