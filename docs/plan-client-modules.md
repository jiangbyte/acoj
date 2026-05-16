# hei-gin client 模块实现计划

## 概述

参考 `hei-fastapi` 中 `modules/client/` 的实现，为 `hei-gin` 实现 **auth**、**user**、**session** 3 个客户端模块。

每个端点和数据响应/请求必须与 FastAPI 版保持一致。ent 模型 `ClientUser` 已生成在 `ent/gen/` 中。

### 基础设施

C 端认证基础设施已就位：
- `core/auth/client_auth_tool.go` — `HeiClientAuthTool` 结构体，`Login()`/`Logout()`/`Kickout()` 等方法
- `core/auth/middleware/client_check_login.go` — `HeiClientCheckLogin()` 中间件
- `core/auth/middleware/client_check_permission.go` — `HeiClientCheckPermission()` 中间件
- `core/auth/middleware/norepeat.go` — `NoRepeat()` 防重复提交中间件（同时支持 C 端和 B 端）
- `core/captcha/captcha.go` — `CCaptcha` 实例（使用 `CAPTCHA_CONSUMER_CACHE_KEY` 前缀）
- `core/log/record.go` — `RecordAuthLog()` 记录认证日志
- `core/enums/status.go` — `UserStatusActive`/`UserStatusInactive`/`UserStatusLocked`

### C 端与 B 端关键区别

| 方面 | B 端 (BUSINESS) | C 端 (CONSUMER) |
|------|----------------|-----------------|
| 认证工具 | 包级函数 `auth.Login()` 等 | `HeiClientAuthTool` 方法 |
| Redis 键前缀 | `hei:auth:BUSINESS:*` | `hei:auth:CONSUMER:*` |
| 路由前缀 | `/api/v1/b/...` | `/api/v1/c/...` |
| 公开前缀 | `/api/v1/public/b/...` | `/api/v1/public/c/...` |
| 登录检查中间件 | `middleware.HeiCheckLogin()` | `middleware.HeiClientCheckLogin()` |
| 权限中间件 | `middleware.HeiCheckPermission()` | `middleware.HeiCheckPermission()`（C 端也用 B 端权限中间件） |
| 验证码 | `captcha.BCaptcha` | `captcha.CCaptcha` |
| 用户表 | `SysUser` | `ClientUser` |
| ent 查询包 | `sysuser` | `clientuser` |

### ClientUser ent 模型字段特点

所有字段除 `ID`(string)、`Status`(string)、`LoginCount`(int) 外均为指针类型（`*string` 或 `*time.Time`）。

关键字段类型：
- `Birthday` → `*time.Time`
- `Password` → `*string`（可能为 nil）
- `Username`/`Nickname`/`Avatar`/`Motto`/`Gender`/`Email`/`Github` → `*string`
- `LastLoginAt`/`CreatedAt`/`UpdatedAt` → `*time.Time`

---

## 实施顺序

| 顺序 | 模块 | 说明 |
|------|------|------|
| 1 | **auth** | captcha/sm2/username 登录注册登出，无外部依赖 |
| 2 | **user** | 9 个端点，CRUD + 当前用户 + 个人设置 |
| 3 | **session** | 6 个端点，纯 Redis 操作（CONSUMER 前缀） |

---

## 1. auth 模块 (`modules/client/auth/`)

### 目录结构

```
modules/client/auth/
├── route.go                    # 汇总注册 captcha/sm2/username
├── captcha/api/v1/api.go       # GET /api/v1/public/c/captcha
├── sm2/api/v1/api.go           # GET /api/v1/public/c/sm2/public-key
├── username/
│   ├── params.go               # 登录/注册/登出 请求响应结构体
│   ├── logic.go                # DoLogin/DoRegister/DoLogout 业务逻辑
│   └── api/v1/api.go           # POST /api/v1/public/c/login 等路由
```

### 1.1 captcha — `GET /api/v1/public/c/captcha`

与 B 端完全相同的模式，仅将 `captcha.BCaptcha` 替换为 `captcha.CCaptcha`。

**api/v1/api.go:**
```go
package captcha_api

import (
    "github.com/gin-gonic/gin"
    "hei-gin/core/captcha"
    "hei-gin/core/result"
)

func RegisterRoutes(r *gin.Engine) {
    r.GET("/api/v1/public/c/captcha", GetCaptcha)
}

func GetCaptcha(c *gin.Context) {
    captchaResult, err := captcha.CCaptcha.GetCaptcha()
    if err != nil {
        c.JSON(200, result.Failure(c, "验证码生成失败", 500, nil))
        return
    }
    c.JSON(200, result.Success(c, captchaResult))
}
```

**响应**: `{"code":200, "message":"请求成功", "data":{"captcha_base64":"...", "captcha_id":"..."}, "success":true, "trace_id":"..."}`

### 1.2 sm2 — `GET /api/v1/public/c/sm2/public-key`

与 B 端完全相同的模式。

**api/v1/api.go:**
```go
package sm2_api

import (
    "github.com/gin-gonic/gin"
    "hei-gin/core/result"
    "hei-gin/core/utils"
)

func RegisterRoutes(r *gin.Engine) {
    r.GET("/api/v1/public/c/sm2/public-key", GetPublicKey)
}

func GetPublicKey(c *gin.Context) {
    publicKey := utils.GetPublicKey()
    c.JSON(200, result.Success(c, publicKey))
}
```

**响应**: `{"code":200, "message":"请求成功", "data":"<SM2-PUBLIC-KEY>", "success":true, "trace_id":"..."}`

### 1.3 username — 登录/注册/登出

#### params.go

```go
package username

type UsernameLoginParam struct {
    Username    string  `json:"username"`
    Password    string  `json:"password"`
    CaptchaCode string  `json:"captcha_code"`
    CaptchaID   string  `json:"captcha_id"`
    DeviceID    *string `json:"device_id"`
}

type UsernameLoginResult struct {
    Token string `json:"token,omitempty"`
}

type UsernameRegisterParam struct {
    Username    string `json:"username"`
    Password    string `json:"password"`
    CaptchaCode string `json:"captcha_code"`
    CaptchaID   string `json:"captcha_id"`
}

type UsernameRegisterResult struct {
    Message string `json:"message,omitempty"`
}

type UsernameLogoutResult struct {
    Message string `json:"message,omitempty"`
}
```

#### logic.go

**DoLogin 流程：**
1. 校验验证码：`captcha.CCaptcha.CheckCaptcha(param.CaptchaID, param.CaptchaCode)`
2. 查询 ClientUser：`db.Client.ClientUser.Query().Where(clientuser.UsernameEQ(param.Username)).Only(ctx)`
   - 未找到 → `panic(BusinessError("用户名或密码错误", 400))`
3. 检查用户状态：
   - LOCKED → "账号已被锁定"
   - INACTIVE → "账号已停用"
   - 非 ACTIVE → "账号状态异常"
4. SM2 解密密码：`utils.Decrypt(param.Password)`，空 → "用户名或密码错误"
5. bcrypt 校验密码：`bcrypt.CompareHashAndPassword`，注意 `user.Password` 可能为 nil
   - nil 密码 → "用户名或密码错误"
6. 构建 extra 字典：
   ```go
   extra := map[string]any{
       "username":    safeStr(entity.Username),
       "nickname":    safeStr(entity.Nickname),
       "status":      entity.Status,
       "device_type": utils.GetBrowser(ua),
       "device_id":   param.DeviceID,
   }
   ```
7. 调用 C 端登录：`auth.NewHeiClientAuthTool().Login(c, userID, extra)`
8. 更新登录信息（直接 ent update，不使用 service 层）：
   ```go
   now := time.Now()
   ip := utils.GetClientIP(c)
   db.Client.ClientUser.UpdateOneID(user.ID).
       SetLastLoginAt(now).
       SetLastLoginIP(ip).
       AddLoginCount(1).
       Exec(ctx)
   ```
9. 记录操作日志：`log.RecordAuthLog(c, "登录", "LOGIN", "SUCCESS", "", username)`
10. 返回 `UsernameLoginResult{Token: token}`

**DoRegister 流程：**
1. 校验验证码：`captcha.CCaptcha.CheckCaptcha(param.CaptchaID, param.CaptchaCode)`
2. 检查用户名唯一性：`db.Client.ClientUser.Query().Where(clientuser.UsernameEQ(param.Username)).Exist(ctx)`
   - 已存在 → "用户名已存在"
3. SM2 解密密码；空 → "密码解密失败"
4. bcrypt 哈希密码
5. 创建 ClientUser：
   ```go
   userID := utils.GenerateID()
   db.Client.ClientUser.Create().
       SetID(userID).
       SetUsername(param.Username).
       SetPassword(hashedPwdStr).
       SetNickname(param.Username).
       SetStatus(string(enums.UserStatusActive)).
       SetCreatedAt(now).
       SetCreatedBy(userID).
       Save(ctx)
   ```
6. 记录操作日志：`log.RecordAuthLog(c, "注册", "REGISTER", "SUCCESS", "", param.Username)`
7. 返回 `UsernameRegisterResult{Message: "注册成功"}`

**DoLogout 流程：**
1. 获取 C 端用户 ID：`auth.NewHeiClientAuthTool().GetLoginIDDefaultNull(c)`
2. 若已登录，查询用户名并记录操作日志：
   ```go
   log.RecordAuthLog(c, "登出", "LOGOUT", "SUCCESS", "", username)
   ```
3. 调用 C 端登出：`auth.NewHeiClientAuthTool().Logout(c)`
4. 返回 `UsernameLogoutResult{Message: "登出成功"}`

#### api/v1/api.go 路由

```go
r.POST("/api/v1/public/c/login", username.DoLogin)
r.POST("/api/v1/public/c/register",
    log.SysLog("注册"),
    middleware.NoRepeat(5000),
    username.DoRegister,
)
r.POST("/api/v1/c/logout",
    middleware.HeiClientCheckLogin(),
    username.DoLogout,
)
```

注意：login 无任何中间件、register 有 SysLog + NoRepeat(5000)、logout 有 HeiClientCheckLogin。

### 1.4 route.go

```go
package auth

import (
    "github.com/gin-gonic/gin"
    captcha_api "hei-gin/modules/client/auth/captcha/api/v1"
    sm2_api "hei-gin/modules/client/auth/sm2/api/v1"
    username_api "hei-gin/modules/client/auth/username/api/v1"
)

func RegisterRoutes(r *gin.Engine) {
    captcha_api.RegisterRoutes(r)
    sm2_api.RegisterRoutes(r)
    username_api.RegisterRoutes(r)
}
```

---

## 2. user 模块 (`modules/client/user/`)

### 目录结构

```
modules/client/user/
├── params.go            # 请求/响应结构体
├── service.go           # 业务逻辑 + entToVO 转换
└── api/v1/api.go        # 9 个端点路由注册
```

### Endpoints（9 个）

| 方法 | 路径 | 权限/中间件 | SysLog | NoRepeat | 处理器 |
|------|------|-------------|--------|----------|--------|
| GET | `/api/v1/client-user/page` | `HeiCheckPermission("client:user:page")` | - | - | page |
| POST | `/api/v1/client-user/create` | `HeiCheckPermission("client:user:create")` | - | - | create |
| POST | `/api/v1/client-user/modify` | `HeiCheckPermission("client:user:modify")` | - | - | modify |
| POST | `/api/v1/client-user/remove` | `HeiCheckPermission("client:user:remove")` | - | - | remove |
| GET | `/api/v1/client-user/detail` | `HeiCheckPermission("client:user:detail")` | - | - | detail |
| GET | `/api/v1/client-user/current` | `HeiClientCheckLogin` | - | - | current |
| POST | `/api/v1/client-user/update-profile` | `HeiClientCheckLogin` | SysLog("C端用户更新个人信息") | NoRepeat(3000) | updateProfile |
| POST | `/api/v1/client-user/update-avatar` | `HeiClientCheckLogin` | SysLog("C端用户更新头像") | - | updateAvatar |
| POST | `/api/v1/client-user/update-password` | `HeiClientCheckLogin` | SysLog("C端用户修改密码") | NoRepeat(3000) | updatePassword |

**权限说明**:
- page/create/modify/remove/detail 使用 B 端权限中间件 `HeiCheckPermission`（后台管理员管理 C 端用户）
- current/update-profile/update-avatar/update-password 使用 C 端登录中间件 `HeiClientCheckLogin`（用户自身操作）
- CRUD 端点**没有** SysLog（与 FastAPI 一致，只有用户自操作有 SysLog）

### params.go

```go
package clientuser

type ClientUserVO struct {
    ID          string  `json:"id,omitempty"`
    Username    string  `json:"username,omitempty"`
    Nickname    string  `json:"nickname,omitempty"`
    Avatar      string  `json:"avatar,omitempty"`
    Motto       string  `json:"motto,omitempty"`
    Gender      string  `json:"gender,omitempty"`
    Birthday    string  `json:"birthday,omitempty"`     // 格式 "2006-01-02"
    Email       string  `json:"email,omitempty"`
    Github      string  `json:"github,omitempty"`
    Status      string  `json:"status,omitempty"`
    LastLoginAt string  `json:"last_login_at,omitempty"` // 格式 "2006-01-02 15:04:05"
    LastLoginIP string  `json:"last_login_ip,omitempty"`
    LoginCount  int     `json:"login_count"`
    CreatedAt   string  `json:"created_at,omitempty"`
    CreatedBy   string  `json:"created_by,omitempty"`
    UpdatedAt   string  `json:"updated_at,omitempty"`
    UpdatedBy   string  `json:"updated_by,omitempty"`
}

type ClientUserPageParam struct {
    Current int    `json:"current" form:"current"`
    Size    int    `json:"size" form:"size"`
    Keyword string `json:"keyword,omitempty" form:"keyword"`
    Status  string `json:"status,omitempty" form:"status"`
}

type UpdateProfileParam struct {
    Nickname *string `json:"nickname,omitempty"`
    Motto    *string `json:"motto,omitempty"`
    Gender   *string `json:"gender,omitempty"`
    Birthday string  `json:"birthday,omitempty"`   // 日期字符串 "2006-01-02"
    Email    *string `json:"email,omitempty"`
    Github   *string `json:"github,omitempty"`
}

type UpdateAvatarParam struct {
    Avatar string `json:"avatar"`
}

type UpdatePasswordParam struct {
    CurrentPassword string `json:"current_password"`
    NewPassword     string `json:"new_password"`
}

// CRUD 请求参数
type ClientUserCreateParam struct {
    Username string  `json:"username"`
    Password string  `json:"password"`
    Nickname *string `json:"nickname,omitempty"`
    Email    *string `json:"email,omitempty"`
    Avatar   *string `json:"avatar,omitempty"`
    Motto    *string `json:"motto,omitempty"`
    Gender   *string `json:"gender,omitempty"`
    Birthday string  `json:"birthday,omitempty"`
    Github   *string `json:"github,omitempty"`
    Status   string  `json:"status,omitempty"`
}

type ClientUserModifyParam struct {
    ID       string  `json:"id"`
    Username string  `json:"username,omitempty"`
    Nickname *string `json:"nickname,omitempty"`
    Email    *string `json:"email,omitempty"`
    Avatar   *string `json:"avatar,omitempty"`
    Motto    *string `json:"motto,omitempty"`
    Gender   *string `json:"gender,omitempty"`
    Birthday string  `json:"birthday,omitempty"`
    Github   *string `json:"github,omitempty"`
    Status   string  `json:"status,omitempty"`
}
```

**注意**:
- `ClientUserVO` 中不含 `password`、`phone`、`org_id`、`position_id`、`group_id`（参考 FastAPI VO 定义）
- `UpdateProfileParam` 不含 `username`、`phone`（参考 FastAPI，不同于 B 端 sys/user）
- `Birthday` 在请求中用 string（"2006-01-02"），在 ent 模型中为 `*time.Time`
- `LoginCount` 默认 0
- create 和 modify 分离参数结构体（create 有 Password，modify 无 Password）

### service.go 关键实现

**entToVO**: 将 `*string` 解引用为空字符串、`*time.Time` 格式化为 `"2006-01-02 15:04:05"`（Birthday 格式化为 `"2006-01-02"`）
```go
func entToVO(entity *gen.ClientUser) ClientUserVO {
    vo := ClientUserVO{
        ID:    entity.ID,
        Status: entity.Status,
        LoginCount: entity.LoginCount,
    }
    if entity.Username != nil { vo.Username = *entity.Username }
    if entity.Nickname != nil { vo.Nickname = *entity.Nickname }
    // ... 其他字段类似
    if entity.Birthday != nil { vo.Birthday = entity.Birthday.Format("2006-01-02") }
    if entity.LastLoginAt != nil { vo.LastLoginAt = entity.LastLoginAt.Format("2006-01-02 15:04:05") }
    // ...
    return vo
}
```

**Page**:
- 使用 `db.Client.ClientUser.Query()` + `Where(...)` 过滤
- 支持 keyword（username/nickname Contains）、status 过滤
- 按 `created_at DESC` 排序
- 标准分页：`Limit(size).Offset((current-1)*size)`
- 使用 `Count(ctx)` 获取总量

**Create**:
- 检查用户名唯一性（已存在 → "账号已存在"）
- ID 用 `utils.GenerateID()`，密码用 bcrypt 哈希
- 设置 `CreatedAt`、`CreatedBy`、`UpdatedAt`、`UpdatedBy`

**Modify**:
- 先查存在性，不存在 → "数据不存在"
- 如果 username 变化，检查唯一性
- 排除 password 字段更新（modify 参数不含 password）
- 更新 `UpdatedAt`、`UpdatedBy`

**Remove**: 按 ids 批量删除（使用 `Where(IDIn(ids...))`）

**Detail**: 按 id 查询 → entToVO 返回

**Current**:
- 使用 `auth.NewHeiClientAuthTool().GetLoginIDDefaultNull(c)` 获取当前 C 端用户 ID
- 查询 ClientUser → entToVO 返回

**UpdateProfile**:
- 获取当前 C 端用户 ID（为空 → "用户未登录"）
- 按需更新 nickname/motto/gender/birthday/email/github
- birthday 字符串解析为 time.Time：`time.Parse("2006-01-02", param.Birthday)`
- 更新 `UpdatedAt`

**UpdateAvatar**:
- 获取当前 C 端用户 ID
- 直接设置 avatar 字段（base64 字符串）

**UpdatePassword**:
- 获取当前 C 端用户 ID
- 查询用户是否存在；密码是否为 nil → "未设置密码，无法修改"
- SM2 解密 `CurrentPassword`，bcrypt 比对 → "当前密码不正确"
- SM2 解密 `NewPassword`，bcrypt 哈希，更新

### api/v1/api.go 路由

Handlers 模式参考 sys/user/api/v1/api.go：
- page/create/modify/remove/detail 用 `middleware.HeiCheckPermission([]string{"client:user:xxx"})`
- current/updateProfile/updateAvatar/updatePassword 用 `middleware.HeiClientCheckLogin()`
- CRUD 端点没有 SysLog
- update-profile/update-password 有 `log.SysLog` + `middleware.NoRepeat(3000)`
- update-avatar 有 `log.SysLog("C端用户更新头像")`

```go
// GET /api/v1/client-user/page
r.GET("/api/v1/client-user/page",
    middleware.HeiCheckPermission([]string{"client:user:page"}),
    pageHandler,
)
// POST /api/v1/client-user/create
r.POST("/api/v1/client-user/create",
    middleware.HeiCheckPermission([]string{"client:user:create"}),
    createHandler,
)
// POST /api/v1/client-user/modify
r.POST("/api/v1/client-user/modify",
    middleware.HeiCheckPermission([]string{"client:user:modify"}),
    modifyHandler,
)
// POST /api/v1/client-user/remove
r.POST("/api/v1/client-user/remove",
    middleware.HeiCheckPermission([]string{"client:user:remove"}),
    removeHandler,
)
// GET /api/v1/client-user/detail
r.GET("/api/v1/client-user/detail",
    middleware.HeiCheckPermission([]string{"client:user:detail"}),
    detailHandler,
)
// GET /api/v1/client-user/current
r.GET("/api/v1/client-user/current",
    middleware.HeiClientCheckLogin(),
    currentHandler,
)
// POST /api/v1/client-user/update-profile
r.POST("/api/v1/client-user/update-profile",
    middleware.HeiClientCheckLogin(),
    log.SysLog("C端用户更新个人信息"),
    middleware.NoRepeat(3000),
    updateProfileHandler,
)
// POST /api/v1/client-user/update-avatar
r.POST("/api/v1/client-user/update-avatar",
    middleware.HeiClientCheckLogin(),
    log.SysLog("C端用户更新头像"),
    updateAvatarHandler,
)
// POST /api/v1/client-user/update-password
r.POST("/api/v1/client-user/update-password",
    middleware.HeiClientCheckLogin(),
    log.SysLog("C端用户修改密码"),
    middleware.NoRepeat(3000),
    updatePasswordHandler,
)
```

---

## 3. session 模块 (`modules/client/session/`)

### 目录结构

```
modules/client/session/
├── params.go            # 与 sys/session 相同的参数结构体
├── service.go           # C 端专用 Redis 操作（使用 CONSUMER 前缀）
└── api/v1/api.go        # 6 个端点路由注册
```

### 概要

C 端 session 模块与 B 端 `sys/session` 逻辑相同，但使用 **CONSUMER** Redis 前缀（`TOKEN_PREFIX_CONSUMER`、`SESSION_PREFIX_CONSUMER`）和 `HeiClientAuthTool`。

**实现策略**：参考 FastAPI，在 `modules/client/session/service.go` 中定义 C 端专用函数，直接调用 sys/session 的公共辅助函数（如 scanKeys、countTokens、countDaily），传入 CONSUMER 前缀。这与 FastAPI 的 re-export 模式等价。

### Endpoints（6 个）

| 方法 | 路径 | 权限 | 处理器 |
|------|------|------|--------|
| GET | `/api/v1/client/session/analysis` | `sys:session:page` | analysis |
| GET | `/api/v1/client/session/page` | `sys:session:page` | page |
| POST | `/api/v1/client/session/exit` | `sys:session:exit` | exit |
| GET | `/api/v1/client/session/tokens` | `sys:session:page` | tokens |
| POST | `/api/v1/client/session/exit-token` | `sys:session:exit` | exitToken |
| GET | `/api/v1/client/session/chart-data` | `sys:session:page` | chartData |

**注意**: C 端 session 与 B 端 session 使用**相同的权限码**（`sys:session:page`、`sys:session:exit`），因为在 FastAPI 中它们共用同一套权限。

### params.go

与 sys/session/params.go 相同的结构体定义，独立定义避免跨包依赖：

```go
package clientsession

type SessionAnalysisResult struct {
    TotalCount        int    `json:"total_count"`
    MaxTokenCount     int    `json:"max_token_count"`
    OneHourNewlyAdded int    `json:"one_hour_newly_added"`
    ProportionOfBAndC string `json:"proportion_of_b_and_c"`
}

type SessionPageResult struct {
    UserID                string  `json:"user_id,omitempty"`
    Username              *string `json:"username,omitempty"`
    Nickname              *string `json:"nickname,omitempty"`
    Avatar                *string `json:"avatar,omitempty"`
    Status                string  `json:"status,omitempty"`
    LastLoginIP           *string `json:"last_login_ip,omitempty"`
    LastLoginAddress      *string `json:"last_login_address,omitempty"`
    LastLoginTime         string  `json:"last_login_time,omitempty"`
    SessionCreateTime     string  `json:"session_create_time,omitempty"`
    SessionTimeout        string  `json:"session_timeout,omitempty"`
    SessionTimeoutSeconds int     `json:"session_timeout_seconds,omitempty"`
    TokenCount            int     `json:"token_count"`
}

type SessionExitParam struct {
    UserID string `json:"user_id"`
}

type SessionExitTokenParam struct {
    UserID string `json:"user_id"`
    Token  string `json:"token"`
}

type SessionTokenResult struct {
    Token          string `json:"token,omitempty"`
    CreatedAt      string `json:"created_at,omitempty"`
    Timeout        string `json:"timeout,omitempty"`
    TimeoutSeconds int    `json:"timeout_seconds,omitempty"`
    DeviceType     string `json:"device_type,omitempty"`
    DeviceID       string `json:"device_id,omitempty"`
}

type SessionPageParam struct {
    Current int    `json:"current" form:"current"`
    Size    int    `json:"size" form:"size"`
    Keyword string `json:"keyword,omitempty" form:"keyword"`
}

type SessionChartData struct {
    BarChart BarChartData `json:"bar_chart"`
    PieChart PieChartData `json:"pie_chart"`
}

type BarChartData struct {
    Days   []string         `json:"days"`
    Series []CategorySeries `json:"series"`
}

type PieChartData struct {
    Data []CategoryTotal `json:"data"`
}

type CategorySeries struct {
    Name string `json:"name"`
    Data []int  `json:"data"`
}

type CategoryTotal struct {
    Category string `json:"category"`
    Total    int    `json:"total"`
}
```

### service.go 实现

与 sys/session/service.go 逻辑相同，但使用 CONSUMER 常量和 HeiClientAuthTool：

**scanKeys / countTokens / countDaily**: 复用 sys/session 中相同的辅助函数（通过调用时传入 CONSUMER 前缀），或直接引用（因为两个 service.go 在同级 modules 下，无法直接 import，所以需要复制这些辅助函数到 client/session/service.go）

```go
var svcCtx = context.Background()

// scanKeys Redis SCAN 辅助函数（与 sys/session 相同）
// countTokens / countDaily / formatTimeout / lastNDays 同上
```

**Analysis**: SCAN CONSUMER session keys，统计 C 端 token 数据
- 只扫描 `SESSION_PREFIX_CONSUMER` 和 `TOKEN_PREFIX_CONSUMER`
- B/C 比例的 ProportionOfBAndC 只用 C 端数据（或者设 "0/total"）

**Page**: 
- `collectSessions` 使用 CONSUMER 前缀
- 关键词过滤 username
- 手动分页，返回 records/total/page/size/pages

**collectSessions**:
- SCAN `SESSION_PREFIX_CONSUMER + "*"`
- SMEMBER 获取 token → GET token data → 解析 extra.username
- 关键词过滤
- TTL 获取超时
- ClientUser 查询（非 SysUser）：`db.Client.ClientUser.Get(ctx, userID)`
- 构建 SessionPageResult

**Exit**: `auth.NewHeiClientAuthTool().Kickout(param.UserID)`

**TokenList**: SMEMBER CONSUMER session key → GET 每个 token data → 返回 token 详情（含 Timeout/DeviceType/DeviceID）

**ExitToken**: `auth.NewHeiClientAuthTool().KickoutToken(param.UserID, param.Token)`

**ChartData**: 
- SCAN CONSUMER session keys
- countDaily 统计每日新增
- 返回 7 天柱状图 + C 端饼图数据

### api/v1/api.go 路由

```go
// GET /api/v1/client/session/analysis
r.GET("/api/v1/client/session/analysis",
    middleware.HeiCheckPermission([]string{"sys:session:page"}),
    analysisHandler,
)
// GET /api/v1/client/session/page
r.GET("/api/v1/client/session/page",
    middleware.HeiCheckPermission([]string{"sys:session:page"}),
    pageHandler,
)
// POST /api/v1/client/session/exit
r.POST("/api/v1/client/session/exit",
    middleware.HeiCheckPermission([]string{"sys:session:exit"}),
    exitHandler,
)
// GET /api/v1/client/session/tokens
r.GET("/api/v1/client/session/tokens",
    middleware.HeiCheckPermission([]string{"sys:session:page"}),
    tokensHandler,
)
// POST /api/v1/client/session/exit-token
r.POST("/api/v1/client/session/exit-token",
    middleware.HeiCheckPermission([]string{"sys:session:exit"}),
    exitTokenHandler,
)
// GET /api/v1/client/session/chart-data
r.GET("/api/v1/client/session/chart-data",
    middleware.HeiCheckPermission([]string{"sys:session:page"}),
    chartDataHandler,
)
```

**注意**: session 所有端点没有 SysLog 和 NoRepeat 装饰器，只有 HeiCheckPermission。

---

## 4. 主路由注册

在 `core/app/router.go` 中新增：

```go
import (
    clientAuth "hei-gin/modules/client/auth"
    clientUser "hei-gin/modules/client/user/api/v1"
    clientSession "hei-gin/modules/client/session/api/v1"
)

func SetupRouters(r *gin.Engine) {
    // ... 已有代码 ...

    // Client modules
    clientAuth.RegisterRoutes(r)       // C端验证码/SM2/登录/注册/登出
    clientUser.RegisterRoutes(r)       // C端用户 CRUD + 个人设置
    clientSession.RegisterRoutes(r)    // C端会话管理 (Redis CONSUMER)
}
```

---

## 5. 错误信息对照

| 场景 | 错误消息 | HTTP 状态码 | 对应端点 |
|------|----------|-------------|----------|
| 登录 - 用户名或密码错误 | `"用户名或密码错误"` | 400 | login |
| 登录 - 账号已被锁定 | `"账号已被锁定"` | 400 | login |
| 登录 - 账号已停用 | `"账号已停用"` | 400 | login |
| 登录 - 账号状态异常 | `"账号状态异常"` | 400 | login |
| 注册 - 用户名已存在 | `"用户名已存在"` | 400 | register |
| 注册 - 密码解密失败 | `"密码解密失败"` | 400 | register |
| 创建 C 端用户 - 账号已存在 | `"账号已存在"` | 400 | user/create |
| 修改 - 数据不存在 | `"数据不存在"` | 400 | user/modify |
| 修改密码 - 未设置密码 | `"未设置密码，无法修改"` | 400 | user/update-password |
| 修改密码 - 当前密码不正确 | `"当前密码不正确"` | 400 | user/update-password |
| 用户未登录 | `"用户未登录"` | 401 | user/current 等 |
| 未授权/未登录 | `"未授权/未登录"` | 401 | HeiClientCheckLogin |
| 缺少权限 | `"缺少权限: ..."` | 403 | HeiCheckPermission |

---

## 6. 分页默认值

```go
if param.Current < 1 { param.Current = 1 }
if param.Size < 1 { param.Size = 10 }
```

---

## 7. 各模块特殊注意事项

### auth
- login 端点**没有** SysLog 中间件（日志在 logic.go 中手动调用 `RecordAuthLog`）
- register 端点有 `SysLog("注册")` + `NoRepeat(5000)` 中间件
- logout 端点有 `HeiClientCheckLogin()` 中间件；日志事件名称为 "登出"（非 "退出登录"）
- logout 返回消息为 "登出成功"（非 "退出登录成功"）
- 所有 C 端 auth 使用 `captcha.CCaptcha`（而非 `captcha.BCaptcha`）
- 登录使用 `auth.NewHeiClientAuthTool().Login(c, userID, extra)`（而非 `auth.Login()`）
- 登出使用 `auth.NewHeiClientAuthTool().Logout(c)`（而非 `auth.Logout()`）
- token `created_at` 使用 `"2006-01-02 15:04:05"` 格式（与 B 端一致）
- 注意 `ClientUser.Password` 可能为 nil → 登录时需做 nil 检查
- `ClientUser.Username`/`Nickname` 为 `*string` → 使用 `safeStr()` 解引用

### user
- page/create/modify/remove/detail 使用 B 端权限中间件 `HeiCheckPermission`（管理后台操作 C 端用户）
- current/update-profile/update-avatar/update-password 使用 C 端登录中间件 `HeiClientCheckLogin`（用户自身操作）
- CRUD 端点**没有** SysLog（与 FastAPI 一致）
- SysLog 名称与 FastAPI 一致："C端用户更新个人信息"、"C端用户更新头像"、"C端用户修改密码"
- update-profile/update-password 有 `NoRepeat(3000)` 防重复
- 创建用户时密码需 bcrypt 哈希
- modify 排除 password 字段（参数中无 password）
- create 检查用户名唯一性 → "账号已存在"
- modify 如果 username 变化，检查唯一性
- `ClientUserVO.Birthday` 格式为 "2006-01-02"（date 类型）
- `UpdateProfileParam.Birthday` 为 string（请求端）
- ent 模型 `Birthday` 为 `*time.Time`，需用 `time.Parse("2006-01-02", ...)` 解析
- create 参数使用 `ClientUserCreateParam`（含 Password），modify 使用 `ClientUserModifyParam`（不含 Password）

### session
- 使用 CONSUMER Redis 前缀常量（`constants.TOKEN_PREFIX_CONSUMER`、`constants.SESSION_PREFIX_CONSUMER`）
- 使用 `auth.NewHeiClientAuthTool().Kickout()`/`KickoutToken()` 退出
- session page 手动构建分页响应（与 sys/session 相同）
- 所有端点使用 `sys:session:page`/`sys:session:exit` 权限码（与 B 端共享）
- 没有 SysLog 和 NoRepeat 中间件
- 辅助函数（scanKeys/countTokens/countDaily/formatTimeout/lastNDays）需在 client/session/service.go 中重新定义（与 sys/session 相同的代码模式）
- `collectSessions` 查询 `ClientUser` 表（非 `SysUser`），使用 `db.Client.ClientUser.Get(ctx, userID)`
- 由于 `ClientUser` 的字段均为指针类型，`collectSessions` 中富化用户信息时需注意 nil 处理（与 `entToVO` 模式相同）
