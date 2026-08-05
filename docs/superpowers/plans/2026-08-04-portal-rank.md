# Portal Rank Implementation Plan

> **For agentic workers:** Implement task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire Portal ranking to real solved + rating APIs with my-rank and summary.

**Architecture:** Extend `user.portal` rank endpoints; aggregate submissions for solved; enrich rating from `oj_contest_rating`; Portal page consumes `/biz/rank/*`.

**Tech Stack:** FastAPI + SQLAlchemy async, Portal React.

**Spec:** `docs/superpowers/specs/2026-08-04-portal-rank-design.md`

## Global Constraints

- Solved count: `AC` and `kind != TRIAL` (same as `solved_problem_ids`)
- Rating board: only profiles with non-null rating
- No region/flags/Guardian badges; no fake weekly delta table
- `max_delta` = max of each user's latest contest delta

---

### Task 1: Schemas + rank service methods

**Files:**
- Modify: `app/modules/user/portal/schema.py`
- Modify: `app/modules/user/portal/repository.py` (optional helpers)
- Modify: `app/modules/user/portal/service.py` (or new `rank_service.py`)

- [x] Add `PortalSolvedRankItem`, enhance `PortalRatingRankItem` (`contests`, `delta`)
- [x] Add `PortalRankMeResponse`, `PortalRankSummaryResponse`
- [x] Implement `page_solved_rank`, enhance `page_rating_rank`, `get_rank_me`, `rank_summary`
- [x] Commit: `feat(portal): rank schemas and service`

---

### Task 2: Router

**Files:**
- Modify: `app/modules/user/portal/router.py`

- [x] `GET /biz/rank/solved`
- [x] Enhance `/biz/rank/rating`
- [x] `GET /biz/rank/me` (PORTAL auth)
- [x] `GET /biz/rank/summary`
- [x] Commit: `feat(portal): rank API routes`

---

### Task 3: Portal API + Rank page

**Files:**
- Create: `web/portal/src/api/rank.ts`
- Modify: `web/portal/src/pages/rank/index.tsx`

- [x] Client for solved/rating/me/summary
- [x] Tabs 练习榜 | 竞赛 Rating; podium + list + pagination + my rank
- [x] Remove mock/global/region/Guardian
- [x] Commit: `feat(portal): wire rank page to APIs`

---

### Task 4: Smoke

- [x] Import-check / hit endpoints if server up
- [x] Empty / logged-out / logged-in paths render
