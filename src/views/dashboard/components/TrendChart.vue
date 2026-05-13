<template>
  <a-card title="用户增长趋势" :bordered="false" :loading="loading" class="chart-card">
    <div ref="chartRef" class="chart-container" />
  </a-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { TrendItem } from '@/api/dashboard'

const props = defineProps<{
  data: TrendItem[]
  loading: boolean
  isDark?: boolean
}>()

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
let ro: ResizeObserver | null = null

function render() {
  if (!chartRef.value) return
  if (!props.data?.length) return

  chart?.dispose()
  chart = echarts.init(chartRef.value)

  const isDark = props.isDark
  const textColor = isDark ? '#a0aec0' : '#666'
  const axisColor = isDark ? '#2d3748' : '#e8e8e8'

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 45, right: 20, bottom: 25, top: 15 },
    xAxis: {
      type: 'category',
      data: props.data.map(t => t.month),
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
      data: props.data.map(t => t.count),
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

watch(() => [props.data, props.isDark], async () => {
  await nextTick()
  render()
}, { deep: false })

onMounted(() => {
  if (chartRef.value) {
    ro = new ResizeObserver(() => chart?.resize())
    ro.observe(chartRef.value)
  }
  // Try initial render (data may already be available)
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
</style>
