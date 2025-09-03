-- 满减活动表
CREATE TABLE full_reduction_activities (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '活动状态：DRAFT, ACTIVE, PAUSED, EXPIRED, CANCELLED',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    applicable_products TEXT COMMENT '适用商品配置(JSON)',
    applicable_categories TEXT COMMENT '适用分类配置(JSON)',
    exclude_products TEXT COMMENT '排除商品配置(JSON)',
    total_usage_limit INT COMMENT '总使用次数限制',
    current_usage INT NOT NULL DEFAULT 0 COMMENT '当前使用次数',
    user_usage_limit INT COMMENT '单用户使用次数限制',
    min_order_amount DECIMAL(10,2) COMMENT '最小订单金额(分)',
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级',
    can_combine_with_coupon TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可与优惠券叠加',
    can_combine_with_member_discount TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可与会员折扣叠加',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新者',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_status (status),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_priority (priority),
    KEY idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='满减活动表';

-- 满减规则表
CREATE TABLE full_reduction_rules (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    threshold_amount DECIMAL(10,2) NOT NULL COMMENT '门槛金额(分)',
    reduction_type VARCHAR(20) NOT NULL COMMENT '减免类型：FIXED_AMOUNT, PERCENTAGE, TIERED_FIXED',
    reduction_value DECIMAL(10,2) NOT NULL COMMENT '减免值',
    max_reduction DECIMAL(10,2) COMMENT '最大减免金额(分)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_threshold_amount (threshold_amount),
    FOREIGN KEY (activity_id) REFERENCES full_reduction_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='满减规则表';

-- 满减活动使用记录表
CREATE TABLE full_reduction_usage_records (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    order_id VARCHAR(100) NOT NULL COMMENT '订单ID',
    order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额(分)',
    reduction_amount DECIMAL(10,2) NOT NULL COMMENT '减免金额(分)',
    used_rule_id BIGINT COMMENT '使用的规则ID',
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id),
    KEY idx_order_id (order_id),
    KEY idx_used_at (used_at),
    KEY idx_user_activity (user_id, activity_id),
    FOREIGN KEY (activity_id) REFERENCES full_reduction_activities(id) ON DELETE CASCADE,
    FOREIGN KEY (used_rule_id) REFERENCES full_reduction_rules(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='满减活动使用记录表';

-- 会员价格配置表
CREATE TABLE member_pricing_configs (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    member_level VARCHAR(50) NOT NULL COMMENT '会员等级',
    discount_type VARCHAR(20) NOT NULL COMMENT '折扣类型：PERCENTAGE, FIXED_AMOUNT, FIXED_PRICE',
    discount_value DECIMAL(10,2) NOT NULL COMMENT '折扣值',
    min_purchase_quantity INT COMMENT '最小购买数量',
    max_purchase_quantity INT COMMENT '最大购买数量',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新者',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_level (product_id, member_level),
    KEY idx_member_level (member_level),
    KEY idx_is_active (is_active),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员价格配置表';

-- 插入示例满减活动数据
INSERT INTO full_reduction_activities (name, description, status, start_time, end_time, total_usage_limit, user_usage_limit, min_order_amount, priority, created_by) VALUES
('新年满减大促', '新年期间全场满减活动', 'DRAFT', '2024-02-08 00:00:00', '2024-02-18 23:59:59', 10000, 3, 5000, 8, 'admin'),
('春季家电满减', '春季家电品类专享满减', 'DRAFT', '2024-03-01 00:00:00', '2024-03-31 23:59:59', 5000, 2, 10000, 7, 'admin'),
('母婴用品满减', '母婴用品专场满减活动', 'DRAFT', '2024-05-01 00:00:00', '2024-05-31 23:59:59', 3000, 5, 2000, 6, 'admin');

-- 插入示例满减规则数据
INSERT INTO full_reduction_rules (activity_id, threshold_amount, reduction_type, reduction_value, max_reduction) VALUES
-- 新年满减大促规则
(1, 5000, 'FIXED_AMOUNT', 500, NULL),  -- 满50减5
(1, 10000, 'FIXED_AMOUNT', 1500, NULL), -- 满100减15
(1, 20000, 'FIXED_AMOUNT', 4000, NULL), -- 满200减40
-- 春季家电满减规则
(2, 10000, 'PERCENTAGE', 10, 5000),     -- 满100享9折，最高减50
(2, 30000, 'PERCENTAGE', 15, 15000),    -- 满300享8.5折，最高减150
-- 母婴用品满减规则
(3, 2000, 'TIERED_FIXED', 200, 2000),  -- 每满20减2，最高减20
(3, 5000, 'FIXED_AMOUNT', 800, NULL);  -- 满50减8

-- 插入示例会员价格配置
INSERT INTO member_pricing_configs (product_id, member_level, discount_type, discount_value, min_purchase_quantity, is_active, created_by) VALUES
(1001, 'SILVER', 'PERCENTAGE', 5, 1, 1, 'admin'),    -- 银卡会员95折
(1001, 'GOLD', 'PERCENTAGE', 10, 1, 1, 'admin'),     -- 金卡会员9折
(1001, 'PLATINUM', 'PERCENTAGE', 15, 1, 1, 'admin'), -- 白金会员85折
(1002, 'GOLD', 'FIXED_AMOUNT', 2000, 1, 1, 'admin'), -- 金卡会员减20元
(1002, 'PLATINUM', 'FIXED_AMOUNT', 5000, 1, 1, 'admin'), -- 白金会员减50元
(1003, 'PLATINUM', 'FIXED_PRICE', 50000, 1, 1, 'admin'); -- 白金会员特价500元