package com.leyue.usercenter.log.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 操作日志配置类
 */
@Configuration
@EnableScheduling
public class OperationLogConfig {

    /**
     * 创建ObjectMapper对象，用于JSON序列化和反序列化
     * 如果应用中已有ObjectMapper的Bean，可以删除此方法
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
