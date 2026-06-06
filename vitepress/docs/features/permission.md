# 权限管理

Hei Gin 实现了一套完整的 RBAC（基于角色的访问控制）权限系统，采用 `PermissionDelegate` 接口模式，支持角色授权和用户直授权限的双层模型，并提供数据权限（行级）控制能力。

## 架构

权限系统基于 `sdk/auth` 中的接口 + 插件注册的方式：

```
sdk/auth/                     ← 定义接口和通用逻辑
├── permission_interface.go   ← PermissionAPI 接口定义 + 接口注册
├── permission_matcher.go     ← 通配符匹配（* / **）
├── permission_scan.go        ← 权限注册与 Redis 缓存
└── permission_tool.go        ← 权限/角色查询门面函数

sdk/registry/                 ← 快捷注册
└── perm.go                   ← Perm() / ClientPerm() 快捷方式

plugins/plugin-sys/           ← 提供具体实现
└── provider/
    └── permission_provider.go ← 实现 PermissionAPI 接口
```

### PermissionDelegate 机制

```go
// sdk/auth/permission_interface.go
var PermissionDelegate api.PermissionAPI

// 由插件在 Init() 中注册具体实现
func RegisterInterface(impl api.PermissionAPI) {
    PermissionDelegate = impl
}

// 插件侧的注册（plugin-sys/plugin.go）：
auth.RegisterInterface(p.permProvider)
```

所有权限查询函数（`GetPermissionList`, `GetRoleList` 等）都委托给 `PermissionDelegate`。

## 权限注册

### 方式一：registry.Perm（推荐，最常用）

`sdk/registry/perm.go` 提供 `Perm()` 和 `ClientPerm()` 函数，同时完成权限注册和中间件返回，自带去重保护：

```go
import "hei-gin/sdk/registry"

// 同时完成两件事：
// 1. 注册权限（sync.Map 去重，仅首次执行）
// 2. 返回 HeiCheckPermission 中间件
g.GET("/list", registry.Perm("sys:user:list", "用户列表查询"), handler.List)
g.POST("/create", registry.Perm("sys:user:create", "创建用户"), handler.Create)
```

内部实现：

```go
func Perm(code, name string) gin.HandlerFunc {
    auth.RegisterPermission(auth.PermissionEntry{
        Code:   code,
        Module: moduleFromCode(code),
        Name:   name,
    })
    return middleware.HeiCheckPermission([]string{code})
}
```

`ClientPerm` 对应 C 端，返回 `HeiClientCheckPermission([]string{code})`。

### 方式二：手动注册 + 中间件

适用于需要多个权限组合（AND/OR）的场景：

```go
import (
    "hei-gin/sdk/auth"
    authMiddleware "hei-gin/sdk/auth/middleware"
)

func init() {
    auth.RegisterPermission(auth.PermissionEntry{
        Code:   "sys:user:list",
        Module: "sys:user",
        Name:   "用户列表查询",
    })
}

// 路由注册时使用中间件
sysApi.GET("/user/list",
    authMiddleware.HeiCheckPermission([]string{"sys:user:list"}),
    handler.UserList,
)

// 多权限组合（OR 模式：任一匹配即可）
sysApi.GET("/user/manage",
    authMiddleware.HeiCheckPermission([]string{"sys:user:create", "sys:user:update"}, "OR"),
    handler.UserManage,
)
```

## 权限自动发现

`RunPermissionScan()` 在 `authModule.Start()` 中自动调用：

1. 遍历全局注册的 `permissionRegistry`
2. 按模块分组构建权限树
3. 以 JSON 格式缓存到 Redis `hei:permission:keys`（TTL=0 永不过期）

```go
// 手动触发（重启后自动执行）
auth.RunPermissionScan()

// 从 Redis 查询已缓存的权限模块
modules, _ := auth.GetModulesFromRedis()

// 查询指定模块的权限列表
perms, _ := auth.GetPermissionsByModuleFromRedis("sys:user")
```

### PermissionEntry

```go
type PermissionEntry struct {
    Code   string `json:"code"`    // 权限代码，如 "sys:user:list"
    Module string `json:"module"`  // 所属模块，如 "sys:user"
    Name   string `json:"name"`    // 权限名称，如 "用户列表查询"
}
```

## 权限匹配器

`sdk/auth/permission_matcher.go` 实现了灵活的通配符匹配。

### 分隔符

- `:`（冒号） — 最常用，如 `sys:user:list`
- `.`（点号） — 如 `sys.user.list`
- `/`（斜杠） — 如 `sys/user/list`

分隔符自动检测：优先 `/` > `:` > `.`。

### 通配符

| 通配符 | 含义 | 示例 |
|--------|------|------|
| `*` | 匹配单级（一个层级） | `sys:*:list` 匹配 `sys:user:list`、`sys:role:list` |
| `**` | 匹配多级（任意层级） | `sys:**` 匹配 `sys:user:list`、`sys:role:create` |

### 匹配函数

```go
auth.Match(pattern, permission string) bool                    // 单个匹配
auth.MatchAny(patterns []string, permission string) bool       // 匹配任意一个模式
auth.MatchAll(patterns []string, permission string) bool       // 匹配全部模式
auth.MatchPermission(required string, permissions []string) bool       // 检查是否在权限列表中
auth.MatchPermissionsAnd(required []string, permissions []string) bool // 全部匹配
auth.MatchPermissionsOr(required []string, permissions []string) bool  // 任一匹配
```

### 匹配规则

```
用户拥有的权限（模式）     →  请求检查的权限          →  结果
sys:*:list                →  sys:user:list          →  匹配
sys:*:list                →  sys:user:create        →  不匹配
sys:**                    →  sys:user:list          →  匹配
sys:**                    →  sys:user:create        →  匹配
**:list                   →  sys:user:list          →  匹配
sys:user:*                →  sys:user:list          →  匹配
sys:user:*                →  sys:user:list:detail   →  不匹配（层级不同）
```

## 数据模型

权限系统的数据模型由 `plugin-sys` 插件中的 GORM 模型定义：

```
用户 (sys_user)
  ├── 角色 (sys_role) ──── 权限 (sys_permission)
  │         │
  │         └── 资源 (sys_resource)  ← 菜单/按钮可见性
  │
  └── 直授权限 (rel_user_permission) ← 直接关联的权限
```

### 关联关系

| 关联表 | 说明 |
|--------|------|
| `rel_user_role` | 用户与角色的多对多关联 |
| `rel_role_permission` | 角色与权限的多对多关联 |
| `rel_user_permission` | 用户与权限的直接关联（P0）|
| `rel_role_resource` | 角色与资源（菜单/按钮）的关联 |

### 权限优先级

- **P0（直接权限）**：通过 `rel_user_permission` 直接授予，优先级高于 P1
- **P1（角色权限）**：通过 `rel_role_permission` 经由角色授予
- **SUPER_ADMIN**：自动拥有所有权限，无需显式配置

### 权限代码示例

| 权限代码 | 说明 |
|---------|------|
| `sys:user:list` | 用户列表查询 |
| `sys:user:create` | 创建用户 |
| `sys:user:update` | 更新用户 |
| `sys:user:delete` | 删除用户 |
| `sys:role:list` | 角色列表查询 |
| `sys:role:assign` | 角色分配权限 |

## 数据权限（行级）

数据权限控制用户对具体数据行的访问范围。

### 数据范围枚举

`sdk/enums/permission.go`：

| Go 常量 | 值 | 说明 | 严格度 |
|---------|-----|------|-------|
| `DataScopeSelf` | `SELF` | 仅本人数据 | 0（最严格）|
| `DataScopeCustomGroup` | `CUSTOM_GROUP` | 自定义组范围 | 1 |
| `DataScopeCustomOrg` | `CUSTOM_ORG` | 自定义组织范围 | 2 |
| `DataScopeGroupAndBelow` | `GROUP_AND_BELOW` | 本组及子组数据 | 3 |
| `DataScopeGroup` | `GROUP` | 本组数据 | 4 |
| `DataScopeOrgAndBelow` | `ORG_AND_BELOW` | 本组织及下级 | 5 |
| `DataScopeOrg` | `ORG` | 本组织数据 | 6 |
| `DataScopeAll` | `ALL` | 全部数据 | 7 |

### 最严格限制优先

```go
func MostRestrictive(scopes ...string) string
// 优先级: SELF(0) < CUSTOM_GROUP(1) < ... < ORG(6) < ALL(7)
```

## 权限验证流程

```
请求到达
    |
    v
HeiCheckPermission([]string{"sys:user:list"}) 或 registry.Perm("sys:user:list", "用户列表查询")
    |
    ├─ 1. 检查登录状态（未登录 → 401）
    |
    ├─ 2. 检查 SUPER_ADMIN 角色（超级管理员 → 放行）
    |
    ├─ 3. 通过 PermissionDelegate.GetPermissionList() 获取用户权限列表
    |     ├─ 查询角色关联的权限（P1）
    |     └─ 查询用户直授权限（P0）
    |
    ├─ 4. 权限匹配器校验（AND 或 OR 模式）
    |     ├─ 遍历用户权限列表，通配符匹配
    |     ├─ 匹配到任何一个 → 有权限
    |     └─ 全部不匹配 → 无权限
    |
    └─ 5. 返回结果
          ├─ 有权限 → 放行
          └─ 无权限 → 403
```

## 权限查询 API

```http
GET /api/v1/sys/permission/modules
# ["sys:user", "sys:role", "sys:org", ...]

GET /api/v1/sys/permission/by-module?module=sys:user
# [{"code":"sys:user:list","module":"sys:user","name":"用户列表查询"}, ...]
```
