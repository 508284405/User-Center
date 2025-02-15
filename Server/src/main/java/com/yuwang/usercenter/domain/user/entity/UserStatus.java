package com.yuwang.usercenter.domain.user.entity;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    /**
     * 正常状态
     */
    ACTIVE,
    /**
     * 未激活
     */
    INACTIVE,
    /**
     * 已锁定
     */
    LOCKED
}