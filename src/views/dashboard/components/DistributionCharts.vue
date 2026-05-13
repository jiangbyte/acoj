<template>
  <a-card title="分布统计" :bordered="false" :loading="loading" class="chart-card">
    <div class="chart-item">
      <div class="chart-subtitle">组织用户分布</div>
      <div ref="orgChartRef" class="chart-pie" />
    </div>
    <div class="chart-item">
      <div class="chart-subtitle" style="margin-top: 8px">角色类别分布</div>
      <div ref="roleChartRef" class="chart-pie" />
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { OrgUserDistribution, CategoryDistribution } from '@/api/dashboard'

const props = defineProps<{
  orgData: OrgUserDistribution[]
  roleData: CategoryDistribution[]
  loading: boolean
  isDark?: boolean
}>()

const orgChartRef = ref<HTMLDivElement>()
const roleChartRef = ref<HTMLDivElement>()
let orgChart: echarts.ECharts | null = null
let roleChart: echarts.ECharts | null = null

function renderOrg() {
  if (!orgChartRef.value || !props.orgData?.length) return
  orgChart?.dispose()
  orgChart = echarts.init(orgChartRef.value)

  const names = props.orgData.map(o => o.name || '未分配')
  const vals = props.orgData.map(o => o.count)
  const isDark = props.isDark
  const textColor = isDark ? '#a0aec0' : '#666'

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
        borderColor: isDark ? '#1a1a2e' : '#fff',
        borderWidth: 2,
      },
    }],
  })
}

function renderRole() {
  if (!roleChartRef.value || !props.roleData?.length) return
  roleChart?.dispose()
  roleChart = echarts.init(roleChartRef.value)

  const isDark = props.isDark
  const textColor = isDark ? '#a0aec0' : '#666'
  const categories = props.roleData.map(r => r.category)
  const counts = props.roleData.map(r => r.count)
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

watch(() => [props.orgData, props.roleData, props.isDark], async () => {
  await nextTick()
  renderOrg()
  renderRole()
}, { deep: false })

onMounted(() => {
  nextTick(() => {
    renderOrg()
    renderRole()
  })
})

onUnmounted(() => {
  orgChart?.dispose()
  roleChart?.dispose()
})
</script>

<style scoped>
.chart-card :deep(.ant-card-body) {
  padding-top: 12px !important;
}
.chart-subtitle {
  font-size: 13px;
  color: var(--text-color-secondary);
  text-align: center;
  margin-bottom: 4px;
}
.chart-pie {
  height: 200px;
}
</style>
