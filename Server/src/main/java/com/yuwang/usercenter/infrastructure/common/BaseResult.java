package com.yuwang.usercenter.infrastructure.common;

import lombok.Data;

/**
 * 统一返回结果对象
 * @param <T> 返回数据类型
 */
@Data
public class BaseResult<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public static <T> BaseResult<T> success() {
        BaseResult<T> result = new BaseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(null);
        return result;
    }

    /**
     * 成功返回结果
     * @param data 返回数据
     * @param <T> 数据类型
     * @return BaseResult对象
     */
    public static <T> BaseResult<T> success(T data) {
        BaseResult<T> result = new BaseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回结果
     * @param message 返回信息
     * @param data 返回数据
     * @param <T> 数据类型
     * @return BaseResult对象
     */
    public static <T> BaseResult<T> success(String message, T data) {
        BaseResult<T> result = new BaseResult<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回结果
     * @param code 错误码
     * @param message 错误信息
     * @return BaseResult对象
     */
    public static <T> BaseResult<T> error(Integer code, String message) {
        BaseResult<T> result = new BaseResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}