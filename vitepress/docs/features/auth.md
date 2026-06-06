# 认证体系

Hei Gin 实现了双端认证体系，B 端（管理后台）和 C 端（客户端）使用独立的 `baseAuthTool` 实例和独立的 Redis 键空间。

## 双端认证设计

B 端和 C 端是两种完全不同的用户群体：

- **B 端用户**：系统管理员、运营人员，数量少、权限大
- **C 端用户**：平台普通用户，数量多、权限小

两端的认证逻辑完全隔离，互不干扰。

### 认证工具对比

| 维度 | B 端（BUSINESS） | C 端（CONSUMER） |
|------|------------------|-----------------|
| 实例变量 | 包级函数（委托给内部 businessAuth）| `auth.Consumer` 变量 |
| 类型 | `*baseAuthTool` | `*baseAuthTool` |
| AuthCheck 路由 | `/api/v1/sys/*`, `/api/v1/b/*` | `/api/v1/c/*` |
| Redis 键前缀 | `hei:auth:BUSINESS:` | `hei:auth:CONSUMER:` |
| Token 配置 | `config.C.Token`（共享配置） | 同上 |

### 认证机制（不是 JWT）

Token 是 **32 字节随机 hex 字符串**，无 JWT 签名、无 HMAC 密钥。认证完全基于 Redis 服务端会话：

1. 登录成功 → `crypto/rand` 生成 32 字节随机数 → hex 编码
2. Token 数据 `{user_id, type, created_at, extra}` 以 JSON 存入 Redis
3. 用户会话集（Redis Set）记录该用户的所有活跃 Token
4. 每次请求从 HTTP 头提取 Token → Redis 查询 → 有效则放行
5. 登出/踢下线 → 从 Redis 删除 Token 数据

## 安装与初始化

认证模块在 `sdk/auth/module.go` 中通过 `init()` 自动注册：

```go
// sdk/auth/module.go（自动执行）
func init() {
    module.Register(&authModule{})
}
```

在 `module.InitAll()` 阶段会自动从 `config.C.Token` 读取配置：

```go
// authModule.Init()
auth.Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)
auth.Consumer.Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)
```

在 `module.StartAll()` 阶段会自动扫描并缓存权限：

```go
// authModule.Start()
auth.RunPermissionScan()
```

## 核心 API

### B 端认证 API（包级函数委托给内部 businessAuth 实例）

```go
import authx "hei-gin/sdk/auth"

// 登录：通过用户 ID 签发 Token，存储会话到 Redis
// 参数：c *gin.Context, id string, extra map[string]any
// 返回：signedToken string, error
token, err := authx.Login(c, "user-id", map[string]any{"role": "admin", "username": "admin"})

// 登出：销毁当前请求的 Token；如果传入 loginID 则踢掉该用户所有会话
authx.Logout(c)
authx.Logout(c, "user-id")  // 踢掉指定用户所有会话

// 踢下线：删除指定用户的全部 Token 和会话数据
authx.Kickout("user-id")

// 踢掉指定用户的指定 Token
authx.KickoutToken("user-id", "token-string")

// 检查当前请求是否已登录
isLogin := authx.IsLogin(c)

// 获取当前请求对应的登录用户 ID
userID := authx.GetLoginID(c)
userID := authx.GetLoginIDDefaultNull(c)

// 从指定 Token 中解析用户 ID
userID := authx.GetLoginIDByToken(tokenString)

// 获取 Token 值（从请求头中提取）
token := authx.GetTokenValue(c)

// 获取当前请求的完整 Token 数据（从 Redis）
data := authx.GetTokenInfo(c)
session := authx.GetSession(c)

// 获取 Token 中的 extra 字段
val := authx.GetExtra(c, "role")

// 刷新 Token 和 Session 的过期时间
authx.RenewTimeout(c)             // 使用默认过期时间
authx.RenewTimeout(c, 7200)       // 指定 7200 秒

// 获取当前 Token 和 Session 的剩余 TTL（秒）
ttl := authx.GetTokenTimeout(c)   // -1 表示无 Token 或已过期
ttl := authx.GetSessionTimeout(c) // -1 表示未登录或已过期

// 获取指定用户的一个或全部 Token
token := authx.GetTokenValueByLoginID("user-id")
tokens := authx.GetTokenValuesByLoginID("user-id")

// 禁用 / 检查 / 解除禁用
authx.Disable("user-id", 300)      // 禁用 300 秒
ok := authx.IsDisable("user-id")   // 是否被禁用
err := authx.CheckDisable("user-id") // 已禁用则返回 error
ttl := authx.GetDisableTime("user-id") // 剩余禁用秒数
authx.UntieDisable("user-id")      // 解除禁用

// 获取登录类型标识
loginType := authx.GetLoginType()  // 返回 "BUSINESS"
```

### C 端认证 API（auth.Consumer 实例方法）

```go
import "hei-gin/sdk/auth"

// auth.Consumer 是预置的 C 端 baseAuthTool 实例
tool := auth.Consumer

// 登录
token, err := tool.Login(c, "user-id", map[string]any{"nickname": "Tom"})

// 登出
tool.Logout(c)
tool.Logout(c, "user-id")

// 踢下线
tool.Kickout("user-id")
tool.KickoutToken("user-id", "token-string")

// 检查
isLogin := tool.IsLogin(c)
loginID := tool.GetLoginID(c)
loginID := tool.GetLoginIDDefaultNull(c)
loginID := tool.GetLoginIDByToken(tokenString)

// 获取 Token
token := tool.GetTokenValue(c)
data := tool.GetTokenInfo(c)
session := tool.GetSession(c)
val := tool.GetExtra(c, "role")

// 刷新过期
tool.RenewTimeout(c)
tool.RenewTimeout(c, 7200)

// TTL
ttl := tool.GetTokenTimeout(c)
ttl := tool.GetSessionTimeout(c)

// 禁用
tool.Disable("user-id", 300)
ok := tool.IsDisable("user-id")
err := tool.CheckDisable("user-id")
ttl := tool.GetDisableTime("user-id")
tool.UntieDisable("user-id")

// 登录类型
loginType := tool.GetLoginType()  // 返回 "CONSUMER"
```

## 登录流程

```
前端                          后端
 │                             │
 ├── 1. 获取验证码 ──────────► │  GET /api/v1/public/b/captcha
 │◄── 返回验证码图片 ──────────┤
 │                             │
 ├── 2. 获取 SM2 公钥 ───────► │  GET /api/v1/public/b/sm2/public-key
 │◄── 返回公钥 ────────────────┤
 │                             │
 ├── 3. 加密密码 ──────────────┤  前端使用公钥 SM2 C1C3C2 模式加密密码
 │                             │
 ├── 4. 提交登录 ────────────► │  POST /api/v1/public/b/login
 │   {                        │  {
 │     captcha_id,            │    验证码校验
 │     captcha_value,         │    SM2 私钥解密密码
 │     username,              │    bcrypt 比对密码
 │     password(加密后)        │    生成随机 Token → 存储 Redis
 │   }                        │    返回 Token 字符串
 │◄── 返回 Token ─────────────┤
 │                             │
 ├── 5. 携带 Token 请求 API ──► │  Authorization: {token}
 │                             │  AuthCheck 中间件自动验证
 │                             │
```

## 认证路由分流

`sdk/middleware/auth_check.go` 中的 `AuthCheck` 中间件根据请求路径自动识别认证上下文：

```go
r.Use(middleware.AuthCheck())

// 路由分流逻辑：
//   /favicon.ico, /docs, /redoc, /openapi.json, /v3/api-docs → 放行
//   /api/v1/sys/dict/tree                                       → 放行
//   OPTIONS 请求                                               → 放行
//   以 /ws 结尾的路径                                           → 放行（WebSocket 自认证）
//   /api/v{n}/public/*                                         → 放行（无需认证）
//   /api/v{n}/c/*                                              → Consumer.IsLogin() 检查
//   其他 /api/v{n}/*                                           → auth.IsLogin() 检查（B 端）
```

AuthCheck 使用正则 `^/api/v\d+/([^/]+)/` 提取路径第一个分段，然后按规则处理。未通过认证的请求返回 401 JSON 响应。

```go
import (
    "hei-gin/sdk/middleware"
    "github.com/gin-gonic/gin"
)

// 使用统一认证中间件（推荐）
r := gin.New()
r.Use(middleware.Recovery())
r.Use(gin.Logger())
r.Use(middleware.Trace())
r.Use(middleware.CORS())
r.Use(middleware.AuthCheck())  // 自动分流 B/C/Public

// 无需手动添加登录检查——AuthCheck 已自动处理
bApi := r.Group("/api/v1/b")
{
    bApi.GET("/user/info", handler.GetUserInfo)
}

cApi := r.Group("/api/v1/c")
{
    cApi.GET("/user/profile", handler.GetProfile)
}

publicB := r.Group("/api/v1/public/b")
{
    publicB.GET("/captcha", handler.Captcha)
    publicB.POST("/login", handler.Login)
}
```

## 会话管理

- **查看在线用户**：通过 Redis 扫描 `hei:auth:BUSINESS:session:*` 或 `hei:auth:CONSUMER:session:*`
- **强制下线**：`auth.Kickout(loginID)` 删除用户所有会话
- **指定 Token 下线**：`auth.KickoutToken(loginID, token)` 删除指定 Token

## Token 会话数据结构

```
=== Token 数据（Redis String）===
Key:   hei:auth:BUSINESS:token:{64-char-hex-token}
Value: {"user_id":"snowflake-id","type":"BUSINESS","created_at":"2026-01-15 10:00:00","extra":{...}}
TTL:   expire_seconds（默认 30 天）

=== 用户会话集（Redis Set）===
Key:   hei:auth:BUSINESS:session:{userID}
Value: [token1, token2, ...] （多个活跃 Token）
TTL:   expire_seconds（每次添加 Token 刷新）

=== 禁用标记（Redis String）===
Key:   hei:auth:BUSINESS:disable:{loginID}
Value: "1"
TTL:   禁用时长（秒）
```
