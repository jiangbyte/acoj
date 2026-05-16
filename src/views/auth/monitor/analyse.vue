<template>
  <!-- Stat cards -->
  <a-row :gutter="[12, 12]">
    <a-col :xs="12" :sm="12" :md="6">
      <a-card :bordered="false">
        <a-statistic title="当前会话总数" :value="data.total_count" />
      </a-card>
    </a-col>
    <a-col :xs="12" :sm="12" :md="6">
      <a-card :bordered="false">
        <a-statistic title="一小时新增" :value="data.one_hour_newly_added" />
      </a-card>
    </a-col>
    <a-col :xs="12" :sm="12" :md="6">
      <a-card :bordered="false">
        <a-statistic title="B/C 端比例" :value="data.proportion_of_b_and_c || '-'" />
      </a-card>
    </a-col>
    <a-col :xs="12" :sm="12" :md="6">
      <a-card :bordered="false">
        <a-statistic title="最大 Token 数" :value="data.max_token_count" />
      </a-card>
    </a-col>
  </a-row>

  <!-- Charts -->
  <a-row :gutter="12" class="mt-2">
    <a-col :xs="24" :lg="14" class="mb-2">
      <a-card size="small" title="周趋势" :bordered="false">
        <div ref="barChartRef" class="chart-container" />
      </a-card>
    </a-col>
    <a-col :xs="24" :lg="10" class="mb-2">
      <a-card size="small" title="B/C 比例" :bordered="false">
        <div ref="pieChartRef" class="chart-container" />
      </a-card>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
defineOptions({ name: 'SessionAnalyse' })
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { fetchSessionAnalysis, fetchSessionChartData } from '@/api/monitor'

const data = reactive({
  total_count: 0,
  max_token_count: 0,
  one_hour_newly_added: 0,
  proportion_of_b_and_c: '',
})

const barChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let roBar: ResizeObserver | null = null
let roPie: ResizeObserver | null = null

onMounted(async () => {
  const [anaRes, chartRes] = await Promise.all([
    fetchSessionAnalysis(),
    fetchSessionChartData(),
  ])
  if (anaRes.success && anaRes.data) {
    Object.assign(data, anaRes.data)
  }
  if (chartRes.success && chartRes.data) {
    await nextTick()
    renderBarChart(chartRes.data.bar_chart)
    renderPieChart(chartRes.data.pie_chart)
  }

  if (barChartRef.value) {
    roBar = new ResizeObserver(() => barChart?.resize())
    roBar.observe(barChartRef.value)
  }
  if (pieChartRef.value) {
    roPie = new ResizeObserver(() => pieChart?.resize())
    roPie.observe(pieChartRef.value)
  }
})

onUnmounted(() => {
  barChart?.dispose()
  pieChart?.dispose()
  roBar?.disconnect()
  roPie?.disconnect()
})

function renderBarChart(data: any) {
  if (!barChartRef.value) return
  barChart?.dispose()
  barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { show: true, bottom: 0 },
    grid: { left: 40, right: 16, bottom: 48, top: 12 },
    xAxis: {
      type: 'category',
      data: data.days?.map((d: string) => d.slice(5)) || [],
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: (data.series || []).map((s: any) => ({
      name: s.name === 'BUSINESS' ? 'B端' : s.name === 'CONSUMER' ? 'C端' : s.name,
      type: 'bar',
      data: s.data,
      barWidth: '36%',
      itemStyle: { color: s.name === 'BUSINESS' ? '#1677ff' : '#52c41a' },
    })),
  })
}

function renderPieChart(data: any) {
  if (!pieChartRef.value) return
  pieChart?.dispose()
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: { show: false },
      data: (data.data || []).map((d: any) => ({
        name: d.category === 'BUSINESS' ? 'B端' : d.category === 'CONSUMER' ? 'C端' : d.category,
        value: d.total,
        itemStyle: { color: d.category === 'BUSINESS' ? '#1677ff' : '#52c41a' },
      })),
    }],
  })
}
</script>

<style scoped>
.chart-container {
  height: 180px;
}
</style>
