package com.leyue.usercenter.infrastructure.common;

import com.leyue.usercenter.domain.common.IdempotencyKey;
import com.leyue.usercenter.domain.common.gateway.IdempotencyGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于Redis的幂等性网关实现
 * 
 * @author Claude Code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyGatewayImpl implements IdempotencyGateway {
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "uc:idempotency:";
    
    private final RedissonClient redissonClient;
    
    @Override
    public boolean checkAndSet(IdempotencyKey key, long ttlSeconds) {
        String redisKey = buildRedisKey(key);
        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        
        try {
            boolean success = bucket.trySet("1", Duration.ofSeconds(ttlSeconds));
            log.debug("Idempotency check and set: key={}, ttl={}, success={}", 
                     key.getValue(), ttlSeconds, success);
            return success;
        } catch (Exception e) {
            log.error("Failed to check and set idempotency key: {}", key.getValue(), e);
            return false;
        }
    }
    
    @Override
    public boolean exists(IdempotencyKey key) {
        String redisKey = buildRedisKey(key);
        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        
        try {
            boolean exists = bucket.isExists();
            log.debug("Idempotency exists check: key={}, exists={}", key.getValue(), exists);
            return exists;
        } catch (Exception e) {
            log.error("Failed to check idempotency key existence: {}", key.getValue(), e);
            return true; // 出错时保守返回true，避免重复操作
        }
    }
    
    @Override
    public void remove(IdempotencyKey key) {
        String redisKey = buildRedisKey(key);
        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        
        try {
            boolean deleted = bucket.delete();
            log.debug("Idempotency key removed: key={}, deleted={}", key.getValue(), deleted);
        } catch (Exception e) {
            log.error("Failed to remove idempotency key: {}", key.getValue(), e);
        }
    }
    
    private String buildRedisKey(IdempotencyKey key) {
        return IDEMPOTENCY_KEY_PREFIX + key.getValue();
    }
}