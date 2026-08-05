<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojTeamApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  page: 1,
  pageSize: 20,
})

const scopeOptions = [
  { label: '独立', value: 'INDEPENDENT' },
  { label: '课程', value: 'COURSE' },
]

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '禁用', value: 'DISABLED' },
  { label: '已解散', value: 'DISSOLVED' },
]

const visibilityOptions = [
  { label: '公开', value: 'PUBLIC' },
  { label: '私有', value: 'PRIVATE' },
]

const visibilityLabel: Record<string, string> = {
  PUBLIC: '公开',
  PRIVATE: '私有',
}

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
    title: '范围',
    path: 'scope',
    field: 'select',
    fieldProps: { options: scopeOptions, clearable: true },
  },
  { title: '课程 ID', path: 'course_id', field: 'input' },
  { title: '小组名称', path: 'name', field: 'input' },
  {
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: { options: statusOptions, clearable: true },
  },
  {
    title: '可见性',
    path: 'visibility',
    field: 'select',
    fieldProps: { options: visibilityOptions, clearable: true },
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
  { title: '主键', path: 'id', width: 150, ellipsis: { tooltip: true } },
  { title: '范围', path: 'scope', width: 90 },
  { title: '名称', path: 'name', width: 150, ellipsis: { tooltip: true } },
  { title: '课程 ID', path: 'course_id', width: 150, ellipsis: { tooltip: true } },
  { title: '状态', path: 'status', width: 90 },
  {
    title: '可见性',
    path: 'visibility',
    width: 90,
    render: row => visibilityLabel[row.visibility] ?? row.visibility,
  },
  { title: '成员数', path: 'member_count', width: 80 },
  { title: '更新时间', path: 'updated_at', width: 190, render: row => formatDateTime(row.updated_at) },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render: row => (
      hasPermission('biz:team:detail') ? (
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
          {renderButtonIcon('icon-park-outline:preview-open')}
        </NButton>
      ) : null
    ),
  },
])

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojTeamApi.page({ current: state.page, size: state.pageSize, ...state.searchValues })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
  } finally {
    state.loading = false
  }
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function openCreateModal() {
  const courseId = state.searchValues.course_id ?? ''
  formModalRef.value?.openModal(courseId ? { course_id: courseId } : {})
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
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="小组管理"
      row-key="id"
      :scroll-x="1100"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('biz:team:create')" type="primary" text @click="openCreateModal">
            <template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template>
          </NButton>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon><NIcon><Icon icon="icon-park-outline:refresh" /></NIcon></template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" @saved="fetchPage" />
  </NFlex>
</template>
