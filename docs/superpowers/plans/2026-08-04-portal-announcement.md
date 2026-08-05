# Portal Announcement Wiring Plan

> **For agentic workers:** Implement task-by-task.

**Goal:** Replace homepage mock course announcements with real published portal announcements.

**Architecture:** Add anonymous portal list API reusing published filters; portal home fetches list + detail modal with optional mark-read.

**Tech Stack:** FastAPI, Portal React/Ant, Admin Vue form status fix.

**Spec:** `docs/superpowers/specs/2026-08-04-portal-announcement-design.md`

## Global Constraints

- Do not change banner carousel
- Guests can see published portal-targeted announcements
- Status enum: `DRAFT|PUBLISHED|REVOKED` only

---

### Task 1: Backend portal list

**Files:** `announcement/repository.py`, `service.py`, `router.py`

- [x] `page_portal_list` / reuse filters with `account_type=PORTAL`, optional read set
- [x] `GET /message/announcements/list` on `portal_router` (no auth required; optional session)
- [x] Smoke curl

---

### Task 2: Admin status default

**Files:** `web/admin/.../announcement/components/ModalForm.vue`

- [x] Default `status: 'DRAFT'`; options from `PUBLISH_STATUS` / hardcode DRAFT|PUBLISHED|REVOKED if dict wrong

---

### Task 3: Portal client + home

**Files:** `web/portal/src/api/message/announcement.ts`, `pages/home/index.tsx`

- [x] list / myDetail / read
- [x] Replace mock; modal detail; mark read when logged in
- [x] Empty / loading states
