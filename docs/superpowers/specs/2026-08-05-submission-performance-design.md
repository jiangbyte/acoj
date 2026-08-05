# 提交表现统计（击败 % / 分布 / 相似解法）

**Date:** 2026-08-05  
**Status:** Approved

## Goal

为非竞赛练习提交提供 LeetCode 风格的「通过」表现页：击败用户百分比、用时/内存分布直方图、相似解法（含源码预览）。Admin 侧同步展示练习分布，并额外支持**竞赛内**同题同语言统计。

## Scope

### In

| 端 | 场景 | 内容 |
|----|------|------|
| Portal | 做题页「通过」Tab | 用户在该题有 ≥1 次官方 AC 才显示；默认最新 AC；分布 + 击败 % + 相似解法预览 |
| Portal | 提交详情 `/submissions/:id` | 官方非竞赛且 AC 时复用同套组件 |
| Admin | 提交详情 ModalDetail | 官方练习 AC：练习分布；竞赛 AC：竞赛内分布；相似解法 + 源码 |
| Backend | 实时聚合 | 同题同语言 AC 集上算分位与直方图；相似解按用时/内存接近度 |

### Out（本轮不做）

- 预聚合物化表 / 异步全量重建（首期实时查库；可选短缓存）
- 代码文本相似度 / embedding
- Portal 竞赛内「通过」统计（竞赛提交仍走现有竞赛详情/提交列表）
- 做题页内联 SubmitPanel 简版分布（可后续加）

## Definitions

### 练习统计池（Portal + Admin practice）

```text
kind = OFFICIAL
AND contest_id IS NULL
AND status = COMPLETED
AND result = AC
AND problem_id = :problem_id
AND language_key = :language_key
```

### 竞赛统计池（Admin only）

```text
kind = CONTEST
AND contest_id = :contest_id
AND status = COMPLETED
AND result = AC
AND problem_id = :problem_id
AND language_key = :language_key
```

同一用户多次 AC：池中保留**全部** AC 提交（与常见 OJ 分布一致；不做「每用户只取最优」）。若后续要改「每用户最优」，单独开需求。

### 击败百分比

对指标 `x`（越小越好：`time_ms` / `memory_kb`）：

```text
beats_pct = 100 * (count(pool where metric > current) / sample_size)
```

即「严格更慢/更大」的比例。`sample_size < 5` 时不返回 `beats_pct` / buckets，仅返回当前值 + `insufficient_sample: true`。

### 直方图

- 桶数：默认 20（样本少时可动态减少）
- 区间：池内 min–max 等宽；当前提交所在桶标记 `is_current`
- 返回：`{ start, end, count, is_current }[]`

### 相似解法

- 同统计池、排除当前提交与当前用户
- 排序键：`|Δtime_ms| + |Δmemory_kb|` 升序，其次 `created_at` 降序
- 默认 `size=10`
- **Portal**：源码受 `submission_source_visibility` 约束；不可见则 `source=null`，仍返回摘要（用户昵称脱敏策略与现有提交详情一致）
- **Admin**：始终可返回源码（沿用 Admin 提交详情权限）

## API

### Portal（`/api/v1/portal`）

1. `GET /biz/submission/performance?id={submission_id}`  
   - 鉴权：登录；可查看该提交详情的用户  
   - 非练习官方 AC → `404` 或 `200` + `{ available: false, reason }`（推荐后者，前端好展示）  
   - 响应要点：
     ```text
     available, reason?,
     scope: "practice",
     problem_id, language_key,
     time_ms, memory_kb,
     sample_size, insufficient_sample,
     beats_time_pct?, beats_memory_pct?,
     runtime_buckets?, memory_buckets?
     ```

2. `GET /biz/submission/similar?id={submission_id}&size=10`  
   - 同上可用性规则  
   - 项：`id, user_id, nickname?, avatar?, language_key, time_ms, memory_kb, created_at, source?`

3. （辅助，可选合并进 problem detail）用户在该题是否有官方 AC / 最新 AC id：  
   - 已有提交列表可筛；若不够则 `GET /biz/submission/my-latest-ac?problem_id=` 返回 `{ submission_id } | null`  
   - 用于控制「通过」Tab 显隐与默认 submission

### Admin（`/api/v1/admin`）

1. `GET /biz/submission/submission/performance?id=`  
   - 自动选池：有 `contest_id` 且 `kind=CONTEST` → 竞赛池；否则练习池  
   - 响应多字段：`scope: "practice" | "contest"`, `contest_id?`

2. `GET /biz/submission/submission/similar?id=&size=`  
   - 池与 performance 一致；Admin 带 `source`

## Index

建议迁移增加（或等价）：

- `(problem_id, language_key, result, time_ms)` 部分过滤练习场景  
- `(contest_id, problem_id, language_key, result, time_ms)` 服务竞赛内统计  

查询层仍显式过滤 `kind` / `status` / `contest_id IS NULL`。

## Portal UI

### 做题页（`/problems/:id`）

- Tab：题目描述 | **通过**（有官方 AC 才显示）| 题解（若有）| 提交记录  
- 「通过」内容（左右分栏，参考 LeetCode）：  
  - 左：返回「全部提交记录」、当前用时/内存大数字 + 击败 %、两张直方图  
  - 右：相似解法列表；点选预览源码（只读编辑器）  
- 样本不足：只显示自身用时/内存 + 文案「样本不足，暂无分布」  
- 支持 `?tab=passed&submission_id=` 深链（从提交详情跳入）

### 提交详情

- 官方非竞赛 AC：嵌入同一 `SubmissionPerformance` 组件  
- 竞赛提交：不展示练习分布（可链到竞赛页）

## Admin UI

- `ModalDetail`：AC 时展示 performance 图表（`@antv/g2`）+ 相似解法表/源码折叠  
- 标题区分：「练习分布」/「竞赛内分布（本场）」  
- 非 AC 或不适用：隐藏图表区

## Implementation approach

**实时查库（方案 A）**：无物化表；热点可加短 TTL Redis 缓存（可选，非必须）。

共享服务：`app/modules/biz/submission/performance/`（或挂在 submission service 下），Portal/Admin router 薄封装。

## Consistency notes

- Portal「通过」Tab **永不**用竞赛 AC 作为入口（即使用户只在竞赛里 AC 过该题，练习 Tab 仍不出现，除非另有官方 AC）  
- Admin 竞赛统计只与同场次 `contest_id` 比较，不跨竞赛、不与练习池混合  
- TRIAL 提交一律不进任何池

## Open points (resolved)

| 项 | 决议 |
|----|------|
| 入口 | 做题页「通过」Tab + 提交详情复用 |
| 相似解法 | 中档：列表 + 源码预览（Portal 遵可见性） |
| 统计范围 | 同题同语言 |
| Tab 显隐 | ≥1 次官方练习 AC |
| Admin 图表 | 要做 |
| 竞赛统计 | Admin 要做；Portal 本轮不做 |
