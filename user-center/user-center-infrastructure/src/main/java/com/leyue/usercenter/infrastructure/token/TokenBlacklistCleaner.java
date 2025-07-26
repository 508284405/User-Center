package com.leyue.usercenter.infrastructure.token;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单清理器
 * 定期清理Redis中过期的黑名单记录
 *
 * @author Admin
 * @since 2025-03-15
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistCleaner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistCleaner.class);
    
    /**
     * Redis 中 Token 黑名单的前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void run(String... args) {
        logger.info("应用启动，Token黑名单清理器初始化完成");
    }
    
    /**
     * 定期清理黑名单中过期的Token
     * 每天凌晨2点执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTokens() {
        logger.info("开始清理过期的Token黑名单记录");
        
        try {
            // 获取所有黑名单key
            Set<String> keys = redisTemplate.keys(TOKEN_BLACKLIST_PREFIX + "*");
            
            if (keys == null || keys.isEmpty()) {
                logger.info("没有找到需要清理的黑名单记录");
                return;
            }
            
            int count = 0;
            for (String key : keys) {
                // 获取剩余过期时间
                Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                
                // 如果TTL为负值，表示key已经过期或不存在TTL
                if (ttl != null && ttl <= 0) {
                    redisTemplate.delete(key);
                    count++;
                }
            }
            
            logger.info("成功清理 {} 条过期的Token黑名单记录", count);
        } catch (Exception e) {
            logger.error("清理过期Token黑名单记录时发生错误", e);
        }
    }
}
