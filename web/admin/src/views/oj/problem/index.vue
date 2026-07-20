<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi } from '@/api'
import { createTagColor, dictList, dictTypeColor, formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NProgress, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'
import ModalDataManager from './components/ModalDataManager.vue'

const detailModalRef = ref<any>(null)
const formModalRef = ref<any>(null)
const dataManagerRef = ref<any>(null)

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
  { title: '题号', path: 'code', field: 'input' },
  { title: '标题', path: 'title', field: 'input' },
  { title: '题目类型', path: 'problem_type', field: 'select', fieldProps: { options: dictList('OJ_PROBLEM_TYPE') } },
  { title: '判题方式', path: 'judge_mode', field: 'select', fieldProps: { options: dictList('OJ_JUDGE_MODE') } },
  { title: '可见性', path: 'visibility', field: 'select', fieldProps: { options: dictList('OJ_PROBLEM_VISIBILITY') } },
  { title: '状态', path: 'status', field: 'select', fieldProps: { options: dictList('COMMON_STATUS') } },
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
  { title: 'id', path: 'id', width: 130, fixed: 'left', ellipsis: { tooltip: true } },
  { title: 'code', path: 'code', width: 110, fixed: 'left', ellipsis: { tooltip: true } },
  { title: 'title', path: 'title', width: 240, ellipsis: { tooltip: true } },
  { title: 'problem_type', path: 'problem_type', width: 140, render: (row) => <NTag color={createTagColor(dictTypeColor('OJ_PROBLEM_TYPE', row.problem_type))} bordered={false}>{row.problem_type}</NTag> },
  { title: 'judge_mode', path: 'judge_mode', width: 150, render: (row) => <NTag color={createTagColor(dictTypeColor('OJ_JUDGE_MODE', row.judge_mode))} bordered={false}>{row.judge_mode}</NTag> },
  { title: 'visibility', path: 'visibility', width: 140, render: (row) => <NTag color={createTagColor(dictTypeColor('OJ_PROBLEM_VISIBILITY', row.visibility))} bordered={false}>{row.visibility}</NTag> },
  { title: 'difficulty', path: 'difficulty', width: 110 },
  { title: 'time_limit_ms', path: 'time_limit_ms', width: 130 },
  { title: 'memory_limit_kb', path: 'memory_limit_kb', width: 150 },
  { title: 'points', path: 'points', width: 100 },
  { title: 'partial', path: 'partial', width: 100 },
  { title: 'ac_rate', path: 'ac_rate', width: 150, render: (row) => <NProgress type="line" percentage={Number(row.ac_rate ?? 0)} indicator-placement="inside" /> },
  { title: 'sort', path: 'sort', width: 90 },
  { title: 'status', path: 'status', width: 110, render: (row) => <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>{row.status}</NTag> },
  { title: 'updated_at', path: 'updated_at', width: 180, render: (row) => formatDateTime(row.updated_at), ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:problems:detail') ? (
          <NButton type="info" size="small" text onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:problems:update') ? (
          <NButton type="primary" size="small" text onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        <NButton type="warning" size="small" text title="数据管理" onClick={() => openDataManager(row.id)}>
          {renderButtonIcon('icon-park-outline:data')}
        </NButton>
        {hasPermission('oj:problems:delete') ? (
          <NButton type="error" size="small" text onClick={() => confirmDelete(row.id)}>
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
    const response = await ojProblemApi.page({
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

function openCreateModal() {
  formModalRef.value?.openDrawer(null)
}

function openEditModal(id: string) {
  formModalRef.value?.openDrawer(id)
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function openDataManager(id: string) {
  dataManagerRef.value?.openDrawer(id)
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) return
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    draggable: true,
    maskClosable: false,
    content: ids.length > 1 ? `删除 ${ids.length} 道题目?` : '删除该题目?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojProblemApi.remove({ ids })
      state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
      window.$message.success('删除成功')
      await fetchPage()
    },
  })
}

async function handleFormSaved() {
  await fetchPage()
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
      title="题目管理"
      row-key="id"
      :scroll-x="2450"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('oj:problems:create')" type="primary" text title="新增" aria-label="新增" @click="openCreateModal">
            <template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template>
          </NButton>
          <NButton text title="刷新" aria-label="刷新" :loading="state.loading" @click="fetchPage">
            <template #icon><NIcon><Icon icon="icon-park-outline:reload" /></NIcon></template>
          </NButton>
          <NButton
            v-if="hasPermission('oj:problems:delete')"
            type="error"
            text
            title="批量删除"
            aria-label="批量删除"
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            <template #icon><NIcon><Icon icon="icon-park-outline:delete" /></NIcon></template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalDetail ref="detailModalRef" />
    <ModalForm ref="formModalRef" @saved="handleFormSaved" @open-data-manager="(id: string) => dataManagerRef.value?.openDrawer(id)" />
    <ModalDataManager ref="dataManagerRef" />
  </NFlex>
</template>
