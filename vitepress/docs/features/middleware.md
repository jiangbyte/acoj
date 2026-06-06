# 中间件体系

Hei Gin 的中间件系统分为全局中间件和业务中间件两层。

## 全局中间件

全局中间件注册在 Gin Engine 上，对所有请求生效。注册顺序见 `sdk/app/app.go`：

```go
r := gin.New()
r.Use(middleware.Recovery())  // ① 最外层：panic → JSON
r.Use(gin.Logger())           // ② 请求日志
r.Use(middleware.Trace())     // ③ 链路追踪
r.Use(middleware.CORS())      // ④ 跨域
r.Use(middleware.AuthCheck()) // ⑤ 认证路由分流
```

### 1. Recovery 中间件

**文件**：`sdk/middleware/recovery.go`

捕获 Gin 处理过程中发生的 panic，转换为友好 JSON 响应。

**处理逻辑**：
- `*exception.BusinessError` → 200 + `{code, message, success:false}`（不记录栈追踪）
- `error` 或其他 panic → 200 + `{code:500, message:"服务器内部错误"}`（日志记录栈追踪）
- `c.Next()` 后的 `c.Errors` → 200 + `{code:400, message: err.Error()}`

```go
import "hei-gin/sdk/exception"

panic(exception.NewBusinessError("用户名或密码错误", 400))
// 响应：{"code": 400, "message": "用户名或密码错误", "success": false, "trace_id": "..."}
```

### 2. Logger 中间件

Gin 内置的日志中间件，记录每个请求的方法、路径、状态码和耗时。

### 3. Trace 中间件

**文件**：`sdk/middleware/trace.go`

生成或透传 `trace_id`。读取请求头的 `trace_id`，不存在则调用 `utils.GenerateTraceID()` 生成新的 UUID（无连字符 hex 编码）。设置到 `gin.Context("trace_id")`。

```go
// 在 Handler 中获取
traceID := c.GetString("trace_id")
```

所有响应自动包含 `trace_id` 字段（通过 `result.Success/Failure` 等函数）。

### 4. CORS 中间件

**文件**：`sdk/middleware/cors.go`

基于 `gin-contrib/cors` 库，配置项来自 `config.yaml` 的 `cors` 段。

```go
// 使用 gin-contrib/cors 库
// AllowOrigins / AllowMethods / AllowHeaders / AllowCredentials 均来自配置
```

### 5. AuthCheck 中间件

**文件**：`sdk/middleware/auth_check.go`

根据请求路径自动识别认证类型：

```
静态路径（/favicon.ico, /docs, /openapi.json 等）         → 放行
/api/v1/sys/dict/tree                                      → 放行
OPTIONS 方法                                                → 放行
以 /ws 结尾的 WebSocket 路径                                 → 放行（由 WS Handler 自认证）
/api/v{n}/public/*                                          → 放行
/api/v{n}/c/*                                               → Consumer.IsLogin() 检查
/api/v{n}/b/* 或 /api/v{n}/<其他>/*                         → auth.IsLogin() 检查
```

未通过认证的请求返回 `{"code":401, "message":"未授权/未登录", "success":false, "trace_id":"..."}`。

WebSocket 路径豁免的原因：WS 连接建立前 AuthCheck 无法从 query 参数中提取 Token，需要在 WS Handler 中使用 `HeiCheckLogin` 中间件手动验证。

## 业务中间件

业务中间件按需注册到具体的路由组上。位于 `sdk/auth/middleware` 包：

```go
import authMiddleware "hei-gin/sdk/auth/middleware"
```

### HeiCheckLogin（B 端登录验证）

**文件**：`sdk/auth/middleware/check_login.go`

```go
func HeiCheckLogin(loginType ...string) gin.HandlerFunc
```

- `loginType` 默认为 `"BUSINESS"`，传入 `"CONSUMER"` 可复用做 C 端检查
- 从 `Authorization` 头（或配置的 `token_name`）提取 Token
- 调用 `auth.IsLogin(c)` 或 `auth.Consumer.IsLogin(c)` 验证
- 验证通过后设置 `loginUser` 到 context（用于操作日志录制）

### HeiClientCheckLogin（C 端登录验证）

**文件**：`sdk/auth/middleware/check_login.go`

```go
func HeiClientCheckLogin() gin.HandlerFunc
```

委托给 `HeiCheckLogin("CONSUMER")`。

### HeiCheckPermission（B 端权限检查）

**文件**：`sdk/auth/middleware/check_permission.go`

```go
func HeiCheckPermission(permissions []string, mode ...string) gin.HandlerFunc
```

- `permissions`：权限代码列表
- `mode`：默认为 `"AND"`（全部匹配），传入 `"OR"` 匹配任意一个
- 检查登录状态 → SUPER_ADMIN 放行 → 通过 PermissionDelegate 查询权限 → 通配符匹配

### HeiClientCheckPermission（C 端权限检查）

**文件**：`sdk/auth/middleware/check_permission.go`

```go
func HeiClientCheckPermission(permissions []string, mode ...string) gin.HandlerFunc
```

委托给 `heiCheckPermissionInner("CONSUMER", permissions, mode)`。

### HeiCheckRole / HeiClientCheckRole（角色检查）

**文件**：`sdk/auth/middleware/check_role.go`

```go
func HeiCheckRole(roles []string, mode ...string) gin.HandlerFunc
func HeiClientCheckRole(roles []string, mode ...string) gin.HandlerFunc
```

- `mode`：`"AND"`（默认，全部匹配）或 `"OR"`（匹配任意一个）

### SysLog（操作日志录制）

**文件**：`sdk/log/syslog.go`

```go
func SysLog(name string) gin.HandlerFunc
```

- `name`：操作名称，如"新增用户"
- 录制内容：请求方法、URL、参数、User-Agent（浏览器+OS）、客户端IP、地理位置、执行状态、trace_id、SM3 签名
- 参数提取：仅 POST/PUT/PATCH 方法，排除 `request`/`db`/`file` 参数
- 异步通过 `LogPersistence` 函数变量写入数据库（由 `plugin-sys` 插件提供实现）
- 异常处理：捕获 handler 中的 BusinessError panic，记录异常日志后重新 panic

### NoRepeat（防重复提交）

**文件**：`sdk/auth/middleware/norepeat.go`

```go
func NoRepeat(interval int) gin.HandlerFunc
```

- `interval`：时间窗口，单位**毫秒**
- 原理：对请求参数（Query + Form + Body）计算 FNV-1a 64 位哈希 → Redis 存储哈希+时间戳 → 窗口内重复返回 429
- 用户标识：优先使用 Consumer 登录 ID，回退到 Business 登录 ID，拼接客户端 IP
- Redis TTL：自动计算，最短 60 秒，最长 3600 秒

## 中间件链完整示例

```go
import (
    "github.com/gin-gonic/gin"
    "hei-gin/sdk/middleware"
    authMiddleware "hei-gin/sdk/auth/middleware"
### RateLimiter（API 限流）

**文件**：`sdk/middleware/ratelimit.go`

```go
func RateLimiter(endpointKey string, window int, maxRequests int) gin.HandlerFunc
```

基于 Redis 的分布式 API 限流中间件，可在路由组上按需注册：

- `endpointKey`：端点标识，用于区分不同的限流规则
- `window`：时间窗口（秒），默认 10 秒
- `maxRequests`：窗口内最大请求数，默认 30
- 用户标识：优先使用登录用户 ID，回退到客户端 IP
- 原理：Redis Lua 脚本原子性 INCR + EXPIRE，首次访问时设置过期时间
- 超出限制：panic `BusinessError{code=429, message="请求过于频繁，请稍后重试"}`

```go
import "hei-gin/sdk/middleware"

// 路由组级别限流：每分钟最多 60 次
r.POST("/api/v1/sys/user/save",
    middleware.RateLimiter("user-save", 60, 60),
    handler.UserSave,
)
```

    "hei-gin/sdk/log"
)

func RegisterRoutes(r *gin.RouterGroup) {
    // 需要登录 + 权限检查 + 操作日志 + 防重复（3 秒）
    r.POST("/save",
        authMiddleware.HeiCheckLogin(),
        authMiddleware.HeiCheckPermission([]string{"sys:user:create"}),
        log.SysLog("创建用户"),
        authMiddleware.NoRepeat(3000),
        h.UserSave,
    )

    // 只需要登录
    r.GET("/info",
        authMiddleware.HeiCheckLogin(),
        h.GetUserInfo,
    )

    // 公开接口（无中间件）
    r.GET("/public-info", h.PublicInfo)
}
```

### registry.Perm / registry.ClientPerm（权限注册 + 检查快捷方式）

**文件**：`sdk/registry/perm.go`

```go
func Perm(code, name string) gin.HandlerFunc
func ClientPerm(code, name string) gin.HandlerFunc
```

这是实际项目中最常用的权限模式。`Perm` 同时完成两件事：
1. 通过 `auth.RegisterPermission()` 注册权限（仅首次调用时注册，带去重）
2. 返回 `HeiCheckPermission([]string{code})` 中间件

```go
import "hei-gin/sdk/registry"

// 实际项目中最常用的写法
g.GET("/list",
    registry.Perm("sys:user:list", "用户列表查询"),
    handler.UserList,
)
g.POST("/create",
    registry.Perm("sys:user:create", "创建用户"),
    log.SysLog("创建用户"),
    handler.UserCreate,
)
```

`ClientPerm` 对应 C 端权限检查（返回 `HeiClientCheckPermission([]string{code})`）。
