# 用户中心子模块设计：账户等级/积分、优惠券、权益管理（设计与集成方案）

## 1. 背景与目标
- 背景：在现有用户中心（Java 17，Spring Boot 3，多模块 Maven）基础上，补齐用户价值体系能力，包括积分、等级、优惠券、会员权益，支撑营销拉新、留存与复购。
- 目标：
  - 提供可扩展、可观测、易集成的积分、等级、优惠券、权益能力。
  - 支持与订单、支付、营销等系统进行一致性良好的集成（锁券、确认、回滚、积分结算、退款回退）。
  - 与现有分层契合：DTO/Client（client）、Adapter（controller）、App（用例编排）、Domain（领域模型与规则）、Infrastructure（持久化/集成）。

## 2. 范围与不在范围
- 在范围：
  - 账户积分（累积/扣减、流水、过期、冻结）。
  - 账户等级（成长值/积分驱动、升级/降级策略、权益包绑定）。
  - 优惠券（模板/发放、锁定/核销、适用范围与校验、过期）。
  - 会员权益（权益模板、权益包、发放与占用、用量限制/计数、过期与撤销）。
  - 事件与接口：REST + 事件（Spring 事件/消息）对外集成。
- 不在范围：
  - 复杂营销定价引擎、跨业务线结算、资金清结算（仅提供接口对接点）。

## 3. 总体架构与模块职责
- 模块划分（与仓库一致）：
  - user-center-client：
    - 对外 DTO、VO、枚举、Feign 客户端接口（供内部服务调用）。
  - user-center-adapter：
    - REST 控制器、参数校验、鉴权与限流、异常映射；API 文档（OpenAPI）。
  - user-center-app：
    - 应用服务与用例编排（事务边界），集成领域服务、网关与外部服务；防重幂等。
  - user-center-domain：
    - 领域模型（聚合根、实体、值对象）、领域服务、领域事件与规则策略。
  - user-center-infrastructure：
    - MyBatis(Plus) 持久化、Mapper/XML、事务型 Outbox、Redis/锁、定时任务、第三方接入。
  - start：
    - Spring Boot 启动与运行配置，Profile 管理、Keystore/JWT。

- 包结构建议（示例）：
  - domain
    - loyalty.points（PointAccount, PointTxn, PointPolicy, ExpirePolicy, DomainService）
    - loyalty.levels（Level, LevelRule, GrowthPolicy, LevelService）
    - coupon（CouponTemplate, Coupon, CouponState, CouponService, ValidatePolicy）
    - benefit（Benefit, BenefitPackage, Entitlement, UsageRule, BenefitService）
    - common（IdempotencyKey, BizType, DomainEvent, Money/Amount 等）
  - app
    - loyalty（PointsAppService, LevelAppService）
    - coupon（CouponAppService）
    - benefit（BenefitAppService）
    - facade（组合用例，如结算占用→支付确认→核销/回滚）
  - adapter
    - loyalty（PointsController, LevelController）
    - coupon（CouponController）
    - benefit（BenefitController）
  - infrastructure
    - mapper（*Mapper）/entity（DO）/xml（MyBatis）
    - repository（具体仓储实现）
    - gateway（对外，如营销/订单）
    - outbox（事务消息表与发布器）
    - job（过期清理、补偿任务）

- 集成方式：
  - 同步 REST：供订单/营销调用，进行券校验、锁定、核销；查询积分、等级、权益。
  - 事件驱动：
    - 进件事件：订单创建、支付成功、订单完成、退款成功等（外部→本系统）。
    - 出件事件：PointsChangedEvent、LevelUpgradedEvent、CouponRedeemedEvent、BenefitUsedEvent（本系统→外部）。
  - 一致性：本地事务 + 事务型 Outbox（Infrastructure 发布到消息系统，或 Spring 事件模拟）。

## 4. 领域建模（核心概念）
- 积分（Points）：
  - PointAccount（用户维度积分账户，含可用/冻结/将过期）
  - PointTxn（积分流水：获取/扣减/冻结/解冻/过期/冲正）
  - PointPolicy（获取规则，按订单金额、活动系数、上限；扣减优先级：先近到期）
  - ExpirePolicy（先到期先出；批次化到期）
- 等级（Levels）：
  - Level（等级定义：code、名称、门槛、权益包绑定）
  - LevelRule/GrowthPolicy（按周期累计成长值或积分；升级/降级策略：即时/周期评估）
  - LevelSnapshot（用户当前等级与门槛快照，用于幂等计算）
- 优惠券（Coupons）：
  - CouponTemplate（券模板：类型、面额/折扣、门槛、适用范围、发放规则、有效期）
  - Coupon（具体券实例，面向用户/码池），状态机详见下节
  - CouponConstraint（适用约束：品类、SKU、渠道、用户等级、时间段等）
  - CouponService（校验/锁定/核销/释放/过期）
- 权益（Benefits）：
  - Benefit（权益定义：如包邮、专属折扣、专属客服、成长加速、生日礼、积分兑券等）
  - BenefitPackage（权益包，与等级或会员计划绑定）
  - Entitlement（权益发放实例，含额度/次数/有效期、状态机）
  - UsageRule（使用规则与计数策略：日/周/月上限、黑白名单）

### 4.1 状态机（关键）
- 优惠券状态（CouponState）：
  - CREATED → ISSUED → AVAILABLE → RESERVED → REDEEMED
  - AVAILABLE → EXPIRED
  - RESERVED → AVAILABLE（保留超时/取消）
  - REDEEMED → REFUNDED（发生退款且可回滚场景时）
- 权益发放状态（EntitlementState）：
  - ACTIVE → USED / EXPIRED / REVOKED
- 积分流水（PointTxnType）：EARN、DEDUCT、FREEZE、UNFREEZE、EXPIRE、REVERSAL

## 5. 数据库模型（表结构建议）
命名前缀建议：`uc_`。主键统一 `id BIGINT`（雪花/ULID）。仅列出核心字段，实际以 MyBatis Plus 实现。

- uc_point_account
  - user_id, available, frozen, will_expire, version, updated_at
- uc_point_lot（积分批次，用于过期策略）
  - user_id, lot_id, amount, remain, expire_at, created_at
- uc_point_txn
  - user_id, lot_id, delta, type, biz_type, biz_id, idempotency_key, created_at
  - 索引：user_id+created_at，idempotency_key 唯一

- uc_level_def
  - level_code, name, threshold, pkg_id, priority, enabled
- uc_user_level
  - user_id, level_code, growth_value, valid_from, valid_to, snapshot_json, updated_at

- uc_coupon_tpl
  - tpl_code, name, type, face_value, discount_rate, threshold_amount, valid_from, valid_to,
    issue_start, issue_end, total, issued, constraints_json, status
- uc_coupon
  - tpl_code, coupon_code, user_id, state, locked_until, assigned_at, valid_from, valid_to
  - 索引：coupon_code 唯一；user_id+state
- uc_coupon_txn
  - coupon_code, action(ISSUE/RESERVE/REDEEM/RELEASE/EXPIRE/REFUND),
    biz_type, biz_id, idempotency_key, created_at

- uc_benefit
  - benefit_code, name, type, rule_json, enabled
- uc_benefit_pkg
  - pkg_id, name, enabled
- uc_benefit_pkg_item
  - pkg_id, benefit_code, quota, period(INFINITE/DAILY/MONTHLY), priority
- uc_entitlement
  - user_id, benefit_code, pkg_id, quota_total, quota_used, state, valid_from, valid_to, updated_at
- uc_entitlement_txn
  - entitlement_id, delta, action(GRANT/USE/REVOKE/EXPIRE), biz_type, biz_id, idempotency_key, created_at

- uc_outbox_event（事务消息）
  - event_id, topic, payload_json, status(NEW/SENT/FAILED), created_at, retry_cnt

## 6. 接口设计（REST + 事件）
### 6.1 REST（对内/对外）
- 积分（/api/loyalty/points）
  - `GET /balance?userId=...`：查询余额
  - `POST /earn`：入参 `{userId, amount, bizType, bizId, idempotencyKey}`
  - `POST /deduct`：入参 `{userId, amount, reason, bizType, bizId, idempotencyKey}`（按到期优先扣）
  - `GET /txns?userId=...&page=...`：流水分页
- 等级（/api/loyalty/levels）
  - `GET /current?userId=...`：查询当前等级
  - `POST /evaluate`：触发评估（可由定时或事件自动触发）
  - `GET /rules`：查询等级定义与阈值
- 优惠券（/api/coupons）
  - 模板：`POST /templates`，`GET /templates/{tplCode}`
  - 发放：`POST /issue` 入参 `{tplCode, userId | userIds[], idempotencyKey}`
  - 列表：`GET /mine?userId=...&state=...`
  - 校验：`POST /validate` 入参 `{couponCode, items[], orderAmount, channel, userId}`
  - 锁定：`POST /reserve` 入参 `{couponCode, orderId, ttlSeconds, idempotencyKey}`
  - 确认核销：`POST /redeem` 入参 `{couponCode, orderId, idempotencyKey}`
  - 释放：`POST /release` 入参 `{couponCode, orderId, reason}`
- 权益（/api/benefits）
  - 定义/包：`POST /packages`，`GET /packages/{pkgId}`
  - 我的权益：`GET /entitlements?userId=...`
  - 占用/使用：`POST /use` 入参 `{benefitCode|entitlementId, userId, amount(次数), bizType, bizId, idempotencyKey}`

说明：所有写接口需支持 `Idempotency-Key`（Header 或 body），并校验签名/鉴权（JWT）。

### 6.2 事件（出站/入站）
- 出站事件（Outbox）：
  - `PointsChangedEvent{userId, delta, balance, reason, bizType, bizId, occurredAt}`
  - `LevelUpgradedEvent{userId, fromLevel, toLevel, occurredAt}`
  - `CouponRedeemedEvent{couponCode, tplCode, userId, orderId, occurredAt}`
  - `BenefitUsedEvent{userId, benefitCode, quotaUsed, bizType, bizId, occurredAt}`
- 入站事件：
  - `OrderCreated{orderId, userId, items, amount, channel}`（用于券校验/推荐）
  - `PaymentSucceeded{orderId, userId, amount}`（触发券核销确认）
  - `OrderCompleted{orderId, userId, amount}`（触发积分发放、等级评估、权益发放）
  - `RefundSucceeded{orderId, userId, amount}`（触发券退款回滚、积分反向流水、权益回滚）

## 7. 关键业务流程
### 7.1 下单用券两阶段（锁定→确认/释放）
1) 结算页：调用 `POST /coupons/validate` 返回可用券；
2) 用户选择券：`POST /coupons/reserve` 锁定，记录 `locked_until=now+ttl`；
3) 支付成功：收银台/支付发 `PaymentSucceeded` 事件 → `POST /coupons/redeem` 确认核销；
4) 支付失败/超时：定时任务或订单调用 `POST /coupons/release`，或锁超时自动回收。

并发与一致性：同一张券在 `RESERVED` 期间不可再次锁定；校验 `coupon.state==AVAILABLE`；使用乐观锁或行级锁 + 幂等键。

### 7.2 订单完成发积分与升级
- 触发：`OrderCompleted` 事件；
- 计算：按 `PointPolicy` 对税后/实付金额×系数→`EARN` 积分入账至批次；
- 到期：生成 `uc_point_lot(expire_at)`；
- 等级：按 `GrowthPolicy` 更新成长值，若跨阈值即升级并发放对应 `BenefitPackage` 的 `Entitlement`；
- 事件：发布 `PointsChangedEvent`、`LevelUpgradedEvent`。

### 7.3 退款回滚
- 券：若支持退款回补，`REDEEMED → REFUNDED`，可按策略返还等值券或积分；
- 积分：生成负向 `DEDUCT` 或 `REVERSAL` 流水，优先回收最近批次；
- 权益：若由该订单发放且未使用的 `Entitlement` 则撤销；已使用则按策略不回滚/补偿。

### 7.4 权益使用
- 校验用户当前 `Entitlement.state==ACTIVE` 且剩余额度足够；
- 生成 `uc_entitlement_txn(USE)` 并递减 `quota_used`；
- 可能联动外部标记（如运费优惠、专属客服标识）。

## 8. 非功能与工程策略
- 幂等：所有写操作要求 `idempotency_key`；DB 唯一约束 + 先查后写；
- 并发：优惠券与积分批次更新使用乐观锁（version）+ 条件更新；必要时短期 Redis 锁；
- 过期：
  - 券与权益：状态定时扫描 + 到期字段索引；
  - 积分：批次到期（定时提取即将到期批次执行 `EXPIRE` 流水）。
- 观测：埋点/日志（入参、校验失败、状态迁移）、指标（券核销率、积分发放/过期量、权益使用率）。
- 安全：JWT 鉴权、接口权限（管理端与用户端区分）、参数签名可选；敏感操作审计。
- 配置：通过 `start` 模块 `application.yml` 分环境；密钥/连接串走环境变量（不落库明文）。
- 迁移：Flyway/Liquibase 维护表结构；灰度启用新规则。

## 9. 与仓库模块落地映射
- user-center-client
  - DTO：`PointsDTO/TxnDTO/BalanceDTO`，`LevelDTO/RuleDTO`，`CouponDTO/TemplateDTO`，`BenefitDTO/EntitlementDTO`
  - Client：`CouponClient`（锁定/核销）、`PointsClient`、`LevelClient`、`BenefitClient`
  - MapStruct：DTO ↔ Domain VO/Entity 映射接口
- user-center-adapter
  - Controller：`PointsController`、`LevelsController`、`CouponsController`、`BenefitsController`
  - 统一异常转换、鉴权拦截器、OpenAPI 文档
- user-center-app
  - 应用服务：编排流程（锁券→支付确认→核销/释放；订单完成→发积分→评估等级→发权益）
  - 防重：校验 `idempotency_key`；
- user-center-domain
  - 领域对象、策略与服务；枚举与状态机；领域事件定义
- user-center-infrastructure
  - MyBatis(Plus) 实体/Mapper/XML；Repository 实现
  - Outbox 表与发布器；定时任务（过期/释放/补偿）

## 10. 示例 API（简化）
- 锁券
```http
POST /api/coupons/reserve
Content-Type: application/json
Idempotency-Key: 8f1a...
{
  "couponCode": "C2025X...",
  "orderId": "O123",
  "ttlSeconds": 900,
  "idempotencyKey": "8f1a..."
}
```
- 发积分
```http
POST /api/loyalty/points/earn
{
  "userId": 1001,
  "amount": 120,
  "bizType": "ORDER",
  "bizId": "O123",
  "idempotencyKey": "k-earn-O123"
}
```

## 11. 实施计划（里程碑）
- M1 基础设施
  - 建表（Flyway）、Repository、通用错误码、幂等组件、Outbox 框架
- M2 积分与等级
  - 积分账户/批次/流水、到期任务；等级规则/评估；API 与事件
- M3 优惠券
  - 模板/券实例、状态机、锁定/核销/释放、过期任务；API 与事件
- M4 权益
  - 权益与权益包、发放与使用、配额与周期限制、API 与事件
- M5 集成联调
  - 与订单/支付/营销的锁券与确认/回滚、订单完成事件、退款回滚
- M6 观测与运营
  - 指标看板、运营报表、风控与阈值策略

## 12. 测试策略
- 单元测试：领域服务、规则策略（边界值、多策略组合）；
- 应用/集成测试：控制器切片测试、Repository 测试（开箱内存 DB）、流程测试（锁券→核销→释放）；
- 回归/性能：高并发锁券与核销、积分到期批量任务、权益计数热点；
- Mock 外部：订单/支付事件与回调。

## 13. 风险与缓解
- 高并发热点（爆款券）：使用模板维度配额控制、分片码池、短期 Redis 锁、限流/排队；
- 事务一致性：强依赖 Outbox 与补偿任务，写路径均落 DB；
- 时钟与过期：统一使用服务器时间，过期任务具备重试与幂等；
- 规则复杂度攀升：策略/规则插件化，配置与灰度生效。

---
本文档为系统高阶设计蓝图，落地时应结合实际表结构、DTO 定义、接口鉴权策略进行细化与评审。
