-- 团购活动表
CREATE TABLE groupbuy_activities (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '活动状态：NOT_STARTED, ONGOING, PAUSED, ENDED, CANCELLED',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价(分)',
    group_price DECIMAL(10,2) NOT NULL COMMENT '团购价(分)',
    required_participants INT NOT NULL COMMENT '成团人数',
    max_participants INT NOT NULL COMMENT '最大参团人数',
    limit_per_user INT NOT NULL DEFAULT 1 COMMENT '单用户限购数量',
    group_timeout_hours INT NOT NULL DEFAULT 24 COMMENT '成团超时时间(小时)',
    total_stock INT NOT NULL COMMENT '总库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    sold_count INT NOT NULL DEFAULT 0 COMMENT '已售数量',
    total_groups INT NOT NULL DEFAULT 0 COMMENT '总开团数',
    successful_groups INT NOT NULL DEFAULT 0 COMMENT '成功成团数',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新者',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_status (status),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_product_id (product_id),
    KEY idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购活动表';

-- 团购团组表
CREATE TABLE groupbuy_groups (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '团组ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    group_number VARCHAR(50) NOT NULL COMMENT '团号',
    leader_user_id VARCHAR(50) NOT NULL COMMENT '团长用户ID',
    status VARCHAR(20) NOT NULL DEFAULT 'FORMING' COMMENT '团状态：FORMING, SUCCESS, FAILED, DISSOLVED',
    required_participants INT NOT NULL COMMENT '需要人数',
    current_participants INT NOT NULL DEFAULT 0 COMMENT '当前人数',
    group_price DECIMAL(10,2) NOT NULL COMMENT '团购价(分)',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额(分)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    completed_at DATETIME COMMENT '成团时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_number (group_number),
    KEY idx_activity_id (activity_id),
    KEY idx_leader_user_id (leader_user_id),
    KEY idx_status (status),
    KEY idx_expire_time (expire_time),
    KEY idx_created_at (created_at),
    FOREIGN KEY (activity_id) REFERENCES groupbuy_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购团组表';

-- 团购参团记录表
CREATE TABLE groupbuy_participants (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参团ID',
    group_id BIGINT NOT NULL COMMENT '团组ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价(分)',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总价(分)',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_group_id (group_id),
    KEY idx_user_id (user_id),
    KEY idx_join_time (join_time),
    FOREIGN KEY (group_id) REFERENCES groupbuy_groups(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购参团记录表';

-- 团购订单表
CREATE TABLE groupbuy_orders (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_number VARCHAR(100) NOT NULL COMMENT '订单号',
    group_id BIGINT NOT NULL COMMENT '团组ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价(分)',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额(分)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING, PAID, CANCELLED, REFUNDED',
    payment_time DATETIME COMMENT '支付时间',
    refund_time DATETIME COMMENT '退款时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_number (order_number),
    KEY idx_group_id (group_id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_created_at (created_at),
    FOREIGN KEY (group_id) REFERENCES groupbuy_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES groupbuy_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购订单表';

-- 插入示例团购活动数据
INSERT INTO groupbuy_activities (
    name, description, status, start_time, end_time, 
    product_id, product_name, original_price, group_price,
    required_participants, max_participants, limit_per_user, group_timeout_hours,
    total_stock, available_stock, created_by
) VALUES
('春季家电团购', '春季家电品类限时团购，3人成团享超低价', 'NOT_STARTED', 
 '2024-04-01 00:00:00', '2024-04-30 23:59:59',
 2001, '智能空气净化器', 159900, 99900, 3, 3, 1, 24, 100, 100, 'admin'),

('母婴用品拼团', '优质母婴用品团购，5人成团更优惠', 'NOT_STARTED',
 '2024-05-01 00:00:00', '2024-05-31 23:59:59', 
 2002, '婴儿推车豪华版', 299900, 199900, 5, 5, 1, 48, 50, 50, 'admin'),

('数码产品团购节', '热门数码产品团购，2人即可成团', 'NOT_STARTED',
 '2024-06-01 00:00:00', '2024-06-30 23:59:59',
 2003, '无线蓝牙耳机', 79900, 59900, 2, 10, 2, 12, 200, 200, 'admin'),

('夏季服装拼团', '夏季新款服装团购，4人成团享7折', 'NOT_STARTED',
 '2024-07-01 00:00:00', '2024-07-31 23:59:59',
 2004, '时尚夏季连衣裙', 39900, 27900, 4, 8, 3, 36, 300, 300, 'admin');

-- 插入示例团购团组数据（用于测试）
INSERT INTO groupbuy_groups (
    activity_id, group_number, leader_user_id, status,
    required_participants, current_participants, group_price, total_amount,
    expire_time, completed_at
) VALUES
(1, 'GB16800000001', 'user001', 'SUCCESS', 3, 3, 99900, 299700, 
 '2024-04-02 00:00:00', '2024-04-01 15:30:00'),
(1, 'GB16800000002', 'user002', 'FORMING', 3, 2, 99900, 199800, 
 '2024-04-05 10:00:00', NULL),
(2, 'GB16800000003', 'user003', 'FAILED', 5, 3, 199900, 599700, 
 '2024-05-03 00:00:00', NULL);

-- 插入示例参团记录
INSERT INTO groupbuy_participants (group_id, user_id, quantity, unit_price, total_price, join_time) VALUES
-- 成功的团
(1, 'user001', 1, 99900, 99900, '2024-04-01 10:00:00'),
(1, 'user011', 1, 99900, 99900, '2024-04-01 12:30:00'),
(1, 'user021', 1, 99900, 99900, '2024-04-01 15:30:00'),
-- 进行中的团
(2, 'user002', 1, 99900, 99900, '2024-04-04 09:00:00'),
(2, 'user012', 1, 99900, 99900, '2024-04-04 14:20:00'),
-- 失败的团
(3, 'user003', 1, 199900, 199900, '2024-05-01 08:00:00'),
(3, 'user013', 1, 199900, 199900, '2024-05-01 16:45:00'),
(3, 'user023', 1, 199900, 199900, '2024-05-02 11:15:00');

-- 插入示例团购订单
INSERT INTO groupbuy_orders (
    order_number, group_id, activity_id, user_id, quantity, 
    unit_price, total_amount, status, payment_time
) VALUES
('GB202404010001', 1, 1, 'user001', 1, 99900, 99900, 'PAID', '2024-04-01 15:35:00'),
('GB202404010002', 1, 1, 'user011', 1, 99900, 99900, 'PAID', '2024-04-01 15:36:00'),
('GB202404010003', 1, 1, 'user021', 1, 99900, 99900, 'PAID', '2024-04-01 15:37:00'),
('GB202404040001', 2, 1, 'user002', 1, 99900, 99900, 'PENDING', NULL),
('GB202404040002', 2, 1, 'user012', 1, 99900, 99900, 'PENDING', NULL);