# Contest Registration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement contest register → approve/auto → enter → gated submit, with Admin registrant management and list visibility.

**Architecture:** Extend `oj_contest` with registration window/mode/list visibility; add `oj_contest_registration`; portal `register`/`unregister`/`enter` replace SCHEDULED join; statement/submit require APPROVED + RUNNING; Admin registration Tab replaces private-contestant UI as source of truth.

**Tech Stack:** FastAPI + SQLAlchemy async, Alembic, Portal React/Ant, Admin Vue.

**Spec:** `docs/superpowers/specs/2026-08-04-contest-registration-design.md`

## Global Constraints

- Private contests: no portal self-register; Admin add only
- Before start: no statements/submit for anyone (including APPROVED)
- Enter sets `real_start`; registration must not
- `list_visibility=INVITE_ONLY` only in 「我的比赛」
- Merge private whitelist into registration (migrate rows)

---

### Task 1: Migration + enums + models

**Files:**
- Create: `migrations/versions/g4h5i6j7k8l9_contest_registration.py`
- Modify: `app/modules/biz/contest/enums.py`
- Modify: `app/modules/biz/contest/contest/model.py`
- Create: `app/modules/biz/contest/registration/model.py`
- Create: `app/modules/biz/contest/registration/__init__.py`
- Create: `app/modules/biz/contest/registration/module.py`

- [ ] Add enums: `ContestRegistrationStatus`, `ContestRegistrationMode`, `ContestListVisibility`, `ContestRegistrationSource`
- [ ] Add contest columns: `register_start`, `register_end`, `registration_mode`, `list_visibility`
- [ ] Create `OjContestRegistration` table + module spec `models=...`
- [ ] Migration: add columns with defaults `AUTO`/`PUBLIC`; create registration table; copy `oj_contest_private_contestant` → `APPROVED`/`ADMIN` registrations (ignore conflicts)
- [ ] Commit: `feat(contest): add registration schema and migration`

---

### Task 2: Registration service + Admin API

**Files:**
- Create: `registration/schema.py`, `repository.py`, `service.py`, `router.py`
- Modify: `registration/module.py` (admin routes)
- Modify: `web/admin/src/api/index.ts` + new `api/biz/contest/registration.ts`

Service methods: `page_admin`, `approve`, `reject`, `add` (APPROVED), `cancel`; helpers `get_status`, `is_approved`.

Router under `/admin` prefix like participation: `/biz/contest/registration/*`.

- [ ] Implement CRUD/approve/reject/add/cancel
- [ ] Wire admin API client
- [ ] Commit: `feat(contest): admin registration APIs`

---

### Task 3: Contest schema/service for new fields

**Files:**
- Modify: `contest/schema.py`, `contest/service.py`, `contest/repository.py` as needed
- Modify: admin form later in Task 6

- [ ] Expose new fields on create/update/detail/page schemas
- [ ] Validate register_end >= register_start
- [ ] Commit: `feat(contest): persist registration settings on contest`

---

### Task 4: Portal register / enter / access

**Files:**
- Create or extend: `registration/service.py` portal methods
- Modify: `submit/service.py` (`join_contest` → thin wrappers; add `enter_contest`; gate private via registration)
- Modify: `portal/router.py`, `portal/schema.py`
- Modify: statement visibility `_can_view_statements` → require RUNNING + approved

Portal endpoints:
- `POST /biz/contest/register`
- `POST /biz/contest/unregister`
- `POST /biz/contest/enter` → `{ participation_id, first_problem_id }`
- Detail/page/mine: `registration_status`, `can_register`, `can_enter`, window fields, `list_visibility`
- `GET /biz/contest/mine`

Access helper: `async def ensure_approved(db, contest_id, account_id)` used by problems/submit.

- [ ] Implement register/unregister/enter
- [ ] Fix list visibility + mine
- [ ] Gate problems/detail/submit
- [ ] Keep `join` as alias of `enter` when RUNNING for compat
- [ ] Commit: `feat(contest): portal registration and enter flow`

---

### Task 5: Portal UI

**Files:**
- Modify: `web/portal/src/api/contest.ts`
- Modify: `web/portal/src/pages/contests/detail.tsx`, `index.tsx`
- Modify: problem page entry if needed

- [ ] Detail CTA per spec
- [ ] Enter → first problem; secondary → problems tab
- [ ] List + 我的比赛 filters
- [ ] Commit: `feat(portal): contest register/enter UX`

---

### Task 6: Admin UI

**Files:**
- Modify: `web/admin/src/views/biz/contest/contest/form.vue` (fields + registration tab)
- Optionally hide private-contestant tab; point to registration
- Create: registration table component under contest views

- [ ] Form fields for window/mode/list_visibility/private
- [ ] Registration Tab: page, approve/reject/add/cancel
- [ ] Commit: `feat(admin): manage contest registrations`

---

### Task 7: Seed + smoke

**Files:**
- Modify: `scripts/seed/seed_portal_demo.py`
- Manual curl smoke against running API

- [ ] Seed one AUTO public + one REVIEW + one private INVITE_ONLY
- [ ] Smoke: register → enter blocked before start; after start enter works
- [ ] Commit: `chore: seed contest registration demos`

---

## Execution

User requested **执行** → inline implementation in this session, task-by-task.
