package com.leyue.usercenter.api.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operationType() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 操作内容表达式，支持SpEL表达式
     * 例如：#user.username + '修改了个人信息'
     */
    String content() default "";

    /**
     * 操作目标ID的SpEL表达式
     * 例如：#user.id
     */
    String targetId() default "";

    /**
     * 操作级别：普通、重要、关键
     */
    String severityLevel() default "普通";

    /**
     * 是否为敏感操作
     */
    boolean isSensitive() default false;

    /**
     * 是否保存请求参数
     */
    boolean saveParams() default true;

    /**
     * 是否保存返回结果
     */
    boolean saveResult() default false;

    /**
     * 是否需要审批
     */
    boolean needApproval() default false;
}
