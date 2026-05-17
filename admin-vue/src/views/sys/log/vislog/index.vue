<template>
  <div class="flex flex-col gap-2">
    <!-- Charts row -->
    <a-row :gutter="12" v-if="chartVisible">
      <a-col :xs="24" :lg="14" class="mb-2">
        <a-card size="small" title="周统计" :bordered="false">
          <div ref="lineChartRef" class="chart-container" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="10" class="mb-2">
        <a-card size="small" title="总比例" :bordered="false">
          <div ref="pieChartRef" class="chart-container" />
        </a-card>
      </a-col>
    </a-row>

    <AppSearchPanel
      :model="searchForm"
      perm="sys:log:page"
      @search="handleSearch"
      @reset="resetSearch"
    >
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="日志分类" name="category">
          <a-radio-group v-model:value="searchForm.category" button-style="solid" size="small">
            <a-radio-button
              v-for="item in logTypeList"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-radio-button>
          </a-radio-group>
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="日志名称" allow-clear />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      perm="sys:log:page"
      :columns="columns"
      :fetch-data="fetchLogPage"
      :search-form="searchForm"
    >
      <template #toolbar>
        <a-popconfirm :title="'确定清空' + logTypeLabel + '吗？'" @confirm="handleClear">
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            清空
          </a-button>
        </a-popconfirm>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
          </a-space>
        </template>
      </template>
    </AppTable>

    <VisLogDetail ref="detailRef" v-model:open="detailOpen" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysLogVislog' })
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { DeleteOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { fetchLogPage, fetchLogDeleteByCategory, fetchVisLineChartData, fetchVisPieChartData } from '@/api/log'
import type { LogBarChartData, LogPieChartData } from '@/api/log'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import VisLogDetail from './components/detail.vue'

const logTypeList = [
  { label: '登录日志', value: 'LOGIN' },
  { label: '登出日志', value: 'LOGOUT' },
]

const searchForm = reactive({ keyword: '', category: 'LOGIN' })

const logTypeLabel = computed(() => {
  const item = logTypeList.find(t => t.value === searchForm.category)
  return item ? item.label : searchForm.category
})

const tableRef = ref()
const detailRef = ref()
const detailOpen = ref(false)
const chartVisible = ref(true)

// Charts
const lineChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let roLine: ResizeObserver | null = null
let roPie: ResizeObserver | null = null

watch(() => searchForm.category, () => {
  tableRef.value?.refresh(true)
})

function handleSearch() {
  tableRef.value?.refresh(true)
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.category = 'LOGIN'
  tableRef.value?.refresh(true)
}

async function handleClear() {
  const { success } = await fetchLogDeleteByCategory({ category: searchForm.category })
  if (success) {
    message.success('已清空')
    tableRef.value?.refresh(true)
  }
}

function openDetail(record: any) {
  detailRef.value?.doOpen(record)
}

function renderLineChart(data: LogBarChartData) {
  if (!lineChartRef.value) return
  lineChart?.dispose()
  lineChart = echarts.init(lineChartRef.value)
  const colors: Record<string, string> = { '登录': '#1677ff', '登出': '#bdbdbd' }
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { show: false },
    grid: { left: 40, right: 16, bottom: 28, top: 12 },
    xAxis: {
      type: 'category',
      data: data.days.map(d => d.slice(5)),
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: data.series.map(s => ({
      name: s.name,
      type: 'line',
      data: s.data,
      smooth: true,
      showSymbol: true,
      symbolSize: 6,
      lineStyle: { color: colors[s.name] || '#1677ff', width: 2 },
      itemStyle: { color: colors[s.name] || '#1677ff' },
    })),
  })
}

function renderPieChart(data: LogPieChartData) {
  if (!pieChartRef.value) return
  pieChart?.dispose()
  pieChart = echarts.init(pieChartRef.value)
  const colors: Record<string, string> = { '登录': '#1677ff', '登出': '#bdbdbd' }
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: { show: false },
      data: data.data.map(d => ({
        name: d.category,
        value: d.total,
        itemStyle: { color: colors[d.category] || '#1677ff' },
      })),
    }],
  })
}

async function loadCharts() {
  try {
    const [lineRes, pieRes] = await Promise.all([fetchVisLineChartData(), fetchVisPieChartData()])
    await nextTick()
    if (lineRes.success && lineRes.data) renderLineChart(lineRes.data)
    if (pieRes.success && pieRes.data) renderPieChart(pieRes.data)
  } catch { /* ignore */ }
}

onMounted(() => {
  loadCharts()
  if (lineChartRef.value) {
    roLine = new ResizeObserver(() => lineChart?.resize())
    roLine.observe(lineChartRef.value)
  }
  if (pieChartRef.value) {
    roPie = new ResizeObserver(() => pieChart?.resize())
    roPie.observe(pieChartRef.value)
  }
})

onUnmounted(() => {
  lineChart?.dispose()
  pieChart?.dispose()
  roLine?.disconnect()
  roPie?.disconnect()
})

const columns = [
  { title: '日志名称', dataIndex: 'name', key: 'name', ellipsis: true, width: 180 },
  { title: 'IP地址', dataIndex: 'op_ip', key: 'op_ip', width: 140 },
  { title: '地址', dataIndex: 'op_address', key: 'op_address', ellipsis: true, width: 140 },
  { title: '浏览器', dataIndex: 'op_browser', key: 'op_browser', ellipsis: true, width: 120 },
  { title: '操作系统', dataIndex: 'op_os', key: 'op_os', ellipsis: true, width: 100 },
  { title: '操作时间', dataIndex: 'op_time', key: 'op_time', sorter: true, width: 170 },
  { title: '操作人', dataIndex: 'op_user', key: 'op_user', width: 120 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
]
</script>

<style scoped>
.chart-container {
  height: 180px;
}
</style>
