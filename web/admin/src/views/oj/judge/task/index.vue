<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojJudgeApi } from '@/api'
import { createTagColor, formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'

const statusOptions = ['PENDING', 'LOCKED', 'RUNNING', 'DONE', 'FAILED', 'CANCELLED'].map((value) => ({ label: value, value }))
const taskTypeOptions = ['JUDGE', 'REJUDGE', 'PRETEST'].map((value) => ({ label: value, value }))

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
  { title: '提交ID', path: 'submission_id', field: 'input' },
  { title: '题目ID', path: 'problem_id', field: 'input' },
  { title: '节点ID', path: 'judge_node_id', field: 'input' },
  { title: '任务类型', path: 'task_type', field: 'select', fieldProps: { options: taskTypeOptions } },
  { title: '状态', path: 'status', field: 'select', fieldProps: { options: statusOptions } },
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
  { title: 'submission_id', path: 'submission_id', width: 160, ellipsis: { tooltip: true } },
  { title: 'problem_id', path: 'problem_id', width: 160, ellipsis: { tooltip: true } },
  { title: 'judge_node_id', path: 'judge_node_id', width: 150, render: (row) => row.judge_node_id || '-' },
  { title: 'task_type', path: 'task_type', width: 120 },
  { title: 'priority', path: 'priority', width: 100 },
  { title: 'status', path: 'status', width: 120, render: (row) => renderTag(row.status, row.status === 'DONE' ? '#18a058' : '#f0a020') },
  { title: 'attempts', path: 'attempts', width: 100 },
  { title: 'started_at', path: 'started_at', width: 180, render: (row) => formatDateTime(row.started_at) },
  { title: 'finished_at', path: 'finished_at', width: 180, render: (row) => formatDateTime(row.finished_at) },
  { title: 'error', path: 'error', width: 220, ellipsis: { tooltip: true }, render: (row) => row.error || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:judgetasks:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:judgetasks:delete') ? (
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
    const response = await ojJudgeApi.task.page({
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
    content: ids.length > 1 ? `删除 ${ids.length} 条任务?` : '删除该任务?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojJudgeApi.task.remove({ ids })
      state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
      window.$message.success('删除成功')
      await fetchPage()
    },
  })
}

function renderTag(text: string, color: string) {
  return <NTag color={createTagColor(color)} bordered={false}>{text}</NTag>
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
      title="判题任务"
      row-key="id"
      :scroll-x="1750"
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
            v-if="hasPermission('oj:judgetasks:delete')"
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
