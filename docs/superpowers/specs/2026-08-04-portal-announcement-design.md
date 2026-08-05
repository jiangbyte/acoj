# Portal 课程公告接入设计

**Date:** 2026-08-04  
**Status:** Approved  
**Scope:** 首页「课程公告」接真实已发布公告；访客可见；登录可标已读。不做独立公告页 / 铃铛 / IM。

## Context

- Admin 公告 CRUD / publish / revoke / pin 后端已存在
- Portal `my-page` / `my-detail` / `read` 已存在但需登录
- 首页「课程公告」为写死占位；Banner 轮播已是另一套真实 API，不动

## API

### 新增（可匿名）

`GET /api/v1/portal/message/announcements/list?current&size`

- `status=PUBLISHED`，未过期
- 受众：`target_scope=ALL` 或 `target_account_types` 含 `PORTAL`（与现有 my-page 一致）
- 排序：置顶 → `publish_at` 降序
- 可选登录：若带 token，填充 `is_read`

### 复用（登录）

- `GET .../my-detail?id=`
- `POST .../read` `{ ids: [...] }`

## Portal

- `api/message/announcement.ts`
- 首页拉前 5 条；空态「暂无公告」
- 点击 Modal/Drawer：标题 + 正文；登录则 `read`
- 摘要：纯文本截断；HTML 可去标签后截断

## Admin（最小）

- 创建默认 `status=DRAFT`（不再用 `ENABLED`）
- 表单状态选项对齐 `DRAFT|PUBLISHED|REVOKED`；选 `PUBLISHED` 创建时可直接上架（或依赖已有 publish；本迭代允许表单直接选已发布以便首页有数据）

## Out of scope

独立公告页、顶栏未读铃、Admin 发布/置顶按钮大改、IM
