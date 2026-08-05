# Team Leader Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Portal 组长可更新独立小组设置（含公开性）、踢人/搜索加人、刷新邀请码。

**Architecture:** Portal 专用 API + `OjTeamService` 的 `_ensure_owner`；UI 落在 `teams/detail.tsx`。无 migration。

**Tech Stack:** FastAPI, SQLAlchemy, React/Ant Portal

## Global Constraints

- 仅 `owner_id == session.account_id` 且 `ENABLED` 可管理
- 课内禁止改 `visibility`
- 加人复用课内班级成员校验；踢人同步 IM
- 无转让/审批/`ADMIN` 角色

---

### Task 1: Backend owner APIs

**Files:**
- Modify: `app/modules/biz/team/schema.py`
- Modify: `app/modules/biz/team/service.py`
- Modify: `app/modules/biz/team/router.py`

- [x] Add schemas: `OjTeamOwnerUpdateRequest`, `OjTeamInviteRefreshRequest`, `OjTeamUserSearchItem`
- [x] Add `_ensure_owner`, `update_by_owner`, `add_members_by_owner`, `remove_member_by_owner`, `refresh_invite`, `search_portal_users`
- [x] Wire portal routes under `/biz/team/*`
- [ ] Manual smoke via curl/OpenAPI if API up

### Task 2: Portal API client + detail UI

**Files:**
- Modify: `web/portal/src/api/team.ts`
- Modify: `web/portal/src/pages/teams/detail.tsx`

- [x] Client methods for update/remove/add/refresh/search
- [x] Owner settings form, invite refresh, member kick + add modal
- [ ] Manual check on `/teams/:id` as owner

### Task 3: Verify

- [ ] Independent: toggle PUBLIC appears on list
- [ ] Kick/add/refresh work; non-owner blocked
- [ ] Course team cannot set visibility
