-- 秒杀活动表
CREATE TABLE seckill_activities (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '活动状态：NOT_STARTED, ONGOING, PAUSED, ENDED, CANCELLED',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价（分）',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价（分）',
    total_stock INT NOT NULL COMMENT '总库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    sold_count INT NOT NULL DEFAULT 0 COMMENT '已售数量',
    limit_per_user INT NOT NULL COMMENT '单用户限购数量',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新者',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_product_id (product_id),
    KEY idx_status (status),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 秒杀订单表
CREATE TABLE seckill_orders (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    activity_name VARCHAR(100) NOT NULL COMMENT '活动名称',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    order_id VARCHAR(100) NOT NULL COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价（分）',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额（分）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态：PENDING_PAYMENT, PAID, CANCELLED, EXPIRED',
    expire_time DATETIME NOT NULL COMMENT '支付过期时间',
    cancel_reason VARCHAR(200) COMMENT '取消原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(50) NOT NULL COMMENT '创建者',
    updated_by VARCHAR(50) COMMENT '更新者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_expire_time (expire_time),
    KEY idx_created_at (created_at),
    KEY idx_user_activity (user_id, activity_id),
    FOREIGN KEY (activity_id) REFERENCES seckill_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单表';

-- 秒杀活动商品快照表（用于记录活动时的商品信息）
CREATE TABLE seckill_product_snapshots (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '快照ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(500) COMMENT '商品图片',
    product_description TEXT COMMENT '商品描述',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价（分）',
    category_id BIGINT COMMENT '商品分类ID',
    category_name VARCHAR(100) COMMENT '商品分类名称',
    brand VARCHAR(100) COMMENT '品牌',
    specifications JSON COMMENT '商品规格（JSON）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_product (activity_id, product_id),
    KEY idx_product_id (product_id),
    FOREIGN KEY (activity_id) REFERENCES seckill_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动商品快照表';

-- 秒杀统计表（用于记录各种统计数据）
CREATE TABLE seckill_statistics (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_views INT NOT NULL DEFAULT 0 COMMENT '总浏览量',
    unique_views INT NOT NULL DEFAULT 0 COMMENT '独立访客数',
    total_participants INT NOT NULL DEFAULT 0 COMMENT '总参与人数',
    successful_orders INT NOT NULL DEFAULT 0 COMMENT '成功订单数',
    paid_orders INT NOT NULL DEFAULT 0 COMMENT '已支付订单数',
    cancelled_orders INT NOT NULL DEFAULT 0 COMMENT '已取消订单数',
    expired_orders INT NOT NULL DEFAULT 0 COMMENT '过期订单数',
    total_sales_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总销售金额（分）',
    conversion_rate DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '转化率',
    payment_rate DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '支付率',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_date (activity_id, stat_date),
    KEY idx_stat_date (stat_date),
    FOREIGN KEY (activity_id) REFERENCES seckill_activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀统计表';

-- 插入示例秒杀活动数据
INSERT INTO seckill_activities (name, description, start_time, end_time, status, product_id, product_name, original_price, seckill_price, total_stock, available_stock, limit_per_user, created_by) VALUES
('iPhone 15 Pro 新年秒杀', '新年限时秒杀，iPhone 15 Pro 256GB 直降1000元', '2024-02-08 10:00:00', '2024-02-08 12:00:00', 'NOT_STARTED', 1001, 'iPhone 15 Pro 256GB 深空黑色', 899900, 799900, 100, 100, 1, 'admin'),
('小米14 Pro 春节特惠', '春节期间小米14 Pro限量秒杀，机会难得', '2024-02-10 14:00:00', '2024-02-10 16:00:00', 'NOT_STARTED', 1002, '小米14 Pro 12GB+256GB 白色', 459900, 399900, 200, 200, 2, 'admin'),
('华为Mate60 Pro 元宵节抢购', '元宵节特别活动，华为Mate60 Pro限时秒杀', '2024-02-15 20:00:00', '2024-02-15 22:00:00', 'NOT_STARTED', 1003, '华为Mate60 Pro 12GB+512GB 雅川青', 689900, 619900, 50, 50, 1, 'admin');

-- 创建库存扣减的存储过程（原子性操作）
DELIMITER //
CREATE PROCEDURE DecreaseStock(
    IN p_activity_id BIGINT,
    IN p_quantity INT,
    OUT p_success INT,
    OUT p_available_stock INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        SET p_success = 0;
        SET p_available_stock = -1;
    END;
    
    START TRANSACTION;
    
    -- 获取当前库存（加锁）
    SELECT available_stock INTO p_available_stock 
    FROM seckill_activities 
    WHERE id = p_activity_id FOR UPDATE;
    
    -- 检查库存是否充足
    IF p_available_stock >= p_quantity THEN
        -- 扣减库存
        UPDATE seckill_activities 
        SET available_stock = available_stock - p_quantity,
            sold_count = sold_count + p_quantity,
            updated_at = NOW()
        WHERE id = p_activity_id;
        
        SET p_success = 1;
        SET p_available_stock = p_available_stock - p_quantity;
    ELSE
        SET p_success = 0;
    END IF;
    
    COMMIT;
END //
DELIMITER ;

-- 创建恢复库存的存储过程（用于订单取消）
DELIMITER //
CREATE PROCEDURE IncreaseStock(
    IN p_activity_id BIGINT,
    IN p_quantity INT
)
BEGIN
    UPDATE seckill_activities 
    SET available_stock = available_stock + p_quantity,
        sold_count = sold_count - p_quantity,
        updated_at = NOW()
    WHERE id = p_activity_id;
END //
DELIMITER ;