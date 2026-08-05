# Cross-End IM Implementation Plan

> **For agentic workers:** Implement phase-by-phase. Checkboxes for tracking.

**Goal:** Cross-end friends + mixed groups; Admin group-mgmt gaps; Portal full IM + WS.

**Architecture:** Reuse `/message/*` APIs with `(account_type, account_id)`; add Portal WS; Admin fix invite/dissolve/roles; Portal `/messages` center.

**Tech Stack:** FastAPI WS, Admin Vue, Portal React/Ant.

**Spec:** `docs/superpowers/specs/2026-08-04-cross-end-im-design.md`

## Global Constraints

- Cross-end friends and mixed groups allowed
- Keys always `(account_type, account_id)`
- Primary color `#1677FF`; no fake OAuth / commercial IM chrome
- No ownership-transfer API unless already present

---

### Phase 1: Backend WS + hygiene

**Files:** `app/modules/message/websocket/module.py`, offline model/migration if needed, profile enrich paths

- [x] Register portal RouteSpec for WS
- [x] Confirm offline queue: migrate or soft-skip
- [x] Smoke: portal WS accepts portal token

---

### Phase 2: Admin gaps

**Files:** `CreateGroupModal.vue`, ContactDetail/group UI, `types.ts` if needed

- [x] After createGroup, `members/add` for invitees with account_type
- [x] Dissolve (owner)
- [x] set-role ADMIN/MEMBER
- [x] Account type badge on friends/members

---

### Phase 3: Portal API + WS client

**Files:** `web/portal/src/api/message/*.ts`, `hooks/useMessageWebSocket.ts`

- [x] Mirror admin message API under `/api/v1/portal/message`
- [x] WS hook to `/api/v1/portal/message/ws`

---

### Phase 4: Portal UI — DM + friends

**Files:** `pages/messages/*`, routes, layout entry

- [x] `/messages` shell: conversation list + chat + contacts
- [x] Friend search/apply/handle, create-direct, send/history
- [x] Type badges

---

### Phase 5: Portal groups + header

- [x] Create group + invite, leave, dissolve, set-role, kick
- [x] Header message entry + unread badge
- [x] Cross-end smoke checklist
