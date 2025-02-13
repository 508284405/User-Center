package com.yuwang.usercenter.infrastructure.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分页数据统一返回结果对象
 * @param <T> 分页数据类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BasePageResult<T> extends BaseResult<List<T>> {
    /**
     * 当前页码
     */
    private long current;

    /**
     * 每页数量
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 成功返回分页结果
     * @param data 分页数据
     * @param current 当前页码
     * @param size 每页数量
     * @param total 总记录数
     * @param <T> 数据类型
     * @return BasePageResult对象
     */
    public static <T> BasePageResult<T> success(List<T> data, long current, long size, long total) {
        BasePageResult<T> result = new BasePageResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setPages((total + size - 1) / size);
        return result;
    }
}