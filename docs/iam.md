# IAM 授权与数据权限设计

本文描述当前项目的 IAM/RBAC 实现。管理端使用 `ADMIN` 账号体系，门户端使用 `PORTAL` 账号体系；
两者登录态、路由入口和前端应用彼此独立。

## 总体链路

IAM 由认证会话、资源授权、权限授权、功能权限判断和数据权限过滤组成：

- 认证会话：登录后生成 `SessionPayload`，写入 Redis。
- 资源授权：主体拥有资源节点后，可看到菜单、页面、按钮，并继承资源绑定的权限项。
- 权限授权：主体直接拥有某个 `permission_key`，可设置数据范围和授权效果。
- 功能权限：路由使用 `require_permission("module:resource:action")` 判断当前 session 是否拥有权限 key。
- 数据权限：业务查询使用当前 session 的权限授权，把数据范围转换为 SQLAlchemy 条件。

`SessionPayload` 当前缓存字段包括：

- `account_id`
- `account_type`
- `role_ids`
- `dept_ids`
- `group_ids`
- `resource_ids`
- `button_codes`
- `permission_keys`
- `permission_grants`

授权变更后，服务会批量刷新相关账号在线 session，避免用户必须重新登录才能拿到新权限。

## 路由前缀和账号边界

管理端 IAM 接口统一挂载在：

```text
/api/v1/admin/*
```

门户端资源接口挂载在：

```text
/api/v1/portal/*
```

后端路由通过 `require_account_type(AccountType.ADMIN)` 或 `require_account_type(AccountType.PORTAL)`
限制账号体系。admin 前端不应调用 portal 接口，portal 前端也不应调用 admin 接口。

## 授权主体和入口

公开授权入口使用业务专用接口，不公开通用 grant 路由。底层 `GrantRepository` 只作为内部复用能力。

管理端当前授权入口包含：

账号：

- `GET /api/v1/admin/sys/accounts/own-permission`
- `GET /api/v1/admin/sys/accounts/own-permission-detail`
- `POST /api/v1/admin/sys/accounts/grant-permission`
- `GET /api/v1/admin/sys/accounts/own-resource`
- `POST /api/v1/admin/sys/accounts/grant-resource`
- `GET /api/v1/admin/sys/accounts/own-role`
- `POST /api/v1/admin/sys/accounts/grant-role`
- `GET /api/v1/admin/sys/accounts/own-group`
- `POST /api/v1/admin/sys/accounts/grant-group`
- `GET /api/v1/admin/sys/accounts/own-dept`
- `POST /api/v1/admin/sys/accounts/grant-dept`

角色：

- `GET /api/v1/admin/sys/roles/own-permission`
- `GET /api/v1/admin/sys/roles/own-permission-detail`
- `POST /api/v1/admin/sys/roles/grant-permission`
- `GET /api/v1/admin/sys/roles/own-resource`
- `POST /api/v1/admin/sys/roles/grant-resource`
- `GET /api/v1/admin/sys/roles/own-user`
- `POST /api/v1/admin/sys/roles/grant-user`

用户组：

- `GET /api/v1/admin/sys/groups/own-permission`
- `GET /api/v1/admin/sys/groups/own-permission-detail`
- `POST /api/v1/admin/sys/groups/grant-permission`
- `GET /api/v1/admin/sys/groups/own-resource`
- `POST /api/v1/admin/sys/groups/grant-resource`
- `GET /api/v1/admin/sys/groups/own-role`
- `POST /api/v1/admin/sys/groups/grant-role`
- `GET /api/v1/admin/sys/groups/own-user`
- `POST /api/v1/admin/sys/groups/grant-user`

## 资源授权和权限授权语义

资源授权表达“主体拥有哪些资源”：

- 资源节点来自 `sys_resource`。
- 资源绑定权限来自 `sys_resource_permission_rel`。
- 资源可用于菜单、页面、按钮和接口权限推导。
- `CASCADE` 资源授权会把资源绑定的权限推导为有效权限。

权限授权表达“主体直接拥有或覆盖某个权限”：

- 权限 key 必须存在于 Redis 权限注册表。
- 权限授权携带 `data_scope`、`custom_scope_dept_ids`、`effect`。
- 有效权限合并优先级为：资源绑定权限 < 角色权限 < 用户组权限 < 账号权限。
- 高优先级 `DENY` 会移除该权限的最终有效授权。

## 权限注册表

权限注册强依赖 Redis：

- 应用启动时扫描所有带 `require_permission(...)` 的 FastAPI 路由。
- 扫描结果写入 Redis。
- 授权时只允许授予 Redis 注册表中存在的权限 key。
- Redis 不可用时应用启动失败。
- 不使用进程内权限注册表作为兜底。

路由路径会在注册时归一化：

- 去掉 `/api/v1` 等版本前缀。
- 去掉路由 tag 对应的入口前缀，如 `/admin`、`/portal`。
- 一个权限 key 只保留一条注册记录。

本地开发和测试真实应用启动时必须先启动 Redis。单元测试如需绕过权限注册，应通过 monkeypatch 精确替换
对应服务中的 `list_registered_permission_keys` 或 `ensure_registered_permission`。

## 数据权限规则

统一使用 `app.core.security.data_scope.build_data_scope_filter` 构造 SQLAlchemy 条件。

支持的数据范围：

- `ALL`：不追加数据过滤。
- `SELF`：`owner_column == session.account_id`。
- `DEPT`：`dept_column in session.dept_ids`。
- `DEPT_AND_CHILD`：读取部门树，在内存中计算当前部门及所有子部门。
- `CUSTOM`：`dept_column in custom_scope_dept_ids`。

默认安全策略：

- 有 `*:*:*` 权限时视为不受数据权限限制。
- 没有匹配权限授权时默认 `SELF`。
- 缺少 `owner_column` 或 `dept_column` 时返回 `false()`，不放开数据。
- `ALL` 返回 `true()`。

接入示例：

```python
from sqlalchemy import select

from app.core.security.data_scope import build_data_scope_filter

condition = await build_data_scope_filter(
    db,
    session,
    "sys:file:page",
    owner_column=SysFile.created_by,
)
stmt = select(SysFile).where(condition)
```

带部门字段的业务表应同时传入 `owner_column` 和 `dept_column`：

```python
condition = await build_data_scope_filter(
    db,
    session,
    "biz:order:page",
    owner_column=BizOrder.created_by,
    dept_column=BizOrder.dept_id,
)
```

如果业务只需要解析部门 ID 列表，可以使用：

```python
from app.core.security.data_scope import resolve_data_scope_dept_ids

dept_ids = await resolve_data_scope_dept_ids(db, session, "biz:order:page")
```

返回值含义：

- `None`：不限制部门。
- `[]`：没有可见部门。
- 非空列表：限制在这些部门内。

## 前端资源使用

管理端默认通过：

```text
/api/v1/admin/sys/resources/current
```

获取当前账号资源树，再生成动态路由和菜单。

门户端默认通过：

```text
/api/v1/portal/sys/resources/current
```

获取门户资源。用户中心、认证页、错误页等基础页面仍由前端静态路由承载。
