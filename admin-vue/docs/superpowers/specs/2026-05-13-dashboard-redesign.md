# Dashboard Redesign

> **Goal:** Restructure the dashboard page with personal info as top priority, followed by data stats and charts, referencing Snowy admin's card-based design patterns.

**Architecture:** Single-page dashboard with modular card sections. Backend additions are minimal — extend the existing `/api/v1/sys/analyze/dashboard` endpoint with `sys_info` and `recent_logins` fields, and add `org_name`/`position_name`/`last_login_at` to the current user API. Frontend restructures into a vertical stack of cards: user info → stats + trend chart → distribution charts → system info + recent activity.

**Tech Stack:** Vue 3 Composition API, Ant Design Vue (a-card, a-row/a-col, a-statistic, a-descriptions, a-tag, a-timeline, a-avatar), ECharts, Pinia (auth store for user info).

---

## Layout

```
Row 1 — 个人信息 (span 24, full width)
  头像 + 问候语/nickname + 组织/职位 + 当前时间

Row 2 — 2 columns
  [Col 8: 数据概览 — 6 stat cards (2x3 grid), colored icon blocks + a-statistic]
  [Col 16: 用户增长趋势 — area chart (290px height)]

Row 3 — 2 columns
  [Col 12: 组织用户分布 — pie/doughnut chart (300px)]
  [Col 12: 角色类别分布 — doughnut chart (300px)]

Row 4 — 2 columns
  [Col 12: 系统信息 — a-descriptions: Python 版本 / 操作系统 / 服务器 IP / 运行时长]
  [Col 12: 最近登录 — a-timeline: recent user login records]
```

## Backend Changes

### 1. Current user API — add fields

File: `modules/sys/user/service.py` → `get_current_user()`

Add `org_name`, `position_name`, `last_login_at` to the returned dict so the user info card can display organization, position, and last login time.

### 2. Dashboard API — add sys_info and recent_logins

File: `modules/sys/analyze/params.py`

```python
class SysInfo(BaseModel):
    python_version: str = ""
    os_name: str = ""
    server_ip: str = ""
    run_time: str = ""  # formatted uptime string

class RecentLogin(BaseModel):
    nickname: str
    account: str
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None

class DashboardVO(BaseModel):
    stats: DashboardStats
    user_trend: List[TrendItem] = []
    org_user_distribution: List[OrgUserDistribution] = []
    role_category_distribution: List[CategoryDistribution] = []
    sys_info: SysInfo = SysInfo()
    recent_logins: List[RecentLogin] = []
```

File: `modules/sys/analyze/dao.py` — add:
- `get_recent_logins(limit=10)` — query sys_user for non-deleted users with last_login_at IS NOT NULL, ordered by last_login_at DESC

File: `modules/sys/analyze/service.py` — add:
- `_get_sys_info()` — collect platform.python_version(), platform.system(), socket.gethostbyname(socket.gethostname()), uptime from psutil or similar
- `_get_recent_logins()` — call dao method, map to RecentLogin VOs

### 3. API response shape

```json
{
  "success": true,
  "data": {
    "stats": { "total_users": 10, "active_users": 5, ... },
    "user_trend": [...],
    "org_user_distribution": [...],
    "role_category_distribution": [...],
    "sys_info": {
      "python_version": "3.12.3",
      "os_name": "Windows 11",
      "server_ip": "192.168.1.100",
      "run_time": "2 days, 3 hours"
    },
    "recent_logins": [
      { "nickname": "Admin", "account": "admin", "last_login_at": "2026-05-13T10:00:00", "last_login_ip": "192.168.1.1" }
    ]
  }
}
```

## Frontend Changes

### 1. `src/api/dashboard.ts`

Add TypeScript interfaces for `SysInfo` and `RecentLogin`, extend `DashboardData`:

```typescript
export interface SysInfo {
  python_version: string
  os_name: string
  server_ip: string
  run_time: string
}

export interface RecentLogin {
  nickname: string
  account: string
  last_login_at: string | null
  last_login_ip: string | null
}

export interface DashboardData {
  stats: DashboardStats
  user_trend: TrendItem[]
  org_user_distribution: OrgUserDistribution[]
  role_category_distribution: CategoryDistribution[]
  sys_info: SysInfo
  recent_logins: RecentLogin[]
}
```

### 2. `src/views/dashboard/index.vue`

Rewrite the template with 4 rows:

**Row 1 — UserInfoCard**: Shows authStore.userInfo. Avatar (a-avatar), greeting (早上好/上午好/下午好/晚上好 based on hour), nickname, org/position if available, current time updating every second. Liveshow clock using setInterval, cleared on unmount.

**Row 2 — Stats + Trend**: Left side shows 6 stats in 2x3 grid. Each stat has a colored background icon block (like Snowy's SysBizDataCard) and a-statistic for the value. Right side same trend chart as current.

**Row 3 — Charts**: Org distribution pie + role category doughnut (same as current).

**Row 4 — SysInfo + RecentLogins**: Left side uses a-descriptions with a-tag for values. Right side uses a-timeline with color-coded items.

Stats icon/color mapping:
| Stat | Icon | Color |
|------|------|-------|
| total_users | UserOutlined | #1677ff (blue) |
| active_users | UserOutlined | #52c41a (green) |
| total_roles | TeamOutlined | #faad14 (gold) |
| total_orgs | ApartmentOutlined | #722ed1 (purple) |
| total_configs | SettingOutlined | #eb2f96 (pink) |
| total_notices | BellOutlined | #fa8c16 (orange) |

### 3. Card list definition

The 6 stat cards should be defined as a reactive array:

```typescript
const statCards = [
  { key: 'total_users', icon: UserOutlined, label: '用户总数', color: '#1677ff', bg: '#e6f4ff' },
  { key: 'active_users', icon: UserOutlined, label: '活跃用户', color: '#52c41a', bg: '#f6ffed' },
  { key: 'total_roles', icon: TeamOutlined, label: '角色总数', color: '#faad14', bg: '#fffbe6' },
  { key: 'total_orgs', icon: ApartmentOutlined, label: '组织总数', color: '#722ed1', bg: '#f9f0ff' },
  { key: 'total_configs', icon: SettingOutlined, label: '配置项数', color: '#eb2f96', bg: '#fff0f6' },
  { key: 'total_notices', icon: BellOutlined, label: '通知总数', color: '#fa8c16', bg: '#fff7e6' },
]
```

Values are populated from `data.stats` after API fetch. Display `'--'` when data is null.

### 4. Loading state

Use `a-card :loading="loading"` prop. Set `loading = true` before fetch, `false` after. Charts only initialize after data is received.

### 5. Error handling

If `fetchDashboard` fails (catch block), keep charts hidden, show empty values in stats. User info from auth store is independent — show regardless.

## Testing

- Verify user info card shows correct avatar, nickname, greeting
- Verify stats populate correctly from API
- Verify charts render
- Verify system info shows server details
- Verify recent logins timeline shows entries (or empty state)
- Verify responsive layout at different breakpoints
- Verify loading state shows correctly
- Verify error state (API failure) doesn't crash page
