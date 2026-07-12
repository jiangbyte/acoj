<template>
  <Layout title="首页">
    <view class="flex flex-col ">
      <view class="dashboard-hero">
        <u-avatar
          :src="authStore.userInfo?.avatar || ''"
          size="48"
          icon="account-fill"
        />
        <view class="flex flex-col gap-1 flex-1">
          <text class="text-2xl font-bold">你好，{{ displayName }}</text>
          <text class="dashboard-hero__subtitle">管理端数据总览</text>
          <view class="flex flex-wrap gap-1 mt-1">
            <u-tag
              v-for="role in roleNames"
              :key="role.id"
              :text="role.name"
              type="primary"
              plain
              size="mini"
            />
            <u-tag
              v-for="dept in deptNames"
              :key="dept.id"
              :text="dept.name"
              type="success"
              plain
              size="mini"
            />
          </view>
        </view>
      </view>

      <view class="mx-4 mt-3 bg-white rounded-lg overflow-hidden">
        <text class="block px-4 py-3 text-base font-bold text-gray-900">核心指标</text>
        <view class="flex flex-nowrap">
          <view class="flex flex-col items-center gap-1 py-2 px-1 flex-1" v-for="metric in visibleMetrics" :key="metric.key">
            <u-icon :name="metricIcon(metric.key)" size="28" color="#2563eb" />
            <text class="text-xs text-gray-500">{{ metricLabel(metric.key) }}</text>
            <text class="text-lg font-bold text-gray-900">{{ metric.value }}</text>
            <text class="text-xs" :class="trendClass(metric.trend_value)">{{ trendText(metric.trend_value) }}</text>
          </view>
        </view>
      </view>

      <view class="mx-4 mt-3 bg-white rounded-lg overflow-hidden">
        <text class="block px-4 py-3 text-base font-bold text-gray-900">账号趋势</text>
        <view v-if="accountTrend.length" class="w-full h-64">
          <canvas
            id="trendCanvas"
            canvas-id="trendCanvas"
            type="2d"
            class="w-full h-64"
          />
        </view>
        <u-empty v-else mode="list" text="暂无趋势数据" />
      </view>

      <view class="mx-4 mt-3 bg-white rounded-lg overflow-hidden">
        <text class="block px-4 py-3 text-base font-bold text-gray-900">文件类型分布</text>
        <view v-if="fileTypeShare.length" class="w-full h-64">
          <canvas
            id="fileCanvas"
            canvas-id="fileCanvas"
            type="2d"
            class="w-full h-64"
          />
        </view>
        <u-empty v-else mode="list" text="暂无文件数据" />
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onUnmounted, ref, watch } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import uCharts from '@qiun/ucharts'
import Layout from '@/layouts/index.vue'
import { dashboardApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useRouteStore } from '@/stores/route'
import { isDictLoaded, refreshDict } from '@/utils/dict'

const authStore = useAuthStore()
const routeStore = useRouteStore()
const instance = getCurrentInstance()
const metrics = ref<any[]>([])
const accountTrend = ref<any[]>([])
const fileTypeShare = ref<any[]>([])
const visibleMetricKeys = new Set(['accounts', 'online_sessions', 'files'])

const displayName = computed(
  () =>
    authStore.userInfo?.nickname ||
    authStore.userInfo?.name ||
    authStore.userInfo?.account ||
    '管理员'
)

const visibleMetrics = computed(() =>
  metrics.value.filter((item) => visibleMetricKeys.has(item.key))
)
const roleNames = computed(() => authStore.userInfo?.roleIdNames ?? [])
const deptNames = computed(() => authStore.userInfo?.deptIdNames ?? [])

let trendChart: any = null
let fileChart: any = null

function getCanvasInfo(canvasId: string): Promise<{ node: any; width: number } | null> {
  return new Promise((resolve) => {
    const query = uni.createSelectorQuery().in(instance?.proxy)
    query
      .select(`#${canvasId}`)
      .fields({ node: true, size: true }, () => {})
      .exec((res: any) => {
        const info = res?.[0]
        if (!info?.node) return resolve(null)
        resolve({ node: info.node, width: info.width || 300 })
      })
  })
}

function buildTrendChartData() {
  const dates = [...new Set(accountTrend.value.map((item: any) => item.date))]
  const types = [...new Set(accountTrend.value.map((item: any) => item.type))]
  const series = types.map((type: any) => ({
    name: trendTypeLabel(type),
    data: dates.map((date: any) => {
      const found = accountTrend.value.find(
        (i: any) => i.date === date && i.type === type
      )
      return found ? Number(found.value) : 0
    }),
  }))
  return { categories: dates, series }
}

function buildFileChartData() {
  const categories = fileTypeShare.value.map((item: any) => item.name)
  return {
    categories,
    series: [
      {
        name: '文件数量',
        data: fileTypeShare.value.map((item: any) => Number(item.value)),
      },
    ],
  }
}


async function renderTrendChart() {
  if (!accountTrend.value.length) return
  await nextTick()
  const info = await getCanvasInfo('trendCanvas')
  if (!info) return
  const ctx = info.node.getContext('2d')
  const data = buildTrendChartData()
  const w = info.width
  if (trendChart) {
    trendChart.updateData({ categories: data.categories, series: data.series })
    return
  }
  trendChart = new uCharts({
    type: 'line',
    context: ctx,
    canvasId: 'trendCanvas',
    categories: data.categories,
    series: data.series,
    animation: true,
    background: '#ffffff',
    color: ['#2563eb', '#16a34a', '#d97706'],
    legend: { show: false },
    xAxis: { disableGrid: true, fontColor: '#6b7280', fontSize: 10, rotateLabel: true },
    yAxis: { disabled: false, fontColor: '#6b7280', fontSize: 10 },
    extra: { line: { type: 'straight', width: 2 } },
    width: w,
    height: 260,
    pixelRatio: 1,
  })
}

async function renderFileChart() {
  if (!fileTypeShare.value.length) return
  await nextTick()
  const info = await getCanvasInfo('fileCanvas')
  if (!info) return
  const ctx = info.node.getContext('2d')
  const data = buildFileChartData()
  const w = info.width
  if (fileChart) {
    fileChart.updateData({ categories: data.categories, series: data.series })
    return
  }
  fileChart = new uCharts({
    type: 'column',
    context: ctx,
    canvasId: 'fileCanvas',
    categories: data.categories,
    series: data.series,
    animation: true,
    background: '#ffffff',
    color: ['#2563eb'],
    legend: { show: false },
    xAxis: { disableGrid: true, fontColor: '#6b7280', fontSize: 10, rotateLabel: true },
    yAxis: { disabled: false, fontColor: '#6b7280', fontSize: 10 },
    extra: { column: { width: 20 } },
    width: w,
    height: 260,
    pixelRatio: 1,
  })
}

async function renderAllCharts() {
  await nextTick()
  renderTrendChart()
  renderFileChart()
}

watch(accountTrend, () => renderTrendChart())
watch(fileTypeShare, () => renderFileChart())

onUnmounted(() => {
  trendChart = null
  fileChart = null
})

const iconMap: Record<string, string> = {
  accounts: 'account',
  online_sessions: 'wifi',
  files: 'file-text',
}

const metricLabelMap: Record<string, string> = {
  accounts: '账号',
  online_sessions: '在线设备数',
  files: '文件',
}

function metricIcon(key: string): string {
  const k = (key || '').toLowerCase()
  for (const [mapKey, icon] of Object.entries(iconMap)) {
    if (mapKey.toLowerCase() === k) return icon
  }
  return 'grid'
}

function metricLabel(key: string): string {
  return metricLabelMap[key] ?? key
}

function trendTypeLabel(type: string): string {
  return type === 'accounts' ? '新增账号' : type
}

function trendClass(value: number | null | undefined): string {
  if (value == null || value === 0) return 'text-gray-400'
  return value > 0 ? 'text-green-600' : 'text-red-600'
}

function trendText(value: number | null | undefined): string {
  if (value == null) return '-'
  if (value === 0) return '→ 0%'
  return `${value > 0 ? '↑' : '↓'} ${Math.abs(value)}%`
}

onShow(async () => {
  if (!authStore.isLogin) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
    return
  }
  await bootstrap()
})

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
})

async function bootstrap() {
  if (!isDictLoaded()) {
    await refreshDict()
  }
  if (!routeStore.modules.length) {
    await routeStore.initRoutes()
  }
  await refresh()
}

async function refresh() {
  const overview = await dashboardApi.overview().catch(() => ({
    metrics: [],
    account_trend: [],
    file_type_share: [],
  }))
  metrics.value = overview.metrics ?? []
  accountTrend.value = overview.account_trend ?? []
  fileTypeShare.value = overview.file_type_share ?? []
  await renderAllCharts()
}
</script>

<style lang="scss" scoped>
.dashboard-hero {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: var(--space-3) var(--space-4) 0;
  padding: var(--space-4);
  border-radius: var(--radius-sm);
  background-color: var(--color-primary);
  color: #ffffff;
}

.dashboard-hero__subtitle {
  font-size: var(--text-sm);
  color: #ffffff;
}
</style>
