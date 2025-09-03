package com.leyue.usercenter.domain.common.gateway;

import com.leyue.usercenter.domain.common.IdempotencyKey;

/**
 * 幂等性网关接口
 * 
 * @author Claude Code
 */
public interface IdempotencyGateway {
    
    /**
     * 检查并设置幂等键，如果键已存在则返回false
     * 
     * @param key 幂等键
     * @param ttlSeconds TTL秒数
     * @return true表示设置成功，false表示键已存在
     */
    boolean checkAndSet(IdempotencyKey key, long ttlSeconds);
    
    /**
     * 检查幂等键是否存在
     * 
     * @param key 幂等键
     * @return true表示存在，false表示不存在
     */
    boolean exists(IdempotencyKey key);
    
    /**
     * 移除幂等键
     * 
     * @param key 幂等键
     */
    void remove(IdempotencyKey key);
}