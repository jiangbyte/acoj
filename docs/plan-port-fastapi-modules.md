# 将 FastAPI 三个模块移植到 Go+Gin+ent 实施计划

## 1. 背景分析

### 1.1 FastAPI 参考模块结构（目标目录结构）

每个模块使用完整路由路径（以 `/api` 开头），目录结构与 `hei-fastapi` 完全一致：

```
hei-fastapi/modules/sys/
├── auth/
│   ├── captcha/api/v1/api.py          # GET  /api/v1/public/b/captcha
│   ├── sm2/api/v1/api.py             # GET  /api/v1/public/b/sm2/public-key
│   └── username/
│       ├── api/v1/api.py             # POST /api/v1/public/b/login
│       │                             # POST /api/v1/public/b/register
│       │                             # POST /api/v1/b/logout
│       ├── logic.py                  # do_login, do_register, do_logout
│       └── params.py                 # UsernameLoginParam, UsernameRegisterParam, ...
├── banner/
│   ├── api/v1/api.py                 # 5 个端点（CRUD + 分页）
│   ├── params.py                     # BannerVO, BannerPageParam
│   └── service.py                    # BannerService
└── resource/
    ├── api/v1/api.py                 # 11 个端点（Module 5个 + Resource 6个）
    ├── params.py                     # ModuleVO, ResourceVO, ModulePageParam, ResourcePageParam
    └── service.py                    # ModuleService, ResourceService
```

**核心原则**：
- 路由路径在模块 api handler 中**完整声明**（如 `/api/v1/sys/banner/page`）
- 模块目录结构与 hei-fastapi **完全一致**
- 注册路由时只需将模块的路由添加到 Gin engine（类似 FastAPI 的 `app.include_router`）

### 1.2 Go 项目现有状态

| 组件 | 状态 |
|------|------|
| ent schema (`ent/schema/sysbanner.go`, `sysmodule.go`, `sysresource.go`, `sysuser.go` 等) | ✅ 已定义 |
| ent 生成代码 (`ent/gen/`) | ✅ 已生成 |
| 核心基础设施 | ✅ 就绪 |
| 业务模块 (`modules/sys/`) | ❌ 空 |
| 路由注册 (`core/app/router.go`) | ❌ 未注册业务路由 |
| AuthCheck 中间件 | ⚠️ 含占位逻辑 |

### 1.3 Go 项目核心基础设施详情

| 包 | 文件 | 关键导出 |
|----|------|---------|
| `core/db` | `ent.go` | `db.Client` — ent 客户端全局实例 |
| | `redis.go` | `db.Redis` — Redis 客户端全局实例 |
| `core/result` | `result.go` | `Success(c, data)`, `Failure(c, msg, code, data)`, `PageDataResult(c, records, total, page, size)` |
| `core/pojo` | `id_params.go` | `IdParam{ID}`, `IdsParam{IDs}` |
| | `datetime_mixin.go` | `ParseDateTime(s)`, `FormatDateTime(t)`, `FormatDate(t)` |
| `core/exception` | `business_error.go` | `BusinessError{Message, Code}` — 用法：`panic(exception.NewBusinessError("msg", 400))`，被 `middleware.Recovery` 捕获返回 JSON |
| `core/auth` | `auth_tool.go` | `auth.Init(expire, tokenName)`, `Login(c, id, extra)`, `Logout(c, loginID...)`, `IsLogin(c)`, `GetLoginIDDefaultNull(c)`, `Kickout(loginID)`, `Disable(loginID, timeSeconds)` 等 |
| `core/auth/middleware` | `check_login.go` | `HeiCheckLogin(loginType...)` — Gin 中间件，校验 BUSINESS/CONSUMER 登录 |
| | `check_permission.go` | `HeiCheckPermission(permissions, mode...)` — Gin 中间件，校验权限 + 自动注册权限码 |
| | `norepeat.go` | `NoRepeat(interval)` — Gin 中间件，防重复提交（Redis 哈希） |
| `core/captcha` | `captcha.go` | `BCaptcha`, `CCaptcha` — 全局实例，`GetCaptcha()`, `CheckCaptcha(id, code)` |
| `core/log` | `syslog.go` | `SysLog(name)` — Gin 中间件，记录操作日志（当前为 console stub） |
| | `record.go` | `RecordAuthLog(c, name, category, exeStatus, exeMessage, opUser)` — 程序化记录认证日志 |
| `core/utils` | `crypto.go` | SM2 加密/解密（`Decrypt(ciphertext)`, `Encrypt(plaintext)`, `GetPublicKey()`, `HashWithSalt(data, salt)`） |
| | `snowflake.go` | `GenerateID()` — 雪花 ID 生成 |
| | `model.go` | `StripSystemFields(data, extraFields...)`, `ApplyUpdate(entity, updateData, extraProtected...)` |
| | `ip.go` | `GetClientIP(c)`, `GetCityInfo(ip)` — ip2region 地理位置 |
| | `user_agent.go` | `GetBrowser(ua)`, `GetOS(ua)` |
| `core/enums` | `status.go` | `UserStatusActive("ACTIVE")`, `UserStatusInactive("INACTIVE")`, `UserStatusLocked("LOCKED")` |
| | `permission.go` | `DataScopeEnum`, `CheckModeEnum`, `PermissionPathEnum`, `LoginTypeEnum` |
| | `resource.go` | `ResourceTypeEnum`, `ResourceCategoryEnum` |
| | `page_data_field.go` | `PageDataFieldRecords/Total/Page/Size/Pages` |
| `core/middleware` | `auth_check.go` | ⚠️ 当前使用 `c.GetString("user_id")` 占位，需修复为真实 auth 工具 |
| | `trace.go` | Trace ID 中间件 |
| | `recovery.go` | 异常恢复 + BusinessError 处理 |
| | `cors.go` | CORS 配置 |

### 1.4 架构映射

| FastAPI | Gin+Go |
|---------|--------|
| `@router.get/post(...)` | `router.GET/POST(...)` + 完整路径（如 `/api/v1/sys/banner/page`） |
| `Depends(get_db)` | 直接使用 `db.Client` 全局 ent 客户端 |
| Pydantic `BaseModel` | struct + `json` binding tags |
| `@HeiCheckPermission(...)` | `middleware.HeiCheckPermission(...)` （传入 `[]string`, mode） |
| `@SysLog(...)` | `log.SysLog(name)` 中间件 |
| `@NoRepeat(...)` | `middleware.NoRepeat(interval)` 中间件 |
| `@HeiCheckLogin` | `middleware.HeiCheckLogin()` 中间件 |
| `raise BusinessException` | `panic(exception.NewBusinessError(...))` + Recovery 捕获 |
| `Result.success(data)` | `result.Success(c, data)` |
| `Result.failure(msg, code)` | `result.Failure(c, msg, code, nil)` |
| `page_data(records, total, page, size)` | `result.PageDataResult(c, records, total, page, size)` |
| `snowflake.generate_id()` | `utils.GenerateID()` |
| `strip_system_fields(...)` | `utils.StripSystemFields(...)` |
| `apply_update(...)` | `utils.ApplyUpdate(...)` |
| `decrypt(password)` (SM2) | `utils.Decrypt(ciphertext)` |
| `bcrypt.hashpw` / `bcrypt.checkpw` | `golang.org/x/crypto/bcrypt` |
| `UserStatusEnum` | `enums.UserStatusActive("ACTIVE")`, `enums.UserStatusLocked("LOCKED")`, `enums.UserStatusInactive("INACTIVE")` （定义在 `core/enums/status.go`） |
| `HeiAuthTool.login(id, request, extra)` | `auth.Login(c, id, extra)` |
| `HeiAuthTool.logout(request)` | `auth.Logout(c)` |
| `HeiAuthTool.getLoginIdDefaultNull(request)` | `auth.GetLoginIDDefaultNull(c)` |
| `get_client_ip(request)` | `utils.GetClientIP(c)` |
| `get_browser(user_agent)` | `utils.GetBrowser(ua)` |
| `get_city_info(ip)` | `utils.GetCityInfo(ip)` |
| `LoginUserApiProvider` → `LoginUserInfo` | 直接使用 `db.Client.SysUser.Query().Where(...).Only(ctx)` — 因 Go 类型安全直接操作 ent |
| `record_auth_log(request, name, category, exeStatus, exeMessage, opUser)` | `log.RecordAuthLog(c, name, category, exeStatus, exeMessage, opUser)` |

---

## 2. 实施步骤

### 步骤 1：修复 AuthCheck 中间件

**文件**: `core/middleware/auth_check.go`

**问题**: 当前使用 `c.GetString("user_id")` 占位，未使用 `core/auth` 工具。

**变更逻辑**:
- 静态路径、OPTIONS → 免登录（不变）
- `/api/v{n}/public/*` → 免登录通过
- `/api/v{n}/b/*` → `auth.IsLogin(c)` 检查，未登录返回 401
- `/api/v{n}/c/*` → `HeiClientAuthTool.IsLogin(c)` 检查，未登录返回 401
- 其他路径 → 放行

**参考**: FastAPI 的 `core/middleware/auth.py` — `AuthMiddleware` ASGI 中间件，逻辑相同。

---

### 步骤 2：Banner 模块

**目录**: `modules/sys/banner/`（与 hei-fastapi 一致）

**新建文件**:

| 文件 | 说明 |
|------|------|
| `modules/sys/banner/params.go` | `BannerVO` + `BannerPageParam` 结构体 |
| `modules/sys/banner/service.go` | `BannerService`（page, detail, create, modify, remove） |
| `modules/sys/banner/api/v1/api.go` | 5 个路由处理器 |

**路由表**:

| 方法 | 完整路径 | 权限 | 中间件 |
|------|----------|------|--------|
| GET | `/api/v1/sys/banner/page` | `sys:banner:page` | `SysLog("获取Banner列表")` |
| POST | `/api/v1/sys/banner/create` | `sys:banner:create` | `SysLog("添加Banner")`, `NoRepeat(3000)` |
| POST | `/api/v1/sys/banner/modify` | `sys:banner:modify` | `SysLog("编辑Banner")` |
| POST | `/api/v1/sys/banner/remove` | `sys:banner:remove` | `SysLog("删除Banner")` |
| GET | `/api/v1/sys/banner/detail` | `sys:banner:detail` | — |

**数据模型**: `ent/schema/sysbanner.go` 已存在，表 `sys_banner`，字段包括 `id, title, image, url, link_type, summary, description, category, type, position, sort_code, view_count, click_count` + 4 个审计字段。

**BannerVO 与 ent 互转**: 通过手动赋值（无 Python 的 `model_validate` 等价物）。查询结果直接用 ent 生成的字段名。

**Service 逻辑**:
- `page` — 通过 `ent.SysBanner.Query().Order(ent.Desc(...)).Limit(size).Offset(offset).All(ctx)` 分页 + `Count(ctx)` 总数
- `detail` — `ent.SysBanner.Get(ctx, id)` + 处理 `ErrNotFound` 返回 nil
- `create` — `utils.StripSystemFields` 去除审计字段，ent 的 `Create()` 设置业务字段，手动填审计字段
- `modify` — 先查询存在性，`utils.ApplyUpdate` 应用更新字段，ent 的 `Update().SetXXX().Save(ctx)` 或 `UpdateOneID().SetXXX().Save(ctx)`
- `remove` — `ent.SysBanner.Delete().Where(...).Exec(ctx)` — 注意 FastAPI 传的是 `IdsParam`（多个 ID）即批量删除

> **ent 操作提示**: 使用 `db.Client.SysBanner` 作为查询入口。分页需要先 `Count()` 再 `Query().Limit().Offset().All()`。

---

### 步骤 3：Auth 模块

**目录**: `modules/sys/auth/`（与 hei-fastapi 一致）

**新建文件**:

| 文件 | 说明 |
|------|------|
| `modules/sys/auth/captcha/api/v1/api.go` | `GET /api/v1/public/b/captcha` |
| `modules/sys/auth/sm2/api/v1/api.go` | `GET /api/v1/public/b/sm2/public-key` |
| `modules/sys/auth/username/params.go` | `UsernameLoginParam`, `UsernameLoginResult`, `UsernameRegisterParam`, `UsernameRegisterResult`, `UsernameLogoutResult` |
| `modules/sys/auth/username/logic.go` | `DoLogin`, `DoRegister`, `DoLogout` |
| `modules/sys/auth/username/api/v1/api.go` | 3 个路由处理器（login, register, logout） |

**路由表**:

| 方法 | 完整路径 | 说明 | 中间件 |
|------|----------|------|--------|
| GET | `/api/v1/public/b/captcha` | B端验证码 | 无 |
| GET | `/api/v1/public/b/sm2/public-key` | 获取SM2公钥 | 无 |
| POST | `/api/v1/public/b/login` | B端登录 | 无 |
| POST | `/api/v1/public/b/register` | B端注册 | `SysLog("注册")`, `NoRepeat(5000)` |
| POST | `/api/v1/b/logout` | B端登出 | `HeiCheckLogin` |

#### 登录逻辑（参考 `hei-fastapi/modules/sys/auth/username/logic.py` `do_login`）

```
func DoLogin(c *gin.Context, param UsernameLoginParam) (*UsernameLoginResult, error) {
    1. 验证码校验（必须）
       captcha.BCaptcha.CheckCaptcha(param.CaptchaID, param.CaptchaCode)
       // CheckCaptcha 校验失败返回 error，成功则自动删除 Redis key（一次性）
       // CaptchaID 和 CaptchaCode 为必填

    2. 查询用户
       sysUser := db.Client.SysUser.Query().
           Where(sysuser.UsernameEQ(param.Username)).
           Only(ctx)
       未找到 → BusinessException("用户名或密码错误")

    3. 用户状态检查
       switch sysUser.Status {
       case string(enums.UserStatusLocked):   → "账号已被锁定"
       case string(enums.UserStatusInactive): → "账号已停用"
       case string(enums.UserStatusActive):   → 正常通过
       default:                               → "账号状态异常"
       }

    4. SM2 解密密码
       rawPassword := utils.Decrypt(param.Password)
       解密失败 → "用户名或密码错误"

    5. bcrypt 验证密码
       bcrypt.CompareHashAndPassword([]byte(sysUser.Password), []byte(rawPassword))
       不匹配 → "用户名或密码错误"

    6. 调用 auth.Login 生成 token
       extra := map[string]any{
           "username":   sysUser.Username,
           "nickname":   sysUser.Nickname,
           "status":     sysUser.Status,
       }
       userAgent := c.GetHeader("User-Agent")
       extra["device_type"] = utils.GetBrowser(userAgent)
       extra["device_id"] = param.DeviceID
       token, err := auth.Login(c, sysUser.ID, extra)

    7. 记录登录信息（更新最后登录时间/IP/次数）
       // 直接操作 ent:
       db.Client.SysUser.UpdateOneID(sysUser.ID).
           SetLastLoginAt(time.Now()).
           SetLastLoginIP(utils.GetClientIP(c)).
           AddLoginCount(1).
           Save(ctx)

    8. 记录认证日志
       log.RecordAuthLog(c, "登录", "LOGIN", "SUCCESS", "", sysUser.Username)

    return &UsernameLoginResult{Token: token}, nil
}
```

**关键细节**：
- 验证码必须校验（`captcha_id` 和 `captcha_code` 均为必填），校验失败返回 BusinessException
- 密码错误不会区分"用户不存在"和"密码错误"，统一返回"用户名或密码错误"（防枚举）
- `auth.Login()` 内部生成 JWT + 存入 Redis + 记录 session 集合

#### 注册逻辑（参考 `hei-fastapi/modules/sys/auth/username/logic.py` `do_register`）

```
func DoRegister(c *gin.Context, param UsernameRegisterParam) (*UsernameRegisterResult, error) {
    1. 验证码校验（可选，同登录）

    2. 检查用户名唯一性
       exist, err := db.Client.SysUser.Query().
           Where(sysuser.UsernameEQ(param.Username)).
           Exist(ctx)
       已存在 → BusinessException("用户名已存在")

    3. SM2 解密密码
    4. bcrypt 密码哈希
       hashed, _ := bcrypt.GenerateFromPassword([]byte(rawPassword), bcrypt.DefaultCost)

    5. 创建用户
       id := utils.GenerateID()
       db.Client.SysUser.Create().
           SetID(id).
           SetUsername(param.Username).
           SetPassword(string(hashed)).
           SetNickname(param.Username).
           SetStatus(string(enums.UserStatusActive)).
           SetCreatedBy(id).
           Save(ctx)

    return &UsernameRegisterResult{Message: "注册成功"}, nil
}
```

**关键细节**：
- `ent.SysUser` 的 `Password` 字段是 `Optional` 类型，需用 `SetNillablePassword` 或 `SetPassword`
- 创建用户时 `created_by` 设为用户自身 ID

#### 登出逻辑（参考 `hei-fastapi/modules/sys/auth/username/logic.py` `do_logout`）

```
func DoLogout(c *gin.Context) (*UsernameLogoutResult, error) {
    1. 获取当前用户 ID
       userID := auth.GetLoginIDDefaultNull(c)

    2. 如果存在，记录登出日志
       if userID != "" {
           // 查询用户信息（获取 username）
           sysUser, _ := db.Client.SysUser.Get(ctx, userID)
           opUser := ""
           if sysUser != nil {
               opUser = sysUser.Username
           }
           log.RecordAuthLog(c, "登出", "LOGOUT", "SUCCESS", "", opUser)
       }

    3. 调用 auth.Logout 清除 Redis token/session
       auth.Logout(c)

    return &UsernameLogoutResult{Message: "登出成功"}, nil
}
```

**关键细节**：
- `auth.Logout(c)` 会自动从 Redis 删除当前 token 并从 session set 中移除
- 日志记录在 `auth.Logout` 之前执行，因为登出后 token 失效无法获取用户信息

---

### 步骤 4：Resource 模块

**目录**: `modules/sys/resource/`（与 hei-fastapi 一致）

**新建文件**:

| 文件 | 说明 |
|------|------|
| `modules/sys/resource/params.go` | `ModuleVO`, `ResourceVO`, `ModulePageParam`, `ResourcePageParam` |
| `modules/sys/resource/service.go` | `ModuleService` + `ResourceService` |
| `modules/sys/resource/api/v1/api.go` | 11 个路由处理器 |

**路由表**:

| 方法 | 完整路径 | 权限 | 中间件 |
|------|----------|------|--------|
| GET | `/api/v1/sys/module/page` | `sys:module:page` | — |
| POST | `/api/v1/sys/module/create` | `sys:module:create` | `SysLog("添加模块")`, `NoRepeat(3000)` |
| POST | `/api/v1/sys/module/modify` | `sys:module:modify` | `SysLog("编辑模块")` |
| POST | `/api/v1/sys/module/remove` | `sys:module:remove` | `SysLog("删除模块")` |
| GET | `/api/v1/sys/module/detail` | `sys:module:detail` | — |
| GET | `/api/v1/sys/resource/tree` | `sys:resource:tree` | — |
| GET | `/api/v1/sys/resource/page` | `sys:resource:page` | — |
| POST | `/api/v1/sys/resource/create` | `sys:resource:create` | `SysLog("添加资源")`, `NoRepeat(3000)` |
| POST | `/api/v1/sys/resource/modify` | `sys:resource:modify` | `SysLog("编辑资源")` |
| POST | `/api/v1/sys/resource/remove` | `sys:resource:remove` | `SysLog("删除资源")` |
| GET | `/api/v1/sys/resource/detail` | `sys:resource:detail` | — |

#### 4.1 ModuleService 逻辑

**标准 CRUD**（与 BannerService 类似）：

- `page(param)` — `ent.SysModule.Query().Order(...).Limit().Offset().All(ctx)` + `Count(ctx)`
- `detail(id)` — `ent.SysModule.Get(ctx, id)`（处理 ent.NotFound）
- `create(vo)` — `StripSystemFields` + `ent.SysModule.Create().SetXXX(...).Save(ctx)`
- `modify(vo)` — 查询存在性 + `UpdateOneID().SetXXX(...).Save(ctx)`
- `remove(ids)` — `ent.SysModule.Delete().Where(sysmodule.IDIn(ids...)).Exec(ctx)`

#### 4.2 ResourceService 逻辑

**分页/详情/创建/修改**（同 Banner 模式），额外实现：

##### Resource Tree（参考 `hei-fastapi/modules/sys/resource/service.py` `ResourceService.tree`）

```
func (s *ResourceService) Tree() []*ResourceVO {
    1. 查询所有资源 db.Client.SysResource.Query().All(ctx)
    2. 按 sort_code 排序
    3. 构建 parent_id → children map
    4. 从 parent_id="" 开始递归构建树状嵌套 VO
}
```

**结构**: 返回 `[]*ResourceVO`，每个 VO 含 `Children []*ResourceVO` 字段。

##### Resource Modify — 权限同步（参考 `hei-fastapi/modules/sys/resource/service.py` `ResourceService.modify`）

当 `extra` 字段中的 `permission_code` 变更时，需要同步 `rel_role_permission` 表：

```
if oldPermissionCode != newPermissionCode {
    1. 查询所有关联角色的 ID:
       relRoleResources := db.Client.RelRoleResource.Query().
           Where(relroleresource.ResourceIDEQ(resourceID)).
           All(ctx)
       roleIDs = 提取 role_id

    2. 如果有旧 code，删除对应关联:
       db.Client.RelRolePermission.Delete().
           Where(relrolepermission.RoleIDIn(roleIDs...)).
           Where(relrolepermission.PermissionCodeEQ(oldCode)).
           Exec(ctx)

    3. 如果有新 code，插入关联:
       for _, roleID := range roleIDs {
           检查是否已存在（去重）
           不存在则创建 RelRolePermission
       }
}
```

##### Resource Remove — 递归删除（参考 `hei-fastapi/modules/sys/resource/service.py` `ResourceService.remove`）

```
func (s *ResourceService) Remove(ids []string) {
    1. 收集所有后代 ID（递归）
       allIDs := collectDescendantIDs(ids)
       // 从 ent 查询全部资源，构建 children map，遍历收集

    2. 删除 RelRoleResource 关联:
       db.Client.RelRoleResource.Delete().
           Where(relroleresource.ResourceIDIn(allIDs...)).
           Exec(ctx)

    3. 删除资源自身:
       db.Client.SysResource.Delete().
           Where(sysresource.IDIn(allIDs...)).
           Exec(ctx)
}
```

##### 循环父级检测（参考 `hei-fastapi/modules/sys/resource/service.py` `_check_circular_parent`）

```
func checkCircularParent(entityID, newParentID string) bool {
    查询所有资源
    从 newParentID 开始沿 parent_id 链向上遍历
    如果链中遇到 entityID，则说明构成循环 → BusinessException("父级不能选择自身或子节点")
}
```

---

### 步骤 5：路由注册

**文件**: `core/app/router.go`

每个模块导出一个 `RegisterRoutes` 函数，接收 `*gin.Engine`，在模块内部注册完整路径路由：

```go
// modules/sys/banner/api/v1/api.go
func RegisterRoutes(r *gin.Engine) {
    r.GET("/api/v1/sys/banner/page", /*handler*/)
    r.POST("/api/v1/sys/banner/create", /*middlewares*/, /*handler*/)
    // ...
}
```

在 `SetupRouters` 中调用各模块的注册函数：

```go
// core/app/router.go
func SetupRouters(r *gin.Engine) {
    r.GET("/", HealthHandler)

    banner.RegisterRoutes(r)
    auth.RegisterRoutes(r)
    resource.RegisterRoutes(r)
}
```

> 注意：Gin 中间件的应用顺序 `r.Use()` 在 `core/app/app.go` 中已设置（Trace → AuthCheck → Recovery → CORS）。路由级别额外中间件（如 SysLog, NoRepeat, HeiCheckPermission）在各路由 handler 处追加。

---

## 3. 实施顺序

```
步骤 1：修复 AuthCheck 中间件 ───── 基础依赖（1 个文件）
        │
步骤 2：Banner 模块 ─────────────── 最简单，验证分层和路由模式（3 个文件）
        │
步骤 3：Auth 模块 ───────────────── 登录/注册核心功能（5 个文件）
        │
步骤 4：Resource 模块 ───────────── 最复杂（树 + 关联同步）（3 个文件）
        │
步骤 5：路由注册 ────────────────── 编译验证（1 个文件修改）
```

---

## 4. 涉及文件清单

### 新建文件（11 个）

```
modules/sys/banner/params.go
modules/sys/banner/service.go
modules/sys/banner/api/v1/api.go
modules/sys/auth/captcha/api/v1/api.go
modules/sys/auth/sm2/api/v1/api.go
modules/sys/auth/username/params.go
modules/sys/auth/username/logic.go
modules/sys/auth/username/api/v1/api.go
modules/sys/resource/params.go
modules/sys/resource/service.go
modules/sys/resource/api/v1/api.go
```

### 修改文件（2 个）

```
core/middleware/auth_check.go    # 修复 AuthCheck 中间件
core/app/router.go              # 注册模块路由
```

---

## 5. 注意事项

1. **bcrypt**: `golang.org/x/crypto` 已在 `go.mod`，直接 `import "golang.org/x/crypto/bcrypt"`
2. **NoRepeat 中间件**: 已存在于 `core/auth/middleware/norepeat.go`
3. **SysLog 中间件**: 已存在于 `core/log/syslog.go`（当前为 console stub）
4. **Permission 自动注册**: `middleware.HeiCheckPermission` 内部调用 `auth.RegisterPermission` 自动注册
5. **ent 查询注意**: 生成的 ent 代码可能使用 Optional 字段的指针，需要正确处理 nillable 字段
6. **ent 客户端**: 全局 `db.Client` 直接使用，无需每次请求创建
7. **ctx 参数**: ent 查询需要 `context.Background()` 或 `c.Request.Context()`
8. **ent 分页查询**: 需先 `Count(ctx)` 取总数，再 `Query().Limit(size).Offset(offset).All(ctx)` 取数据
9. **批量删除**: 使用 `db.Client.SysBanner.Delete().Where(sysbanner.IDIn(ids...)).Exec(ctx)`
10. **存在性检查**: 使用 `.Exist(ctx)` 返回 bool，使用 `.Only(ctx)` 返回 0 或 1 条结果（多于 1 条报错）
11. **路由路径**: 各模块 api handler 中写完整路径（如 `/api/v1/sys/banner/page`）
12. **中间件注册顺序**: 在 Gin 中 `r.GET(path, m1, m2, handler)` — 中间件从左到右执行
13. **用户状态使用枚举**: 使用 `enums.UserStatusActive`（"ACTIVE"）、`enums.UserStatusLocked`（"LOCKED"）、`enums.UserStatusInactive`（"INACTIVE"），定义在 `core/enums/status.go`
14. **登录用户信息查询**: 直接使用 `db.Client.SysUser` 操作，无需 FastAPI 的 `LoginUserApiProvider` 中间层
15. **登录记录**: 登录成功后通过 `ent.SysUser.UpdateOneID(id).SetLastLoginAt(time.Now()).SetLastLoginIP(ip).AddLoginCount(1).Save(ctx)` 更新
16. **统一所有 JSON 字段使用下划线命名**: 请求（request body JSON）和响应字段均使用下划线风格。Go struct 定义时通过 `json:"xxx"` tag 指定：

    ```go
    // 请求参数示例（符合前端传入的下划线命名）
    type UsernameLoginParam struct {
        Username    string `json:"username"`
        Password    string `json:"password"`
        CaptchaCode string `json:"captcha_code"`
        CaptchaID   string `json:"captcha_id"`
        DeviceID    string `json:"device_id"`
    }

    // 响应 VO 示例（后端返回也用下划线）
    type BannerVO struct {
        ID        string `json:"id"`
        Title     string `json:"title"`
        SortCode  int    `json:"sort_code"`
        ViewCount int    `json:"view_count"`
        CreatedAt string `json:"created_at"`
    }
    ```
