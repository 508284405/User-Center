package com.yuwang.usercenter.infrastructure.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     * @param message 错误信息
     */
    public BusinessException(String message) {
        this(500, message);
    }
}