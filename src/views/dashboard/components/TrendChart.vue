<template>
  <a-card title="用户增长趋势" :bordered="false" :loading="loading" class="chart-card">
    <div ref="chartRef" class="chart-container" />
    <div class="chart-legend">
      <span class="legend-item"><span class="dot sys-dot" />B端用户</span>
      <span class="legend-item"><span class="dot client-dot" />C端用户</span>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { TrendItem } from '@/api/dashboard'

const props = defineProps<{
  sysData: TrendItem[]
  clientData: TrendItem[]
  loading: boolean
  isDark?: boolean
}>()

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
let ro: ResizeObserver | null = null

function render() {
  if (!chartRef.value) return
  if (!props.sysData?.length && !props.clientData?.length) return

  chart?.dispose()
  chart = echarts.init(chartRef.value)

  const isDark = props.isDark
  const textColor = isDark ? '#a0aec0' : '#666'
  const axisColor = isDark ? '#2d3748' : '#e8e8e8'

  // Merge all months from both datasets
  const months = new Set<string>()
  props.sysData.forEach(t => months.add(t.month))
  props.clientData.forEach(t => months.add(t.month))
  const sortedMonths = Array.from(months).sort()

  const sysCounts = sortedMonths.map(m => props.sysData.find(t => t.month === m)?.count ?? 0)
  const clientCounts = sortedMonths.map(m => props.clientData.find(t => t.month === m)?.count ?? 0)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
    },
    legend: { show: false },
    grid: { left: 45, right: 20, bottom: 25, top: 15 },
    xAxis: {
      type: 'category',
      data: sortedMonths,
      axisLabel: { color: textColor, fontSize: 11 },
      axisLine: { lineStyle: { color: axisColor } },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: textColor },
      splitLine: { lineStyle: { color: axisColor } },
    },
    series: [
      {
        name: 'B端用户',
        type: 'line',
        data: sysCounts,
        smooth: true,
        showSymbol: true,
        symbolSize: 6,
        lineStyle: { color: '#1677ff', width: 2 },
        itemStyle: { color: '#1677ff' },
      },
      {
        name: 'C端用户',
        type: 'line',
        data: clientCounts,
        smooth: true,
        showSymbol: true,
        symbolSize: 6,
        lineStyle: { color: '#13c2c2', width: 2 },
        itemStyle: { color: '#13c2c2' },
      },
    ],
  })
}

watch(
  () => [props.sysData, props.clientData, props.isDark],
  async () => {
    await nextTick()
    render()
  },
  { deep: false }
)

onMounted(() => {
  if (chartRef.value) {
    ro = new ResizeObserver(() => chart?.resize())
    ro.observe(chartRef.value)
  }
  nextTick(() => render())
})

onUnmounted(() => {
  chart?.dispose()
  ro?.disconnect()
})
</script>

<style scoped>
.chart-card :deep(.ant-card-body) {
  padding: 12px;
}
.chart-container {
  height: 200px;
}
.chart-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 4px;
}
.legend-item {
  font-size: 12px;
  color: var(--text-color-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.sys-dot {
  background: #1677ff;
}
.client-dot {
  background: #13c2c2;
}
</style>
