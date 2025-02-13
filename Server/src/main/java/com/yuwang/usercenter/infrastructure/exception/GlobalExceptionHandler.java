package com.yuwang.usercenter.infrastructure.exception;

import com.yuwang.usercenter.infrastructure.common.BaseResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     * @param e 业务异常
     * @return 统一返回结果
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResult<Object> handleBusinessException(BusinessException e) {
        return BaseResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     * @param e 参数校验异常
     * @return 统一返回结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<Object> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField()+fieldError.getDefaultMessage();
            }
        }
        return BaseResult.error(400, message == null ? "参数校验错误" : message);
    }

    /**
     * 处理参数绑定异常
     * @param e 参数绑定异常
     * @return 统一返回结果
     */
    @ExceptionHandler(BindException.class)
    public BaseResult<Object> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField()+fieldError.getDefaultMessage();
            }
        }
        return BaseResult.error(400, message == null ? "参数绑定错误" : message);
    }

    /**
     * 处理其他未知异常
     * @param e 未知异常
     * @return 统一返回结果
     */
    @ExceptionHandler(Exception.class)
    public BaseResult<Object> handleException(Exception e) {
        return BaseResult.error(500, "系统内部错误");
    }
}