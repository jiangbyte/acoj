# 组长管理小组设计

**Date:** 2026-08-05  
**Status:** Approved

## Goal

Portal 组长（`owner_id` / `OWNER`）可管理自己的小组：独立小组可改公开性与基本信息；两种小组均可踢人、搜索加人、刷新邀请码。

## Scope

**做：**

- Portal 专用组长 API（不复用 Admin 路由）
- 独立小组：名称、简介、人数上限、公开/私有
- 课内 + 独立：踢人、按用户搜索加人、刷新邀请码
- Portal `/teams/:id` 组长管理 UI

**不做：**

- 转让组长、入组审批、`ADMIN` 角色启用
- 课内小组改公开性
- Admin 侧改动（已有 update/member 保持不变）

## Decisions

| 项 | 选择 |
|----|------|
| 管理范围 | A：两种小组都可管成员；仅独立可改公开 |
| 成员操作 | B+C：踢人 + 搜索加人 + 刷新邀请码 |
| 独立设置 | B：公开/私有 + 名称/简介/人数上限 |
| 实现方式 | 方案 1：Portal 专用组长 API |

## Permissions

- 管理接口统一校验：`team.owner_id == session.account_id`，且 `status == ENABLED`
- 非组长调用 → 业务错误「仅组长可操作」
- 不能移除组长本人
- `max_members` 不可小于当前 `member_count`；范围仍 2–500
- 课内加人：与现有 `add_members` / 邀请加入一致——非 OPEN 课程时，被加账号须为课程关联班级成员
- 独立小组可改 `visibility`；课内请求若带 `visibility` → 显式拒绝

## Backend API

前缀：`/api/v1/portal`，均需 Portal 登录。

| Method | Path | Body / Query | 说明 |
|--------|------|--------------|------|
| POST | `/biz/team/update` | `id`, 可选 `name`/`description`/`max_members`/`visibility` | 组长更新；课内禁止 `visibility` |
| POST | `/biz/team/member/remove` | `team_id`, `account_id` | 踢人；同步踢出 IM 群 |
| POST | `/biz/team/member/add` | `team_id`, `account_ids[]` | 加人；复用课内资格校验与人数上限 |
| POST | `/biz/team/invite/refresh` | `team_id` | 生成新 8 位邀请码，旧码立即失效；返回新码 |
| GET | `/biz/team/user/search` | `keyword`, 可选 `current`/`size` | 搜 Portal 用户（用户名 / 昵称），返回 `account_id, username, nickname, avatar`；供加人弹窗 |

Service：

- `_ensure_owner(team_id, account_id) -> OjTeam`
- 踢人/加人：复用现有 `remove_members` / `_add_members_internal` 路径（含 IM）
- 刷新邀请码：复用 `_unique_invite_code()`

无新表 / 无 migration。

## Portal UI

页面：`web/portal/src/pages/teams/detail.tsx`（组长且 `ENABLED` 时显示管理能力）。

1. **设置区**（独立）：名称、简介、人数上限、公开开关 → 保存调 `update`  
   **设置区**（课内）：名称、简介、人数上限（无公开开关）
2. **邀请码**：展示 + 复制 +「刷新」二次确认 → `invite/refresh`，成功后更新展示
3. **成员表**：非组长行「移除」→ `member/remove`；「添加成员」弹窗：关键词搜索 → 勾选 → `member/add`
4. 普通成员：仍可看邀请码、退出；非成员逻辑不变
5. API：`web/portal/src/api/team.ts` 增加上述客户端方法

## Error cases

| 场景 | 行为 |
|------|------|
| 非组长 | 「仅组长可操作」 |
| 踢组长 | 「不能移除组长」 |
| 课内改公开 | 「课内小组不可修改公开性」 |
| 上限 < 当前人数 | 「人数上限不能小于当前成员数」 |
| 课内加非班级成员 | 与现 Admin 一致的业务错误 |
| 小组已解散/停用 | 「小组不可用」 |
| 搜索无结果 | 空列表 |

## Testing (manual)

1. 独立小组组长：改公开 → 出现在 `/teams` 公开列表；改回私有后列表消失  
2. 独立/课内组长：踢人后成员表与 `member_count` 更新；被踢者无法再看邀请码  
3. 搜索加人成功；课内加非成员失败  
4. 刷新邀请码后旧码 `join` 失败、新码成功  
5. 非组长调用管理 API 均失败  
6. 课内 update 带 visibility 失败  
