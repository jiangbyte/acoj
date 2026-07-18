<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojSubmissionApi } from '@/api'
import { createTagColor, formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'

const resultOptions = ['AC', 'WA', 'TLE', 'MLE', 'OLE', 'RE', 'CE', 'PE', 'IE', 'SE', 'SKIPPED', 'PARTIAL'].map(labelValue)
const statusOptions = ['QUEUED', 'DISPATCHED', 'RUNNING', 'JUDGING', 'COMPLETED', 'FAILED', 'CANCELLED'].map(labelValue)

const detailModalRef = ref<InstanceType<typeof ModalDetail> | null>(null)

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
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
  { title: '题目ID', path: 'problem_id', field: 'input' },
  { title: '题号', path: 'problem_code', field: 'input' },
  { title: '账号ID', path: 'account_id', field: 'input' },
  { title: '状态', path: 'status', field: 'select', fieldProps: { options: statusOptions } },
  { title: '结果', path: 'result', field: 'select', fieldProps: { options: resultOptions } },
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
  { type: 'selection', fixed: 'left' },
  { title: 'ID', path: 'id', width: 130, fixed: 'left', ellipsis: { tooltip: true } },
  { title: '题号', path: 'problem_code', width: 120 },
  { title: '题目ID', path: 'problem_id', width: 160, ellipsis: { tooltip: true } },
  { title: 'account_type', path: 'account_type', width: 130 },
  { title: 'account_id', path: 'account_id', width: 170, ellipsis: { tooltip: true } },
  { title: '语言', path: 'language_id', width: 130, render: (row) => row.language_id || '-' },
  { title: '判题方式', path: 'judge_mode', width: 150, render: (row) => renderTag(row.judge_mode, '#64748b') },
  { title: '状态', path: 'status', width: 130, render: (row) => renderTag(row.status, row.status === 'COMPLETED' ? '#18a058' : '#f0a020') },
  { title: '结果', path: 'result', width: 110, render: (row) => renderTag(row.result || '-', resultColor(row.result)) },
  { title: '得分', path: 'score', width: 90 },
  { title: '耗时', path: 'time_ms', width: 100, render: (row) => row.time_ms ? `${row.time_ms}ms` : '-' },
  { title: '内存', path: 'memory_kb', width: 110, render: (row) => row.memory_kb ? `${row.memory_kb}KB` : '-' },
  { title: 'current_case', path: 'current_case', width: 120 },
  { title: 'case_points', path: 'case_points', width: 120 },
  { title: 'case_total', path: 'case_total', width: 120 },
  { title: '提交时间', path: 'submitted_at', width: 180, render: (row) => formatDateTime(row.submitted_at) },
  { title: '判题时间', path: 'judged_at', width: 180, render: (row) => formatDateTime(row.judged_at) },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:submissions:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:submissions:delete') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

onMounted(fetchPage)

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
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
  } finally {
    state.loading = false
  }
}

function openDetail(id: string) {
  detailModalRef.value?.openModal(id)
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) {
    return
  }
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    draggable: true,
    maskClosable: false,
    content: ids.length > 1 ? `删除 ${ids.length} 条提交?` : '删除该提交?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojSubmissionApi.remove({ ids })
      state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
      window.$message.success('删除成功')
      await fetchPage()
    },
  })
}

function renderTag(text: string, color: string) {
  return <NTag color={createTagColor(color)} bordered={false}>{text}</NTag>
}

function resultColor(result?: string | null) {
  if (result === 'AC') {
    return '#18a058'
  }
  if (!result || result === 'SKIPPED') {
    return '#909399'
  }
  if (result === 'PARTIAL') {
    return '#f0a020'
  }
  return '#d03050'
}

function labelValue(value: string) {
  return { label: value, value }
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: '重置' }"
        :search-button-props="{ content: '搜索' }"
        :collapse-button-props="{ content: searchForm.collapsed.value ? '展开' : '收起' }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="提交记录"
      row-key="id"
      :scroll-x="2300"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton text title="刷新" aria-label="刷新" :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('oj:submissions:delete')"
            type="error"
            text
            title="批量删除"
            aria-label="批量删除"
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:delete" />
              </NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
