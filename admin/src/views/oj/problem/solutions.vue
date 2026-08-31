<!--
  Author: Charlie

  OJ 题目参考答案维护（独立页，自题目列表操作列进入）。
-->
<script setup lang="tsx">
import type { PaginationProps, SelectOption } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi, ojProblemSolutionApi } from '@/api'
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
import ModalSolutionForm from './components/ModalSolutionForm.vue'

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
  return key ? `参考答案 · ${displayValue(key)}` : '参考答案'
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)
const canUpdate = computed(() => hasPermission('oj:problem:update'))

const languageOptions = computed<SelectOption[]>(() => {
  const langs = Array.isArray(state.problem.allowed_languages)
    ? state.problem.allowed_languages
    : []
  return langs.map((lang: string) => ({ label: lang, value: lang }))
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
  { title: '语言', path: 'language', field: 'input' },
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
  { title: '语言', path: 'language', width: 120 },
  {
    title: '默认',
    path: 'is_default',
    width: 80,
    render: (row) => (
      <NTag size="small" bordered={false} type={row.is_default ? 'success' : 'default'}>
        {row.is_default ? '是' : '否'}
      </NTag>
    ),
  },
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
    title: '备注',
    path: 'remark',
    ellipsis: { tooltip: true },
    render: (row) => displayValue(row.remark),
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
    width: 110,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
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
    const response = await ojProblemSolutionApi.page({
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

function openCreateModal() {
  if (!problemId.value) return
  formModalRef.value?.openModal({
    problemId: problemId.value,
    languageOptions: languageOptions.value,
  })
}

function openEditModal(id: string) {
  if (!problemId.value) return
  formModalRef.value?.openModal({
    problemId: problemId.value,
    id,
    languageOptions: languageOptions.value,
  })
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) return
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除参考答案',
    content: ids.length > 1 ? `删除 ${ids.length} 条参考答案?` : '删除该参考答案?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojProblemSolutionApi.remove({ ids })
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
      :scroll-x="900"
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
            @click="openCreateModal"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
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
          <NButton
            v-if="canUpdate"
            type="error"
            text
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

    <ModalSolutionForm
      ref="formModalRef"
      @saved="fetchPage"
    />
  </NFlex>
</template>
