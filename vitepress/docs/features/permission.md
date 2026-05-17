# 权限管理

Hei FastAPI 实现了一套完整的 RBAC（基于角色的访问控制）权限系统，支持角色授权和用户直授权限的双层模型，并提供数据权限（行级）控制能力。

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
| `sys:user:modify` | 修改用户 |
| `sys:user:remove` | 删除用户 |
| `sys:role:list` | 角色列表查询 |
| `sys:role:grant-permission` | 角色分配权限 |
| `sys:banner:page` | Banner 分页查询 |

## 权限自动发现

Hei FastAPI 提供权限自动发现机制，在应用启动时自动扫描所有 `@HeiCheckPermission` 装饰器，并缓存到 Redis 中。

### 实现原理

`core/auth/permission_scan.py` 中的 `run_permission_scan` 函数在应用启动阶段执行：

1. 遍历所有已注册的 FastAPI 路由
2. 检查路由的 `endpoint` 是否附加了 `HeiCheckPermission` 元数据
3. 提取权限代码并按模块分组
4. 将权限树以 JSON 形式缓存到 Redis

```python
from core.auth.permission_scan import run_permission_scan

# 在应用启动时调用（自动在 lifespan 中执行，异步函数）
run_permission_scan(app)
```

### 权限注册

权限在编写路由时通过 `@HeiCheckPermission` 装饰器自动注册，无需额外配置：

```python
from core.auth.decorator import HeiCheckPermission

@router.get("/api/v1/sys/user/page")
@HeiCheckPermission("sys:user:page")
async def page(request: Request, db: Session = Depends(get_db)):
    """用户列表查询"""
    ...
```

装饰器会自动将权限元数据附加到 endpoint 函数上，启动时由扫描器收集。

### Redis 缓存

扫描到的权限以 JSON 树结构存储在 Redis 中：

```
Redis Key: hei:permission:keys
Redis Type: String (JSON)
Redis Value: {
  "sys:user": {
    "sys:user:page": {"code": "sys:user:page", "name": "用户列表查询"},
    "sys:user:create": {"code": "sys:user:create", "name": "创建用户"}
  },
  ...
}
```

## 权限匹配器

权限匹配器（`core/auth/permission/hei_permission_matcher.py`）实现了灵活的通配符匹配。

### 分隔符

权限匹配器支持多种分隔符：

- `:`（冒号） — 最常用，如 `sys:user:list`
- `.`（点号） — 如 `sys.user.list`
- `/`（斜杠） — 如 `sys/user/list`

### 通配符

| 通配符 | 含义 | 示例 |
|--------|------|------|
| `*` | 匹配单级（一个层级） | `sys:*:list` 匹配 `sys:user:list` |
| `**` | 匹配多级（任意层级） | `sys:**` 匹配所有 `sys` 开头的权限 |

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

## 数据权限（行级权限）

数据权限控制用户对具体数据行的访问范围。例如：用户 A 只能查看本部门的数据，用户 B 可以查看全公司的数据。

### 数据范围枚举

| 枚举值 | 说明 | 严格度优先级 |
|--------|------|-------------|
| `SELF` | 仅本人数据 | 0（最严格） |
| `CUSTOM_GROUP` | 自定义组范围 | 1 |
| `CUSTOM_ORG` | 自定义组织范围 | 2 |
| `GROUP_AND_BELOW` | 本组及子组数据 | 3 |
| `GROUP` | 本组数据 | 4 |
| `ORG_AND_BELOW` | 本组织及下级数据 | 5 |
| `ORG` | 本组织数据 | 6 |
| `ALL` | 全部数据（无限制） | 7 |

### 数据权限合并规则

当用户同时拥有多个数据范围时，框架会自动合并：

1. **组织维度和组维度独立计算**：两个维度分别取限制最严格的范围
2. **P0（直接权限）优先于 P1（角色权限）**
3. **同优先级取最严格**：多个角色或多个直接权限的数据范围取最严格的那个

```
用户角色A：数据范围 ORG（本组织，P1）
用户角色B：数据范围 SELF（仅本人，P1）

在同一优先级（P1）下取最严格：
  SELF(0) < ORG(6)，SELF 胜出
  最终范围：SELF（仅本人可看）
```

## 权限验证流程

```
请求到达
    |
    v
@HeiCheckPermission("sys:user:list") 装饰器
    |
    ├─ 1. 从 Request 获取当前用户 ID（通过 JWT Token）
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
          ├─ 有权限 -> 继续执行后续逻辑
          └─ 无权限 -> 返回 403 无权限错误
```

## 权限查询 API

权限模块提供了查询接口，用于前端展示和管理员配置：

```http
# 获取所有已注册的权限模块列表
GET /api/v1/sys/permission/modules
# 响应：["sys:user", "sys:role", "sys:org", ...]

# 获取指定模块下的所有权限
GET /api/v1/sys/permission/by-module?module=sys:user
# 响应：[{"code":"sys:user:page", "name":"用户列表查询"}, ...]
```

这两个接口从 Redis 缓存（`hei:permission:keys`）读取数据。管理员可以通过这些接口在前端配置角色权限。
