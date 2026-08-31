<!--
  Author: Charlie

  OJ 题目试跑历史分页。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi } from '@/api'
import { readPageMeta } from '@/utils/wire'
import {
  displayValue,
  formatDateTime,
  normalizeSearchValues,
} from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const limitModeOptions = [
  { label: '题目限额', value: 'PROBLEM' },
  { label: '宽松限额', value: 'RELAXED' },
]

const modeOptions = [
  { label: '全部测例', value: 'ALL' },
  { label: '单测例', value: 'SINGLE' },
]

const state = reactive({
  problem: {} as any,
  rows: [] as any[],
  total: 0,
  loading: false,
  page: 1,
  pageSize: 20,
  searchValues: {} as any,
})

const problemId = computed(() => {
  const id = route.query.id
  return typeof id === 'string' ? id : ''
})

const tableTitle = computed(() => {
  const key = state.problem.problem_key
  return key ? `试跑历史 · ${displayValue(key)}` : '试跑历史'
})

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values)
    state.page = 1
    fetchPage()
  },
  onReset() {
    state.searchValues = {}
    state.page = 1
    fetchPage()
  },
})

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  {
    title: '限额模式',
    path: 'limit_mode',
    field: 'select',
    fieldProps: { options: limitModeOptions },
  },
  {
    title: '范围',
    path: 'mode',
    field: 'select',
    fieldProps: { options: modeOptions },
  },
  { title: '语言', path: 'language', field: 'input' },
  { title: '结果', path: 'overall_status', field: 'input' },
])

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50, 100],
  prefix: ({ itemCount }) => `${itemCount} 条`,
  onUpdatePage: (value) => {
    state.page = value
    fetchPage()
  },
  onUpdatePageSize: (value) => {
    state.pageSize = Math.min(100, value)
    state.page = 1
    fetchPage()
  },
}))

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  {
    title: '时间',
    path: 'created_at',
    width: 170,
    render: (row) => formatDateTime(row.created_at),
  },
  { title: '版本', path: 'case_version', width: 70 },
  {
    title: '范围',
    path: 'mode',
    width: 100,
    render: (row) =>
      row.mode === 'SINGLE' ? `单测例 ${displayValue(row.case_key)}` : '全部',
  },
  {
    title: '限额模式',
    path: 'limit_mode',
    width: 110,
    render: (row) => (
      <NTag size="small" bordered={false} type={row.limit_mode === 'PROBLEM' ? 'info' : 'warning'}>
        {row.limit_mode === 'PROBLEM' ? '题目限额' : '宽松限额'}
      </NTag>
    ),
  },
  { title: '语言', path: 'language', width: 90 },
  {
    title: '来源',
    path: 'source_from',
    width: 90,
    render: (row) => displayValue(row.source_from),
  },
  {
    title: '结果',
    path: 'overall_status',
    width: 80,
    render: (row) => (
      <NTag
        size="small"
        bordered={false}
        type={row.overall_status === 'AC' ? 'success' : 'warning'}
      >
        {displayValue(row.overall_status)}
      </NTag>
    ),
  },
  {
    title: '峰值耗时',
    path: 'max_time_ms',
    width: 90,
    align: 'right',
    render: (row) => displayValue(row.max_time_ms),
  },
  {
    title: '峰值内存',
    path: 'max_memory_bytes',
    width: 120,
    align: 'right',
    render: (row) => displayValue(row.max_memory_bytes),
  },
  {
    title: '建议时限',
    path: 'suggested_time_ms',
    width: 90,
    align: 'right',
    render: (row) => displayValue(row.suggested_time_ms),
  },
  {
    title: '建议内存',
    path: 'suggested_memory_bytes',
    width: 120,
    align: 'right',
    render: (row) => displayValue(row.suggested_memory_bytes),
  },
])

async function fetchProblem() {
  if (!problemId.value) return
  const response = await ojProblemApi.detail({ id: problemId.value })
  state.problem = response.data ?? {}
}

async function fetchPage() {
  if (!problemId.value) return
  state.loading = true
  try {
    if (!state.problem.id) {
      await fetchProblem()
    }
    const response = await ojProblemApi.dryRunsPage({
      current: state.page,
      size: state.pageSize,
      problem_id: problemId.value,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = Math.min(100, pageMeta.size)
  } finally {
    state.loading = false
  }
}

async function initPage() {
  state.page = 1
  state.searchValues = {}
  state.problem = {}
  await fetchProblem()
  await fetchPage()
}

function goBack() {
  if (!problemId.value) {
    router.push('/oj/problem')
    return
  }
  router.push({ path: '/oj/problem/cases', query: { id: problemId.value } })
}

onMounted(() => {
  void initPage()
})
watch(problemId, () => {
  void initPage()
})
</script>

<template>
  <NFlex
    class="h-full min-h-0"
    vertical
  >
    <ProCard>
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: '重置' }"
        :search-button-props="{ content: '搜索' }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="tableTitle"
      row-key="id"
      :scroll-x="1200"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NFlex>
          <NButton
            text
            @click="goBack"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:back" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            text
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:refresh" />
              </NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>
  </NFlex>
</template>
