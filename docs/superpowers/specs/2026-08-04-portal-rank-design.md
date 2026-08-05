# Portal 排名系统设计

**Date:** 2026-08-04  
**Status:** Approved

## Goal

将 Portal「排名」页从占位数据改为真实校园榜：练习通关榜（AC 题数）+ 竞赛 Rating 榜，并展示「我的排名」。

## Scope

- 扩展现有 `/api/v1/portal/biz/rank/*`
- Portal `pages/rank` 接真实 API
- 不做：地区/国旗、称号徽章、周快照表、WebSocket、缓存层

## Boards

| Tab | 指标 | 数据源 |
|-----|------|--------|
| 练习榜 | 去重 AC 题数 | `oj_submission`：`result=AC` 且 `kind != TRIAL` |
| 竞赛 Rating | 当前 Rating | `portal_user_profile.rating`（竞赛结算回写）；附 `oj_contest_rating` 场数与最近一场 `delta` |

排序：主指标降序，`account_id` 升序。仅 `solved > 0` / `rating IS NOT NULL` 上榜。

## API

前缀：`/api/v1/portal`

1. `GET /biz/rank/solved?current&size` → `PageData[SolvedRankItem]`  
   `rank, account_id, nickname, avatar, solved`
2. `GET /biz/rank/rating?current&size` → 增强现有项：+ `contests`, `delta`
3. `GET /biz/rank/me?board=solved|rating`（需登录）  
   `board, rank|null, score, nickname, avatar`；rating 另含 `contests, delta`  
   未上榜：`rank=null`，`score` 仍为真实值
4. `GET /biz/rank/summary?board=solved|rating`  
   `total_users, top_score, avg_score`；rating 另含 `max_delta`（最近一场 delta 的最大值，非周涨跌）

## Portal UI

- Tab：练习榜 | 竞赛 Rating
- 领奖台（前三）+ 分页列表 + 侧栏「我的排名」
- Rating 侧栏可展示本页最大涨幅选手；练习榜侧栏改为通关最多或省略「上升榜」
- 文案校园向；去掉全球/地区/Guardian 假数据

## Consistency

练习榜计数与 `solved_problem_ids` 一致（含正式练习与竞赛 AC，排除 TRIAL）。
