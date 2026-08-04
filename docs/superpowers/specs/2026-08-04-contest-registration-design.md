# 竞赛报名 / 准入 / Admin 管理设计

## 目标

补齐校赛常见流程：**报名窗口可控 → 自动通过或人工审核 → 开赛后进入 → 题面/提交拦截**；Admin 可完整管理报名人员与参赛人员。私有赛门户不能自助报名；列表可见性可配。

## 非目标（本阶段不做）

- 虚拟重赛 UX 大改（保留现有 backend 能力即可）
- 队伍赛 / 多账号组队
- 报名缴费、资格证书
- 大规模邮件/站内信通知（可预留 remark；通知后续再做）

## 背景与现状缺口

- 门户有「报名」文案，但 `join_contest` 在 `SCHEDULED` 时拒绝 → 报名链路不通
- 无 `register_start` / `register_end`、无审核状态
- 参赛即 join；无「有资格」与「已进入」分离
- Admin 仅有 participation / private-contestant CRUD，无审核流
- 列表 `joined` 未正确计算 →「我的比赛」不可靠

## 方案概览

**报名记录 + 参赛激活（方案 1）**

1. 扩展 `oj_contest`：报名窗口、报名模式、列表可见性
2. 新表 `oj_contest_registration`：报名与审核
3. 开赛后「进入比赛」才激活 / 创建 LIVE `oj_contest_participation`（`real_start`）
4. 私有赛：门户不可自助报名；Admin 在「报名人员」Tab 加人（合并原私有白名单）
5. 题面与提交 API 统一拦截，不只藏按钮

---

## 数据模型

### `oj_contest` 新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `register_start` | datetime tz, nullable | 报名开始；公开赛建议必填，私有赛可空 |
| `register_end` | datetime tz, nullable | 报名截止；可早于/等于/晚于 `start_time` |
| `registration_mode` | string(16) | `AUTO` \| `REVIEW`；仅公开赛自助报名生效 |
| `list_visibility` | string(16) | `PUBLIC` \| `INVITE_ONLY` |

与现有字段关系：

| 字段 | 含义 |
|------|------|
| `is_visible=false` | 草稿/全站隐藏（Admin 可见） |
| `is_private=true` | 门户**禁止**自助报名；只能 Admin 加人 |
| `access_code` | 公开赛报名时可选校验；无私有自助报名场景 |
| `list_visibility=PUBLIC` | 出现在 portal 竞赛列表（且 `is_visible`） |
| `list_visibility=INVITE_ONLY` | 不出现在公开列表；已通过报名的用户可在「我的比赛」看到 |

默认建议：

- `registration_mode=AUTO`
- `list_visibility=PUBLIC`
- 迁移时：已有私有赛可设 `list_visibility=INVITE_ONLY`（可选，或保持 PUBLIC 由 Admin 再改）

校验（写接口）：

- `register_end >= register_start`（二者皆非空时）
- `end_time > start_time`（已有）
- 私有赛保存时：忽略门户自助报名；表单提示「仅管理员加人」；`registration_mode` 仍可存但不用于门户报名

### 新表 `oj_contest_registration`

| 字段 | 说明 |
|------|------|
| `id` | 雪花主键 |
| `contest_id` | 竞赛 |
| `account_id` | 用户 |
| `status` | `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED` |
| `applied_at` | 申请时间 |
| `reviewed_at` | 审核时间（可空） |
| `reviewed_by` | 审核人 account_id（可空；AUTO 通过时可空或系统） |
| `remark` | 拒绝原因或备注 |
| `source` | `SELF` \| `ADMIN`（自助 / 管理员添加） |

约束：`UNIQUE(contest_id, account_id)`；索引 `(contest_id, status)`、`(account_id)`。

### 与 `oj_contest_participation` 的关系

- **报名通过** = 有参赛资格（registration=`APPROVED`）
- **参赛** = 用户在比赛进行中点「进入」后存在 LIVE participation，并设置 `real_start`
- `user_count`：统计 LIVE 正式参赛人数（进入后 +1，行为与现网 join 一致）
- 取消资格（DQ）作用在 participation，不自动改 registration（仍保留「曾获准」记录；是否禁止再进由 DQ 拦截）

### 私有白名单合并

- 权威名单 = `oj_contest_registration` 且 `status=APPROVED`
- Admin「报名人员」Tab：添加用户 → 直接 `APPROVED` + `source=ADMIN`
- 现有 `oj_contest_private_contestant`：**本阶段迁移/双写后弃用 UI**；兼容期内 `is_private` 校验改为读 registration 已通过；提供一次性数据迁移（白名单 → APPROVED registration）
- Admin 侧隐藏或只读旧 private-contestant 菜单，避免两套入口

---

## 门户行为

### 列表

- 公开列表：`is_visible` 且 `list_visibility=PUBLIC`
- 「我的比赛」：当前用户存在 registration（任意非 `CANCELLED`，或至少 `APPROVED`/`PENDING`）的竞赛，**含** `INVITE_ONLY`
- 列表项展示：生命周期、报名状态、是否可进入；正确填充 `joined` / `registration_status`

### 报名（公开赛）

前置：已登录、非私有、当前时间 ∈ `[register_start, register_end]`、未封禁、邀请码正确（若配置）。

- `AUTO` → 创建 `APPROVED`
- `REVIEW` → 创建 `PENDING`
- 重复报名：已有有效记录则幂等返回当前状态
- 取消：`PENDING`/`APPROVED` 且尚未进入比赛（无 LIVE participation）且仍在可取消策略内 → `CANCELLED`；开赛后已进入不可取消报名

### 私有赛

- 详情可经直链打开（若 `is_visible`）；无「报名」按钮
- 未在已通过名单：展示「需由管理员添加」类提示
- 已通过：与公开赛相同的赛前等待 / 赛中进入

### 详情 CTA

| 条件 | CTA |
|------|-----|
| 未登录 | 登录 |
| 公开 + 报名窗口内 + 未报 | 立即报名 |
| `PENDING` | 审核中 |
| `REJECTED` | 已拒绝（展示 remark） |
| `APPROVED` + 未开赛 | 已报名 + 开赛倒计时（不可进题） |
| `APPROVED` + `RUNNING` | 主按钮「进入比赛」→ 第一题做题页；次按钮「题目列表」 |
| 未获准 + `RUNNING` | 拦截提示，无进题入口 |
| `ENDED` / `LOCKED` | 本阶段：可看榜（按原规则）；进题/提交按现有 lock 规则 |

「进入比赛」：

1. 校验 `APPROVED`、未封禁、未 DQ、生命周期 `RUNNING`
2. 若不存在 LIVE participation → 创建并设 `real_start=now`，`user_count++`
3. 重定向：第一题 `/contests/:id/problems/:firstProblemId`；题目列表仍回详情 Problems Tab 或同页列表

### 硬拦截（后端必须）

适用于：`problems`、`problem/detail`、`submit`、做题页依赖的 API。

1. **开赛前（`SCHEDULED`）**：任何人不可看题面、不可提交（含已 `APPROVED`）
2. **进行中**：仅 `APPROVED` 且未 ban/DQ 可看题/提交
3. **锁定后**：沿用现有 locked 逻辑
4. Staff（AUTHOR/CURATOR/TESTER）是否豁免：沿用现有 staff 能力；若无则本阶段仅 Admin 后台豁免，门户 staff 与选手相同（实现时对照现网 `join`/`submit`）

前端藏按钮不能替代上述校验。

---

## Admin 行为

### 竞赛表单

- 时间：比赛 `start`/`end`；报名 `register_start`/`register_end`
- `registration_mode`：AUTO / REVIEW（私有赛置灰说明）
- `list_visibility`：PUBLIC / INVITE_ONLY
- 保留 `is_private`、`access_code`、`is_visible`

### 报名人员 Tab（合并私有名单）

- 筛选：status、账号关键词
- 操作：通过、拒绝（备注）、批量通过/拒绝、取消/移除、**手动添加（直接 APPROVED）**
- 统计：待审 / 已通过 / 已拒绝 / 总数

### 参赛人员 Tab

- 基于 LIVE participation：分数、`real_start`、DQ
- 操作：DQ / 取消 DQ（若现网支持则接上）
- 与报名 Tab 文案区分：「报名=资格；参赛=已进入」

---

## API 草案

### Portal

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/biz/contest/register` | 报名（access_code 可选） |
| POST | `/biz/contest/unregister` | 取消报名 |
| POST | `/biz/contest/enter` | 开赛后进入（激活 participation）；可返回 `first_problem_id` |
| GET | `/biz/contest/detail` | 增补 `registration_status`、`can_register`、`can_enter`、报名窗口字段 |
| GET | `/biz/contest/page` | 公开列表规则 + 登录用户「我的」可用独立接口或 `mine=1` |
| GET | `/biz/contest/mine` | 我的比赛（含 INVITE_ONLY） |

保留或收敛旧 `join`/`leave`：实现时将 `join` 语义拆到 `register`+`enter`，避免 SCHEDULED 误 join；兼容期可让 `join` 在 RUNNING 时等价 `enter`。

### Admin

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/biz/contest/registration/page` | 分页 |
| POST | `/biz/contest/registration/approve` | 单/批通过 |
| POST | `/biz/contest/registration/reject` | 单/批拒绝 |
| POST | `/biz/contest/registration/add` | 手动添加（APPROVED） |
| POST | `/biz/contest/registration/cancel` | 移除报名 |

Contest create/update schema 增加新字段。

---

## 错误与边界

- 报名窗外 / 私有自助报名 → 明确业务错误文案
- 待审用户开赛后尝试 enter → 拒绝
- 拒绝后是否允许再次报名：默认允许（新申请覆盖或同一行改回 PENDING）；Admin 可再拒
- `INVITE_ONLY` 直链：可见详情壳（简介），无资格不展示题
- 个人 `time_limit_seconds`：仅在 `enter` 时起算 `real_start`，禁止报名时写入

## 测试要点

- 公开 AUTO：窗内报名 → 赛前不可看题 → 开赛 enter → 第一题可交
- 公开 REVIEW：PENDING 不可 enter；Admin 通过后可 enter
- 私有：门户无报名；Admin 加人后 INVITE_ONLY 出现在「我的」；列表不可见
- 未报名 RUNNING：problems/submit 403
- 取消报名后再报；已 enter 后不可 unregister
- 列表 `mine` 含邀请赛；公开列表不含

## 实现顺序建议

1. Migration + model/schema/enums
2. Registration service + Admin API/UI Tab
3. Portal register/unregister/enter + detail CTA
4. 统一题面/提交拦截；废弃门户 join-as-register
5. 列表可见性 + 我的比赛；私有名单迁移
6. Seed / E2E 补一条报名→进入链路

## 决策记录

- 报名模式：每场可配 AUTO / REVIEW
- 报名窗口：独立可配
- 进入：主按钮第一题 + 次按钮题目列表
- 私有：门户不可自助报名；Admin 加人；名单合并进报名 Tab
- 列表：可配 PUBLIC / INVITE_ONLY
- 赛前：任何人不可看题/提交
