<script setup lang="ts">
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
  type GridComponentOption,
  type LegendComponentOption,
  type TooltipComponentOption,
} from 'echarts/components'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { BarSeriesOption, LineSeriesOption, PieSeriesOption } from 'echarts/charts'
import type { ComposeOption } from 'echarts/core'
import type { TableColumnsType } from 'ant-design-vue'
import VChart from 'vue-echarts'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type {
  GovernanceIssue,
  GovernanceModuleHealth,
  GovernanceRiskSlice,
  GovernanceTrendPoint,
  MetricItem,
} from '@/types/api'
import StatusTag from '@/components/common/StatusTag.vue'
import { getAnalysisData } from '@/apis/dashboard'
import { useAppStore } from '@/stores/app'

type ECOption = ComposeOption<
  | BarSeriesOption
  | LineSeriesOption
  | PieSeriesOption
  | GridComponentOption
  | LegendComponentOption
  | TooltipComponentOption
>

use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const app = useAppStore()
const { t, tm } = useI18n()

const metrics = ref<MetricItem[]>([])
const trends = ref<GovernanceTrendPoint[]>([])
const moduleHealth = ref<GovernanceModuleHealth[]>([])
const riskSlices = ref<GovernanceRiskSlice[]>([])
const issues = ref<GovernanceIssue[]>([])

const axisColor = computed(() => (app.isDark ? '#71717a' : '#94a3b8'))
const textColor = computed(() => (app.isDark ? '#d4d4d8' : '#475569'))
const splitLineColor = computed(() => (app.isDark ? '#27272a' : '#e2e8f0'))
const tooltipBg = computed(() => (app.isDark ? '#18181b' : '#ffffff'))
const tooltipBorder = computed(() => (app.isDark ? '#3f3f46' : '#e2e8f0'))

const metricHints = computed(() => tm('dashboard.metricHints') as string[])

const healthStatusColor: Record<GovernanceModuleHealth['status'], string> = {
  healthy: 'success',
  warning: 'warning',
  risk: 'error',
}

const healthStatusText: Record<GovernanceModuleHealth['status'], string> = {
  healthy: 'dashboard.healthy',
  warning: 'dashboard.warning',
  risk: 'dashboard.risk',
}

const issueStatusColor: Record<GovernanceIssue['status'], string> = {
  open: 'error',
  processing: 'processing',
  closed: 'success',
}

const issueStatusText: Record<GovernanceIssue['status'], string> = {
  open: 'dashboard.open',
  processing: 'dashboard.processing',
  closed: 'dashboard.closed',
}

const issueColumns = computed<TableColumnsType<GovernanceIssue>>(() => [
  { title: t('dashboard.governanceItem'), dataIndex: 'title', key: 'title', ellipsis: true },
  { title: t('common.module'), dataIndex: 'module', key: 'module', width: 110 },
  { title: t('dashboard.impact'), dataIndex: 'impact', key: 'impact', width: 140 },
  { title: t('common.owner'), dataIndex: 'owner', key: 'owner', width: 130 },
  { title: t('dashboard.trend'), dataIndex: 'trend', key: 'trend', width: 90, align: 'center' },
  { title: t('common.status'), dataIndex: 'status', key: 'status', width: 110, align: 'center' },
])

const trendOption = computed<ECOption>(() => ({
  color: ['#1677ff', '#52c41a', '#fa8c16'],
  tooltip: {
    trigger: 'axis',
    backgroundColor: tooltipBg.value,
    borderColor: tooltipBorder.value,
    textStyle: { color: textColor.value },
  },
  legend: {
    top: 0,
    right: 0,
    textStyle: { color: textColor.value },
  },
  grid: {
    top: 44,
    right: 20,
    bottom: 28,
    left: 42,
  },
  xAxis: {
    type: 'category',
    data: trends.value.map((item) => item.date),
    axisLine: { lineStyle: { color: axisColor.value } },
    axisLabel: { color: axisColor.value },
    axisTick: { show: false },
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: axisColor.value },
    splitLine: { lineStyle: { color: splitLineColor.value } },
  },
  series: [
    {
      name: t('dashboard.visits'),
      type: 'bar',
      barWidth: 18,
      data: trends.value.map((item) => item.visits),
    },
    {
      name: t('dashboard.grants'),
      type: 'line',
      smooth: true,
      symbolSize: 6,
      data: trends.value.map((item) => item.grants),
    },
    {
      name: t('dashboard.audits'),
      type: 'line',
      smooth: true,
      symbolSize: 6,
      data: trends.value.map((item) => item.audits),
    },
  ],
}))

const riskOption = computed<ECOption>(() => ({
  color: ['#ff4d4f', '#fa8c16', '#1677ff', '#52c41a', '#722ed1'],
  tooltip: {
    trigger: 'item',
    backgroundColor: tooltipBg.value,
    borderColor: tooltipBorder.value,
    textStyle: { color: textColor.value },
  },
  legend: {
    bottom: 0,
    left: 'center',
    textStyle: { color: textColor.value },
  },
  series: [
    {
      name: t('dashboard.riskDistribution'),
      type: 'pie',
      radius: ['48%', '70%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      label: {
        color: textColor.value,
        formatter: '{b}',
      },
      data: riskSlices.value.map((item) => ({
        name: item.name,
        value: item.value,
      })),
    },
  ],
}))

function asIssue(record: unknown) {
  return record as GovernanceIssue
}

onMounted(async () => {
  const data = await getAnalysisData()
  metrics.value = data.metrics
  trends.value = data.trends
  moduleHealth.value = data.moduleHealth
  riskSlices.value = data.riskSlices
  issues.value = data.issues
})
</script>

<template>
  <div class="text-slate-700 dark:text-zinc-300">
    <ARow :gutter="[24, 24]" class="mb-6">
      <ACol v-for="(item, index) in metrics" :key="item.title" :xs="24" :md="12" :xl="6" class="flex">
        <ACard :bordered="false" class="w-full" :body-style="{ height: '172px', padding: '18px 22px 12px' }">
          <div class="flex h-full flex-col">
            <div class="flex items-center justify-between gap-3 text-slate-500 leading-6 dark:text-zinc-400">
              <span class="min-w-0 truncate">{{ item.title }}</span>
              <ATooltip :title="metricHints[index] || t('dashboard.metricDefaultHint')">
                <InfoCircleOutlined class="shrink-0" />
              </ATooltip>
            </div>
            <div class="mt-2 truncate text-30px text-slate-900 font-600 leading-10 dark:text-zinc-100">
              {{ item.value }}
            </div>
            <div class="mt-2 flex min-w-0 items-center justify-between gap-3 text-13px text-slate-600 dark:text-zinc-300">
              <span class="min-w-0 truncate">{{ t('dashboard.comparedLastWeek', { change: item.change }) }}</span>
              <StatusTag :status="item.trend" />
            </div>
            <div class="mt-auto border-t border-slate-100 pt-2 text-13px text-slate-500 leading-6 dark:border-zinc-800 dark:text-zinc-400">
              {{ t('dashboard.governanceScope') }}
            </div>
          </div>
        </ACard>
      </ACol>
    </ARow>

    <ARow :gutter="[24, 24]">
      <ACol :xs="24" :xl="16" class="flex">
        <ACard :title="t('dashboard.governanceTrend')" :bordered="false" class="w-full" :body-style="{ height: '390px', padding: '16px 20px 20px' }">
          <template #extra>
            <div class="hidden items-center gap-2 sm:flex">
              <ATag color="processing" class="m-0">{{ t('dashboard.last7Days') }}</ATag>
              <ATag color="success" class="m-0">{{ t('dashboard.realtimeSync') }}</ATag>
            </div>
          </template>
          <VChart class="h-full w-full" :option="trendOption" autoresize />
        </ACard>
      </ACol>

      <ACol :xs="24" :xl="8" class="flex">
        <ACard :title="t('dashboard.moduleHealth')" :bordered="false" class="w-full" :body-style="{ height: '390px', padding: '20px 24px' }">
          <div class="flex h-full flex-col justify-between gap-3">
            <div v-for="item in moduleHealth" :key="item.module" class="min-w-0">
              <div class="mb-1 flex items-center justify-between gap-3">
                <div class="min-w-0">
                  <div class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{ item.module }}</div>
                  <div class="truncate text-12px text-slate-500 dark:text-zinc-400">{{ item.owner }}</div>
                </div>
                <ATag :color="healthStatusColor[item.status]" class="m-0 shrink-0">
                  {{ t(healthStatusText[item.status]) }}
                </ATag>
              </div>
              <AProgress :percent="item.score" :stroke-color="item.status === 'risk' ? '#ff4d4f' : item.status === 'warning' ? '#faad14' : '#1677ff'" />
            </div>
          </div>
        </ACard>
      </ACol>
    </ARow>

    <ARow :gutter="[24, 24]" class="mt-6">
      <ACol :xs="24" :xl="9" class="flex">
        <ACard :title="t('dashboard.riskDistribution')" :bordered="false" class="w-full" :body-style="{ height: '360px', padding: '12px 16px 16px' }">
          <VChart class="h-full w-full" :option="riskOption" autoresize />
        </ACard>
      </ACol>

      <ACol :xs="24" :xl="15" class="flex">
        <ACard :title="t('dashboard.issueDetails')" :bordered="false" class="w-full" :body-style="{ minHeight: '360px' }">
          <ATable
            :columns="issueColumns"
            :data-source="issues"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'trend'">
                <span
                  :class="
                    asIssue(record).trend === 'up'
                      ? 'text-red-500'
                      : asIssue(record).trend === 'down'
                        ? 'text-green-600'
                        : 'text-slate-500 dark:text-zinc-400'
                  "
                >
                  {{ asIssue(record).trend === 'up' ? '↑' : asIssue(record).trend === 'down' ? '↓' : '-' }}
                </span>
              </template>
              <template v-if="column.key === 'status'">
                <ATag :color="issueStatusColor[asIssue(record).status]" class="m-0">
                  {{ t(issueStatusText[asIssue(record).status]) }}
                </ATag>
              </template>
            </template>
          </ATable>
        </ACard>
      </ACol>
    </ARow>
  </div>
</template>
