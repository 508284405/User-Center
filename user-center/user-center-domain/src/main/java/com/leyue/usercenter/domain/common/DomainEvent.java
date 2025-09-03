package com.leyue.usercenter.domain.common;

import java.time.LocalDateTime;

/**
 * 领域事件基类
 * 
 * @author Claude Code
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredAt;
    
    protected DomainEvent(String eventId) {
        this.eventId = eventId;
        this.occurredAt = LocalDateTime.now();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    public abstract String getEventType();
}