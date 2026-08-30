<!--
  Author: Charlie

  登录日志：复用操作审计 API，固定 action=login。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { auditApi } from '@/api'
import {
  auditDurationText,
  auditOperatorName,
} from '@/utils/audit'
import {
  createTagColor,
  formatDateTime,
  hasPermission,
  normalizeSearchValues,
  renderButtonIcon,
} from '@/utils'
import { readPageMeta, wireBool } from '@/utils/wire'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from '../audit/components/ModalDetail.vue'

const detailModalRef = ref<any>(null)
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
    title: '操作人',
    key: 'operator_name',
    width: 140,
    ellipsis: { tooltip: true },
    render: (row) => auditOperatorName(row),
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
    title: '操作内容',
    path: 'summary',
    minWidth: 200,
    ellipsis: { tooltip: true },
  },
  {
    title: 'IP',
    path: 'ip',
    width: 140,
    ellipsis: { tooltip: true },
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('sys:audit:detail') ? (
          <NButton type="info" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
            <span style="margin-left: 4px">详细</span>
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
    const response = await auditApi.page({
      current: state.page,
      size: state.pageSize,
      action: 'login',
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

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
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
      title="登录日志"
      row-key="id"
      :scroll-x="1200"
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

    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
