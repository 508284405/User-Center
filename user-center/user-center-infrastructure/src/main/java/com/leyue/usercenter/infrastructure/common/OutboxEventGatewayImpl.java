package com.leyue.usercenter.infrastructure.common;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.domain.common.DomainEvent;
import com.leyue.usercenter.domain.common.gateway.OutboxEventGateway;
import com.leyue.usercenter.infrastructure.common.dataobject.OutboxEventDO;
import com.leyue.usercenter.infrastructure.common.mapper.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbox事件网关实现
 * 
 * @author Claude Code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventGatewayImpl implements OutboxEventGateway {
    
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public void save(DomainEvent event) {
        try {
            OutboxEventDO outboxEvent = new OutboxEventDO();
            outboxEvent.setEventId(event.getEventId());
            outboxEvent.setTopic(event.getEventType());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(event));
            outboxEvent.setStatus("NEW");
            outboxEvent.setRetryCnt(0);
            
            outboxEventMapper.insert(outboxEvent);
            
            log.debug("Outbox event saved: eventId={}, topic={}", 
                     event.getEventId(), event.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event: {}", event.getEventId(), e);
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }
    
    @Override
    @Transactional
    public void saveAll(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            save(event);
        }
    }
    
    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        List<OutboxEventDO> eventDOs = outboxEventMapper.findPendingEvents(limit);
        
        return eventDOs.stream()
                .map(this::toOutboxEvent)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void markAsSent(String eventId) {
        LambdaUpdateWrapper<OutboxEventDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OutboxEventDO::getEventId, eventId)
                     .set(OutboxEventDO::getStatus, "SENT")
                     .set(OutboxEventDO::getUpdatedAt, LocalDateTime.now());
        
        outboxEventMapper.update(null, updateWrapper);
        
        log.debug("Outbox event marked as sent: eventId={}", eventId);
    }
    
    @Override
    @Transactional
    public void markAsFailed(String eventId, String errorMsg) {
        LambdaUpdateWrapper<OutboxEventDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OutboxEventDO::getEventId, eventId)
                     .set(OutboxEventDO::getStatus, "FAILED")
                     .set(OutboxEventDO::getErrorMsg, errorMsg)
                     .set(OutboxEventDO::getNextRetryAt, calculateNextRetryTime())
                     .set(OutboxEventDO::getUpdatedAt, LocalDateTime.now())
                     .setSql("retry_cnt = retry_cnt + 1");
        
        outboxEventMapper.update(null, updateWrapper);
        
        log.warn("Outbox event marked as failed: eventId={}, error={}", eventId, errorMsg);
    }
    
    private OutboxEvent toOutboxEvent(OutboxEventDO eventDO) {
        return new OutboxEvent(
                eventDO.getEventId(),
                eventDO.getTopic(),
                eventDO.getPayloadJson(),
                eventDO.getStatus(),
                eventDO.getRetryCnt()
        );
    }
    
    private LocalDateTime calculateNextRetryTime() {
        // 简单的指数退避策略: 1分钟后重试
        return LocalDateTime.now().plusMinutes(1);
    }
}