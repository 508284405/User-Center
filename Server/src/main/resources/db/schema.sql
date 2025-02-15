-- 创建用户表
CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username          VARCHAR(50)  NOT NULL COMMENT '用户名',
    email             VARCHAR(100) COMMENT '邮箱',
    phone             VARCHAR(20) COMMENT '手机号',
    password_hash     VARCHAR(255) NOT NULL COMMENT '密码哈希',
    avatar_url        VARCHAR(255) COMMENT '头像URL',
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态',
    audit_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    membership_level  VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '会员等级',
    membership_expiry DATETIME COMMENT '会员过期时间',
    points            INT          NOT NULL DEFAULT 0 COMMENT '积分',
    created_by        VARCHAR(50) COMMENT '创建人',
    updated_by        VARCHAR(50) COMMENT '更新人',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 创建管理员用户表
CREATE TABLE IF NOT EXISTS user_admins
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username          VARCHAR(50)  NOT NULL COMMENT '用户名',
    email             VARCHAR(100) COMMENT '邮箱',
    phone             VARCHAR(20) COMMENT '手机号',
    password_hash     VARCHAR(255) NOT NULL COMMENT '密码哈希',
    avatar_url        VARCHAR(255) COMMENT '头像URL',
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态',
    audit_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    membership_level  VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '会员等级',
    membership_expiry DATETIME COMMENT '会员过期时间',
    points            INT          NOT NULL DEFAULT 0 COMMENT '积分',
    created_by        VARCHAR(50) COMMENT '创建人',
    updated_by        VARCHAR(50) COMMENT '更新人',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员用户表';

-- Spring Security默认的权限表结构
CREATE TABLE IF NOT EXISTS authorities
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username  VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    status      INT         NOT NULL DEFAULT 1 COMMENT '状态',
    description VARCHAR(255) COMMENT '描述',
    level       INT         NOT NULL DEFAULT 0 COMMENT '权限等级，0: 普通管理员，1: 超级管理员',
    system_id   BIGINT COMMENT '关联的系统ID',
    created_by  VARCHAR(50) COMMENT '创建人',
    updated_by  VARCHAR(50) COMMENT '更新人',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_name (role_name),
    INDEX idx_system_id (system_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色表';

-- 创建系统表
CREATE TABLE IF NOT EXISTS systems
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    system_name VARCHAR(100) NOT NULL COMMENT '系统名称',
    system_code VARCHAR(50)  NOT NULL COMMENT '系统编码',
    owner_name  VARCHAR(50) COMMENT '责任人',
    owner_email VARCHAR(100) COMMENT '责任人邮箱',
    owner_phone VARCHAR(20) COMMENT '责任人电话',
    api_key     VARCHAR(100) NOT NULL COMMENT '系统接入密钥',
    api_secret  VARCHAR(255) NOT NULL COMMENT '系统接入密钥',
    description VARCHAR(255) COMMENT '描述',
    created_by  VARCHAR(50) COMMENT '创建人',
    updated_by  VARCHAR(50) COMMENT '更新人',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_system_code (system_code),
    INDEX idx_system_name (system_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统表';

-- 创建菜单权限表
CREATE TABLE IF NOT EXISTS menu_permissions
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    system_id  BIGINT      NOT NULL COMMENT '关联的系统ID',
    menu_name  VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code  VARCHAR(50) NOT NULL COMMENT '菜单编码',
    menu_url   VARCHAR(255) COMMENT '菜单URL',
    parent_id  BIGINT COMMENT '父菜单ID',
    sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
    icon       VARCHAR(50) COMMENT '菜单图标',
    permission VARCHAR(100) COMMENT '权限标识',
    menu_type  VARCHAR(20) NOT NULL DEFAULT 'MENU' COMMENT '菜单类型',
    visible    BOOLEAN     NOT NULL DEFAULT true COMMENT '是否可见',
    created_by VARCHAR(50) COMMENT '创建人',
    updated_by VARCHAR(50) COMMENT '更新人',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_system_id (system_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_code (menu_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='菜单权限表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_logs
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id        BIGINT      NOT NULL COMMENT '操作用户ID',
    username       VARCHAR(50) NOT NULL COMMENT '操作用户名',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（CREATE, UPDATE, DELETE）',
    module         VARCHAR(50) NOT NULL COMMENT '操作模块（USER, ROLE, MENU, SYSTEM）',
    target_id      BIGINT COMMENT '操作目标ID',
    description    VARCHAR(255) COMMENT '操作描述',
    ip_address     VARCHAR(50) COMMENT '操作者IP地址',
    created_by     VARCHAR(50) COMMENT '创建人',
    updated_by     VARCHAR(50) COMMENT '更新人',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';