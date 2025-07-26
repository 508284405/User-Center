create table user_center.operation_logs
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    user_id        bigint                             null,
    username       varchar(50)                        null,
    operation_type varchar(20)                        null,
    module         varchar(50)                        null,
    target_id      bigint                             null comment '操作目标ID',
    description    varchar(255)                       null comment '操作描述',
    ip_address     varchar(50)                        null comment '操作者IP地址',
    created_by     varchar(50)                        null comment '创建人',
    updated_by     varchar(50)                        null comment '更新人',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '操作日志表';

create index idx_created_at
    on user_center.operation_logs (created_at);

create index idx_module
    on user_center.operation_logs (module);

create index idx_operation_type
    on user_center.operation_logs (operation_type);

create index idx_user_id
    on user_center.operation_logs (user_id);

