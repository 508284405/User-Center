package com.leyue.usercenter.infrastructure.config.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 撤销当前Token注解
 * 在方法上添加此注解，会自动将当前请求中的Token加入黑名单
 *
 * @author Admin
 * @since 2025-03-15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RevokeCurrentToken {
    /**
     * 是否仅在方法成功执行后才撤销Token
     */
    boolean onlyOnSuccess() default true;
}
