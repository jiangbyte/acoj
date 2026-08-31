<!--
  Author: Charlie

  OJ 题目测例维护（与其它管理表格页同一套布局）。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi, ojProblemCaseApi } from '@/api'
import { readPageMeta } from '@/utils/wire'
import {
  createTagColor,
  dictList,
  dictTypeColor,
  dictTypeData,
  displayValue,
  formatDateTime,
  hasPermission,
  normalizeSearchValues,
  renderButtonIcon,
} from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModalCaseForm from './components/ModalCaseForm.vue'

const route = useRoute()
const router = useRouter()
const formModalRef = ref<any>(null)

const state = reactive({
  problem: {} as any,
  rows: [] as any[],
  total: 0,
  loading: false,
  page: 1,
  pageSize: 20,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
})

const problemId = computed(() => {
  const id = route.query.id
  return typeof id === 'string' ? id : ''
})

const tableTitle = computed(() => {
  const key = state.problem.problem_key
  const ver = state.problem.case_version
  if (key) {
    return `测例维护 · ${displayValue(key)}（v${displayValue(ver)}）`
  }
  return '测例维护'
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)
const canUpdate = computed(() => hasPermission('oj:problem:update'))

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
  { title: '测例号', path: 'case_key', field: 'input' },
  {
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: { options: dictList('COMMON_STATUS') },
  },
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
  ...(canUpdate.value ? [{ type: 'selection' as const, fixed: 'left' as const }] : []),
  { title: '测例号', path: 'case_key', width: 120, ellipsis: { tooltip: true } },
  {
    title: '排序',
    path: 'sort_no',
    width: 80,
    align: 'right',
  },
  {
    title: '样例',
    path: 'is_sample',
    width: 80,
    render: (row) => (
      <NTag size="small" bordered={false} type={row.is_sample ? 'success' : 'default'}>
        {row.is_sample ? '是' : '否'}
      </NTag>
    ),
  },
  {
    title: '分值',
    path: 'score',
    width: 70,
    align: 'right',
  },
  { title: '输入', path: 'input_storage', width: 90 },
  { title: '输出', path: 'output_storage', width: 90 },
  {
    title: '状态',
    path: 'status',
    width: 100,
    render: (row) => (
      <NTag
        size="small"
        color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))}
        bordered={false}
      >
        {dictTypeData('COMMON_STATUS', row.status) || displayValue(row.status)}
      </NTag>
    ),
  },
  {
    title: '更新时间',
    path: 'updated_at',
    width: 170,
    render: (row) => formatDateTime(row.updated_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {canUpdate.value ? (
          <NButton
            type="warning"
            text={true}
            title="试跑"
            onClick={() => openDryRun(row.case_key)}
          >
            {renderButtonIcon('icon-park-outline:play')}
          </NButton>
        ) : null}
        {canUpdate.value ? (
          <NButton type="primary" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {canUpdate.value ? (
          <NButton type="error" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
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
    const response = await ojProblemCaseApi.page({
      current: state.page,
      size: state.pageSize,
      problem_id: problemId.value,
      case_version: state.problem.case_version,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = Math.min(100, pageMeta.size)
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.rows.some((item) => item.id === key),
    )
  } finally {
    state.loading = false
  }
}

async function initPage() {
  state.page = 1
  state.searchValues = {}
  state.checkedRowKeys = []
  state.problem = {}
  await fetchProblem()
  await fetchPage()
}

function nextSortNo() {
  return Math.max(1, (state.total || 0) + 1)
}

function openCreateModal() {
  if (!problemId.value) return
  formModalRef.value?.openModal({
    problemId: problemId.value,
    caseVersion: state.problem.case_version ?? 1,
    nextSortNo: nextSortNo(),
  })
}

function openEditModal(id: string) {
  if (!problemId.value) return
  formModalRef.value?.openModal({
    problemId: problemId.value,
    caseVersion: state.problem.case_version ?? 1,
    id,
  })
}

function openDryRun(caseKey?: string) {
  if (!problemId.value) return
  router.push({
    path: '/oj/problem/dry-run',
    query: {
      id: problemId.value,
      ...(caseKey ? { case_key: caseKey } : {}),
    },
  })
}

function goDryRuns() {
  if (!problemId.value) return
  router.push({ path: '/oj/problem/dry-runs', query: { id: problemId.value } })
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) return
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除测例',
    content: ids.length > 1 ? `删除 ${ids.length} 条测例?` : '删除该测例?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojProblemCaseApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success('删除成功')
  if (state.rows.length <= ids.length && state.page > 1) {
    state.page -= 1
  }
  await fetchPage()
}

function goBack() {
  router.push('/oj/problem')
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
      :scroll-x="1100"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton
            text
            title="返回"
            aria-label="返回"
            @click="goBack"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:back" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="canUpdate"
            type="primary"
            text
            title="新增测例"
            aria-label="新增测例"
            @click="openCreateModal"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="canUpdate"
            type="warning"
            text
            title="试跑全部"
            aria-label="试跑全部"
            @click="openDryRun()"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:play" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            text
            title="试跑历史"
            aria-label="试跑历史"
            @click="goDryRuns"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:history" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            text
            title="刷新"
            aria-label="刷新"
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:refresh" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="canUpdate"
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

    <ModalCaseForm
      ref="formModalRef"
      @saved="fetchPage"
    />
  </NFlex>
</template>
