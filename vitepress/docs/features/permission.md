# 权限管理

Hei Gin 实现了一套完整的 RBAC（基于角色的访问控制）权限系统，支持角色授权和用户直授权限的双层模型，并提供数据权限（行级）控制能力。

## 数据模型

权限系统的核心数据模型包含以下实体：

```
用户 (sys_user)
  ├── 角色 (sys_role) ──── 权限 (sys_permission)
  │         │
  │         └── 资源 (sys_resource)  ← 菜单/按钮可见性控制
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

权限系统采用两级优先级模型：

- **P0（直接权限）**：通过 `rel_user_permission` 直接授予用户的权限
- **P1（角色权限）**：通过 `rel_role_permission` 经由角色授予的权限

P0 的优先级高于 P1。当用户同时通过直接授权和角色授权获得某个权限时，以直接授权的配置为准。

**特殊角色**：`SUPER_ADMIN`（超级管理员）角色自动拥有所有权限，无需显式配置。

## 权限代码

权限使用字符串代码（Permission Code）标识，遵循 `module:action` 的命名规范：

| 权限代码 | 说明 |
|---------|------|
| `sys:user:list` | 用户列表查询 |
| `sys:user:create` | 创建用户 |
| `sys:user:update` | 更新用户 |
| `sys:user:delete` | 删除用户 |
| `sys:role:list` | 角色列表查询 |
| `sys:role:assign` | 角色分配权限 |
| `sys:org:list` | 组织列表查询 |

## 权限自动发现

Hei Gin 提供权限自动发现机制，在应用启动时自动扫描所有通过 `RegisterPermission` 注册的权限，并缓存到 Redis 中。

### 实现原理

`core/auth/permission_scan.go` 中的 `RunPermissionScan` 函数在应用启动阶段执行：

1. 遍历全局注册的权限条目
2. 按模块分组构建权限树
3. 将权限树缓存到 Redis

```go
import "hei-gin/core/auth"

// 在应用启动时调用
// 将 RegisterPermission 注册的权限分组并缓存到 Redis
auth.RunPermissionScan()
```

`RunPermissionScan` 无参数。它读取 `permissionRegistry` 全局变量中所有通过 `RegisterPermission()` 注册的条目，按模块分组后以 JSON 形式写入 Redis。

### 权限注册

权限在使用 `HeiCheckPermission` 中间件注册路由时通过 `RegisterPermission` 自动注册：

```go
import (
    middleware "hei-gin/core/auth/middleware"
    "hei-gin/core/auth"
)

// 在路由注册时，权限中间件自动注册权限
sysApi.GET("/user/list",
    middleware.HeiCheckPermission("sys:user:list"),
    handler.UserList,
)

// 同时需要在初始化阶段调用 RegisterPermission
auth.RegisterPermission(auth.PermissionEntry{
    Code:   "sys:user:list",
    Module: "sys:user",
    Name:   "用户列表查询",
})
```

### Redis 缓存

扫描到的权限以 JSON 树结构存储在 Redis 中：

```
Redis Key: hei:permission:keys
Redis Type: String (JSON)
Redis Value: {"sys:user": {"sys:user:list": {"code":"sys:user:list","module":"sys:user","name":"用户列表查询"}, ...}, ...}
```

所有模块的权限存放在同一个 Redis Key 中，是一个按模块分组的 JSON 对象。

## 权限匹配器

权限匹配器（`core/auth/permission_matcher.go`）实现了灵活的通配符匹配，支持三种分隔符和两级通配符。

### 分隔符

权限匹配器支持三种分隔符：

- `:`（冒号） - 最常用，如 `sys:user:list`
- `.`（点号） - 如 `sys.user.list`
- `/`（斜杠） - 如 `sys/user/list`

### 通配符

| 通配符 | 含义 | 示例 |
|--------|------|------|
| `*` | 匹配单级（一个层级） | `sys:*:list` 匹配 `sys:user:list`、`sys:role:list` |
| `**` | 匹配多级（任意层级） | `sys:**` 匹配 `sys:user:list`、`sys:role:create`、`sys:org:delete` |

### 匹配规则

```
用户拥有的权限（模式）     →  请求检查的权限          →  结果
sys:*:list                →  sys:user:list          →  匹配 
sys:*:list                →  sys:user:create        →  不匹配 
sys:**                    →  sys:user:list          →  匹配 
sys:**                    →  sys:user:create        →  匹配 
**:list                   →  sys:user:list          →  匹配 
sys:user:*                →  sys:user:list          →  匹配 
sys:user:*                →  sys:user:list:detail   →  不匹配 
```

### 匹配实现

匹配器的核心逻辑在 `permission_matcher.go` 中，它将权限模式按分隔符拆分为片段数组，然后逐段比较：

1. 如果模式片段是 `**`，直接匹配剩余所有层级
2. 如果模式片段是 `*`，跳过一个层级
3. 否则进行精确字符串比较

## 数据权限（行级权限）

数据权限控制用户对具体数据行的访问范围。例如：用户 A 只能查看本部门的数据，用户 B 可以查看全公司的数据。

### 数据范围枚举

数据范围使用字符串常量定义，定义在 `core/enums/permission.go` 中：

| Go 常量 | 字符串值 | 说明 | 严格度优先级 |
|---------|----------|------|-------------|
| `enums.DataScopeSelf` | `SELF` | 仅本人数据 | 0（最严格） |
| `enums.DataScopeCustomGroup` | `CUSTOM_GROUP` | 自定义组范围 | 1 |
| `enums.DataScopeCustomOrg` | `CUSTOM_ORG` | 自定义组织范围 | 2 |
| `enums.DataScopeGroupAndBelow` | `GROUP_AND_BELOW` | 本组及子组数据 | 3 |
| `enums.DataScopeGroup` | `GROUP` | 本组数据 | 4 |
| `enums.DataScopeOrgAndBelow` | `ORG_AND_BELOW` | 本组织及下级组织数据 | 5 |
| `enums.DataScopeOrg` | `ORG` | 本组织数据 | 6 |
| `enums.DataScopeAll` | `ALL` | 全部数据，无限制 | 7 |

严格度由低到高（数值越小越严格），使用 `enums.MostRestrictive()` 函数确定：

```go
// MostRestrictive 返回给定数据范围中最严格的一个
// 优先级: SELF(0) < CUSTOM_GROUP(1) < CUSTOM_ORG(2) < GROUP_AND_BELOW(3) < GROUP(4) < ORG_AND_BELOW(5) < ORG(6) < ALL(7)
func MostRestrictive(scopes ...string) string
```

### 二维数据模型

数据权限采用 **组织（Org）+ 组（Group）** 二维模型：

- **组织维度**：基于组织架构树的层级控制（集团->公司->部门->小组）
- **组维度**：基于用户组的跨组织控制（可包含不同组织的用户）

### 范围合并规则

当用户同时拥有多个数据范围时，采用 **最严格限制优先**（Most Restrictive Wins）的合并策略。

合并规则如下：

- **组织维度和组维度独立计算**：两个维度分别取限制最严格的范围
- **P0（直接权限）优先于 P1（角色权限）**：优先级字符串排序 "P0" < "P1"，P0 覆盖 P1
- **同优先级取最严格**：在同一优先级，多个角色或多个直接权限的数据范围取最严格的那个

例如：

```
用户角色A：数据范围 ORG（本组织，P1 角色权限）
用户角色B：数据范围 SELF（仅本人，P1 角色权限）

在同一优先级（P1）下取最严格：
  SELF(0) < ORG(6)，SELF 胜出
  最终范围：SELF（仅本人可看）
```

跨优先级示例：

```
用户角色权限（P1）：数据范围 SELF（仅本人）
用户直授权限（P0）：数据范围 ALL（全部）

P0 优先级高于 P1，以直接授权为准：
  最终范围：ALL（全部数据）
```

### 数据权限在业务中的使用

数据权限需要在 Service 层手动应用。获取当前用户的权限信息并据此构建查询条件：

```go
import (
    authx "hei-gin/core/auth"
    "hei-gin/core/enums"
)

// 获取当前用户的权限列表
permissions, _ := authx.GetPermissionList(c, enums.LoginTypeBusiness)

// 获取数据范围
scopeMap, _ := authx.GetPermissionScopeMap(loginID, loginType)
scopeInfo := scopeMap["sys:user:list"]

// 根据 scope 构建 Ent 查询条件
query := s.client.SysUser.Query()

switch scopeInfo.OrgScope {
case string(enums.DataScopeSelf):
    query.Where(sysuser.ID(loginID))
case string(enums.DataScopeOrg):
    query.Where(sysuser.OrgID(userOrgID))
case string(enums.DataScopeOrgAndBelow):
    query.Where(sysuser.OrgIDIn(orgAndBelowIDs))
// ...
}
```

## 权限验证流程

完整的权限验证流程：

```
请求到达
    |
    v
HeiCheckPermission("sys:user:list") 中间件
    |
    ├─ 1. 从 Context 获取当前用户 ID（通过 JWT Token）
    |
    ├─ 2. 获取用户权限列表
    |     ├─ 查询用户关联的角色
    |     ├─ 查询角色关联的权限（P1）
    |     └─ 查询用户直授权限（P0）
    |
    ├─ 3. 检查 SUPER_ADMIN 角色
    |     └─ 超级管理员 -> 直接放行
    |
    ├─ 4. 权限匹配器校验
    |     ├─ 遍历用户权限列表
    |     ├─ 对每个权限执行通配符匹配
    |     ├─ 匹配到任何一个 -> 有权限
    |     └─ 全部不匹配 -> 无权限
    |
    └─ 5. 返回结果
          ├─ 有权限 -> 继续执行后续中间件和 Handler
          └─ 无权限 -> 返回 403 无权限错误
```

## 权限查询 API

权限模块提供了查询接口，用于前端展示和管理员配置：

```
# 获取所有已注册的权限模块列表
GET /api/v1/sys/permission/modules
# 响应：["sys:user", "sys:role", "sys:org", ...]

# 获取指定模块下的所有权限
GET /api/v1/sys/permission/by-module?module=sys:user
# 响应：[{"code":"sys:user:list","module":"sys:user","name":"用户列表查询"}, ...]
```

这两个接口从 Redis 缓存（`hei:permission:keys`）读取数据，返回已注册的权限信息。管理员可以通过这些接口在前端配置角色权限。
