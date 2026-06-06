# 架构概述

## 应用初始化流程

Hei Gin 的应用启动流程由 `sdk/app/app.go` 中的应用工厂编排，遵循清晰的初始化顺序：

```
config.FindAndLoad() ──── 搜索并加载 config.yaml（HEI_CONFIG env → CWD → 父目录 → go.work 目录）
    │
    ▼
db.InitDB() ────────────── GORM 初始化（MySQL 连接池：MaxOpenConns = PoolSize + MaxOverflow）
    │
    ▼
db.InitRedis() ─────────── go-redis 初始化（连接池、超时、健康检查）
    │
    ▼
module.InitAll() ───────── 所有插件模块的 Init() 调用
    │                        ├── auth 模块：初始化 Business + Consumer Token 配置
    │                        ├── scheduler 模块：无操作
    │                        ├── plugin-sys：注册 PermissionDelegate、初始化日志持久化
    │                        ├── plugin-client：客户端插件初始化
    │                        └── plugin-im：IM 插件初始化
    │
    ▼
gin.New() ──────────────── 创建 Gin 引擎（非 gin.Default()，避免内置 Recovery）
    │
    ▼
全局 Middleware 注册 ─────── 按顺序：
    │                        ① Recovery（最外层，捕获所有 panic）
    │                        ② Logger（Gin 内置）
    │                        ③ Trace（trace_id 注入）
    │                        ④ CORS（跨域配置）
    │                        ⑤ AuthCheck（认证路由分流）
    │
    ▼
registry.ApplyMiddlewares() ─ 执行模块注册的额外中间件
    │
    ▼
SetupRouters() ──────────── 执行所有模块通过 registry.RegisterRoute 注册的路由
    │                         ├── 健康检查 GET /
    │                         └── 各模块 API 路由
    │
    ▼
module.StartAll() ───────── 启动所有模块（auth → RunPermissionScan, scheduler → cron.Start）
    │
    ▼
HTTP Server 启动 ────────── 监听端口，Enter 循环
    │
    ▼
SIGINT/SIGTERM 信号 ─────── 优雅关闭
                             ├── Server Shutdown（15s 超时）
                             ├── module.StopAll()（逆序停止）
                             ├── db.Close()
                             └── db.CloseRedis()
```

## 请求生命周期

一个完整的 HTTP 请求处理流程：

```
客户端请求
    │
    ▼
① Recovery 中间件（defer）
    ├─ 捕获 panic → JSON 响应
    │   ├─ *exception.BusinessError → 200 + {code, message, success: false}
    │   └─ 其他 panic → 200 + {code:500, message:"服务器内部错误"} + 日志栈追踪
    └─ c.Next() 后检查 c.Errors → 400 响应
    │
② Logger 中间件（Gin 内置）
    └─ 请求日志记录
    │
③ Trace 中间件
    ├─ 读取请求头 trace_id（原样透传），不存在则生成
    ├─ 不存在则生成 UUID 无连字符格式
    └─ 设置到 gin.Context("trace_id")
    │
④ CORS 中间件（gin-contrib/cors）
    ├─ config.yaml cors 配置
    └─ OPTIONS 预检自动处理
    │
⑤ AuthCheck 中间件
    ├─ 静态路径（/favicon.ico, /docs 等）→ 放行
    ├─ WebSocket 路径（/ws 后缀）→ 放行（由 WS Handler 自行认证）
    ├─ OPTIONS 请求 → 放行
    ├─ /api/v{n}/public/* → 放行（无需认证）
    ├─ /api/v{n}/c/* → consumer.IsLogin() 检查
    └─ 其他 /api/v{n}/* → auth.IsLogin() 检查（B 端）
    │
⑥ 路由匹配 → 路由组中间件链
    ├─ HeiCheckLogin()        → 从 Header 取 Token → Redis 查询会话
    ├─ HeiCheckPermission()   → 通过 PermissionDelegate 查询用户权限 → 通配符匹配
    ├─ SysLog("操作名")        → 录制请求参数、响应、User-Agent、IP、签名
    └─ NoRepeat(ms)           → 基于 Redis 的请求哈希去重
    │
⑦ 业务 Handler
    ├─ 参数校验（ShouldBindQuery/ShouldBindJSON）
    ├─ 业务逻辑（panic-based 异常处理）
    └─ 统一响应 result.Success / result.PageDataResult / result.Failure
    │
    ▼
客户端收到 JSON 响应
```

## 双端认证架构

```
                      ┌──────────────────────┐
                      │    客户端请求          │
                      └──────────┬───────────┘
                                 │
                                 ▼
                      ┌──────────────────────┐
                      │   AuthCheck 中间件     │
                      │   基于路径自动分流      │
                      └─────┬────────────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
  ┌─────────────────────┐   ┌─────────────────────┐
  │  B 端 (BUSINESS)     │   │  C 端 (CONSUMER)     │
  │                     │   │                     │
  │  auth.Business      │   │  auth.Consumer      │
  │  (baseAuthTool 实例) │   │  (baseAuthTool 实例) │
  │                     │   │                     │
  │  Redis 键前缀:       │   │  Redis 键前缀:       │
  │  hei:auth:BUSINESS: │   │  hei:auth:CONSUMER: │
  │                     │   │                     │
  │  Token: 32字节随机   │   │  Token: 32字节随机   │
  │  hex 字符串          │   │  hex 字符串          │
  │                     │   │                     │
  │  /api/v1/public/b/* │   │  /api/v1/public/c/* │
  │  /api/v1/sys/*      │   │  /api/v1/c/*        │
  │  /api/v1/b/*        │   │                     │
  │                     │   │                     │
  │  → 后台管理员        │   │  → 普通用户          │
  └─────────────────────┘   └─────────────────────┘
```

## Token 认证机制

**不是 JWT**。Token 是 32 字节随机 hex 字符串，无签名/加密。

```
登录成功：
  1. crypto/rand 生成 32 字节随机数 → hex 编码（64 字符）
  2. 构建 Token 数据 JSON：
     {"user_id": "snowflake-id", "type": "BUSINESS",
      "created_at": "2026-01-15 10:00:00", "extra": {...}}
  3. 存储到 Redis：SETEX hei:auth:BUSINESS:token:{token} {json} {expire}
  4. 用户会话集：SADD hei:auth:BUSINESS:session:{userID} {token}
  5. 返回 token 字符串给客户端

请求验证：
  1. 从请求头 Authorization（可配置）中提取 token
  2. Redis GET hei:auth:BUSINESS:token:{token}
  3. 存在 → 有效，不存在 / 过期 → 401

登出 / 踢下线：
  1. DEL hei:auth:BUSINESS:token:{token}
  2. SREM hei:auth:BUSINESS:session:{userID} {token}
```

## Redis 键结构

```
hei:auth:{BUSINESS|CONSUMER}:token:{tokenHex}      → String: Token 数据 JSON
hei:auth:{BUSINESS|CONSUMER}:session:{userID}       → Set: 用户的所有活跃 token
hei:auth:{BUSINESS|CONSUMER}:disable:{loginID}      → String: 禁用标记（1）
hei:permission:keys                                 → String: 权限树 JSON（TTL=0 永不过期）
hei:dict:tree                                       → String: 数据字典树
hei:dict:fulltree                                   → String: 完整字典树
{BUSINESS|CONSUMER}:captcha:{captchaId}              → String: 验证码（300s TTL）
norepeat:{ip}:{userID}:{path}                        → String: 防重复指纹 JSON（hash + timestamp）
```

## 中间件体系

全局中间件注册顺序（`sdk/app/app.go`）：

```
① Recovery（sdk/middleware/recovery.go）— 最外层
② Logger（gin.Logger）
③ Trace（sdk/middleware/trace.go）
④ CORS（sdk/middleware/cors.go）
⑤ AuthCheck（sdk/middleware/auth_check.go）
```

业务中间件（注册在路由组，位于 `sdk/auth/middleware/`）：

- **HeiCheckLogin(loginType...)** — Token 登录验证，设置 `loginUser` 到 Context
- **HeiClientCheckLogin()** — C 端登录验证（委托给 HeiCheckLogin("CONSUMER")）
- **HeiCheckPermission(permissions []string, mode ...string)** — 权限检查（AND/OR 模式）
- **HeiClientCheckPermission(permissions []string, mode ...string)** — C 端权限检查
- **HeiCheckRole(roles []string, mode ...string)** — 角色检查
- **HeiClientCheckRole(roles []string, mode ...string)** — C 端角色检查
- **SysLog(name string)** — 操作日志录制（位于 `sdk/log/`）
- **NoRepeat(interval int)** — 防重复提交（interval 毫秒）

## 异常处理机制

```go
import "hei-gin/sdk/exception"

// 业务层抛出异常
panic(exception.NewBusinessError("用户名已存在", 400))
// 或直接使用结构体
panic(&exception.BusinessError{Message: "错误", Code: 400})

// Recovery 中间件捕获（sdk/middleware/recovery.go）：
//   *BusinessError → 200 + {code:400, message:"用户名已存在", success:false}（无栈追踪日志）
//   error          → 200 + {code:500, message:"服务器内部错误"}（日志记录栈追踪）
//   其他 panic     → 200 + {code:500, message:"服务器内部错误"}（日志记录栈追踪）
//   c.Errors       → 200 + {code:400, message: err.Error(), success:false}
```

## 安全特性

1. **密码传输加密**：SM2 国密 C1C3C2 模式加密（前端使用公钥加密，后端私钥解密）
2. **密码存储**：bcrypt 加盐哈希
3. **防篡改日志**：SM3 哈希 + 盐值 "hei-log-sign" 签名
4. **服务端会话**：Token 存储在 Redis，服务端可主动失效
5. **防暴力破解**：Disable 机制临时禁止登录（Redis TTL）
6. **防重复提交**：NoRepeat 中间件（请求哈希 + 时间窗口）
7. **验证码**：图形验证码 Redis 存储 300s TTL
