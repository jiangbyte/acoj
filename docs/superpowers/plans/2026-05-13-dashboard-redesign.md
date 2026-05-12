# Dashboard Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure the dashboard page with personal info as top priority, followed by data stats and charts, referencing Snowy admin's card-based design patterns.

**Architecture:** Backend additions are minimal — add `sys_info` and `recent_logins` to the existing dashboard endpoint, extend current user API with org/position/last-login. Frontend restructures into 4 vertical card rows: user info → stats + trend chart → distribution charts → system info + recent logins.

**Tech Stack:** Python 3.12+ / FastAPI / SQLAlchemy, Vue 3 + Composition API, Ant Design Vue, ECharts, Pinia

---

### Task 1: Backend — Extend current user API with org/position/login info

**Files:**
- Modify: `E:\DevProjects\hei\hei-fastapi\modules\sys\user\service.py` (get_current_user method, ~line 182)

- [ ] **Step 1: Add org/position/last-login fields to get_current_user()**

In `modules/sys/user/service.py`, replace the `get_current_user` method:

```python
async def get_current_user(self, request: Request) -> Optional[Dict]:
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return None
    entity = self.find_by_id(user_id)
    if not entity:
        return None
    
    org_name = None
    position_name = None
    if entity.org_id:
        from modules.sys.org.models import SysOrg
        org = self.dao.db.get(SysOrg, entity.org_id)
        if org:
            org_name = org.name
    if entity.position_id:
        from modules.sys.position.models import SysPosition
        pos = self.dao.db.get(SysPosition, entity.position_id)
        if pos:
            position_name = pos.name
    
    return {
        "id": entity.id,
        "account": entity.account,
        "nickname": entity.nickname,
        "avatar": entity.avatar,
        "status": entity.status,
        "org_name": org_name,
        "position_name": position_name,
        "last_login_at": entity.last_login_at.isoformat() if entity.last_login_at else None,
        "last_login_ip": entity.last_login_ip,
        "login_count": entity.login_count or 0,
    }
```

- [ ] **Step 2: Commit**

```bash
git add "E:\DevProjects\hei\hei-fastapi\modules\sys\user\service.py"
git commit -m "feat: add org/position/last-login fields to current user API"
```

---

### Task 2: Backend — Add SysInfo and RecentLogin to dashboard API

**Files:**
- Modify: `E:\DevProjects\hei\hei-fastapi\modules\sys\analyze\params.py`
- Modify: `E:\DevProjects\hei\hei-fastapi\modules\sys\analyze\dao.py`
- Modify: `E:\DevProjects\hei\hei-fastapi\modules\sys\analyze\service.py`

- [ ] **Step 1: Add SysInfo and RecentLogin models to params.py**

Add after `class CategoryDistribution`:

```python
import platform
import socket
import datetime
from datetime import datetime as dt


class SysInfo(BaseModel):
    python_version: str = ""
    os_name: str = ""
    server_ip: str = ""
    run_time: str = ""


class RecentLogin(BaseModel):
    nickname: str
    account: str
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None
```

- [ ] **Step 2: Add recent_logins and sys_info fields to DashboardVO**

Replace the `DashboardVO` class:

```python
class DashboardVO(BaseModel):
    stats: DashboardStats
    user_trend: List[TrendItem] = []
    org_user_distribution: List[OrgUserDistribution] = []
    role_category_distribution: List[CategoryDistribution] = []
    sys_info: SysInfo = SysInfo()
    recent_logins: List[RecentLogin] = []
```

- [ ] **Step 3: Add get_recent_logins to dao.py**

Add to `AnalyzeDao` class:

```python
def get_recent_logins(self, limit: int = 10) -> List[dict]:
    stmt = text("""
        SELECT nickname, account, last_login_at, last_login_ip
        FROM sys_user
        WHERE is_deleted = 'NO' AND last_login_at IS NOT NULL
        ORDER BY last_login_at DESC
        LIMIT :limit
    """)
    result = self.db.execute(stmt, {"limit": limit})
    return [
        {"nickname": row[0], "account": row[1], "last_login_at": row[2], "last_login_ip": row[3]}
        for row in result
    ]
```

- [ ] **Step 4: Add sys_info and recent_logins to service.py**

Add server start time tracker at the top of `service.py`:

```python
import platform
import socket
import datetime

SERVER_START_TIME = datetime.datetime.now()
```

Add methods to `AnalyzeService`:

```python
def _get_sys_info(self) -> SysInfo:
    try:
        hostname = socket.gethostname()
        ip = socket.gethostbyname(hostname)
    except Exception:
        ip = "unknown"
    
    uptime = datetime.datetime.now() - SERVER_START_TIME
    days = uptime.days
    hours, remainder = divmod(uptime.seconds, 3600)
    minutes = remainder // 60
    if days > 0:
        run_time = f"{days}天 {hours}小时 {minutes}分钟"
    else:
        run_time = f"{hours}小时 {minutes}分钟"
    
    return SysInfo(
        python_version=platform.python_version(),
        os_name=platform.system() + " " + platform.release(),
        server_ip=ip,
        run_time=run_time,
    )

def _get_recent_logins(self) -> List[RecentLogin]:
    return [RecentLogin(**item) for item in self.dao.get_recent_logins()]
```

Update the `dashboard()` method:

```python
def dashboard(self) -> DashboardVO:
    stats = DashboardStats(
        total_users=self.dao.count_users(),
        active_users=self.dao.count_active_users(),
        total_roles=self.dao.count_roles(),
        total_orgs=self.dao.count_orgs(),
        total_configs=self.dao.count_configs(),
        total_notices=self.dao.count_notices(),
    )
    user_trend = [TrendItem(**item) for item in self.dao.user_trend()]
    org_dist = [OrgUserDistribution(**item) for item in self.dao.org_user_distribution()]
    role_dist = [CategoryDistribution(**item) for item in self.dao.role_category_distribution()]

    return DashboardVO(
        stats=stats,
        user_trend=user_trend,
        org_user_distribution=org_dist,
        role_category_distribution=role_dist,
        sys_info=self._get_sys_info(),
        recent_logins=self._get_recent_logins(),
    )
```

- [ ] **Step 5: Commit**

```bash
git add "E:\DevProjects\hei\hei-fastapi\modules\sys\analyze\params.py" "E:\DevProjects\hei\hei-fastapi\modules\sys\analyze\dao.py" "E:\DevProjects\hei\hei-fastapi\modules\sys\analyze\service.py"
git commit -m "feat: add sys_info and recent_logins to dashboard API"
```

---

### Task 3: Frontend — Update dashboard API types

**Files:**
- Modify: `E:\DevProjects\hei\hei-admin-vue\src\api\dashboard.ts`

- [ ] **Step 1: Add SysInfo, RecentLogin interfaces and update DashboardData**

Replace `DashboardData` section in `src/api/dashboard.ts`:

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

- [ ] **Step 2: Commit**

```bash
git add "E:\DevProjects\hei\hei-admin-vue\src\api\dashboard.ts"
git commit -m "feat: add SysInfo and RecentLogin types for dashboard"
```

---

### Task 4: Frontend — Rewrite dashboard page

**Files:**
- Modify: `E:\DevProjects\hei\hei-admin-vue\src\views\dashboard\index.vue`

- [ ] **Step 1: Write the full new template and script**

Replace `src/views/dashboard/index.vue`:

```vue
<template>
  <div class="dashboard">
    <!-- Row 1: User Info Card -->
    <a-card :bordered="false" class="mb-4 user-info-card">
      <div class="user-info-inner">
        <div class="user-info-left">
          <a-avatar :size="56" :src="userInfo.avatar">
            {{ userInfo.nickname?.charAt(0) || '?' }}
          </a-avatar>
          <div class="user-details">
            <div class="greeting">{{ greeting }}，{{ userInfo.nickname || userInfo.account }}</div>
            <div class="user-meta">
              <template v-if="userInfo.org_name">{{ userInfo.org_name }}</template>
              <template v-if="userInfo.org_name && userInfo.position_name"> | </template>
              <template v-if="userInfo.position_name">{{ userInfo.position_name }}</template>
              <template v-if="!userInfo.org_name && !userInfo.position_name">{{ userInfo.account }}</template>
            </div>
          </div>
        </div>
        <div class="user-info-right">
          <div class="current-time">{{ currentTime }}</div>
          <div class="last-login" v-if="userInfo.last_login_at">
            上次登录：{{ userInfo.last_login_at }}
          </div>
        </div>
      </div>
    </a-card>

    <!-- Row 2: Stats + Trend Chart -->
    <a-row :gutter="12" class="mb-4 equal-height-row">
      <a-col :xs="24" :lg="8" class="equal-height-col">
        <a-card title="数据概览" :body-style="{ padding: '12px' }" :loading="loading" class="equal-height-card">
          <div class="stats-grid">
            <div
              v-for="card in statCards"
              :key="card.key"
              class="stat-item"
            >
              <div
                class="stat-icon"
                :style="{ background: card.bg, color: card.color }"
              >
                <component :is="card.icon" />
              </div>
              <a-statistic
                :value="card.value ?? '--'"
                :title="card.label"
                :value-style="{ fontSize: '22px', fontWeight: 700, color: 'var(--text-color)' }"
              />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="16" class="equal-height-col">
        <a-card title="用户增长趋势" :body-style="{ padding: '12px' }" :loading="loading" class="equal-height-card">
          <div ref="trendChartRef" style="height: 290px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- Row 3: Distribution Charts -->
    <a-row :gutter="12" class="mb-4">
      <a-col :xs="24" :lg="12" class="max-lg:mb-3">
        <a-card title="组织用户分布" :body-style="{ padding: '12px' }" :loading="loading">
          <div ref="orgChartRef" style="height: 300px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="角色类别分布" :body-style="{ padding: '12px' }" :loading="loading">
          <div ref="roleChartRef" style="height: 300px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- Row 4: System Info + Recent Logins -->
    <a-row :gutter="12">
      <a-col :xs="24" :lg="12" class="max-lg:mb-3">
        <a-card title="系统信息" :body-style="{ padding: '12px' }" :loading="loading">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="Python版本">
              <a-tag color="blue">{{ (data?.sys_info?.python_version || '-') }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="操作系统">
              <span class="info-value">{{ data?.sys_info?.os_name || '-' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="服务器IP">
              <span class="info-value">{{ data?.sys_info?.server_ip || '-' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="运行时长">
              <a-tag color="green">{{ data?.sys_info?.run_time || '-' }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="最近登录" :body-style="{ padding: '12px' }" :loading="loading">
          <div class="timeline-div" v-if="data?.recent_logins?.length">
            <a-timeline>
              <a-timeline-item
                v-for="log in data.recent_logins"
                :key="log.last_login_at + log.account"
                color="blue"
              >
                <div class="log-item">
                  <div class="log-header">
                    <span class="log-name">{{ log.nickname || log.account }}</span>
                    <span class="log-time">{{ formatTime(log.last_login_at) }}</span>
                  </div>
                  <p class="log-address" v-if="log.last_login_ip">{{ log.last_login_ip }}</p>
                </div>
              </a-timeline-item>
            </a-timeline>
          </div>
          <div v-else-if="!loading" class="empty-timeline">暂无登录记录</div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysDashboard' })
import { ref, computed, onMounted, onUnmounted, nextTick, shallowRef } from 'vue'
import * as echarts from 'echarts'
import {
  UserOutlined,
  TeamOutlined,
  ApartmentOutlined,
  SettingOutlined,
  BellOutlined,
} from '@ant-design/icons-vue'
import { fetchDashboard, type DashboardData } from '@/api/dashboard'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo || {})

const data = shallowRef<DashboardData | null>(null)
const loading = ref(true)

// ---- Live clock ----
const currentTime = ref('')
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

let timeInterval: ReturnType<typeof setInterval> | null = null

function updateClock() {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}年${pad(now.getMonth() + 1)}月${pad(now.getDate())}日 ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

function formatTime(iso: string | null) {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(0, 19)
}

// ---- Stats ----
const statCards = ref([
  { key: 'total_users', icon: UserOutlined, label: '用户总数', color: '#1677ff', bg: '#e6f4ff', value: null as number | null },
  { key: 'active_users', icon: UserOutlined, label: '活跃用户', color: '#52c41a', bg: '#f6ffed', value: null as number | null },
  { key: 'total_roles', icon: TeamOutlined, label: '角色总数', color: '#faad14', bg: '#fffbe6', value: null as number | null },
  { key: 'total_orgs', icon: ApartmentOutlined, label: '组织总数', color: '#722ed1', bg: '#f9f0ff', value: null as number | null },
  { key: 'total_configs', icon: SettingOutlined, label: '配置项数', color: '#eb2f96', bg: '#fff0f6', value: null as number | null },
  { key: 'total_notices', icon: BellOutlined, label: '通知总数', color: '#fa8c16', bg: '#fff7e6', value: null as number | null },
])

// ---- Charts ----
const trendChartRef = ref<HTMLDivElement>()
const orgChartRef = ref<HTMLDivElement>()
const roleChartRef = ref<HTMLDivElement>()

let trendChart: echarts.ECharts | null = null
let orgChart: echarts.ECharts | null = null
let roleChart: echarts.ECharts | null = null

const isDark = ref(false)

function initCharts() {
  if (!data.value) return
  const d = data.value
  const textColor = isDark.value ? '#a0aec0' : '#666'
  const axisColor = isDark.value ? '#2d3748' : '#e8e8e8'

  statCards.value = statCards.value.map(card => ({
    ...card,
    value: d.stats[card.key as keyof typeof d.stats] ?? null,
  }))

  if (trendChartRef.value) {
    trendChart?.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 45, right: 20, bottom: 25, top: 15 },
      xAxis: {
        type: 'category',
        data: d.user_trend.map(t => t.month),
        axisLabel: { color: textColor, fontSize: 11 },
        axisLine: { lineStyle: { color: axisColor } },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: textColor },
        splitLine: { lineStyle: { color: axisColor } },
      },
      series: [{
        type: 'line',
        data: d.user_trend.map(t => t.count),
        smooth: true,
        showSymbol: true,
        symbolSize: 6,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22,119,255,0.25)' },
            { offset: 1, color: 'rgba(22,119,255,0.02)' },
          ]),
        },
        lineStyle: { color: '#1677ff', width: 2 },
        itemStyle: { color: '#1677ff' },
      }],
    })
  }

  if (orgChartRef.value) {
    orgChart?.dispose()
    orgChart = echarts.init(orgChartRef.value)
    const names = d.org_user_distribution.map(o => o.name || '未分配')
    const vals = d.org_user_distribution.map(o => o.count)
    orgChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '50%'],
        data: names.map((n, i) => ({ name: n, value: vals[i] })),
        label: { color: textColor, fontSize: 11 },
        itemStyle: {
          borderRadius: 4,
          borderColor: isDark.value ? '#1a1a2e' : '#fff',
          borderWidth: 2,
        },
      }],
    })
  }

  if (roleChartRef.value) {
    roleChart?.dispose()
    roleChart = echarts.init(roleChartRef.value)
    const categories = d.role_category_distribution.map(r => r.category)
    const counts = d.role_category_distribution.map(r => r.count)
    const colorMap: Record<string, string> = {
      ADMIN: '#1677ff',
      NORMAL: '#52c41a',
      OTHER: '#faad14',
    }
    roleChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { color: textColor, fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        data: categories.map((c, i) => ({
          name: c,
          value: counts[i],
          itemStyle: { color: colorMap[c] || '#faad14' },
        })),
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 13, fontWeight: 'bold' },
        },
      }],
    })
  }
}

let resizeObserver: ResizeObserver | null = null

function setupResize() {
  const parent = document.querySelector('.dashboard')
  if (!parent || typeof ResizeObserver === 'undefined') return
  resizeObserver = new ResizeObserver(() => {
    trendChart?.resize()
    orgChart?.resize()
    roleChart?.resize()
  })
  resizeObserver.observe(parent)
}

onMounted(async () => {
  isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'

  updateClock()
  timeInterval = setInterval(updateClock, 1000)

  try {
    const res = await fetchDashboard()
    if (res.success) {
      data.value = res.data
    }
  } catch { /* ignore */ }
  loading.value = false

  await nextTick()
  initCharts()
  setupResize()
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
  trendChart?.dispose()
  orgChart?.dispose()
  roleChart?.dispose()
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* User Info Card */
.user-info-card {
  border-radius: 8px;
}
.user-info-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.user-info-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.greeting {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
}
.user-meta {
  font-size: 14px;
  color: var(--text-color-secondary);
}
.user-info-right {
  text-align: right;
}
.current-time {
  font-size: 16px;
  font-weight: 500;
  color: var(--primary-color);
}
.last-login {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-top: 2px;
}
@media (max-width: 768px) {
  .user-info-right { display: none; }
}

/* Equal-height */
.equal-height-row {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
}
.equal-height-col {
  display: flex;
}
.equal-height-col > :deep(.ant-card) {
  flex: 1;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: var(--background-color-light, #f5f5f5);
  border-radius: 6px;
  transition: all 0.3s;
}
.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

/* Timeline */
.timeline-div {
  max-height: 330px;
  overflow-y: auto;
  padding: 4px 0;
}
.timeline-div::-webkit-scrollbar {
  width: 4px;
}
.timeline-div::-webkit-scrollbar-thumb {
  background: var(--border-color-split, #e8e8e8);
  border-radius: 2px;
}
.log-item {
  margin-bottom: 4px;
}
.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.log-name {
  font-weight: 500;
  color: var(--text-color);
  font-size: 13px;
}
.log-time {
  font-size: 12px;
  color: var(--text-color-secondary);
}
.log-address {
  margin-bottom: 0;
  font-size: 12px;
  color: var(--text-color-secondary);
  opacity: 0.7;
}
.empty-timeline {
  padding: 40px 0;
  text-align: center;
  color: var(--text-color-secondary);
}

/* System Info */
.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
}
</style>
```

- [ ] **Step 2: Restart dev server and verify**

The user should restart the dev server to pick up API changes and verify the dashboard renders correctly.

- [ ] **Step 3: Commit**

```bash
git add "E:\DevProjects\hei\hei-admin-vue\src\views\dashboard\index.vue"
git commit -m "feat: redesign dashboard with user info card, stats grid, system info, and recent logins"
```
