-- ============================================
-- 用户中心忠诚度系统数据库表创建脚本
-- Version: V2 
-- Description: Create loyalty system tables including points, levels, coupons, and benefits
-- ============================================

-- ========================
-- 积分相关表
-- ========================

-- 积分账户表
CREATE TABLE `uc_point_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `available` DECIMAL(20, 2) NOT NULL DEFAULT 0.00 COMMENT '可用积分',
    `frozen` DECIMAL(20, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结积分',
    `will_expire` DECIMAL(20, 2) NOT NULL DEFAULT 0.00 COMMENT '将过期积分(30天内)',
    `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';

-- 积分批次表（用于FIFO过期策略）
CREATE TABLE `uc_point_lot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `lot_id` VARCHAR(64) NOT NULL COMMENT '批次ID',
    `amount` DECIMAL(20, 2) NOT NULL COMMENT '批次总积分',
    `remain` DECIMAL(20, 2) NOT NULL COMMENT '剩余积分',
    `expire_at` DATETIME NOT NULL COMMENT '过期时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lot_id` (`lot_id`),
    KEY `idx_user_id_expire_at` (`user_id`, `expire_at`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分批次表';

-- 积分流水表
CREATE TABLE `uc_point_txn` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `lot_id` VARCHAR(64) DEFAULT NULL COMMENT '批次ID',
    `delta` DECIMAL(20, 2) NOT NULL COMMENT '积分变化量（正数为增加，负数为扣减）',
    `type` VARCHAR(32) NOT NULL COMMENT '交易类型：EARN, DEDUCT, FREEZE, UNFREEZE, EXPIRE, REVERSAL',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `biz_id` VARCHAR(128) NOT NULL COMMENT '业务ID',
    `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    KEY `idx_user_id_created_at` (`user_id`, `created_at`),
    KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- ========================
-- 等级相关表
-- ========================

-- 等级定义表
CREATE TABLE `uc_level_def` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_code` VARCHAR(64) NOT NULL COMMENT '等级编码',
    `name` VARCHAR(128) NOT NULL COMMENT '等级名称',
    `threshold` DECIMAL(20, 2) NOT NULL COMMENT '等级门槛（成长值/积分）',
    `pkg_id` BIGINT DEFAULT NULL COMMENT '关联权益包ID',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（数值越小优先级越高）',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_code` (`level_code`),
    KEY `idx_threshold` (`threshold`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级定义表';

-- 用户等级表
CREATE TABLE `uc_user_level` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `level_code` VARCHAR(64) NOT NULL COMMENT '当前等级编码',
    `growth_value` DECIMAL(20, 2) NOT NULL DEFAULT 0.00 COMMENT '成长值',
    `valid_from` DATETIME NOT NULL COMMENT '等级生效时间',
    `valid_to` DATETIME DEFAULT NULL COMMENT '等级失效时间（NULL表示永久有效）',
    `snapshot_json` JSON DEFAULT NULL COMMENT '等级快照（用于幂等计算）',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_level_code` (`level_code`),
    KEY `idx_valid_from` (`valid_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级表';

-- ========================
-- 优惠券相关表
-- ========================

-- 优惠券模板表
CREATE TABLE `uc_coupon_tpl` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tpl_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
    `name` VARCHAR(128) NOT NULL COMMENT '券名称',
    `type` VARCHAR(32) NOT NULL COMMENT '券类型：DISCOUNT, CASH, FREEBIE等',
    `face_value` DECIMAL(20, 2) DEFAULT NULL COMMENT '面额（现金券）',
    `discount_rate` DECIMAL(5, 4) DEFAULT NULL COMMENT '折扣率（折扣券，如0.8表示8折）',
    `threshold_amount` DECIMAL(20, 2) DEFAULT NULL COMMENT '使用门槛金额',
    `valid_from` DATETIME NOT NULL COMMENT '券有效期开始时间',
    `valid_to` DATETIME NOT NULL COMMENT '券有效期结束时间',
    `issue_start` DATETIME NOT NULL COMMENT '发放开始时间',
    `issue_end` DATETIME NOT NULL COMMENT '发放结束时间',
    `total` INT NOT NULL COMMENT '总发放量',
    `issued` INT NOT NULL DEFAULT 0 COMMENT '已发放量',
    `constraints_json` JSON DEFAULT NULL COMMENT '使用约束（品类、SKU、渠道等）',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, PAUSED, FINISHED',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tpl_code` (`tpl_code`),
    KEY `idx_issue_time` (`issue_start`, `issue_end`),
    KEY `idx_valid_time` (`valid_from`, `valid_to`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 优惠券实例表
CREATE TABLE `uc_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tpl_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
    `coupon_code` VARCHAR(128) NOT NULL COMMENT '券码（全局唯一）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `state` VARCHAR(32) NOT NULL COMMENT '状态：CREATED, ISSUED, AVAILABLE, RESERVED, REDEEMED, EXPIRED, REFUNDED',
    `locked_until` DATETIME DEFAULT NULL COMMENT '锁定截止时间（预留状态）',
    `assigned_at` DATETIME NOT NULL COMMENT '分配时间',
    `valid_from` DATETIME NOT NULL COMMENT '有效期开始',
    `valid_to` DATETIME NOT NULL COMMENT '有效期结束',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupon_code` (`coupon_code`),
    KEY `idx_user_id_state` (`user_id`, `state`),
    KEY `idx_valid_time` (`valid_from`, `valid_to`),
    KEY `idx_locked_until` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券实例表';

-- 优惠券操作流水表
CREATE TABLE `uc_coupon_txn` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `coupon_code` VARCHAR(128) NOT NULL COMMENT '券码',
    `action` VARCHAR(32) NOT NULL COMMENT '操作类型：ISSUE, RESERVE, REDEEM, RELEASE, EXPIRE, REFUND',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `biz_id` VARCHAR(128) NOT NULL COMMENT '业务ID',
    `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    KEY `idx_coupon_code_created_at` (`coupon_code`, `created_at`),
    KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券操作流水表';

-- ========================
-- 权益相关表
-- ========================

-- 权益定义表
CREATE TABLE `uc_benefit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `name` VARCHAR(128) NOT NULL COMMENT '权益名称',
    `type` VARCHAR(32) NOT NULL COMMENT '权益类型：SHIPPING, DISCOUNT, SERVICE, ACCELERATION等',
    `rule_json` JSON DEFAULT NULL COMMENT '权益规则（JSON格式）',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_benefit_code` (`benefit_code`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益定义表';

-- 权益包表
CREATE TABLE `uc_benefit_pkg` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pkg_id` BIGINT NOT NULL COMMENT '权益包ID（业务主键）',
    `name` VARCHAR(128) NOT NULL COMMENT '权益包名称',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pkg_id` (`pkg_id`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益包表';

-- 权益包明细表
CREATE TABLE `uc_benefit_pkg_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pkg_id` BIGINT NOT NULL COMMENT '权益包ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `quota` INT NOT NULL DEFAULT 1 COMMENT '配额（次数/数量）',
    `period` VARCHAR(32) NOT NULL DEFAULT 'INFINITE' COMMENT '周期：INFINITE, DAILY, WEEKLY, MONTHLY, YEARLY',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pkg_benefit` (`pkg_id`, `benefit_code`),
    KEY `idx_benefit_code` (`benefit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益包明细表';

-- 用户权益发放表
CREATE TABLE `uc_entitlement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `pkg_id` BIGINT DEFAULT NULL COMMENT '权益包ID',
    `quota_total` INT NOT NULL COMMENT '总配额',
    `quota_used` INT NOT NULL DEFAULT 0 COMMENT '已使用配额',
    `state` VARCHAR(32) NOT NULL COMMENT '状态：ACTIVE, USED, EXPIRED, REVOKED',
    `valid_from` DATETIME NOT NULL COMMENT '生效时间',
    `valid_to` DATETIME DEFAULT NULL COMMENT '失效时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_benefit_state` (`user_id`, `benefit_code`, `state`),
    KEY `idx_valid_time` (`valid_from`, `valid_to`),
    KEY `idx_state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权益发放表';

-- 权益使用流水表
CREATE TABLE `uc_entitlement_txn` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entitlement_id` BIGINT NOT NULL COMMENT '权益发放ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `delta` INT NOT NULL COMMENT '使用数量变化（正数为使用，负数为撤销）',
    `action` VARCHAR(32) NOT NULL COMMENT '操作类型：GRANT, USE, REVOKE, EXPIRE',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `biz_id` VARCHAR(128) NOT NULL COMMENT '业务ID',
    `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    KEY `idx_entitlement_id` (`entitlement_id`),
    KEY `idx_user_id_created_at` (`user_id`, `created_at`),
    KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益使用流水表';

-- ========================
-- 事务消息表（Outbox模式）
-- ========================

-- 事务消息表
CREATE TABLE `uc_outbox_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id` VARCHAR(128) NOT NULL COMMENT '事件ID（UUID）',
    `topic` VARCHAR(128) NOT NULL COMMENT '消息主题',
    `payload_json` JSON NOT NULL COMMENT '事件载荷（JSON格式）',
    `status` VARCHAR(32) NOT NULL DEFAULT 'NEW' COMMENT '状态：NEW, SENT, FAILED',
    `retry_cnt` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_at` DATETIME DEFAULT NULL COMMENT '下次重试时间',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_at`),
    KEY `idx_topic_created_at` (`topic`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事务消息表（Outbox模式）';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化等级定义数据
INSERT INTO `uc_level_def` (`level_code`, `name`, `threshold`, `priority`, `created_at`) VALUES
('LV0', '普通会员', 0.00, 0, NOW()),
('LV1', '铜牌会员', 1000.00, 1, NOW()),
('LV2', '银牌会员', 5000.00, 2, NOW()),
('LV3', '金牌会员', 20000.00, 3, NOW()),
('LV4', '钻石会员', 100000.00, 4, NOW());

-- 初始化权益定义数据
INSERT INTO `uc_benefit` (`benefit_code`, `name`, `type`, `rule_json`, `created_at`) VALUES
('FREE_SHIPPING', '包邮特权', 'SHIPPING', '{"minAmount": 0, "description": "全场包邮"}', NOW()),
('BIRTHDAY_GIFT', '生日礼品', 'GIFT', '{"giftType": "COUPON", "value": 50}', NOW()),
('GROWTH_BOOST', '成长加速', 'ACCELERATION', '{"multiplier": 1.5}', NOW()),
('VIP_SERVICE', '专属客服', 'SERVICE', '{"priority": "HIGH"}', NOW());

-- 初始化权益包数据
INSERT INTO `uc_benefit_pkg` (`pkg_id`, `name`, `created_at`) VALUES
(1, '普通会员权益包', NOW()),
(2, '铜牌会员权益包', NOW()),
(3, '银牌会员权益包', NOW()),
(4, '金牌会员权益包', NOW()),
(5, '钻石会员权益包', NOW());

-- 初始化权益包明细数据
INSERT INTO `uc_benefit_pkg_item` (`pkg_id`, `benefit_code`, `quota`, `period`, `created_at`) VALUES
-- 普通会员
(1, 'BIRTHDAY_GIFT', 1, 'YEARLY', NOW()),
-- 铜牌会员
(2, 'BIRTHDAY_GIFT', 1, 'YEARLY', NOW()),
(2, 'GROWTH_BOOST', 1, 'INFINITE', NOW()),
-- 银牌会员  
(3, 'FREE_SHIPPING', 1, 'INFINITE', NOW()),
(3, 'BIRTHDAY_GIFT', 1, 'YEARLY', NOW()),
(3, 'GROWTH_BOOST', 1, 'INFINITE', NOW()),
-- 金牌会员
(4, 'FREE_SHIPPING', 1, 'INFINITE', NOW()),
(4, 'BIRTHDAY_GIFT', 1, 'YEARLY', NOW()),
(4, 'GROWTH_BOOST', 1, 'INFINITE', NOW()),
(4, 'VIP_SERVICE', 1, 'INFINITE', NOW()),
-- 钻石会员
(5, 'FREE_SHIPPING', 1, 'INFINITE', NOW()),
(5, 'BIRTHDAY_GIFT', 2, 'YEARLY', NOW()),
(5, 'GROWTH_BOOST', 1, 'INFINITE', NOW()),
(5, 'VIP_SERVICE', 1, 'INFINITE', NOW());

-- 更新等级定义，关联权益包
UPDATE `uc_level_def` SET `pkg_id` = 1 WHERE `level_code` = 'LV0';
UPDATE `uc_level_def` SET `pkg_id` = 2 WHERE `level_code` = 'LV1';
UPDATE `uc_level_def` SET `pkg_id` = 3 WHERE `level_code` = 'LV2';
UPDATE `uc_level_def` SET `pkg_id` = 4 WHERE `level_code` = 'LV3';
UPDATE `uc_level_def` SET `pkg_id` = 5 WHERE `level_code` = 'LV4';