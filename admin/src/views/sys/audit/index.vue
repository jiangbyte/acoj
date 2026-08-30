<!--
  Author: Charlie

  操作审计：分页筛选 + 详情弹窗。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { NFlex, NIcon, NTag } from 'naive-ui'
import { auditApi } from '@/api'
import {
  auditActionName,
  auditActionTypeLabel,
  auditDurationText,
  auditModuleLabel,
  auditOperatorName,
} from '@/utils/audit'
import {
  createTagColor,
  formatDateTime,
  normalizeSearchValues,
} from '@/utils'
import { readPageMeta, wireBool } from '@/utils/wire'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  page: 1,
  pageSize: 20,
})

const successOptions = [
  { label: '成功', value: true },
  { label: '失败', value: false },
]

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      module: (value) => String(value).trim(),
      action: (value) => String(value).trim(),
      account_id: (value) => String(value).trim(),
    })
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
  { title: '操作模块', path: 'module', field: 'input' },
  { title: '操作', path: 'action', field: 'input' },
  { title: '操作人', path: 'account_id', field: 'input' },
  {
    title: '操作结果',
    path: 'success',
    field: 'select',
    fieldProps: {
      options: successOptions as any,
      clearable: true,
    },
  },
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
  {
    title: '日志编号',
    path: 'id',
    width: 170,
    ellipsis: { tooltip: true },
  },
  {
    title: '操作模块',
    key: 'module_label',
    width: 140,
    ellipsis: { tooltip: true },
    render: (row) => auditModuleLabel(row),
  },
  {
    title: '操作名',
    key: 'action_name',
    width: 140,
    ellipsis: { tooltip: true },
    render: (row) => auditActionName(row),
  },
  {
    title: '操作类型',
    path: 'action_type',
    width: 90,
    render: (row) => auditActionTypeLabel(row.action_type),
  },
  {
    title: '操作人',
    key: 'operator_name',
    width: 120,
    ellipsis: { tooltip: true },
    render: (row) => auditOperatorName(row),
  },
  {
    title: '操作内容',
    path: 'summary',
    minWidth: 220,
    ellipsis: { tooltip: true },
  },
  {
    title: '操作结果',
    path: 'success',
    width: 90,
    render: (row) => {
      const ok = wireBool(row.success)
      return (
        <NTag size="small" color={createTagColor(ok ? '#52c41a' : '#ff4d4f')} bordered={false}>
          {ok ? '成功' : '失败'}
        </NTag>
      )
    },
  },
  {
    title: '操作时间',
    path: 'created_at',
    width: 170,
    ellipsis: { tooltip: true },
    render: (row) => formatDateTime(row.created_at),
  },
  {
    title: '执行时长',
    path: 'duration_ms',
    width: 100,
    render: (row) => auditDurationText(row.duration_ms),
  },
  {
    title: '业务编号',
    path: 'resource_id',
    width: 140,
    ellipsis: { tooltip: true },
    render: (row) => row.resource_id || '-',
  },
  {
    title: 'IP',
    path: 'ip',
    width: 130,
    ellipsis: { tooltip: true },
  },
  {
    title: 'User-Agent',
    path: 'user_agent',
    minWidth: 220,
    ellipsis: { tooltip: true },
    render: (row) => row.user_agent || '-',
  },
])

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await auditApi.page({
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
        :collapse-button-props="{
          content: searchForm.collapsed.value ? '展开' : '收起',
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="操作审计"
      row-key="id"
      :scroll-x="1800"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NFlex align="center">
          <NButton
            text
            title="刷新"
            aria-label="刷新"
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>
  </NFlex>
</template>
