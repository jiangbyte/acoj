<!--
  Author: Charlie

  OJ 提交管理。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojSubmissionApi } from '@/api'
import {
  displayValue,
  formatDateTime,
  hasPermission,
  normalizeSearchValues,
  renderButtonIcon,
} from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { readPageMeta } from '@/utils/wire'

const router = useRouter()

const statusOptions = [
  { label: 'PENDING', value: 'PENDING' },
  { label: 'JUDGING', value: 'JUDGING' },
  { label: 'AC', value: 'AC' },
  { label: 'WA', value: 'WA' },
  { label: 'TLE', value: 'TLE' },
  { label: 'MLE', value: 'MLE' },
  { label: 'OLE', value: 'OLE' },
  { label: 'RE', value: 'RE' },
  { label: 'CE', value: 'CE' },
  { label: 'SE', value: 'SE' },
]

const statusType: Record<string, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  PENDING: 'default',
  JUDGING: 'info',
  AC: 'success',
  WA: 'error',
  TLE: 'warning',
  MLE: 'warning',
  OLE: 'warning',
  RE: 'error',
  CE: 'error',
  SE: 'error',
}

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  page: 1,
  pageSize: 20,
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
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: { options: statusOptions },
  },
  { title: '题目ID', path: 'problem_id', field: 'input' },
  { title: '账户ID', path: 'account_id', field: 'input' },
])

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => `${itemCount} 条`,
  onUpdatePage: (value) => {
    state.page = value
    fetchPage()
  },
  onUpdatePageSize: (value) => {
    state.pageSize = value
    state.page = 1
    fetchPage()
  },
}))

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  { title: 'ID', path: 'id', width: 180, ellipsis: { tooltip: true } },
  { title: '题目ID', path: 'problem_id', width: 180, ellipsis: { tooltip: true } },
  { title: '账户ID', path: 'account_id', width: 180, ellipsis: { tooltip: true } },
  { title: '语言', path: 'language', width: 100 },
  {
    title: '状态',
    path: 'status',
    width: 100,
    render: (row) => (
      <NTag size="small" bordered={false} type={statusType[row.status] || 'default'}>
        {displayValue(row.status)}
      </NTag>
    ),
  },
  {
    title: '得分',
    path: 'score',
    width: 70,
    align: 'right',
  },
  {
    title: '耗时(ms)',
    path: 'time_ms',
    width: 90,
    align: 'right',
    render: (row) => displayValue(row.time_ms),
  },
  {
    title: '提交时间',
    path: 'created_at',
    width: 170,
    render: (row) => formatDateTime(row.created_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:submission:detail') ? (
          <NButton type="info" text={true} onClick={() => openDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojSubmissionApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = pageMeta.size
  } finally {
    state.loading = false
  }
}

function openDetail(id: string) {
  router.push({ path: '/oj/submission/detail', query: { id } })
}
</script>

<template>
  <NFlex
    class="h-full min-h-0"
    vertical
    :size="8"
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
      title="提交管理"
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
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>
  </NFlex>
</template>
