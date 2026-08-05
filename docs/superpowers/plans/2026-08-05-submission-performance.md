# Submission Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Portal「通过」Tab + 提交详情展示击败 % / 用时内存分布 / 相似解法；Admin 提交详情同步展示，并支持竞赛内统计。

**Architecture:** Shared `performance` service 实时聚合 `oj_submission`（练习池 vs 竞赛池）；Portal/Admin 薄路由；前端复用直方图 + 相似解预览组件。

**Tech Stack:** FastAPI + SQLAlchemy async, Alembic indexes, Portal React + Ant Design, Admin Vue + `@antv/g2`.

**Spec:** `docs/superpowers/specs/2026-08-05-submission-performance-design.md`

## Global Constraints

- 练习池：`kind=OFFICIAL AND contest_id IS NULL AND status=COMPLETED AND result=AC`，同题同语言
- 竞赛池（仅 Admin）：`kind=CONTEST AND contest_id=:id AND status=COMPLETED AND result=AC`，同题同语言
- 击败 %：`100 * count(metric > current) / sample_size`；`sample_size < 5` → `insufficient_sample`
- Portal「通过」Tab：仅当用户有 ≥1 次**官方练习** AC；竞赛 AC 不计入
- Portal 相似解源码遵守 `submission_source_visibility`；Admin 始终可读源码
- TRIAL 永不入池；不做物化表

## File map

| Path | Role |
|------|------|
| `app/modules/biz/submission/performance/schema.py` | 响应模型 |
| `app/modules/biz/submission/performance/service.py` | 池选择、分位、直方图、相似解 |
| `app/modules/biz/submission/portal/router.py` | Portal routes |
| `app/modules/biz/submission/submission/router.py`（或 admin 路由现有文件） | Admin routes |
| `migrations/versions/*_submission_performance_indexes.py` | 索引 |
| `web/portal/src/api/submission.ts` | API client |
| `web/portal/src/components/oj/SubmissionPerformance.tsx` | 分布 + 相似解 UI |
| `web/portal/src/pages/problems/detail.tsx` | 「通过」Tab |
| `web/portal/src/pages/submissions/detail.tsx` | 嵌入组件 |
| `web/admin/src/api/biz/submission/submission.ts` | Admin API |
| `web/admin/.../ModalDetail.vue` (+ 可选子组件) | Admin 图表 |

---

### Task 1: Schemas + performance service + indexes

**Files:**
- Create: `app/modules/biz/submission/performance/__init__.py`
- Create: `app/modules/biz/submission/performance/schema.py`
- Create: `app/modules/biz/submission/performance/service.py`
- Create: `migrations/versions/i6j7k8l9m0n1_submission_performance_indexes.py`（revision id 按仓库最新 head 衔接）

**Interfaces:**
- Produces:
  - `SubmissionPerformanceOut`（`available`, `reason`, `scope`, metrics, buckets…）
  - `SimilarSubmissionItem` / list wrapper
  - `SubmissionPerformanceService.get_performance(submission_id, *, viewer, for_admin: bool)`
  - `SubmissionPerformanceService.list_similar(submission_id, *, size, viewer, for_admin: bool)`
  - `SubmissionPerformanceService.my_latest_practice_ac(user_id, problem_id) -> str | None`

- [ ] **Step 1:** 定义 schema（bucket: `start, end, count, is_current`；percent fields optional）
- [ ] **Step 2:** 实现池过滤 + 加载同池 `time_ms`/`memory_kb` 列表；算 beats + 等宽直方图（默认最多 20 桶）
- [ ] **Step 3:** 实现 similar：排除当前用户与当前提交；`|Δt|+|Δm|` 排序；Portal 套用现有源码可见性逻辑（可复用 `PortalSubmissionService` 的 visibility 判断）
- [ ] **Step 4:** Alembic 索引：`(problem_id, language_key, result, time_ms)`、`(contest_id, problem_id, language_key, result, time_ms)`（名称与现有风格一致）
- [ ] **Step 5:** 单元/轻测：手工构造 metrics 列表测 beats 与 bucket 边界（可放 `tests/` 或 service 内纯函数测）
- [ ] **Step 6:** Commit: `feat(submission): performance service and indexes`

---

### Task 2: Portal + Admin API routes

**Files:**
- Modify: `app/modules/biz/submission/portal/router.py`
- Modify: Admin submission router（`app/modules/biz/submission/submission/router.py` 或现有 admin 挂载点）

**Interfaces:**
- Consumes: Task 1 service
- Produces:
  - Portal `GET /biz/submission/performance?id=`
  - Portal `GET /biz/submission/similar?id=&size=`
  - Portal `GET /biz/submission/my-latest-ac?problem_id=`（需 PORTAL 登录）
  - Admin `GET .../performance?id=`、`.../similar?id=&size=`

- [ ] **Step 1:** Portal 路由接 service；非适用返回 `available:false` + reason（勿 500）
- [ ] **Step 2:** Admin 路由：`scope` 自动 practice|contest
- [ ] **Step 3:** 权限：Portal 沿用 detail 可见性；Admin 沿用现有 submission detail 权限
- [ ] **Step 4:** Smoke：curl 一条练习 AC / 竞赛 AC / 非 AC
- [ ] **Step 5:** Commit: `feat(submission): performance API routes`

---

### Task 3: Portal UI — component + 通过 Tab + 提交详情

**Files:**
- Modify: `web/portal/src/api/submission.ts`
- Create: `web/portal/src/components/oj/SubmissionPerformance.tsx`（可用 CSS/SVG 柱状图，避免强依赖新图表库；若已有 plots 再用）
- Modify: `web/portal/src/pages/problems/detail.tsx`
- Modify: `web/portal/src/pages/submissions/detail.tsx`

- [ ] **Step 1:** API 方法：`submissionPerformance` / `submissionSimilar` / `myLatestPracticeAc`
- [ ] **Step 2:** `SubmissionPerformance`：左分布（用时/内存 + 击败 % + 直方图高亮当前桶），右相似解列表 + 只读源码
- [ ] **Step 3:** `detail.tsx`：登录后查 `my-latest-ac`；有则插入「通过」Tab；支持 `?tab=passed&submission_id=`
- [ ] **Step 4:** 提交详情：官方非竞赛 AC 时嵌入组件；竞赛提交不展示练习分布
- [ ] **Step 5:** 样本不足 / unavailable 文案
- [ ] **Step 6:** Commit: `feat(portal): submission performance UI`

---

### Task 4: Admin UI — ModalDetail charts + similar

**Files:**
- Modify: `web/admin/src/api/biz/submission/submission.ts`
- Modify: `web/admin/src/views/biz/submission/submission/components/ModalDetail.vue`
- Optional Create: `.../SubmissionPerformancePanel.vue`（G2 直方图）

- [ ] **Step 1:** Admin API client
- [ ] **Step 2:** AC 时拉 performance + similar；标题区分「练习分布」/「竞赛内分布」
- [ ] **Step 3:** `@antv/g2` 渲染两图；相似解表 + 源码折叠
- [ ] **Step 4:** 非 AC / unavailable 隐藏
- [ ] **Step 5:** Commit: `feat(admin): submission performance charts`

---

### Task 5: Verification

- [ ] 练习题：用户 AC 后出现「通过」Tab；击败 % 与直方图合理；相似解可预览（可见性允许时）
- [ ] 仅竞赛 AC、无练习 AC：做题页无「通过」Tab
- [ ] Admin：练习 AC 与竞赛 AC 分别显示对应 scope
- [ ] `sample_size < 5`：无 beats/buckets，有提示
- [ ] 迁移可 apply；关键路径无回归

---

## Execution notes

- 直方图可用纯函数：输入 sorted metrics + current → buckets；便于测
- 内存展示 Portal 已有 `formatMemory`；击败文案：`击败 xx.xx%`
- 相似解昵称/头像：走现有 account lookup 若 list/detail 已有同类逻辑则复用
