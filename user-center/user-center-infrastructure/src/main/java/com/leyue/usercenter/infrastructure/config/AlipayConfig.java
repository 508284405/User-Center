package com.leyue.usercenter.infrastructure.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 * 用于初始化支付宝SDK客户端
 *
 * @author Cascade
 * @since 2025-05-06
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {
    /**
     * 应用ID，您的APPID
     */
    private String appId;

    /**
     * 应用私钥，您的PKCS8格式RSA2私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 支付宝网关，默认为正式环境
     */
    private String serverUrl = "https://openapi.alipay.com/gateway.do";

    /**
     * 字符集，默认为UTF-8
     */
    private String charset = "UTF-8";

    /**
     * 数据格式，默认为json
     */
    private String format = "json";

    /**
     * 签名类型，默认为RSA2
     */
    private String signType = "RSA2";

    /**
     * 授权回调地址
     */
    private String redirectUri;

    /**
     * 创建支付宝客户端
     * 注意：这个方法会在项目实际运行时由Spring加载支付宝SDK后生效
     */
    @Bean
    public AlipayClient alipayClient() {
        // 在实际运行时，这将返回DefaultAlipayClient实例
        // 由于编译期可能找不到支付宝SDK类，此处返回Object类型
        return new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset, alipayPublicKey, signType);
    }
}
