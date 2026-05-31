# 全面项目审查报告

> 生成日期: 2026-05-21
> 范围: 设计架构、安全、高并发、性能、API设计、错误处理、DB Schema、日志监控、业务逻辑
> 状态: 三批次 agent 全部完成，共发现 90+ 项问题

---

## 目录

1. [严重缺陷 (Critical)](#1-严重缺陷-critical)
2. [高危缺陷 (High)](#2-高危缺陷-high)
3. [中危缺陷 (Medium)](#3-中危缺陷-medium)
4. [低危缺陷 (Low)](#4-低危缺陷-low)
5. [已修复项 (Phase 1)](#5-已修复项-phase-1)
6. [汇总统计](#6-汇总统计)

---

## 1. 严重缺陷 (Critical)

### C01. Context.Background() 滥用 (~200+ 处)

**文件:** 所有 service 文件
**类型:** 设计/高并发

几乎每个 service 函数都使用 `context.Background()` 而不是从 Gin 上下文中传播 `c.Request.Context()`。这意味着：

- 请求取消信号不会传递到数据库/Redis 层
- 超时控制无法生效
- Trace ID 等上下文信息在 DB 层丢失
- 高并发场景下无法优雅降级

**示例模式:**
```go
// 错误做法 - 遍布整个代码库
ctx := context.Background()
db.Client.SysDict.Query().All(ctx)

// 正确做法
ctx := c.Request.Context()
db.Client.SysDict.Query().All(ctx)
```

**修复建议:** 所有 service 函数签名需要从 `func Xxx(c *gin.Context, ...)` 改为提取 `c.Request.Context()` 并向下传递。

---

### C02. Token 不包含过期时间 (exp) 声明

**文件:** `core/auth/base_auth.go:110-113`
**类型:** 安全

Token claims 仅包含 `jti` 和 `iat`，**没有 `exp`（过期时间）、`nbf` 或 `aud` 声明**。Token 本身永久有效 —— 过期完全依赖 Redis TTL。如果 Redis 被清空或故障，Token 无法被验证为无效。

---

### C03. 认证系统在 Redis 故障时完全瘫痪

**文件:** `core/auth/base_auth.go`
**类型:** 安全/高并发

所有认证状态存储在 Redis 中，没有任何降级或后备机制。当 Redis 不可用时：
- `decodeToken` 返回 nil → 所有用户都被视为未登录
- `IsLogin` 总是失败 → 所有需要认证的 API 返回 401
- `CheckDisable` 返回 false → 禁用检查不生效

---

### C04. 登录端点没有速率限制

**文件:**
- `modules/sys/auth/username/api/v1/api.go:14`
- `modules/client/auth/username/api/v1/api.go:14`
**类型:** 安全

登录端点完全暴露在公网（`/api/v1/public/`），没有任何速率限制中间件。攻击者可以：
- 对单个账户进行暴力破解（仅受验证码限制）
- 对多个账户进行凭证填充攻击
- 耗尽服务器资源

代码库中不存在 `ratelimit`、`limiter` 或 `throttle` 实现。

---

### C05. 密码更改后现有会话未失效

**文件:**
- `modules/sys/user/service.go:978-1015` (`UserUpdatePassword`)
- `modules/client/user/service.go:425-462` (`UpdatePassword`)
**类型:** 安全

更改密码后**未调用 `auth.Kickout(userID)`**，现有会话仍然有效。如果攻击者获取了会话令牌，合法用户更改密码后攻击者的会话仍然可用，直到 Redis TTL 过期。

---

### C06. 管理员更改用户密码后会话未失效

**文件:** `modules/sys/user/service.go:436-447` (`UserModify`)
**类型:** 安全

管理员通过 `UserModify` 设置用户新密码后，同样**未调用 `auth.Kickout(userID)`**。

---

### C07. 文件上传无文件类型/MIME 验证

**文件:** `modules/sys/file/service.go:28-87`
**类型:** 安全

Upload 函数仅从文件名提取后缀（第 52 行），但：
- 从未将后缀与允许类型列表比较
- 从未检查 MIME 类型
- 从未验证文件内容（魔数检查）

攻击者可上传 `.exe`、`.php`、`.jsp`、`.html` 等恶意文件。

---

### C08. 分析仪表盘无认证

**文件:** `modules/sys/analyze/api/v1/api.go:10-13` / `core/app/router.go:59`
**类型:** 安全

仪表盘端点完全没有认证中间件，暴露敏感系统信息：
- 总用户数、活跃用户数
- 角色数、组织数
- 服务器运行时间、服务器 IP

---

### C09. 所有多表写入操作无事务包裹（新增）

**文件:** `modules/sys/role/service.go`, `modules/sys/user/service.go`, `modules/sys/resource/service.go` 等
**类型:** 数据一致性

整个代码库使用 `db.Client.Tx()` 的调用次数为 **零**。所有多表写入操作直接使用裸 `db.Client.X` 调用，没有任何事务保证。关键受损场景：

| 操作 | 文件 | 风险 |
|------|------|------|
| RoleRemove | role/service.go:164-203 | 删除关联权限/资源后，删除角色本身失败 → 孤立关联记录 |
| RoleGrantPermissions | role/service.go:225-254 | Delete-all 后 Insert 中途失败 → 角色丢失所有权限 |
| RoleGrantResources | role/service.go:258-339 | Delete 资源 → Insert 资源 → Insert 权限，三步无事务 |
| UserCreate + grantRoles | user/service.go:373-462 | 用户创建成功但角色赋予失败 → 用户无角色 |
| UserModify + grantRoles | user/service.go:469-552 | 用户更新成功但角色重赋失败 |
| UserRemove | user/service.go:559-582 | Delete Rel → Delete User，角色/权限关联成为孤儿 |
| UserGrantPermissions | user/service.go:643-681 | Delete-all 后 Insert 中途失败 → 权限不完整 |
| OrgRemove | org/service.go:370-416 | 清理岗位引用成功后删除组织失败 |
| ResourceRemove | resource/service.go:394-411 | 删除关联角色资源后资源本身删除失败 |

**修复建议:** 所有 delete-then-insert 模式必须使用 `db.Client.Tx()` 包裹，配合 `Commit`/`Rollback`。

---

### C10. 审计日志数据库写入失败时静默丢弃（新增）

**文件:** `core/log/record.go:51-53`, `core/log/syslog.go:110-112`
**类型:** 审计

```go
if err != nil {
    _ = err   // silently discarded
}
```

如果 `sys_log` 表写入失败，审计事件完全静默丢失。关键操作（密码更改、用户删除、登录）可能在无人知晓的情况下变得不可审计。

---

### C11. 日志防篡改功能存在但从未被调用（新增）

**文件:** `core/log/utils.go:77-86`
**类型:** 审计

`GenerateLogSignature` 函数使用 SM3 哈希定义完整，但**未被任何代码调用**。SysLog 模式中的 `sign_data` 字段始终为空。日志防篡改已计划但未实现。

---

## 2. 高危缺陷 (High)

### H01. Token 签名密钥和 SM2 私钥来自 YAML 配置

**文件:** `config/config.go:57,63-66`
**类型:** 安全

`SecretKey` 和 SM2 私钥直接从 YAML 配置文件中读取。如果 YAML 文件泄露（例如提交到 Git、服务器被入侵），所有 Token 都可以被伪造，所有传输密码可以被解密。

虽然已创建 `config.example.yaml` 并添加 `.gitignore`，但最佳实践是使用环境变量或密钥管理服务。

此外，配置从硬编码路径 `"config.yaml"` 加载（`core/app/app.go:25`），不支持环境变量覆盖或容器化秘密注入。

---

### H02. 权限检查每次请求都查询数据库，无缓存

**文件:** `core/auth/permission_tool.go:70-76`
**类型:** 性能/高并发

每个权限角色检查都为当前用户查询数据库。没有进程级或 Redis 缓存。对于每个受保护的端点，每次请求都会执行多个数据库查询（角色 → 角色-权限关联 → 用户-直接权限）。

如果数据库变慢或不可用，整个授权系统立即失效。

**此外**，如果在单个请求上应用了多个权限中间件，每个中间件独立查询数据库，没有每请求缓存。

---

### H03. 角色/权限在 Gin 上下文中无缓存

**文件:** `core/auth/middleware/check_permission.go:33-65`
**类型:** 性能

同 H02，如果单个请求需要多个权限，每个权限中间件会多次查询数据库。

---

### H04. N+1 查询问题 (getOrgUserDistribution)

**文件:** `modules/sys/analyze/service.go:132-150`
**类型:** 性能

`getOrgUserDistribution` 函数遍历所有组织，对每个组织执行一次用户计数查询：

```go
for _, o := range orgs {
    count, err := db.Client.SysUser.Query().Where(sysuser.OrgID(o.ID)).Count(ctx)
}
```

如果有 100 个组织，会产生 1+100 次数据库查询。

**修复建议:** 使用 `SysUser.GroupBy(sysuser.OrgIDColumn)` 聚合查询。

---

### H05. N+1 查询问题 (Position 名称解析)

**文件:** `modules/sys/position/service.go:84-124`
**类型:** 性能

`resolveOrgNamePath` 和 `resolveGroupNamePath` 各在每个祖先层级执行一次 `Get()` 查询。一页 20 个岗位，每个组织深度 4、组深度 3，共产生 20×(4+3)=140 次数据库查询。

对比：`modules/sys/user/service.go` 的 `batchEnrichNames` 正确使用批量加载。

---

### H06. permissionRegistry 无并发保护

**文件:** `core/auth/permission_scan.go:26`
**类型:** 高并发

`permissionRegistry` 是一个共享切片，在 `RegisterPermission` 中被多个 goroutine 并发写入，**没有任何互斥锁保护**。在并发注册场景下会导致数据竞争。

---

### H07. Redis 单节点配置（无 Sentinel/Cluster）

**文件:** `core/db/redis.go:20-28`
**类型:** 高并发/设计

使用 `redis.NewClient`（单节点），不是 `redis.NewClusterClient`。没有配置 Redis Sentinel 或 Cluster。生产环境中这是单点故障。

---

### H08. SM2 私钥位于配置文件中

**文件:** `config/config.go:63-66`
**类型:** 安全

SM2 私钥用于解密前端传输的密码。私钥泄露后攻击者可：
- 解密传输中的密码
- 进行离线暴力破解或凭证填充

---

### H09. 无暴力破解防护（无账户锁定）

**类型:** 安全

没有失败的登录尝试跟踪、账户锁定阈值或渐进式延迟。用户在一个账户上可以无限次尝试（仅受验证码限制）。

---

### H10. 无全局速率限制（缺少每 IP 限制器）

**类型:** 安全

没有基于 IP 的全局速率限制来防御针对多个账户的分布式暴力破解。

---

### H11. Recovery 中间件中 `c.Next()` 之后缺少 return

**文件:** `core/middleware/recovery.go:39`
**类型:** 错误处理

```go
if err := c.Errors.Last(); err != nil {
    c.JSON(200, result.Failure(c, err.Error(), 400, nil))
    c.Abort()
}
```
`c.JSON` 后缺少 `return`。虽然当前路径风险低，但后续新增代码路径时可能引入 Bug。

---

### H12. ShouldBind 无验证标签

**类型:** API设计

整个代码库中没有一个 `ShouldBind` 调用使用 Gin 的验证标签（如 `binding:"required"`、`binding:"min=6"`）。输入验证完全依赖应用层手动代码，容易遗漏。

**受影响端点:** 登录、注册、用户管理、分页参数等所有 API。

---

### H13. 无最大页面大小限制

**类型:** API设计

所有分页函数中没有 `param.Size` 的上限。恶意客户端可请求 `size=99999999` 导致数据库大量加载和内存膨胀。

**修复建议:** 添加 `if param.Size > 100 { param.Size = 100 }`。

---

### H14. 密码无最低复杂度要求

**文件:**
- `modules/sys/user/service.go:436-447`（管理员创建用户）
- `modules/client/user/service.go:425-462`（用户更改密码）
**类型:** 安全

密码经过 SM2 解密和 bcrypt 哈希处理，但没有强制执行最低复杂度要求（长度、字符类型等）。

---

### H15. 资源/权限同步时静默丢弃错误

**文件:** `modules/sys/resource/service.go`
**类型:** 错误处理

- `collectDescendantIDs`: 子资源查询错误被 `continue` 静默忽略（第 504 行）
- `syncPermission`: 角色关系查询错误后静默返回；删除/创建操作错误被 `_, _ =` 丢弃（第 532-565 行）

```go
_, _ = db.Client.RelRolePermission.Delete()....Exec(ctx)
_ = db.Client.RelRolePermission.Create()....Exec(ctx)
```
权限变更无法持久化时，Resource 表中的 `extra.permission_code` 将与实际权限表产生漂移，系统无任何告警。

---

### H16. 文件 engine 参数被忽略（getStorage() 为空实现）

**文件:** `modules/sys/file/service.go:248-250`
**类型:** 设计

```go
func getStorage(engine string) storage.FileStorage {
    return storage.NewLocalStorage("uploads")
}
```
`engine` 参数从请求中读取但完全被忽略，所有上传都使用 LOCAL 存储。如果系统意图支持 S3/OSS/MinIO，上传文件被静默错误路由。

---

### H17. 日期范围解析错误被静默忽略

**文件:**
- `modules/sys/file/service.go:157-167`
- 其他模块类似模式
**类型:** API设计

```go
t, err := time.Parse("2006-01-02 15:04:05", param.DateRangeStart)
if err == nil {
    query = query.Where(sysfile.CreatedAtGTE(t))
}
```
无效日期格式被静默忽略，用户不会收到反馈且过滤不生效。

---

### H18. 数据库外键被禁用（新增）

**文件:** `cmd/migrate/main.go:67`
**类型:** 数据完整性

```go
opts := []schema.MigrateOption{
    schema.WithForeignKeys(false),
}
```

外键被显式禁用。导致：
- 没有 CASCADE DELETE，所有级联依赖应用层手动完成（且无事务）
- 没有数据库级参照完整性，`rel_user_role.role_id` 可指向不存在的角色
- 业务逻辑遗漏清理步骤时，孤立行静默累积

---

### H19. 缺少唯一约束（sys_user.username 等）（新增）

**文件:** `ent/schema/sysuser.go:50`, `ent/schema/clientuser.go:50`
**类型:** 数据完整性

`sys_user.username` 仅定义了非唯一索引（`index.Fields("username")` 而非 `Unique()`），`sys_user.email` 甚至没有索引。唯一性完全依赖应用层的 `Exist(ctx)` 检查（`modules/sys/user/service.go:377-392`），存在 **TOCTOU 竞态**：两个并发请求使用相同用户名可同时通过检查和创建。

---

### H20. 没有 Prometheus/pprof/expvar 指标端点（新增）

**类型:** 监控

项目完全没有：
- Prometheus 指标端点
- Go pprof 调试端点
- expvar 变量
- 任何运行时指标收集

生产环境中无法监控请求速率、错误率、goroutine 数量、内存使用或 GC 暂停。

---

### H21. 没有请求延迟中间件（新增）

**类型:** 监控

没有中间件记录或测量请求延迟。SysLog 中间件捕获了 `startTime` 但从未将其作为延迟暴露。无法追踪慢请求。

---

### H22. golang.org/x/net v0.54.0 过时（含 CVE）（新增）

**文件:** `go.mod:62`
**类型:** 依赖

`golang.org/x/net v0.54.0`（2023 年 3 月）严重过时，包含已知 CVE：CVE-2024-24788 (HTTP/2 DoS)、CVE-2024-24789 (HTTP/2 洪水攻击)。`golang.org/x/crypto v0.51.0` 和 `golang.org/x/text v0.37.0` 同样过时。

---

### H23. 无结构化日志（log.Printf 遍布全局）（新增）

**类型:** 日志

整个项目使用 Go 标准库 `log.Printf`。没有使用 zap、logrus、zerolog 或 slog。日志是自由格式字符串，无法通过日志管理系统进行结构化查询、过滤或告警。

**影响范围:** `log.Printf` 在 30+ 处使用。

---

### H24. 审计参数中可能捕获明文密码（新增）

**文件:** `core/log/utils.go:42-46`
**类型:** 审计

```go
excluded := map[string]bool{"request": true, "db": true, "file": true}
```

`ExtractParamsJson` 排除了 `request`、`db`、`file` 字段，但 `password` 字段存在于请求体中时会被记录到审计日志。虽然传输中使用 SM2 加密，但在日志中持久化密码相关数据是不必要的敏感暴露。

---

### H25. Dict 根节点重复检查遗漏 NULL/空 parent_id（新增）

**文件:** `modules/sys/dict/service.go:440-489`
**类型:** 业务逻辑

`dictCheckDuplicate` 使用 `sysdict.ParentID("0")` 查询根节点，但根节点可能为 `parent_id IS NULL` 或 `parent_id = ''`（`getParentIDKey` 第 643-648 行同时处理两者作为根）。树构建代码（第 114-117 行）明确处理 `"0"` 和 `""`，但重复检查只查了 `"0"`。一个具有相同标签的根字典项（NULL parent_id）不会被捕捉为重复。

---

### H26. Config Modify 在键名变更时只清除旧缓存键（新增）

**文件:** `modules/sys/config/service.go:169-175`
**类型:** 业务逻辑

```go
if entity.ConfigKey != nil {
    db.Redis.Del(ctx, "sys-config:"+*entity.ConfigKey)
}
```

如果 `ConfigKey` 在修改中被更改，只清除了旧键的缓存。新键可能有来自之前值的过期缓存。而 `EditBatch` 函数（第 296-301 行）正确处理了新旧两个键。

---

## 3. 中危缺陷 (Medium)

### M01. db.Client/db.Redis 在关闭期间存在竞态

**文件:** `core/db/*.go`
**类型:** 高并发

`db.Client` 和 `db.Redis` 是全局变量。如果关闭过程中将它们设置为 nil，同时仍有正在处理的请求在访问它们，会发生 nil pointer dereference。

**修复建议:** 使用健康检查模式 + 优雅关闭的请求屏障。

---

### M02. 文件上传完全缓冲在内存

**文件:** `modules/sys/file/service.go:47`
**类型:** 性能

```go
data, err := io.ReadAll(file)
```
大文件上传时整个文件内容读入内存，可能导致 OOM。应该使用流式处理或临时文件。

---

### M03. 配置缓存（Config）—— Redis Key 删除但从未读回

**文件:** `modules/sys/config/service.go`
**类型:** 性能

Config 模块在写入时删除 Redis 缓存键（使其失效），但权限检查等相关代码从未读取这些缓存键。缓存失效是单向操作，没有实际价值。

---

### M04. 权限缓存 TTL=0（永不过期）

**文件:** `core/auth/permission_scan.go:16`
**类型:** 性能

权限缓存的 TTL 设置为 0，意味着永不过期。如果数据库中的权限定义发生变化，缓存不会自动失效。

---

### M05. Redis SCAN 在全量会话查询中代价高昂

**文件:** `modules/sys/session/service.go:21-36`
**类型:** 性能

`scanKeys` 使用 Redis `SCAN` 每批 200 个遍历所有会话键。在生产环境中数千个会话键的场景下，`SCAN` 消耗显著的 Redis CPU。

---

### M06. 会话集不清理过期令牌条目

**文件:** `core/auth/base_auth.go:143-151`
**类型:** 设计

用户会话使用 Redis Set 存储所有活跃令牌的 JTI。过期令牌条目不会从 Set 中移除，随着时间推移 Set 会持续增长。

---

### M07. NoRepeat 中间件使用 FNV-64a 非加密哈希

**文件:** `core/auth/middleware/norepeat.go:127`
**类型:** 安全

`fnv.New64a()` 是非加密哈希。攻击者可构造哈希碰撞绕过基于重复的检查。对于防止意外重复提交尚可接受，但安全场景应使用 SHA-256。

---

### M08. NoRepeat 使用 context.Background()

**类型:** 高并发

NoRepeat 使用 `context.Background()` 而不是请求上下文。

---

### M09. 登录验证码使用 context.Background()

**文件:** `core/captcha/captcha.go`
**类型:** 高并发

验证码校验使用 `context.Background()`，不是从请求传播上下文。

---

### M10. log.Fatalf 在 goroutine 中绕过优雅关闭

**文件:** `core/app/app.go:83`
**类型:** 设计

在 goroutine 中使用 `log.Fatalf` 会直接终止进程，绕过任何优雅关闭逻辑。

---

### M11. 无密码历史记录或轮换策略

**类型:** 安全

- 没有防止最近密码重复使用的机制
- 没有强制定期密码轮换
- 没有检查密码是否泄露

---

### M12. CORS 完全基于配置无验证

**文件:** `core/middleware/cors.go`
**类型:** 安全

CORS 设置完全来自 `config.C.CORS`，没有积极限制。可能在生产配置中设置通配符 `*` 或过于宽松的来源。

---

### M13. 认证中间件绕过 result.Failure()，缺少 trace_id

**文件:**
- `core/auth/middleware/check_login.go:27`
- `core/auth/middleware/check_permission.go:45,53,59`
- `core/auth/middleware/check_role.go:45,53,59`
- `core/middleware/auth_check.go:73,83`
- `core/auth/middleware/norepeat.go:59`
**类型:** API设计

所有认证中间件直接返回裸 `gin.H`，产生的响应缺少 `trace_id` 字段，影响调试。

---

### M14. 客户端会话路由使用 B 端权限

**文件:** `modules/client/session/api/v1/api.go:15,20,26,32,38,44`
**类型:** 设计

客户端会话管理路由使用管理员权限 `sys:session:*`，意味着管理员可以强制登出客户端用户。虽然可能是有意设计，但文档不明确。

---

### M15. 多处静默丢弃错误

**文件:**
- `modules/sys/user/service.go:566-568,571-573,656-658`（删除用户角色/权限关系）
- `modules/sys/file/service.go:237`（`os.Remove` 错误丢弃）
- `modules/sys/resource/service.go:545-558`（syncPermission 删除/创建）
- `core/log/syslog.go:112`（日志持久化失败）
**类型:** 错误处理

使用 `_, _ = db.Client.X.Delete()` 或 `_ = os.Remove()` 等模式静默丢弃错误，掩盖了潜在问题。

---

### M16. 系统 IP 泄露风险

**文件:** `modules/sys/analyze/service.go:180-191`
**类型:** 安全

`getLocalIP()` 通过仪表盘暴露服务器内网 IP。虽然通常风险较低，但结合其他信息可辅助攻击者了解网络拓扑。

---

### M17. 存储引擎未实现，返回 LocalStorage

**文件:** `modules/sys/file/service.go:248-250`
**类型:** 设计

同 H16，`getStorage` 忽略 `engine` 参数，仅支持本地存储。此条目保留以标记为设计缺陷。

---

### M18. `/docs`、`/redoc`、`/openapi.json` 等路径暴露

**文件:** `core/middleware/auth_check.go:14-19`
**类型:** 安全

API 文档路径被列为无认证静态路径。生产环境中 OpenAPI 规范暴露详细 API 结构，增加攻击面。

---

### M19. resource 模块 ShouldBindQuery 错误处理不一致

**文件:** `modules/sys/resource/api/v1/api.go:80-84`
**类型:** API设计

绑定失败时静默捕获并应用默认值，而不是返回错误响应，与其他模块行为不一致。

---

### M20. session 分页全量加载到内存再分页

**文件:** `modules/sys/session/service.go:138-181`
`modules/client/session/service.go:131-173`
**类型:** 性能

会话分页获取所有会话数据后在内存中分页，对于数千会话的系统扩展性差。

---

### M21. BusinessError 缺少错误链/堆栈跟踪

**文件:** `core/exception/business_error.go`
**类型:** 错误处理

BusinessError 仅包装字符串消息，没有使用 `fmt.Errorf("...%w")` 或捕获堆栈跟踪，生产问题难以定位根本原因。

---

### M22. 验证码无 Redis 回退时静默通过

**文件:** `core/captcha/captcha.go:141-144`
**类型:** 安全

Redis 未初始化时验证码检查直接通过（`return nil`），虽然已添加警告日志，但仍存在安全风险。

---

### M23. Health Check 无深度探测（新增）

**文件:** `core/app/health.go:8-13`
**类型:** 监控

健康端点只返回应用名称和版本，不检查数据库连接（MySQL ping）、Redis 连接及任何下游依赖。没有独立的存活/就绪探针用于 Kubernetes 部署。

---

### M24. 分析仪表板为月度趋势加载所有用户（新增）

**文件:** `modules/sys/analyze/service.go:88-130`
**类型:** 性能

`getUserTrend` 和 `getClientUserTrend` 通过 `Query().All(ctx)` 加载所有用户记录到内存中进行按月聚合。对于大型数据集会使用高内存。应使用数据库级 GROUP BY 查询。

---

### M25. Resource collectDescendantIDs 使用 O(层级) 查询（新增）

**文件:** `modules/sys/resource/service.go:482-520`
**类型:** 性能

循环中每个父节点执行一次子资源查询。对比：`modules/sys/dict/service.go` 的 `dictCollectDescendantIDs` 正确使用单次加载所有记录 + 内存 map 遍历。

---

### M26. Dict/User Modify nil 语义不一致（新增）

**文件:** `modules/sys/dict/service.go:222-241` vs `modules/sys/user/service.go:516-536`
**类型:** API设计

- Dict Modify: `vo.Label == nil` → 清空该字段（`ClearLabel()`）
- User Modify: `vo.OrgID == nil` → 不触碰该字段

同一 JSON body 发送到不同端点产生不同行为。

---

### M27. RemoveAbsolute 磁盘/DB 删除顺序可能导致不一致（新增）

**文件:** `modules/sys/file/service.go:235-244`
**类型:** 业务逻辑

先删除磁盘文件（第 235-239 行），再删除 DB 记录（第 241 行）。如果磁盘删除成功后 DB 删除失败，文件已丢失但 DB 记录仍存在，状态不一致。

---

### M28. 下载路径遍历保护依赖 CWD（新增）

**文件:** `modules/sys/file/service.go:113-123`
**类型:** 安全

```go
uploadsAbs, err := filepath.Abs("uploads")
```

安全路径检查依赖进程工作目录。如果从不同目录启动，验证可能过于宽松或严格。

---

### M29. batchGetRoleIDs panic 而非返回错误（新增）

**文件:** `modules/sys/user/service.go:166-167`
**类型:** 错误处理

```go
if err != nil {
    panic(exception.NewBusinessError("查询角色关系失败: "+err.Error(), 500))
}
```

函数签名返回 `map[string][]string` 无 error，数据库失败直接 panic，使其无法在非 panic-recovery 上下文（测试、后台任务）中使用。

---

### M30. DictGetLabel 在值重复时静默返回首个匹配（新增）

**文件:** `modules/sys/dict/service.go:311-323`
**类型:** 业务逻辑

同父下多个字典项拥有相同 `Value`（重复检查的 parent_id 问题已覆盖），只返回第一个标签，无歧义指示。

---

### M31. 客户端用户注册存在 TOCTOU 竞态（新增）

**文件:** `modules/client/user/service.go:158-163`
**类型:** 高并发

```go
exists, err := db.Client.ClientUser.Query().Where(clientuser.UsernameEQ(param.Username)).Exist(ctx)
if err == nil && exists {
    panic(exception.NewBusinessError("账号已存在", 400))
}
```

存在检查和 `Save()` 之间另一个请求可能创建相同用户名。数据库唯一约束会捕捉，但错误信息将是通用"添加用户失败"。

---

### M32. syncDictCache 误导性错误消息（新增）

**文件:** `modules/sys/dict/service.go:368`
**类型:** 错误处理

```go
panic(exception.NewBusinessError("校验字典循环引用失败: "+err.Error(), 500))
```

实际失败是数据库查询错误，但消息说"循环引用检查失败"，对调试产生误导。

---

### M33. ModuleRemove 未检查关联资源（新增）

**文件:** `modules/sys/resource/service.go:163-171`
**类型:** 业务逻辑

模块可在存在关联资源时被删除，导致资源孤立或无法访问。

---

### M34. 无日志级别（只有 Printf/Fatalf）（新增）

**类型:** 日志

所有日志使用 `log.Printf`（Info 级别）。没有 `Warn`、`Error`、`Debug`。`log.Fatalf` 用于致命错误（终止进程）。严重性不同的错误无法区分。

---

### M35. 生产中无 SQL 查询日志（新增）

**文件:** `core/db/ent.go`
**类型:** 监控

ent 客户端在生产模式下未启用调试日志。没有慢查询日志记录或查询性能监控。

---

### M36. 记录的错误缺乏堆栈/调用者信息（新增）

**类型:** 日志

标准库 `log` 不提供堆栈跟踪或调用者信息。错误消息无法追溯特定代码行。

---

### M37. 配置中缺失日志/监控部分（新增）

**文件:** `config/config.go`
**类型:** 配置

配置结构缺少日志配置（级别、路径、格式、轮转）和监控配置（指标端口、追踪导出器）。

---

### M38. UserRemove 关联关系删除错误被静默丢弃（与 M15 重叠，独立编号）

**文件:** `modules/sys/user/service.go:566-568`
**类型:** 业务逻辑

```go
_, _ = db.Client.RelUserRole.Delete().Where(reluserrole.UserIDIn(ids...)).Exec(ctx)
_, _ = db.Client.RelUserPermission.Delete().Where(reluserpermission.UserIDIn(ids...)).Exec(ctx)
```

删除失败时用户仍被移除，关联表留下孤立记录。

---

## 4. 低危缺陷 (Low)

### L01. 死代码: `var _ = json.Valid` (已修复)

**文件:** `modules/sys/config/service.go`（已在 Phase 1 修复）

### L02. 死存储: `_ = delCount`

**文件:** `modules/sys/user/service.go:309`
**类型:** 代码质量

来自第 292 行的 `delCount` 赋值被空白标识符丢弃。

### L03. GroupUnionTree 死代码变量（新增）

**文件:** `modules/sys/group/service.go:218-220`
**类型:** 代码质量

```go
existingChildren := orgNode["children"].([]map[string]interface{})
orgNode["children"] = append(..., children...)
_ = existingChildren
```

`existingChildren` 被计算但从未使用，下一行的 append 重新读取 `orgNode["children"]`。

### L04. DictCreate 未验证父节点存在（新增）

**文件:** `modules/sys/dict/service.go:142-175`
**类型:** 业务逻辑

如果 `vo.ParentID` 指向不存在的记录，子字典被静默创建为孤立引用。

### L05. 响应状态码字面量不一致

**文件:** 多处
**类型:** 代码质量

有些模块使用 `http.StatusOK`，其他模块使用字面量 `200`。

### L06. 健康检查使用裸 `gin.H`

**文件:** `core/app/health.go:9`
**类型:** API设计

健康检查端点返回裸 `gin.H`，没有 `code`、`success`、`trace_id` 字段。

### L07. Remove 注释错误标注为"软删除"（新增）

**文件:** `modules/sys/file/service.go:209`
**类型:** 文档

```go
// Remove soft-deletes file records by IDs.
```
实际是硬删除（`db.Client.SysFile.Delete()`）。

### L08. 连接池配置可优化（新增）

**文件:** `core/db/ent.go:29-32`
**类型:** 配置

`MaxIdleConns = PoolSize = 20`，低流量时可能浪费资源。`ConnMaxLifetime = 3600s` 可适当降低（如 1800s）以更频繁循环连接。

### L09. Snowflake 实例 ID 固定为 1（新增）

**文件:** `core/utils/snowflake.go`
**类型:** 高可用

Snowflake 生成器使用 `instance: 1`（来自配置）。单实例部署无问题，多实例部署如无唯一实例 ID 可能导致 ID 碰撞。

### L10. 配置全局可变单例（新增）

**文件:** `config/config.go:79`
**类型:** 设计

```go
var C *Config
```

全局可变状态使测试复杂化，并导致包初始化排序问题。

### L11. 安全扫描工具缺失（新增）

**类型:** 工具链

项目中没有 `govulncheck`、`gosec` 或 `staticcheck` 的工具集成或 CI 配置。

### L12. Password 模块级 svcCtx（新增）

**文件:** `modules/client/session/service.go:21`
**类型:** 代码质量

```go
var svcCtx = context.Background()
```
包级别的可变 context.Background() 不符合 Go 惯例。

---

## 5. 已修复项 (Phase 1)

以下缺陷已在 Phase 1 修复中处理：

| # | 缺陷 | 文件 | 修复内容 |
|---|------|------|----------|
| F01 | Recovery 中间件顺序错误 | `core/app/app.go` | Recovery → Trace → AuthCheck → CORS |
| F02 | GetTraceID 始终返回空 | `core/utils/trace.go` | 改为从 Gin Context 读取 |
| F03 | RecordAuthLog 只打印不存库 | `core/log/record.go` | 实现 DB 持久化 |
| F04 | loginUser 始终为 "-" | `core/auth/middleware/check_login.go` | 设置 loginUser context 键 |
| F05 | AbortWithStatusJSON 后缺 return | `core/auth/permission_tool.go` | 在 4 个位置添加 return |
| F06 | 权限中间件每请求重复注册 | `core/auth/middleware/check_permission.go` | 移除 for-range 注册循环 |
| F07 | sort.Slice(nil) panic | `modules/sys/session/service.go` | 增加 nil 检查 |
| F08 | SM2 解密后 bcrypt 哈希缺失 | `modules/sys/user/service.go` | 添加 bcrypt 哈希处理 |
| F09 | MinIO 错误码误判 | `core/storage/minio.go` | 仅忽略 NoSuchKey |
| F10 | 文件上传无大小限制 | `modules/sys/file/service.go` | 添加 UploadMaxSize 检查 |
| F11 | 文件下载路径遍历 | `modules/sys/file/service.go` | 添加 filepath.Abs 验证 |
| F12 | Dict/analyze 错误被静默忽略 | `modules/sys/dict/service.go`, `modules/sys/analyze/service.go` | 改为 panic/log |
| F13 | .gitignore 缺少配置/上传 | `.gitignore` | 添加 config.yaml, uploads/ |
| F14 | 配置示例缺失 | `config.example.yaml` | 创建模板文件 |
| F15 | 验证码 Redis 不可用时静默 | `core/captcha/captcha.go` | 添加警告日志 |
| F16 | Dict 缓存序列化错误忽略 | `modules/sys/dict/service.go` | 添加 json.Marshal 错误检查 |
| F17 | 循环引用检查静默返回 | `modules/sys/dict/service.go` | 改为 panic |
| F18 | config 死代码 | `modules/sys/config/service.go` | 移除未使用的 import 和死代码 |

---

## 6. 汇总统计

| 严重级别 | 已修复 | 待修复 | 合计 |
|----------|--------|--------|------|
| **严重 (Critical)** | 0 | 11 | 11 |
| **高危 (High)** | 0 | 26 | 26 |
| **中危 (Medium)** | 0 | 38 | 38 |
| **低危 (Low)** | 1 | 11 | 12 |
| **合计** | **18** | **86** | **104** |

### 按类别分布

| 类别 | 数量 | 主要问题 |
|------|------|----------|
| 安全 (Security) | 18 | 无速率限制、会话未失效、Token 无 exp、密钥泄露、外键禁用 |
| 性能 (Performance) | 14 | Context.Background() 滥用、N+1 查询（4处）、全量内存分页、缺少监控指标 |
| 高并发 (Concurrency) | 8 | permissionRegistry 无锁、关闭竞态、无 Sentinel/Cluster、TOCTOU 竞态 |
| 数据一致性 (Data Integrity) | 8 | 无事务包裹、外键禁用、无唯一约束、删除顺序、缓存漂移 |
| 错误处理 (Error Handling) | 10 | 静默丢弃错误（7处）、BusinessError 无堆栈、误导性消息 |
| 日志/监控 (Logging) | 12 | 无结构化日志、审计日志静默丢弃、防篡改未调用、无延迟监控 |
| API 设计 (API Design) | 8 | 无验证标签、无分页上限、日期解析静默忽略、nil 语义不一致 |
| 业务逻辑 (Business Logic) | 6 | 重复检查遗漏 parent_id、Modify 缓存只清旧键、Remove 顺序问题 |
| 代码质量 (Code Quality) | 7 | 死代码、状态码不一致、注释错误、包级变量 |
| 配置/依赖 (Config) | 6 | 密钥来自 YAML、全局单例、无日志配置、x/net 过时 |

---

## 7. 修复优先级建议

### 第一优先级（影响安全性）
1. **C04** - 登录端点添加速率限制
2. **C05/C06** - 密码更改后踢出会话
3. **C02** - Token 添加 exp 声明
4. **C07** - 文件上传添加类型验证
5. **H01/H08** - 密钥从环境变量/密钥管理服务读取
6. **H09/H10** - 添加暴力破解防护和全局速率限制
7. **C10/C11** - 审计日志防篡改和错误处理

### 第二优先级（影响系统可用性/数据一致性）
8. **C01** - Context 传播改造（影响最大、范围最广）
9. **C09** - 所有多表操作添加事务包裹
10. **C03** - Redis 故障降级机制
11. **H02/H03** - 权限缓存
12. **H18** - 重新考虑外键策略
13. **H19** - 添加唯一约束
14. **M10** - 优雅关闭修复

### 第三优先级（影响性能）
15. **H04/H05/H25** - N+1 查询优化（4处）
16. **M02** - 文件上传流式处理
17. **M05/M20** - 会话扫描和分页优化
18. **M03/M04** - 缓存策略优化
19. **M24** - 仪表板月度趋势改用 SQL GROUP BY

### 第四优先级（代码质量与监控）
20. **H20/H21/H23** - 添加 Prometheus/promf/结构化日志
21. **M13/M19/M26** - API 响应一致性
22. **M15/M21/M29/M32/M38** - 错误处理改进
23. **H11** - Recovery 中间件加 return
24. **L01-L12** - 代码清理与文档修复
