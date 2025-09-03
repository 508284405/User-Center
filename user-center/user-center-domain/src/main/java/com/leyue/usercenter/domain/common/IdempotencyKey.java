package com.leyue.usercenter.domain.common;

import lombok.Value;

import java.util.Objects;

/**
 * 幂等键值对象
 * 
 * @author Claude Code
 */
@Value
public class IdempotencyKey {
    
    String value;
    
    public static IdempotencyKey of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key cannot be null or empty");
        }
        return new IdempotencyKey(value.trim());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IdempotencyKey that = (IdempotencyKey) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}