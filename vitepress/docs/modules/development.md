# 模块开发规范

Hei Gin 采用垂直切片（Vertical Slice）架构组织业务模块。每个模块独立包含参数定义、业务逻辑和 API 层，具有高内聚低耦合的特点。

## 模块结构约定

每个业务模块必须遵循统一的结构约定：

```
modules/<domain>/<module>/
├── params.go              # 请求参数和响应结构体
├── service.go             # 业务逻辑层
└── api/v1/
    └── api.go             # 路由注册 + HTTP Handler
```

### 文件名说明

| 文件 | 必选 | 说明 |
|------|------|------|
| `params.go` | 是 | 定义请求参数结构体和响应结构体 |
| `service.go` | 是 | 业务逻辑层，调用 Ent DAO 进行数据操作 |
| `api/v1/api.go` | 是 | 路由注册函数和 HTTP Handler（控制器）|

## 文件模板

### params.go - 参数定义

```go
package sysuser

import "hei-gin/core/enums"

// ---------- 请求参数 ----------

// UserListReq 用户列表查询参数
type UserListReq struct {
    Page     int    `json:"page" form:"page"`
    PageSize int    `json:"page_size" form:"page_size"`
    Username string `json:"username" form:"username"`
    Status   int    `json:"status" form:"status"`
    OrgID    string `json:"org_id" form:"org_id"`
}

// UserCreateReq 创建用户请求参数
type UserCreateReq struct {
    Username string   `json:"username" binding:"required"`
    Password string   `json:"password" binding:"required"`
    RealName string   `json:"real_name" binding:"required"`
    Email    string   `json:"email"`
    Phone    string   `json:"phone"`
    OrgID    string   `json:"org_id"`
    RoleIDs  []string `json:"role_ids"`
}

// UserUpdateReq 更新用户请求参数
type UserUpdateReq struct {
    ID       string   `json:"id" binding:"required"`
    RealName string   `json:"real_name"`
    Email    string   `json:"email"`
    Phone    string   `json:"phone"`
    Status   int      `json:"status"`
    OrgID    string   `json:"org_id"`
    RoleIDs  []string `json:"role_ids"`
}

// ---------- 响应结构体 ----------

// UserListResp 用户列表响应
type UserListResp struct {
    Total int          `json:"total"`
    List  []*UserItem  `json:"list"`
}

// UserItem 用户列表项
type UserItem struct {
    ID        string `json:"id"`
    Username  string `json:"username"`
    RealName  string `json:"real_name"`
    Email     string `json:"email"`
    Status    int    `json:"status"`
    CreatedAt int64  `json:"created_at"`
}
```

### service.go - 业务逻辑

```go
package sysuser

import (
    "context"
    "golang.org/x/crypto/bcrypt"
    "hei-gin/core/exception"
    "hei-gin/ent"
    "hei-gin/ent/sysuser"
)

// Service 用户管理业务逻辑
type Service struct {
    client *ent.Client
}

// NewService 创建 Service 实例
func NewService(client *ent.Client) *Service {
    return &Service{client: client}
}

// List 用户列表查询
func (s *Service) List(ctx context.Context, req *UserListReq) (*UserListResp, error) {
    query := s.client.SysUser.Query()

    // 条件查询
    if req.Username != "" {
        query = query.Where(sysuser.UsernameContains(req.Username))
    }
    if req.Status > 0 {
        query = query.Where(sysuser.StatusEQ(req.Status))
    }
    if req.OrgID != "" {
        query = query.Where(sysuser.OrgID(req.OrgID))
    }

    // 分页
    total, err := query.Count(ctx)
    if err != nil {
        return nil, err
    }

    users, err := query.
        Offset((req.Page - 1) * req.PageSize).
        Limit(req.PageSize).
        Order(ent.Desc(sysuser.FieldCreatedAt)).
        All(ctx)
    if err != nil {
        return nil, err
    }

    // 转换为响应结构
    items := make([]*UserItem, 0, len(users))
    for _, u := range users {
        items = append(items, &UserItem{
            ID:        u.ID,
            Username:  u.Username,
            RealName:  u.RealName,
            Email:     u.Email,
            Status:    u.Status,
            CreatedAt: u.CreatedAt,
        })
    }

    return &UserListResp{
        Total: total,
        List:  items,
    }, nil
}

// Create 创建用户
func (s *Service) Create(ctx context.Context, req *UserCreateReq) error {
    // 参数校验
    exist, err := s.client.SysUser.Query().
        Where(sysuser.UsernameEQ(req.Username)).
        Exist(ctx)
    if err != nil {
        return err
    }
    if exist {
        return exception.NewBusinessError("用户名已存在", 400)
    }

    // bcrypt 加密密码（直接使用 bcrypt 包，无 utils.HashPassword 封装）
    hashedPwd, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
    if err != nil {
        return err
    }

    // 创建用户
    return s.client.SysUser.Create().
        SetUsername(req.Username).
        SetPassword(string(hashedPwd)).
        SetRealName(req.RealName).
        SetEmail(req.Email).
        SetPhone(req.Phone).
        SetOrgID(req.OrgID).
        Exec(ctx)
}
```

### api/v1/api.go - 路由与 Handler

```go
package sysuser

import (
    "github.com/gin-gonic/gin"
    "hei-gin/core/result"
    middleware "hei-gin/core/auth/middleware"
    "hei-gin/core/log"
    "hei-gin/core/exception"
)

// Handler 用户管理控制器
type Handler struct {
    svc *Service
}

// NewHandler 创建 Handler 实例
func NewHandler(svc *Service) *Handler {
    return &Handler{svc: svc}
}

// RegisterRoutes 注册路由（模块入口）
func (h *Handler) RegisterRoutes(r *gin.RouterGroup) {
    // 用户管理路由组 - 需要登录 + 权限
    r.GET("/list",
        middleware.HeiCheckLogin(),
        middleware.HeiCheckPermission("sys:user:list"),
        h.List,
    )

    r.POST("/create",
        middleware.HeiCheckLogin(),
        middleware.HeiCheckPermission("sys:user:create"),
        log.SysLog("创建用户"),
        middleware.NoRepeat(),
        h.Create,
    )

    r.POST("/update",
        middleware.HeiCheckLogin(),
        middleware.HeiCheckPermission("sys:user:update"),
        log.SysLog("更新用户"),
        h.Update,
    )

    r.POST("/delete",
        middleware.HeiCheckLogin(),
        middleware.HeiCheckPermission("sys:user:delete"),
        log.SysLog("删除用户"),
        h.Delete,
    )
}

// ---------- Handler 方法 ----------

func (h *Handler) List(c *gin.Context) {
    var req UserListReq
    if err := c.ShouldBindQuery(&req); err != nil {
        c.JSON(200, result.Failure(c, "参数错误", 400, nil))
        return
    }

    resp, err := h.svc.List(c.Request.Context(), &req)
    if err != nil {
        c.JSON(200, result.Failure(c, err.Error(), 500, nil))
        return
    }

    c.JSON(200, result.Success(c, resp))
}

func (h *Handler) Create(c *gin.Context) {
    var req UserCreateReq
    if err := c.ShouldBindJSON(&req); err != nil {
        c.JSON(200, result.Failure(c, "参数错误", 400, nil))
        return
    }

    if err := h.svc.Create(c.Request.Context(), &req); err != nil {
        if bizErr, ok := err.(*exception.BusinessError); ok {
            panic(bizErr)
        }
        c.JSON(200, result.Failure(c, "创建失败", 500, nil))
        return
    }

    c.JSON(200, result.Success(c, nil))
}
```

## 路由注册

在模块创建完成后，需要在 `core/app/router.go` 中注册模块路由：

```go
// core/app/router.go

func RegisterRouters(r *gin.Engine, client *ent.Client) {
    // ... 现有路由 ...

    // 注册 B 端路由组
    sysApi := r.Group("/api/v1/sys")
    {
        // ... 已有模块 ...

        // 注册新模块
        userSvc := sysuser.NewService(client)
        userHdl := sysuser.NewHandler(userSvc)
        userGroup := sysApi.Group("/user")
        userHdl.RegisterRoutes(userGroup)
    }
}
```

## 创建新模块的完整步骤

### 第一步：创建模块目录

```bash
mkdir -p modules/sys/<module>/api/v1
```

### 第二步：创建 Ent Schema

在 `ent/schema/` 下创建对应的 Schema 文件：

```go
// ent/schema/sys<entity>.go
package schema

import (
    "entgo.io/ent"
    "entgo.io/ent/schema/field"
)

type Sys<Entity> struct {
    ent.Schema
}

func (Sys<Entity>) Fields() []ent.Field {
    return []ent.Field{
        field.String("id"),                    // 雪花 ID
        field.String("name"),                  // 名称
        field.Int("status").Default(1),        // 状态
        field.Int64("created_at"),             // 创建时间
        field.String("created_by").Optional(), // 创建人
        field.Int64("updated_at"),             // 更新时间
        field.String("updated_by").Optional(), // 更新人
    }
}
```

### 第三步：生成 Ent 代码

```bash
go generate ./ent
```

这会自动生成 CRUD 操作代码到 `ent/gen/` 目录。

### 第四步：实现 params.go

定义请求和响应结构体。

### 第五步：实现 service.go

实现业务逻辑，调用 Ent 生成的 DAO 方法。

### 第六步：实现 api/v1/api.go

实现 HTTP Handler 和路由注册。

### 第七步：在 router.go 中注册

将模块路由挂载到对应的路由组。

## 权限代码命名规范

权限代码使用统一的命名规范：`<模块>:<操作>`。

### 标准操作

| 权限代码 | 说明 |
|---------|------|
| `<module>:list` | 列表查询 |
| `<module>:create` | 新增 |
| `<module>:update` | 修改 |
| `<module>:delete` | 删除 |
| `<module>:detail` | 详情查询 |
| `<module>:export` | 导出 |
| `<module>:import` | 导入 |

### 示例

| 模块 | 权限代码示例 |
|------|------------|
| 用户管理 | `sys:user:list`, `sys:user:create`, `sys:user:delete` |
| 角色管理 | `sys:role:list`, `sys:role:assign` |
| 配置管理 | `sys:config:list`, `sys:config:update` |
| 字典管理 | `sys:dict:list`, `sys:dict:create`, `sys:dict:delete` |
| C 端订单 | `client:order:list`, `client:order:create` |

## 技术要点

### 统一响应

所有 API 响应必须使用 `result` 包提供的函数：

```go
import "hei-gin/core/result"

// 成功响应
c.JSON(200, result.Success(c, data))

// 失败响应
c.JSON(200, result.Failure(c, message, code, data))
```

函数签名说明：

```go
// Success 返回标准成功响应
func Success(c *gin.Context, data any) gin.H

// Failure 返回标准失败响应，参数顺序为：(c, message, code, data)
func Failure(c *gin.Context, message string, code int, data any) gin.H
```

注意：`result.Failure()` 的参数顺序是 `(c, message, code, data)`，与常见的 `(c, code, message)` 顺序不同。

### 业务异常

业务错误使用 `panic` + `BusinessError` 模式：

```go
import "hei-gin/core/exception"

// 抛出业务异常
panic(exception.NewBusinessError("用户名已存在", 400))

// BusinessError 结构体定义
type BusinessError struct {
    Message string
    Code    int
}

// 构造函数
func NewBusinessError(message string, code int) *BusinessError
```

`SysLog` 中间件会自动捕获 `panic(exception.BusinessError)`，记录异常日志后重新 panic，由 Gin 的 Recovery 中间件返回统一响应。

### 获取当前登录用户

```go
import authx "hei-gin/core/auth"

// 获取当前登录用户 ID（返回 string，空字符串表示未登录）
loginID := authx.GetLoginID(c)
if loginID == "" {
    panic(exception.NewBusinessError("未登录", 401))
}
```

`authx.GetLoginID(c *gin.Context)` 从当前请求的 JWT Token 中解析出用户 ID，返回一个 `string` 类型值，未登录时返回空字符串。

### 分页查询

```go
// 标准分页参数
page := req.Page
if page < 1 {
    page = 1
}
pageSize := req.PageSize
if pageSize < 1 || pageSize > 100 {
    pageSize = 20
}

// Ent 分页查询
total, err := query.Count(ctx)
data, err := query.
    Offset((page - 1) * pageSize).
    Limit(pageSize).
    Order(ent.Desc(sysuser.FieldCreatedAt)).
    All(ctx)
```

## 现有模块参考

编写新模块时，可以参考以下现有模块的实现：

- **sys/auth**：认证模块，包含登录、注册、验证码、SM2 公钥。结构为 `route.go` 作为入口，内含三个子模块目录：`captcha/`（验证码）、`sm2/`（SM2 公钥）、`username/`（用户名密码登录）
- **sys/user**：用户管理，标准的 CRUD 模块
- **sys/role**：角色管理，包含权限分配
- **sys/org**：组织管理，树形结构
- **sys/banner**：Banner 管理，包含文件上传

每个模块都遵循相同的 `params.go` + `service.go` + `api/v1/api.go` 结构。对于复杂的模块（如 sys/auth），可以在模块内进一步划分子模块目录，并通过顶层的 `route.go` 统一注册子路由。
