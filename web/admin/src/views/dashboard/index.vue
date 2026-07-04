<script setup lang="ts">
import { Chart } from '@antv/g2'
import { dashboardApi } from '@/api'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { NIcon } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'

type ChartInstance = InstanceType<typeof Chart>

const trendChartRef = ref<HTMLDivElement | null>(null)
const fileChartRef = ref<HTMLDivElement | null>(null)
const charts: ChartInstance[] = []
const state = reactive({
  loading: false,
  chartLoadError: false,
  overview: {
    metrics: [] as any[],
    account_trend: [] as any[],
    file_type_share: [] as any[],
  },
})

const metricMeta: Record<string, { icon: string; color: string }> = {
  accounts: { icon: 'icon-park-outline:people', color: '#2563eb' },
  online_sessions: { icon: 'icon-park-outline:connection', color: '#0891b2' },
  files: { icon: 'icon-park-outline:file-code', color: '#0f766e' },
  banners: { icon: 'icon-park-outline:ad-product', color: '#7c3aed' },
  notifications: { icon: 'icon-park-outline:tips-one', color: '#16a34a' },
}
const metricTitleMap: Record<string, string> = {
  accounts: 'Accounts',
  online_sessions: 'Online Devices',
  files: 'Files',
  banners: 'Banners',
  notifications: 'Published Notices',
}
const metricHelperMap: Record<string, string> = {
  accounts: 'Accounts created today are tracked separately',
  online_sessions: 'Current online tokens in Redis',
  files: 'File count and storage usage',
  banners: 'Total banner configurations',
  notifications: 'Notices currently published',
}
const metricUnitMap: Record<string, { one: string; other: string }> = {
  accounts: { one: 'account', other: 'accounts' },
  online_sessions: { one: 'device', other: 'devices' },
  files: { one: 'file', other: 'files' },
  banners: { one: 'banner', other: 'banners' },
  notifications: { one: 'notice', other: 'notices' },
}

const metricCards = computed(() =>
  state.overview.metrics.map((item) => {
    const meta = metricMeta[item.key] ?? { icon: 'icon-park-outline:analysis', color: '#64748b' }
    return {
      ...item,
      title: metricTitleMap[item.key] ?? item.key,
      helper: metricHelperMap[item.key] ?? '',
      value: item.value ?? 0,
      unitText: formatMetricUnit(item),
      ...meta,
    }
  }),
)

const trendData = computed(() => [
  ...state.overview.account_trend.map((item) => ({
    ...item,
    type: 'New Accounts',
  })),
])

onMounted(fetchOverview)

onBeforeUnmount(() => {
  destroyCharts()
})

watch(
  () => [trendData.value, state.overview.file_type_share],
  async () => {
    await nextTick()
    await renderCharts()
  },
)

async function fetchOverview() {
  state.loading = true
  try {
    const response = await dashboardApi.overview()
    state.overview = Object.assign(state.overview, response.data ?? {})
    await nextTick()
    await renderCharts()
  } finally {
    state.loading = false
  }
}

function formatMetricUnit(item: any) {
  if (item.key === 'files') {
    return `${formatMetricUnitName(item.value, item.key)} / ${formatFileSize(item.trend_value)}`
  }
  return formatMetricUnitName(item.value, item.key)
}

function formatMetricUnitName(value: number | string | null | undefined, key: string) {
  const count = Number(value ?? 0)
  const unitType = count === 1 ? 'one' : 'other'
  return metricUnitMap[key]?.[unitType] ?? key
}

function formatFileSize(size?: number | string | null) {
  const value = Number(size ?? 0)
  if (!Number.isFinite(value) || value <= 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let current = value
  let unitIndex = 0
  while (current >= 1024 && unitIndex < units.length - 1) {
    current /= 1024
    unitIndex += 1
  }
  return `${current.toFixed(unitIndex === 0 ? 0 : 2)} ${units[unitIndex]}`
}

async function renderCharts() {
  destroyCharts()
  state.chartLoadError = false
  try {
    await Promise.all([renderTrendChart(), renderFileChart()])
  } catch {
    state.chartLoadError = true
  }
}

function destroyCharts() {
  while (charts.length) {
    charts.pop()?.destroy()
  }
}

async function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }
  const chart = new Chart({ container: trendChartRef.value, autoFit: true, height: 280 })
  chart.options({
    type: 'line',
    data: trendData.value,
    encode: { x: 'date', y: 'value', color: 'type' },
    scale: { color: { range: ['#2563eb'] } },
    style: { lineWidth: 2.4 },
    axis: { x: { title: false }, y: { title: false, grid: true } },
    legend: { color: { position: 'top' } },
  })
  charts.push(chart)
  await chart.render()
}

async function renderFileChart() {
  if (!fileChartRef.value) {
    return
  }
  const chart = new Chart({ container: fileChartRef.value, autoFit: true, height: 240 })
  chart.options({
    type: 'interval',
    data: state.overview.file_type_share,
    encode: { x: 'name', y: 'value', color: 'name' },
    axis: { x: { title: false }, y: { title: false, grid: true } },
    legend: false,
  })
  charts.push(chart)
  await chart.render()
}
</script>

<template>
  <NSpin :show="state.loading">
    <n-el class="dashboard-page">
      <div class="dashboard-header">
        <div class="min-w-0">
          <h1>{{ 'Operations Workbench' }}</h1>
          <p>{{ 'A live system overview for accounts, online sessions, files, banners, and published notices.' }}</p>
        </div>
        <NButton text :loading="state.loading" @click="fetchOverview">
          <template #icon>
            <NIcon>
              <Icon icon="icon-park-outline:reload" />
            </NIcon>
          </template>
        </NButton>
      </div>

      <NGrid cols="1 s:2 m:3 xl:5" responsive="screen" :x-gap="16" :y-gap="16">
        <NGridItem v-for="item in metricCards" :key="item.key">
          <NCard class="metric-card" :bordered="false">
            <div class="metric-card__top">
              <span
                class="metric-card__icon"
                :style="{ color: item.color, backgroundColor: `${item.color}14` }"
              >
                <NovaIcon :icon="item.icon" :size="22" />
              </span>
            </div>
            <div class="metric-card__title">{{ item.title }}</div>
            <div class="metric-card__value">
              <span class="metric-card__number">{{ item.value }}</span>
              <span class="metric-card__unit">{{ item.unitText }}</span>
            </div>
            <div class="metric-card__helper">{{ item.helper }}</div>
          </NCard>
        </NGridItem>
      </NGrid>

      <NGrid class="mt-4" cols="1 xl:24" responsive="screen" :x-gap="16" :y-gap="16">
        <NGridItem span="1 xl:16">
          <NCard
            class="dashboard-card"
            title="Last 7 Days"
            :bordered="false"
          >
            <div ref="trendChartRef" class="chart-box" />
          </NCard>
        </NGridItem>
        <NGridItem span="1 xl:8">
          <NCard
            class="dashboard-card"
            title="File Types"
            :bordered="false"
          >
            <div ref="fileChartRef" class="chart-box chart-box--small" />
          </NCard>
        </NGridItem>
      </NGrid>

      <NAlert v-if="state.chartLoadError" class="mt-4" type="warning" :show-icon="false">
        {{ 'Chart runtime failed to load. Refresh the page or restart the dev server.' }}
      </NAlert>
    </n-el>
  </NSpin>
</template>

<style scoped>
.dashboard-page {
  min-height: 100%;
  min-width: 0;
}

.dashboard-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.25;
}

.dashboard-header p {
  margin: 8px 0 0;
  color: var(--text-color-3);
}

.metric-card :deep(.n-card__content) {
  display: grid;
  gap: 10px;
  min-height: 138px;
}

.metric-card__top {
  display: flex;
  align-items: center;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.metric-card__title,
.metric-card__helper {
  color: var(--text-color-3);
  font-size: 13px;
}

.metric-card__value {
  display: grid;
  gap: 4px;
  color: var(--text-color-base);
  word-break: break-word;
}

.metric-card__number {
  min-width: 0;
  overflow-wrap: anywhere;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}

.metric-card__unit {
  min-width: 0;
  color: var(--text-color-3);
  font-size: 13px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.dashboard-card {
  height: 100%;
}

.dashboard-card :deep(.n-card__content) {
  min-width: 0;
  min-height: 290px;
}

.chart-box {
  width: 100%;
  min-width: 0;
  height: 280px;
}

.chart-box--small {
  height: 240px;
}
</style>
