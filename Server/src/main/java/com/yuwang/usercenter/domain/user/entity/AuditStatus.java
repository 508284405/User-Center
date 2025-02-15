package com.yuwang.usercenter.domain.user.entity;

/**
 * 审核状态枚举
 */
public enum AuditStatus {
    /**
     * 待审核
     */
    PENDING,
    /**
     * 已通过
     */
    APPROVED,
    /**
     * 已拒绝
     */
    REJECTED
}