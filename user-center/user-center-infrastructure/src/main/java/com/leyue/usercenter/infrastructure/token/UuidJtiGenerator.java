package com.leyue.usercenter.infrastructure.token;

import com.leyue.usercenter.domain.token.JtiGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 基于UUID的JTI生成器实现
 *
 * @author Trae
 * @since 2024-01-20
 */
@Component
public class UuidJtiGenerator implements JtiGenerator {
    
    @Override
    public String generateJti() {
        return UUID.randomUUID().toString();
    }
}