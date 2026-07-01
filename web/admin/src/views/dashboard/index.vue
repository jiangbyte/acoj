<script setup lang="ts">
import { Chart } from '@antv/g2'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/stores'

type Trend = 'up' | 'down'
type TagType = 'default' | 'success' | 'warning' | 'error' | 'info' | 'primary'
type ChartInstance = InstanceType<typeof Chart>

interface MetricCard {
  key: string
  title: string
  value: string
  unit: string
  trend: Trend
  trendValue: string
  helper: string
  icon: string
  color: string
}

const { t } = useI18n()
const appStore = useAppStore()
const operationChartRef = ref<HTMLDivElement | null>(null)
const conversionChartRef = ref<HTMLDivElement | null>(null)
const channelChartRef = ref<HTMLDivElement | null>(null)
const charts: ChartInstance[] = []
const chartLoadError = ref(false)

const metricCards = computed<MetricCard[]>(() => [
  {
    key: 'gmv',
    title: t('resource.dashboard.metrics.revenue'),
    value: '1,284,600',
    unit: t('resource.dashboard.units.currency'),
    trend: 'up',
    trendValue: '12.8%',
    helper: t('resource.dashboard.metrics.revenue_helper'),
    icon: 'icon-park-outline:chart-line',
    color: '#2563eb',
  },
  {
    key: 'orders',
    title: t('resource.dashboard.metrics.orders'),
    value: '8,426',
    unit: t('resource.dashboard.units.count'),
    trend: 'up',
    trendValue: '8.3%',
    helper: t('resource.dashboard.metrics.orders_helper'),
    icon: 'icon-park-outline:transaction-order',
    color: '#0f766e',
  },
  {
    key: 'ticket',
    title: t('resource.dashboard.metrics.ticket'),
    value: '152.5',
    unit: t('resource.dashboard.units.currency'),
    trend: 'down',
    trendValue: '2.1%',
    helper: t('resource.dashboard.metrics.ticket_helper'),
    icon: 'icon-park-outline:wallet',
    color: '#7c3aed',
  },
  {
    key: 'risk',
    title: t('resource.dashboard.metrics.risk'),
    value: '17',
    unit: t('resource.dashboard.units.items'),
    trend: 'down',
    trendValue: '18.6%',
    helper: t('resource.dashboard.metrics.risk_helper'),
    icon: 'icon-park-outline:alarm',
    color: '#dc2626',
  },
])

const operationTrend = computed(() => [
  { date: '07-01', type: t('resource.dashboard.series.revenue'), value: 128 },
  { date: '07-02', type: t('resource.dashboard.series.revenue'), value: 152 },
  { date: '07-03', type: t('resource.dashboard.series.revenue'), value: 176 },
  { date: '07-04', type: t('resource.dashboard.series.revenue'), value: 169 },
  { date: '07-05', type: t('resource.dashboard.series.revenue'), value: 198 },
  { date: '07-06', type: t('resource.dashboard.series.revenue'), value: 226 },
  { date: '07-07', type: t('resource.dashboard.series.revenue'), value: 241 },
  { date: '07-01', type: t('resource.dashboard.series.orders'), value: 88 },
  { date: '07-02', type: t('resource.dashboard.series.orders'), value: 96 },
  { date: '07-03', type: t('resource.dashboard.series.orders'), value: 118 },
  { date: '07-04', type: t('resource.dashboard.series.orders'), value: 105 },
  { date: '07-05', type: t('resource.dashboard.series.orders'), value: 124 },
  { date: '07-06', type: t('resource.dashboard.series.orders'), value: 139 },
  { date: '07-07', type: t('resource.dashboard.series.orders'), value: 151 },
])

const conversionStages = computed(() => [
  { stage: t('resource.dashboard.conversion.visit'), value: 68400 },
  { stage: t('resource.dashboard.conversion.signup'), value: 21600 },
  { stage: t('resource.dashboard.conversion.trial'), value: 11800 },
  { stage: t('resource.dashboard.conversion.order'), value: 8426 },
  { stage: t('resource.dashboard.conversion.repurchase'), value: 3290 },
])

const channelShare = computed(() => [
  { channel: t('resource.dashboard.channels.search'), value: 38 },
  { channel: t('resource.dashboard.channels.direct'), value: 24 },
  { channel: t('resource.dashboard.channels.partner'), value: 18 },
  { channel: t('resource.dashboard.channels.campaign'), value: 12 },
  { channel: t('resource.dashboard.channels.other'), value: 8 },
])

const todoList = computed(() => [
  {
    title: t('resource.dashboard.todos.refund_audit'),
    meta: t('resource.dashboard.todos.refund_meta'),
    count: 24,
    type: 'warning' as TagType,
  },
  {
    title: t('resource.dashboard.todos.enterprise_lead'),
    meta: t('resource.dashboard.todos.enterprise_meta'),
    count: 16,
    type: 'info' as TagType,
  },
  {
    title: t('resource.dashboard.todos.contract_renewal'),
    meta: t('resource.dashboard.todos.contract_meta'),
    count: 9,
    type: 'success' as TagType,
  },
])

const alertList = computed(() => [
  {
    title: t('resource.dashboard.alerts.payment_delay'),
    description: t('resource.dashboard.alerts.payment_delay_desc'),
    type: 'error' as TagType,
  },
  {
    title: t('resource.dashboard.alerts.stock_pressure'),
    description: t('resource.dashboard.alerts.stock_pressure_desc'),
    type: 'warning' as TagType,
  },
  {
    title: t('resource.dashboard.alerts.sla'),
    description: t('resource.dashboard.alerts.sla_desc'),
    type: 'info' as TagType,
  },
])

const activityTimeline = computed(() => [
  {
    title: t('resource.dashboard.timeline.pricing'),
    time: '09:42',
    content: t('resource.dashboard.timeline.pricing_desc'),
    type: 'success' as const,
  },
  {
    title: t('resource.dashboard.timeline.campaign'),
    time: '11:10',
    content: t('resource.dashboard.timeline.campaign_desc'),
    type: 'info' as const,
  },
  {
    title: t('resource.dashboard.timeline.audit'),
    time: '14:25',
    content: t('resource.dashboard.timeline.audit_desc'),
    type: 'warning' as const,
  },
])

const rankingList = computed(() => [
  { name: t('resource.dashboard.ranking.saas'), value: '486K', percent: 86 },
  { name: t('resource.dashboard.ranking.training'), value: '312K', percent: 68 },
  { name: t('resource.dashboard.ranking.marketplace'), value: '246K', percent: 54 },
  { name: t('resource.dashboard.ranking.service'), value: '198K', percent: 43 },
])

onMounted(async () => {
  await nextTick()
  await renderCharts()
})

onBeforeUnmount(() => {
  destroyCharts()
})

watch(
  () => [appStore.storeColorMode, operationTrend.value, conversionStages.value, channelShare.value],
  async () => {
    await nextTick()
    await renderCharts()
  },
)

async function renderCharts() {
  destroyCharts()
  chartLoadError.value = false
  try {
    await Promise.all([renderOperationChart(), renderConversionChart(), renderChannelChart()])
  } catch {
    chartLoadError.value = true
  }
}

function destroyCharts() {
  while (charts.length) {
    charts.pop()?.destroy()
  }
}

async function renderOperationChart() {
  if (!operationChartRef.value) {
    return
  }
  const chart = new Chart({
    container: operationChartRef.value,
    autoFit: true,
    height: 280,
  })
  chart.options({
    type: 'line',
    data: operationTrend.value,
    encode: {
      x: 'date',
      y: 'value',
      color: 'type',
    },
    scale: {
      color: {
        range: ['#2563eb', '#0f766e'],
      },
    },
    style: {
      lineWidth: 2.4,
    },
    axis: {
      x: { title: false },
      y: { title: false, grid: true },
    },
    legend: {
      color: { position: 'top' },
    },
    tooltip: {
      title: 'date',
      items: [{ field: 'value', name: 'type' }],
    },
  })
  charts.push(chart)
  await chart.render()
}

async function renderConversionChart() {
  if (!conversionChartRef.value) {
    return
  }
  const chart = new Chart({
    container: conversionChartRef.value,
    autoFit: true,
    height: 280,
  })
  chart.options({
    type: 'interval',
    data: conversionStages.value,
    encode: {
      x: 'stage',
      y: 'value',
      color: () => '#2563eb',
    },
    style: {
      radiusTopLeft: 6,
      radiusTopRight: 6,
      maxWidth: 34,
    },
    axis: {
      x: { title: false },
      y: { title: false, grid: true },
    },
    labels: [
      {
        text: 'value',
        position: 'top',
        style: {
          fill: 'var(--text-color-2)',
          fontSize: 12,
        },
      },
    ],
  })
  charts.push(chart)
  await chart.render()
}

async function renderChannelChart() {
  if (!channelChartRef.value) {
    return
  }
  const chart = new Chart({
    container: channelChartRef.value,
    autoFit: true,
    height: 240,
  })
  chart.options({
    type: 'interval',
    data: channelShare.value,
    encode: {
      y: 'value',
      color: 'channel',
    },
    transform: [{ type: 'stackY' }],
    coordinate: {
      type: 'theta',
      outerRadius: 0.86,
      innerRadius: 0.62,
    },
    scale: {
      color: {
        range: ['#2563eb', '#0f766e', '#7c3aed', '#f59e0b', '#64748b'],
      },
    },
    legend: {
      color: { position: 'bottom' },
    },
    labels: [
      {
        text: 'channel',
        radius: 0.82,
        style: {
          fontSize: 11,
          fontWeight: 500,
          fill: 'var(--text-color-2)',
        },
      },
    ],
    tooltip: {
      title: 'channel',
      items: [{ field: 'value' }],
    },
  })
  charts.push(chart)
  await chart.render()
}
</script>

<template>
  <n-el class="workbench-page">
    <div class="workbench-header">
      <div class="min-w-0">
        <h1>{{ t('resource.dashboard.title') }}</h1>
        <p>{{ t('resource.dashboard.subtitle') }}</p>
      </div>
    </div>

    <n-grid cols="1 s:2 xl:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item v-for="item in metricCards" :key="item.key">
        <n-card class="metric-card" :bordered="false">
          <div class="metric-card__top">
            <span class="metric-card__icon" :style="{ color: item.color, backgroundColor: `${item.color}14` }">
              <NovaIcon :icon="item.icon" :size="22" />
            </span>
            <n-tag :type="item.trend === 'up' ? 'success' : 'warning'" :bordered="false" size="small">
              {{ item.trend === 'up' ? '+' : '-' }}{{ item.trendValue }}
            </n-tag>
          </div>
          <div class="metric-card__title">
            {{ item.title }}
          </div>
          <div class="metric-card__value">
            <span>{{ item.value }}</span>
            <small>{{ item.unit }}</small>
          </div>
          <div class="metric-card__helper">
            {{ item.helper }}
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-grid class="mt-4" cols="1 xl:24" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item span="1 xl:16">
        <n-card
          class="workbench-card workbench-card--chart"
          :title="t('resource.dashboard.charts.operation_trend')"
          :bordered="false"
        >
          <div class="chart-shell">
            <div ref="operationChartRef" class="chart-box" />
            <div v-if="chartLoadError" class="chart-error">
              {{ t('resource.dashboard.charts.load_failed') }}
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item span="1 xl:8">
        <n-card
          class="workbench-card workbench-card--chart"
          :title="t('resource.dashboard.charts.channel_share')"
          :bordered="false"
        >
          <div class="chart-shell">
            <div ref="channelChartRef" class="chart-box chart-box--small" />
            <div v-if="chartLoadError" class="chart-error">
              {{ t('resource.dashboard.charts.load_failed') }}
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-grid class="mt-4" cols="1 xl:24" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item span="1 xl:15">
        <n-card
          class="workbench-card workbench-card--chart"
          :title="t('resource.dashboard.charts.conversion')"
          :bordered="false"
        >
          <div class="chart-shell">
            <div ref="conversionChartRef" class="chart-box" />
            <div v-if="chartLoadError" class="chart-error">
              {{ t('resource.dashboard.charts.load_failed') }}
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item span="1 xl:9">
        <n-card
          class="workbench-card workbench-card--chart"
          :title="t('resource.dashboard.ranking.title')"
          :bordered="false"
        >
          <div class="ranking-list">
            <div v-for="item in rankingList" :key="item.name" class="ranking-item">
              <div class="ranking-item__main">
                <span>{{ item.name }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <n-progress type="line" :show-indicator="false" :percentage="item.percent" :height="8" />
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-grid class="mt-4" cols="1 l:3" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item>
        <n-card
          class="workbench-card workbench-card--panel"
          :title="t('resource.dashboard.todos.title')"
          :bordered="false"
        >
          <n-list hoverable clickable>
            <n-list-item v-for="item in todoList" :key="item.title">
              <n-thing :title="item.title" :description="item.meta">
                <template #header-extra>
                  <n-tag :type="item.type" :bordered="false">
                    {{ item.count }}
                  </n-tag>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card
          class="workbench-card workbench-card--panel"
          :title="t('resource.dashboard.alerts.title')"
          :bordered="false"
        >
          <div class="alert-list">
            <div v-for="item in alertList" :key="item.title" class="alert-item">
              <n-tag :type="item.type" :bordered="false" size="small">
                {{ item.title }}
              </n-tag>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card
          class="workbench-card workbench-card--panel"
          :title="t('resource.dashboard.timeline.title')"
          :bordered="false"
        >
          <n-timeline>
            <n-timeline-item
              v-for="item in activityTimeline"
              :key="item.title"
              :type="item.type"
              :title="item.title"
              :content="item.content"
              :time="item.time"
            />
          </n-timeline>
        </n-card>
      </n-grid-item>
    </n-grid>
  </n-el>
</template>

<style scoped>
.workbench-page {
  min-height: 100%;
  min-width: 0;
}

.workbench-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.workbench-header h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.25;
}

.workbench-header p {
  margin: 8px 0 0;
  color: var(--text-color-3);
}

.metric-card :deep(.n-card__content) {
  display: grid;
  gap: 10px;
  min-height: 142px;
}

.metric-card__top,
.metric-card__value,
.ranking-item__main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.metric-card__value span {
  color: var(--text-color-base);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}

.metric-card__value small {
  color: var(--text-color-3);
}

.chart-shell {
  position: relative;
  min-width: 0;
}

.workbench-card {
  height: 100%;
}

.workbench-card :deep(.n-card__content) {
  min-width: 0;
}

.workbench-card--chart :deep(.n-card__content) {
  min-height: 312px;
}

.workbench-card--panel :deep(.n-card__content) {
  min-height: 292px;
}

.chart-box {
  width: 100%;
  min-width: 0;
  height: 280px;
}

.chart-box--small {
  height: 240px;
}

.chart-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-color-3);
  background: var(--card-color);
  border: 1px dashed var(--border-color);
  border-radius: 6px;
}

.ranking-list,
.alert-list {
  display: grid;
  gap: 16px;
}

.ranking-item {
  display: grid;
  gap: 8px;
}

.ranking-item__main span {
  color: var(--text-color-2);
}

.ranking-item__main strong {
  color: var(--text-color-base);
}

.alert-item {
  padding: 12px;
  background: var(--body-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
}

.alert-item p {
  margin: 8px 0 0;
  color: var(--text-color-3);
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 720px) {
  .workbench-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
