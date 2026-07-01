# IAM 授权与数据权限设计

## 总体链路

IAM 由认证、功能权限、资源授权、权限授权和数据权限组成：

- 认证：登录后生成 `SessionPayload`，缓存账号、角色、用户组、部门、资源、权限和数据权限上下文。
- 功能权限：路由使用 `require_permission("module:resource:action")`，只判断当前 session 是否拥有该权限 key。
- 资源授权：主体拥有资源节点后，继承资源绑定的权限项，用于菜单、页面、按钮等资源可见性和基础权限推导。
- 权限授权：主体直接拥有某个 `permission_key`，并可为该权限设置数据范围。
- 数据权限：业务查询根据当前 session 的权限授权，把 `ALL / SELF / DEPT / DEPT_AND_CHILD / CUSTOM` 转换为 SQL 条件。

## 授权主体和入口

公开授权入口只使用业务专用接口：

- 账号：`/sys/accounts/own-*`、`/sys/accounts/grant-*`
- 角色：`/sys/roles/own-*`、`/sys/roles/grant-*`
- 用户组：`/sys/groups/own-*`、`/sys/groups/grant-*`

通用 grant 路由不再公开注册。底层 `GrantRepository` 只作为内部复用能力，避免出现两套授权入口。

## 资源授权和权限授权语义

资源授权只表达主体拥有资源：

- 资源节点来自 `sys_resource`。
- 资源绑定权限来自 `sys_resource_permission_rel`。
- `CASCADE` 资源授权会把资源绑定的权限推导成有效权限。

权限授权只表达主体直接拥有或覆盖某个权限：

- 权限 key 必须来自 Redis 中的权限注册表。
- 权限授权携带 `data_scope` 和 `custom_scope_dept_ids`。
- 有效权限合并优先级为：资源绑定权限 < 角色权限 < 用户组权限 < 账号权限。
- 高优先级 `DENY` 会移除该权限的最终有效授权。

## 数据权限规则

统一使用 `app.core.security.data_scope.build_data_scope_filter` 构造 SQLAlchemy 条件。

规则：

- `ALL`：不追加数据过滤。
- `SELF`：`owner_column == session.account_id`。
- `DEPT`：`dept_column in session.dept_ids`。
- `DEPT_AND_CHILD`：一次性读取部门树，在内存中计算当前部门及所有子部门。
- `CUSTOM`：`dept_column in custom_scope_dept_ids`。
- 没有匹配的权限授权时默认 `SELF`，避免越权。
- 缺少所需字段时返回 `false()`，不放开数据。

接入示例：

```python
from app.core.security.data_scope import build_data_scope_filter

condition = await build_data_scope_filter(
    db,
    session,
    "file:file:page",
    owner_column=SysFile.created_by,
    dept_column=None,
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

## Redis 权限注册

权限注册强依赖 Redis：

- 应用启动时扫描所有 `require_permission(...)` 路由并同步到 Redis。
- Redis 不可用时应用启动失败。
- 授权时只允许授予 Redis 注册表中存在的权限 key。
- 不再使用进程内存权限注册表作为兜底。

本地开发和测试真实应用启动时必须先启动 Redis。单元测试如需绕过权限注册，应通过 monkeypatch 精确替换对应服务中的 `list_registered_permission_keys` 或 `ensure_registered_permission`。
