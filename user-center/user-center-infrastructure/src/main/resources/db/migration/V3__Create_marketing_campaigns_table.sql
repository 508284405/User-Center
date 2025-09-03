-- 营销活动表
CREATE TABLE marketing_campaigns (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    type VARCHAR(50) NOT NULL COMMENT '活动类型：DISCOUNT, POINTS_MULTIPLY, COUPON_RAIN, LUCKY_DRAW, LIMITED_TIME_OFFER',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '活动状态：DRAFT, ACTIVE, PAUSED, COMPLETED, CANCELLED',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    target_audience TEXT COMMENT '目标用户群体配置(JSON)',
    rules TEXT COMMENT '活动规则配置(JSON)',
    budget INT COMMENT '预算(分)',
    used_budget INT NOT NULL DEFAULT 0 COMMENT '已使用预算(分)',
    participant_limit INT COMMENT '参与人数限制',
    current_participants INT NOT NULL DEFAULT 0 COMMENT '当前参与人数',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新者',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_type (type),
    KEY idx_status (status),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动表';

-- 营销活动参与记录表
CREATE TABLE marketing_campaign_participants (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    campaign_id BIGINT NOT NULL COMMENT '活动ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    participated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
    reward_type VARCHAR(50) COMMENT '奖励类型',
    reward_value VARCHAR(200) COMMENT '奖励值(JSON)',
    cost_budget INT COMMENT '消耗预算(分)',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '参与状态：SUCCESS, FAILED',
    PRIMARY KEY (id),
    UNIQUE KEY uk_campaign_user (campaign_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_participated_at (participated_at),
    FOREIGN KEY (campaign_id) REFERENCES marketing_campaigns(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动参与记录表';

-- 促销规则表
CREATE TABLE promotion_rules (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    description VARCHAR(500) COMMENT '规则描述',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型：DISCOUNT, BUY_X_GET_Y, FULL_REDUCTION, TIERED_DISCOUNT',
    conditions TEXT NOT NULL COMMENT '触发条件(JSON)',
    actions TEXT NOT NULL COMMENT '执行动作(JSON)',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级',
    start_time DATETIME NOT NULL COMMENT '生效开始时间',
    end_time DATETIME NOT NULL COMMENT '生效结束时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
    usage_limit INT COMMENT '使用次数限制',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用次数',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新者',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_rule_type (rule_type),
    KEY idx_priority (priority),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销规则表';

-- 促销规则使用记录表
CREATE TABLE promotion_rule_usage (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    order_id VARCHAR(100) COMMENT '订单ID',
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
    discount_amount INT NOT NULL COMMENT '折扣金额(分)',
    original_amount INT NOT NULL COMMENT '原始金额(分)',
    final_amount INT NOT NULL COMMENT '最终金额(分)',
    PRIMARY KEY (id),
    KEY idx_rule_id (rule_id),
    KEY idx_user_id (user_id),
    KEY idx_order_id (order_id),
    KEY idx_used_at (used_at),
    FOREIGN KEY (rule_id) REFERENCES promotion_rules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销规则使用记录表';

-- 插入示例促销规则数据
INSERT INTO promotion_rules (name, description, rule_type, conditions, actions, priority, start_time, end_time, created_by) VALUES
('新用户首单8折', '新注册用户首次下单享受8折优惠', 'DISCOUNT', '{"userType":"NEW","orderCount":0}', '{"discountType":"PERCENTAGE","discountValue":20}', 10, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'system'),
('满100减20', '订单满100元减20元', 'FULL_REDUCTION', '{"minAmount":10000}', '{"discountType":"AMOUNT","discountValue":2000}', 5, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'system'),
('买二送一', '购买2件相同商品送1件', 'BUY_X_GET_Y', '{"buyQuantity":2,"productSame":true}', '{"freeQuantity":1}', 8, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'system');

-- 插入示例营销活动数据
INSERT INTO marketing_campaigns (name, description, type, status, start_time, end_time, target_audience, rules, budget, participant_limit, created_by) VALUES
('春节积分翻倍活动', '春节期间所有订单积分翻倍奖励', 'POINTS_MULTIPLY', 'DRAFT', '2024-02-08 00:00:00', '2024-02-18 23:59:59', '{"userLevel":["GOLD","PLATINUM"]}', '{"multiplier":2}', 1000000, 10000, 'system'),
('新用户优惠券雨', '新注册用户7天内随机获得优惠券', 'COUPON_RAIN', 'DRAFT', '2024-01-01 00:00:00', '2024-03-31 23:59:59', '{"userType":"NEW","registrationDays":7}', '{"coupons":[{"type":"DISCOUNT","value":10},{"type":"FULL_REDUCTION","value":20}]}', 500000, 5000, 'system'),
('周年庆抽奖活动', '平台周年庆典限时抽奖活动', 'LUCKY_DRAW', 'DRAFT', '2024-06-01 00:00:00', '2024-06-07 23:59:59', '{"allUsers":true}', '{"prizes":[{"name":"一等奖","probability":0.01},{"name":"二等奖","probability":0.05}]}', 2000000, 50000, 'system');