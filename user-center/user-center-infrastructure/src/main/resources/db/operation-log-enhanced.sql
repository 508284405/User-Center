-- 增强的操作日志表
CREATE TABLE user_center.operation_logs
(
    id                   BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    user_id              BIGINT                             NULL,
    username             VARCHAR(50)                        NULL,
    operation_type       VARCHAR(20)                        NULL,
    module               VARCHAR(50)                        NULL,
    target_id            BIGINT                             NULL COMMENT '操作目标ID',
    description          VARCHAR(255)                       NULL COMMENT '操作描述',
    operation_content    JSON                               NULL COMMENT '操作详细内容，包括修改前后的值',
    operation_result     VARCHAR(20)                        NULL COMMENT '操作结果：成功、失败、部分成功等',
    related_operation_id BIGINT                             NULL COMMENT '关联操作ID',
    severity_level       VARCHAR(10)                        NULL COMMENT '操作级别：普通、重要、关键',
    is_sensitive         TINYINT   DEFAULT 0                NULL COMMENT '是否敏感操作：0-否，1-是',
    approval_info        JSON                               NULL COMMENT '审批信息',
    ip_address           VARCHAR(50)                        NULL COMMENT '操作者IP地址',
    created_by           VARCHAR(50)                        NULL COMMENT '创建人',
    updated_by           VARCHAR(50)                        NULL COMMENT '更新人',
    created_at           DATETIME  DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at           DATETIME  DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '操作日志表';

-- 原有索引
CREATE INDEX idx_created_at ON user_center.operation_logs (created_at);
CREATE INDEX idx_module ON user_center.operation_logs (module);
CREATE INDEX idx_operation_type ON user_center.operation_logs (operation_type);
CREATE INDEX idx_user_id ON user_center.operation_logs (user_id);

-- 新增索引
CREATE INDEX idx_operation_result ON user_center.operation_logs (operation_result);
CREATE INDEX idx_severity_level ON user_center.operation_logs (severity_level);
CREATE INDEX idx_is_sensitive ON user_center.operation_logs (is_sensitive);
CREATE INDEX idx_related_operation ON user_center.operation_logs (related_operation_id);

-- 操作日志归档表
CREATE TABLE user_center.operation_logs_archive
(
    id                   BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    user_id              BIGINT                             NULL,
    username             VARCHAR(50)                        NULL,
    operation_type       VARCHAR(20)                        NULL,
    module               VARCHAR(50)                        NULL,
    target_id            BIGINT                             NULL COMMENT '操作目标ID',
    description          VARCHAR(255)                       NULL COMMENT '操作描述',
    operation_content    JSON                               NULL COMMENT '操作详细内容，包括修改前后的值',
    operation_result     VARCHAR(20)                        NULL COMMENT '操作结果：成功、失败、部分成功等',
    related_operation_id BIGINT                             NULL COMMENT '关联操作ID',
    severity_level       VARCHAR(10)                        NULL COMMENT '操作级别：普通、重要、关键',
    is_sensitive         TINYINT   DEFAULT 0                NULL COMMENT '是否敏感操作：0-否，1-是',
    approval_info        JSON                               NULL COMMENT '审批信息',
    ip_address           VARCHAR(50)                        NULL COMMENT '操作者IP地址',
    created_by           VARCHAR(50)                        NULL COMMENT '创建人',
    updated_by           VARCHAR(50)                        NULL COMMENT '更新人',
    created_at           DATETIME                           NOT NULL COMMENT '创建时间',
    updated_at           DATETIME                           NOT NULL COMMENT '更新时间',
    archived_at          DATETIME  DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '归档时间'
) COMMENT '操作日志归档表';

-- 归档表索引
CREATE INDEX idx_archived_at ON user_center.operation_logs_archive (archived_at);
CREATE INDEX idx_created_at_archive ON user_center.operation_logs_archive (created_at);
CREATE INDEX idx_module_archive ON user_center.operation_logs_archive (module);
CREATE INDEX idx_operation_type_archive ON user_center.operation_logs_archive (operation_type);
CREATE INDEX idx_user_id_archive ON user_center.operation_logs_archive (user_id);

-- 操作日志统计表
CREATE TABLE user_center.operation_logs_statistics
(
    id            BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    statistic_date DATE                              NOT NULL COMMENT '统计日期',
    module        VARCHAR(50)                        NULL COMMENT '模块',
    operation_type VARCHAR(20)                        NULL COMMENT '操作类型',
    user_count    INT                                NULL COMMENT '操作用户数',
    operation_count INT                                NULL COMMENT '操作次数',
    success_count INT                                NULL COMMENT '成功操作数',
    fail_count    INT                                NULL COMMENT '失败操作数',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '操作日志统计表';

-- 统计表索引
CREATE INDEX idx_statistic_date ON user_center.operation_logs_statistics (statistic_date);
CREATE INDEX idx_module_stat ON user_center.operation_logs_statistics (module);
CREATE INDEX idx_operation_type_stat ON user_center.operation_logs_statistics (operation_type);
