-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`       VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`       VARCHAR(128) NOT NULL COMMENT '密码',
    `google_id`      VARCHAR(64)           DEFAULT NULL COMMENT 'Google ID',
    `alipay_id`      VARCHAR(64)           DEFAULT NULL COMMENT 'alipay_id',
    `wechat_open_id` VARCHAR(64)           DEFAULT NULL COMMENT '微信OpenID',
    `email`          VARCHAR(128)          DEFAULT NULL COMMENT '邮箱',
    `avatar`         VARCHAR(255)          DEFAULT NULL COMMENT '头像URL',
    `user_type`      TINYINT      NOT NULL DEFAULT 0 COMMENT '用户类型：0-客户端用户，1-运营端用户',
    `remark`         VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`     VARCHAR(64)           DEFAULT NULL COMMENT '创建者',
    `updated_by`     VARCHAR(64)           DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name`   VARCHAR(64) NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(64) NOT NULL COMMENT '角色编码',
    `role_type`   TINYINT     NOT NULL DEFAULT 0 COMMENT '角色类型：0-客户端角色，1-运营端角色',
    `description` VARCHAR(255)         DEFAULT NULL COMMENT '角色描述',
    `remark`      VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_deleted`  TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  VARCHAR(64)          DEFAULT NULL COMMENT '创建者',
    `updated_by`  VARCHAR(64)          DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`    BIGINT   NOT NULL COMMENT '角色ID',
    `remark`     VARCHAR(255)      DEFAULT NULL COMMENT '备注',
    `is_deleted` TINYINT  NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64)       DEFAULT NULL COMMENT '创建者',
    `updated_by` VARCHAR(64)       DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色关联表';

-- 菜单表（重新定义，使其支持更精细的权限控制）
CREATE TABLE IF NOT EXISTS `menu`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `menu_id`     VARCHAR(64)  NOT NULL COMMENT '菜单ID（业务ID）',
    `menu_name`   VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    `menu_code`   VARCHAR(64)  NOT NULL COMMENT '菜单编码',
    `path`        VARCHAR(128)          DEFAULT NULL COMMENT '路由路径',
    `component`   VARCHAR(128)          DEFAULT NULL COMMENT '前端组件',
    `type`        TINYINT      NOT NULL COMMENT '类型：1-目录 2-菜单 3-按钮',
    `parent_id`   BIGINT                DEFAULT NULL COMMENT '父菜单ID',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `icon`        VARCHAR(64)           DEFAULT NULL COMMENT '图标',
    `target`      VARCHAR(20)           DEFAULT NULL COMMENT '打开方式（_self/_blank）',
    `visible`     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见：0-不可见，1-可见',
    `menu_type`   TINYINT      NOT NULL DEFAULT 0 COMMENT '菜单类型：0-客户端菜单，1-运营端菜单',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  VARCHAR(64)           DEFAULT NULL COMMENT '创建者',
    `updated_by`  VARCHAR(64)           DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_menu_code` (`menu_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='菜单表';

-- 按钮权限表（细化到按钮级别的权限控制）
CREATE TABLE IF NOT EXISTS `menu_permission`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `menu_id`     VARCHAR(64)  NOT NULL COMMENT '菜单ID',
    `permission`  VARCHAR(128) NOT NULL COMMENT '权限标识',
    `description` VARCHAR(255)          DEFAULT NULL COMMENT '权限描述',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  VARCHAR(64)           DEFAULT NULL COMMENT '创建者',
    `updated_by`  VARCHAR(64)           DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_menu_permission` (`menu_id`, `permission`),
    KEY `idx_permission` (`permission`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='菜单权限表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `role_menu`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `menu_id`     VARCHAR(64)      NOT NULL COMMENT '菜单ID',
    `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_deleted`  TINYINT  NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  VARCHAR(64)       DEFAULT NULL COMMENT '创建者',
    `updated_by`  VARCHAR(64)       DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色菜单关联表';

-- 初始化管理员用户
INSERT INTO `user` (`id`, `username`, `password`, `status`, `create_at`, `update_at`)
VALUES ('1', 'admin', '$2a$10$vDm3tUH8M12VIGlFVXwqxOYsPKiYh4c8.E8HqkZHNbvPIY5wEVK8e', 1, NOW(), NOW());

-- 初始化角色
INSERT INTO `role` (`id`, `role_name`, `role_code`, `role_type`, `description`, `status`, `create_at`, `update_at`)
VALUES ('1', '系统管理员', 'ROLE_ADMIN', 1, '系统管理员，拥有所有权限', 1, NOW(), NOW()),
       ('2', '运营专员', 'ROLE_OPERATOR', 1, '运营专员，负责日常运营', 1, NOW(), NOW()),
       ('3', '普通用户', 'ROLE_USER', 0, '普通客户用户', 1, NOW(), NOW()),
       ('4', '高级用户', 'ROLE_VIP', 0, '高级客户用户', 1, NOW(), NOW());

-- 初始化用户角色关联
INSERT INTO `user_role` (`user_id`, `role_id`, `create_at`)
VALUES ('1', '1', NOW());

-- 运营端菜单初始数据
INSERT INTO `menu` (`menu_id`, `menu_name`, `menu_code`, `type`, `sort`, `menu_type`, `create_time`, `update_time`)
VALUES ('100', '系统管理', 'system', 1, 1, 1, NOW(), NOW()),
       ('101', '用户管理', 'user', 2, 1, 1, NOW(), NOW()),
       ('102', '角色管理', 'role', 2, 2, 1, NOW(), NOW()),
       ('103', '菜单管理', 'menu', 2, 3, 1, NOW(), NOW()),
       ('104', '用户新增', 'user:add', 3, 1, 1, NOW(), NOW()),
       ('105', '用户编辑', 'user:edit', 3, 2, 1, NOW(), NOW()),
       ('106', '用户删除', 'user:delete', 3, 3, 1, NOW(), NOW()),
       ('107', '角色新增', 'role:add', 3, 1, 1, NOW(), NOW()),
       ('108', '角色编辑', 'role:edit', 3, 2, 1, NOW(), NOW()),
       ('109', '角色删除', 'role:delete', 3, 3, 1, NOW(), NOW()),
       ('110', '菜单新增', 'menu:add', 3, 1, 1, NOW(), NOW()),
       ('111', '菜单编辑', 'menu:edit', 3, 2, 1, NOW(), NOW()),
       ('112', '菜单删除', 'menu:delete', 3, 3, 1, NOW(), NOW());

-- 客户端菜单初始数据
INSERT INTO `menu` (`menu_id`, `menu_name`, `menu_code`, `type`, `sort`, `menu_type`, `create_time`, `update_time`)
VALUES ('200', '首页', 'home', 2, 1, 0, NOW(), NOW()),
       ('201', '个人中心', 'profile', 2, 2, 0, NOW(), NOW()),
       ('202', '修改信息', 'profile:edit', 3, 1, 0, NOW(), NOW());

-- 按钮权限定义
INSERT INTO `menu_permission` (`menu_id`, `permission`, `description`, `create_time`)
VALUES ('101', 'user:view', '查看用户列表', NOW()),
       ('101', 'user:add', '新增用户', NOW()),
       ('101', 'user:edit', '编辑用户', NOW()),
       ('101', 'user:delete', '删除用户', NOW()),
       ('102', 'role:view', '查看角色列表', NOW()),
       ('102', 'role:add', '新增角色', NOW()),
       ('102', 'role:edit', '编辑角色', NOW()),
       ('102', 'role:delete', '删除角色', NOW()),
       ('103', 'menu:view', '查看菜单列表', NOW()),
       ('103', 'menu:add', '新增菜单', NOW()),
       ('103', 'menu:edit', '编辑菜单', NOW()),
       ('103', 'menu:delete', '删除菜单', NOW()),
       ('201', 'profile:view', '查看个人信息', NOW()),
       ('201', 'profile:edit', '编辑个人信息', NOW());