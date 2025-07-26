package com.leyue.usercenter.domain.token;

/**
 * JTI（JWT ID）生成器接口
 * 负责生成JWT的唯一标识符
 *
 * @author Trae
 * @since 2024-01-20
 */
public interface JtiGenerator {
    
    /**
     * 生成唯一的JTI（JWT ID）
     *
     * @return 生成的JTI字符串
     */
    String generateJti();
}