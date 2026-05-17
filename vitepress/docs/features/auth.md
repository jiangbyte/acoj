# 认证体系

Hei Gin 实现了双端认证体系，B 端（管理后台）和 C 端（客户端）使用独立的认证工具实例和独立的 Redis 键空间，但共享同一个 JWT 配置。

## 双端认证设计

### 设计理念

B 端和 C 端是两种完全不同的用户群体，有着不同的安全要求和业务逻辑：

- **B 端用户**：系统管理员、运营人员，数量少、权限大，需要严格的权限管控
- **C 端用户**：平台普通用户，数量多、权限小，需要高并发支持

因此 Hei Gin 将两端的认证逻辑完全隔离，互不干扰。

### 认证工具对比

| 维度 | B 端（BUSINESS） | C 端（CONSUMER） |
|------|------------------|-----------------|
| 实现方式 | 包级函数（全局） | 结构体方法（实例） |
| 文件 | `core/auth/auth_tool.go` | `core/auth/client_auth_tool.go` |
| 类型 | 包级函数 | `HeiClientAuthTool` 结构体 |
| JWT Secret | `config.C.JWT.SecretKey` | `config.C.JWT.SecretKey`（**相同配置**） |
| Redis 键前缀 | `hei:auth:BUSINESS:` | `hei:auth:CONSUMER:` |
| 认证中间件 | `middleware.AuthCheck()`（统一路由分流） | 同上 |

> **注意**：B 端和 C 端使用同一份 JWT 配置（`config.yaml` 中的 `jwt` 段），包括 `secret_key`、`expire_seconds`、`token_name`、`algorithm`。没有独立的 b_side/c_side JWT 密钥。

## JWT 会话管理

### Token 设计

每个登录会话生成一个 **单一 JWT Token**，没有 Access/Refresh Token 对。Token 的 `jti` 声明唯一标识该令牌，`iat` 记录签发时间。

### 会话存储

JWT 会话信息存储在 Redis 中，数据结构如下：

```
Redis Key: hei:auth:{BUSINESS|CONSUMER}:token:{signedToken}
Redis Value (JSON):
{
  "user_id": "snowflake-id",
  "type": "BUSINESS",
  "created_at": "2006-01-02 15:04:05",
  "extra": { ... }
}
```

同一用户的令牌通过 Redis Set 管理，用于批量踢下线：

```
Redis Key: hei:auth:{BUSINESS|CONSUMER}:session:{userID}
Redis Value: 多个 signedToken（Redis Set）
Redis TTL: 等于 token 过期时间
```

### 令牌禁用

当用户主动登出或被踢下线时：

1. 从 `session:{userID}` 的 Set 中移除该 token
2. 删除 `token:{signedToken}` 键值对
3. Token 立即失效（下次请求时无法从 Redis 获取数据）

- **Disable 机制**：可临时禁止某个 loginID 登录（如密码错误多次），使用独立的 Redis 键记录。

## 核心 API

### B 端认证 API（包级函数）

```go
import authx "hei-gin/core/auth"

// 登录：通过用户 ID 签发 JWT，存储会话到 Redis
// 参数：c *gin.Context, id string, extra map[string]any
// 返回：signedToken string, error
token, err := authx.Login(c, "user-id", map[string]any{"role": "admin"})

// 登出：销毁当前请求的 Token
// 如果传入 loginID，则调用 Kickout 踢掉该用户所有会话
authx.Logout(c)
authx.Logout(c, "user-id")  // 踢掉指定用户所有会话

// 踢下线：删除指定用户的全部 Token 和会话数据
authx.Kickout("user-id")

// 检查当前请求是否已登录
isLogin := authx.IsLogin(c)

// 获取当前请求对应的登录用户 ID（为空表示未登录）
userID := authx.GetLoginID(c)
userID := authx.GetLoginIDDefaultNull(c)  // 同上，语义更明确

// 从指定 Token 中解析用户 ID
userID := authx.GetLoginIDByToken(tokenString)

// 获取 Token 值（从请求头中提取）
token := authx.GetTokenValue(c)

// 获取当前请求的完整 Token 数据
data := authx.GetTokenInfo(c)       // 从 Redis 获取完整 payload
session := authx.GetSession(c)      // 等同 GetTokenInfo

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

### C 端认证 API（结构体方法）

```go
import clientAuth "hei-gin/core/auth"

tool := clientAuth.NewHeiClientAuthTool()

// 登录
token, err := tool.Login(c, "user-id", map[string]any{"nickname": "Tom"})

// 登出
tool.Logout(c)
tool.Logout(c, "user-id")          // 踢掉指定用户所有会话

// 踢下线
tool.Kickout("user-id")

// 检查是否已登录
isLogin := tool.IsLogin(c)

// 获取当前登录用户 ID
userID := tool.GetLoginID(c)
userID := tool.GetLoginIDDefaultNull(c)

// 从指定 Token 中解析用户 ID
userID := tool.GetLoginIDByToken(tokenString)

// 获取 Token 值
token := tool.GetTokenValue(c)

// 获取 Token 数据
data := tool.GetTokenInfo(c)
session := tool.GetSession(c)
val := tool.GetExtra(c, "role")

// 刷新过期时间
tool.RenewTimeout(c)
tool.RenewTimeout(c, 7200)

// 获取 TTL
ttl := tool.GetTokenTimeout(c)
ttl := tool.GetSessionTimeout(c)

// 获取用户 Token
token := tool.GetTokenValueByLoginID("user-id")
tokens := tool.GetTokenValuesByLoginID("user-id")

// 禁用 / 检查
tool.Disable("user-id", 300)
ok := tool.IsDisable("user-id")
err := tool.CheckDisable("user-id")
ttl := tool.GetDisableTime("user-id")
tool.UntieDisable("user-id")

// 获取登录类型标识
loginType := tool.GetLoginType()  // 返回 "CONSUMER"
```

## 登录流程

完整的登录流程涉及多个步骤：

```
前端                          后端
 │                             │
 ├── 1. 获取验证码 ──────────► │  GET /api/v1/public/b/captcha
 │◄── 返回验证码图片 ──────────┤
 │                             │
 ├── 2. 获取 SM2 公钥 ───────► │  GET /api/v1/public/b/sm2-public-key
 │◄── 返回公钥 ────────────────┤
 │                             │
 ├── 3. 加密密码 ──────────────┤  前端使用公钥 SM2 加密密码
 │                             │
 ├── 4. 提交登录 ────────────► │  POST /api/v1/public/b/login
 │   {                        │  {
 │     captcha_id,            │    验证码校验
 │     captcha_value,         │    SM2 私钥解密密码
 │     username,              │    bcrypt 比对密码
 │     password(加密后)        │    生成 JWT Token（单一 Token）
 │   }                        │    存储 Redis 会话
 │◄── 返回 Token ─────────────┤    返回 JWT Token
 │                             │
 ├── 5. 携带 Token 请求 API ─► │  Authorization: Bearer <token>
 │                             │  AuthCheck 中间件自动验证
 │                             │
```

## 认证路由分流

`core/middleware/auth_check.go` 中的 `AuthCheck` 中间件根据请求路径的前缀自动识别认证上下文：

```go
import "hei-gin/core/middleware"

r.Use(middleware.AuthCheck())

// 路由分流逻辑（中间件内部实现）：
//   /favicon.ico, /docs, /redoc, /openapi.json, /v3/api-docs → 无需认证
//   OPTIONS 请求 → 无需认证
//   /api/v{n}/public/* → 无需认证
//   /api/v{n}/c/* → 使用 HeiClientAuthTool.IsLogin 检查
//   其他 /api/v{n}/* → 使用 auth.IsLogin（B 端）检查
```

`AuthCheck` 使用正则 `^/api/v\d+/([^/]+)/` 提取路径第一个分段，然后按以下规则处理：

1. **静态路径**（`/favicon.ico`、`/docs`、`/redoc`、`/openapi.json`、`/v3/api-docs`）-- 放行
2. **OPTIONS 请求** -- 放行
3. **`/api/v{n}/public/*`** -- 放行（无需认证）
4. **`/api/v{n}/c/*`** -- 要求客户端认证（401 未登录则中断）
5. **其他 `/api/v{n}/*`**（包括 `/api/v{n}/b/*`、`/api/v{n}/sys/*` 等） -- 要求 B 端认证（401 未登录则中断）

```go
import (
    "hei-gin/core/middleware"
    "hei-gin/core/auth"
    "github.com/gin-gonic/gin"
)

// 使用统一认证中间件（推荐）
r := gin.Default()
r.Use(middleware.AuthCheck())

// 无需在路由组中重复添加检查——AuthCheck 已自动分流
bApi := r.Group("/api/v1/b")
{
    bApi.GET("/user/info", handler.GetUserInfo)
    bApi.POST("/user/update", handler.UpdateUser)
}

cApi := r.Group("/api/v1/c")
{
    cApi.GET("/user/profile", handler.GetProfile)
}

publicB := r.Group("/api/v1/public/b")
{
    publicB.GET("/captcha", handler.Captcha)
    publicB.GET("/sm2-public-key", handler.Sm2PublicKey)
    publicB.POST("/login", handler.Login)
}
```

## 会话管理

系统管理员可以通过会话管理模块查看和操作在线用户：

- **查看在线用户**：通过 Redis 扫描 `hei:auth:BUSINESS:session:*` 或 `hei:auth:CONSUMER:session:*` 列出活跃 session
- **强制下线**：`Kickout(loginID)` 踢掉指定用户的所有会话
- **指定 Token 下线**：`KickoutToken(loginID, token)` 删除指定 Token

## 安全特性

1. **密码传输加密**：使用 SM2 国密算法加密密码传输
2. **密码存储哈希**：使用 bcrypt 加盐哈希存储密码
3. **单一 JWT Token**：JWT 签名 + Redis 服务端会话，双重验证
4. **Redis 会话**：服务端会话管理，可主动失效
5. **Token 禁用列表**：登出/踢下线后 Token 立即从 Redis 删除
6. **Disable 机制**：支持按 loginID 临时禁止登录（防暴力破解）
7. **验证码保护**：登录需要图形验证码，防止暴力破解
