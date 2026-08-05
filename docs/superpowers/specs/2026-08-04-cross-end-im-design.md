# 跨端 IM 打通设计

**Date:** 2026-08-04  
**Status:** Approved

## Goal

打通 Admin + Portal 即时通讯：跨端好友、混合群聊；Admin 补齐群管理缺口；Portal 完整聊天中心与 WebSocket。

## Account model

- 身份键：`(account_type, account_id)`，`ADMIN` | `PORTAL`
- 允许跨端好友、混合群成员
- 展示资料按类型解析 Admin/Portal profile；UI 标注「管理员 / 学生」（或 Admin / Portal）

## Backend

1. Portal WS：`/api/v1/portal/message/ws`（复用现有 handler/manager）
2. 确认群 `members/add`、`set-role`、`dissolve` 支持异端成员
3. 会话/消息列表昵称头像：ADMIN+PORTAL 批量解析补齐缺口
4. Offline queue: migration `h5i6j7k8l9m0` for `msg_offline_message_queue`; WS `pull_offline` soft-fails if unavailable
5. 不做：群主转让 API（无现成则跳过）、已读回执花活

## Admin 优化

1. 建群：邀请好友后调用 `members/add`（成员 key 含 `account_type`）
2. 群主：解散 → `dissolve`
3. 成员：设/取消管理员 → `set-role`
4. 列表展示账号类型标签

## Portal

1. API：`/api/v1/portal/message/*` 客户端（conversation / message / friend / group）
2. 页面：`/messages` 三栏（会话 | 聊天 | 联系人/群）
3. 能力：好友申请与处理、私聊、建群（可邀跨端）、退群、解散、设角色、踢人、入群审批（若开启）
4. WS：Portal 连接 + 收消息刷新
5. 顶栏消息入口 + 未读角标（有 unread-count 则接）
6. 主色 `#1677FF`，布局对齐现有 Admin 消息中心结构，不另起商业皮肤

## Acceptance

- Admin ↔ Portal 互加好友、互发私聊（两端各一端）
- 混合群建群、发言、群主解散、设管理员生效

## Phases

1. Backend WS + profile gaps  
2. Admin gap fixes  
3. Portal API + shell + DM/friends  
4. Portal groups + header entry  
5. Smoke
