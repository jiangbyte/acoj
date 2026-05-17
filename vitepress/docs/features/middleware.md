# 中间件体系

Hei Gin 的中间件系统分为三个层次：全局中间件、业务中间件和内置中间件。以下逐一介绍每个中间件的功能、配置和使用方式。

## 全局中间件

全局中间件注册在 Gin Engine 上，对所有请求生效。

### 1. Trace 中间件

**文件**：`core/middleware/trace.go`

Trace 中间件负责全链路追踪，为每个请求生成或透传唯一的 trace_id。

**功能**：
- 检查请求头中是否包含 `trace_id`
- 如果存在，透传该 Trace ID
- 如果不存在，调用 `utils.GenerateTraceID()` 生成新的 Trace ID（UUID 的十六进制编码，不含连字符）
- 将 Trace ID 设置到 Gin Context 的 `"trace_id"` 键中

**注册方式**：

```go
import "hei-gin/core/middleware"

r.Use(middleware.Trace())
```

**使用方式**：

```go
// 在 Handler 中获取 Trace ID
traceID := c.GetString("trace_id")

// 使用 result 包自动在响应中包含 trace_id
import "hei-gin/core/result"

result.Success(c, data)  // 响应中自动包含 "trace_id" 字段
result.Failure(c, message, code, data)  // 同上
```

**响应示例**：

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {},
  "success": true,
  "trace_id": "a1b2c3d4e5f67890abcdef1234567890"
}
```

### 2. AuthCheck 中间件

**文件**：`core/middleware/auth_check.go`

AuthCheck 中间件根据请求路径自动识别认证类型，对无需认证的路径放行，对需要认证的路径进行登录检查。

**路由分流规则**：

```
静态路径 (/favicon.ico, /docs, /redoc, /openapi.json, /v3/api-docs)  -> 放行
OPTIONS 方法                                                          -> 放行
/api/v{n}/public/*                                                    -> 放行（无需认证）
/api/v{n}/c/*                                                         -> 要求 C 端登录认证
/api/v{n}/b/* 或 /api/v{n}/<其他>/*                                   -> 要求 B 端登录认证
```

未通过认证的请求会收到 401 JSON 响应。

**注册方式**：

```go
r.Use(middleware.AuthCheck())
```

### 3. Recovery 中间件

**文件**：`core/middleware/recovery.go`

Recovery 中间件捕获 Gin 处理过程中发生的 panic，转换为友好的 JSON 错误响应。它替换了 Gin 默认的 Recovery 中间件，提供了业务异常（BusinessError）支持。

**功能**：
- 捕获所有 panic
- 如果是 `*exception.BusinessError` 或 `exception.BusinessError`，提取错误码和错误信息，通过 `result.Failure` 返回 200 状态码（业务码在 JSON body 中）
- 如果是其他 panic，返回 500 "服务器内部错误"
- 处理 handler 链中通过 `c.Error()` 收集的错误，返回 400

**BusinessError 使用方式**：

```go
import "hei-gin/core/exception"

// 使用构造函数创建业务异常并 panic
panic(exception.NewBusinessError("用户名或密码错误", 400))

// 或直接使用结构体字面量
panic(exception.BusinessError{
    Code:    400,
    Message: "用户名或密码错误",
})
```

**对应响应**：

```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null,
  "success": false,
  "trace_id": "a1b2c3d4..."
}
```

**注册方式**：

```go
r.Use(middleware.Recovery())
```

### 4. CORS 中间件

**文件**：`core/middleware/cors.go`

CORS 中间件处理跨域请求，配置项来源于 `config.yaml` 中的 `cors` 配置段。

**功能**：
- 处理 OPTIONS 预检请求
- 配置允许的来源、方法、头
- 支持 Allow Credentials 配置

**注册方式**：

```go
r.Use(middleware.CORS())
```

## 业务中间件

业务中间件按需注册到具体的路由组上。以下中间件均位于 `core/auth/middleware` 包，导入方式：

```go
import middleware "hei-gin/core/auth/middleware"
```

### 5. HeiCheckLogin（B 端登录验证）

**文件**：`core/auth/middleware/check_login.go`

验证请求携带的 JWT Token 是否有效。

**函数签名**：

```go
func HeiCheckLogin(loginType ...string) gin.HandlerFunc
```

`loginType` 默认为 `"BUSINESS"`。传入 `"CONSUMER"` 可复用此中间件做 C 端登录检查。

**功能**：
- 从 `Authorization` 头中提取 Bearer Token
- 解析 JWT Token 的有效性
- 验证 Token 是否在禁用列表中
- 从 Redis 中读取会话信息
- 将会话信息注入到 Context 中

**注册方式**：

```go
bApi := r.Group("/api/v1/b", middleware.HeiCheckLogin())
```

### 6. HeiClientCheckLogin（C 端登录验证）

**文件**：`core/auth/middleware/client_check_login.go`

C 端的专用登录验证中间件，内部委托给 `HeiCheckLogin("CONSUMER")`。

**函数签名**：

```go
func HeiClientCheckLogin() gin.HandlerFunc
```

**注册方式**：

```go
cApi := r.Group("/api/v1/c", middleware.HeiClientCheckLogin())
```

### 7. HeiCheckPermission（B 端权限检查）

**文件**：`core/auth/middleware/check_permission.go`

检查当前登录用户是否拥有指定的权限。同时具有**权限自动注册**功能。

**函数签名**：

```go
func HeiCheckPermission(permissions []string, mode ...string) gin.HandlerFunc
```

- `permissions`：权限代码列表（如 `[]string{"sys:user:list"}`）
- `mode`：匹配模式，默认为 `"AND"`（需要全部权限），传入 `"OR"` 则满足任意一个即可

**功能**：
- 自动注册权限代码到权限扫描系统
- 检查用户是否为 SUPER_ADMIN，是则直接放行
- 从 Redis 缓存中获取用户权限集合
- 使用权限匹配器进行通配符匹配
- 匹配成功则放行，失败则返回 403

**注册方式**：

```go
// 单个权限（AND 模式）
sysApi.GET("/user/list",
    middleware.HeiCheckPermission([]string{"sys:user:list"}),
    handler.UserList,
)

// 多个权限（OR 模式：任意一个匹配即可）
sysApi.POST("/user/save",
    middleware.HeiCheckPermission([]string{"sys:user:create", "sys:user:update"}, "OR"),
    handler.UserSave,
)
```

### 8. HeiClientCheckPermission（C 端权限检查）

**文件**：`core/auth/middleware/client_check_permission.go`

C 端的权限检查中间件，内部委托给 `heiCheckPermissionInner("CONSUMER", ...)`。

**函数签名**：

```go
func HeiClientCheckPermission(permissions []string, mode ...string) gin.HandlerFunc
```

用法与 `HeiCheckPermission` 相同，但检查的是 C 端用户的权限。

**注册方式**：

```go
clientApi.GET("/order/list",
    middleware.HeiClientCheckPermission([]string{"client:order:list"}),
    handler.OrderList,
)
```

### 9. HeiCheckRole（B 端角色检查）

**文件**：`core/auth/middleware/check_role.go`

检查当前用户是否拥有指定的角色。

**函数签名**：

```go
func HeiCheckRole(roles []string, mode ...string) gin.HandlerFunc
```

- `roles`：角色代码列表
- `mode`：匹配模式，默认为 `"AND"`（需要全部角色），传入 `"OR"` 则满足任意一个即可

**功能**：
- 检查用户是否为 SUPER_ADMIN，是则直接放行
- 从 Redis/DB 获取用户角色
- 检查用户是否拥有指定的角色代码

**注册方式**：

```go
// 单个角色
sysApi.DELETE("/user/delete",
    middleware.HeiCheckRole([]string{"admin"}),
    handler.UserDelete,
)

// 多个角色（OR 模式：任意一个匹配即可）
sysApi.POST("/sys/config/save",
    middleware.HeiCheckRole([]string{"admin", "operator"}, "OR"),
    handler.ConfigSave,
)
```

### 10. HeiClientCheckRole（C 端角色检查）

**文件**：`core/auth/middleware/client_check_role.go`

C 端的角色检查中间件。

**函数签名**：

```go
func HeiClientCheckRole(roles []string, mode ...string) gin.HandlerFunc
```

用法与 `HeiCheckRole` 相同，但检查的是 C 端用户的角色。

### 11. SysLog（操作日志录制）

**文件**：`core/log/syslog.go`

自动录制用户的操作日志，在 handler 执行完成后异步写入 sys_log 表。

**函数签名**：

```go
func SysLog(name string) gin.HandlerFunc
```

- `name`：操作日志的名称（如 "新增用户"、"删除配置"）

**功能**：
- 记录请求方法、路径、参数
- 自动捕获 handler 中的 panic（记录异常日志后重新 panic）
- 记录响应状态（SUCCESS / FAIL）
- 异步写入 sys_log 表（ent ORM）

**注册方式**：

```go
import "hei-gin/core/log"

// 注册到具体路由
sysApi.POST("/user/save",
    middleware.HeiCheckLogin(),
    middleware.HeiCheckPermission([]string{"sys:user:create"}),
    log.SysLog("新增用户"),
    handler.UserSave,
)
```

### 12. NoRepeat（防重复提交）

**文件**：`core/auth/middleware/norepeat.go`

防止用户在指定时间间隔内重复提交相同的请求。

**函数签名**：

```go
func NoRepeat(interval int) gin.HandlerFunc
```

- `interval`：防重复时间间隔，单位**毫秒**（ms）

**原理**：
- 基于请求的路径 + 参数 + 用户 ID + IP 生成缓存键
- 对请求参数（Query、Form、Body）计算 FNV-1a 64 位哈希值
- 在 Redis 中记录该哈希值和时间戳（TTL 为 3600 秒）
- 同一请求在指定间隔内重复提交会被拒绝（返回 429）

**注册方式**：

```go
// 3 秒内禁止重复提交
sysApi.POST("/user/save",
    middleware.HeiCheckLogin(),
    middleware.HeiCheckPermission([]string{"sys:user:create"}),
    middleware.NoRepeat(3000),
    handler.UserSave,
)
```

## 中间件链完整示例

一个典型的 B 端 API 路由注册：

```go
import (
    "github.com/gin-gonic/gin"
    "hei-gin/core/middleware"
    authMiddleware "hei-gin/core/auth/middleware"
    "hei-gin/core/log"
)

// 在模块的 api.go 中
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

    // 公开接口（无需认证）
    r.GET("/public-info", h.PublicInfo)
}
```

## 中间件执行顺序

在同一个请求中，中间件的执行顺序遵循**先进后出**原则（类似洋葱模型）：

```
Request
  │
  ▼
① Trace ───────────────────────── 生成 Trace ID
② AuthCheck ───────────────────── 路径认证检查
③ Recovery ────────────────────── panic 保护（defer）
④ CORS ────────────────────────── 跨域处理
⑤ HeiCheckLogin ───────────────── JWT 验证
⑥ HeiCheckPermission ──────────── 权限检查
⑦ SysLog ──────────────────────── 开始录制
⑧ NoRepeat ────────────────────── 防重复检查
  │
  ▼
Handler ────────────────────────── 业务逻辑
  │
  ▼
⑨ NoRepeat ────────────────────── 设置防重复标记
⑩ SysLog ──────────────────────── 完成录制（写入 DB）
⑪ HeiCheckPermission ──────────── 后处理
⑫ HeiCheckLogin ───────────────── 后处理
⑬ CORS ────────────────────────── 后处理
⑭ Recovery ────────────────────── 后处理（如有 panic）
⑮ AuthCheck ───────────────────── 后处理
⑯ Trace ───────────────────────── 后处理
  │
  ▼
Response
```

> 注意：中间件的"后处理"阶段通常不执行额外操作，核心逻辑在"前处理"阶段完成。Recovery 的 defer 在请求全过程中都有效。
