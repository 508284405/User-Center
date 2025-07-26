package com.leyue.usercenter.infrastructure.config.rediss;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Redisson 配置类
 *
 * @author Admin
 * @since 2025-03-16
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.timeout:3000}")
    private int timeout;

    /**
     * 配置 Redisson 客户端（单机模式）
     *
     * @return RedissonClient 实例
     */
    @Bean
    @Primary
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 单机模式配置
        String redisUrl = String.format("redis://%s:%d", host, port);
        config.useSingleServer()
                .setAddress(redisUrl)
                .setDatabase(database)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(10)
                .setConnectTimeout(timeout);
        
        // 如果有密码，设置密码
        if (password != null && !password.isEmpty()) {
            config.useSingleServer().setPassword(password);
        }
        
        return Redisson.create(config);
    }
}
