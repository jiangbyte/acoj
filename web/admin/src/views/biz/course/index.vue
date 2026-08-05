<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojCourseApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModalForm from './components/ModalForm.vue'

const route = useRoute()
const router = useRouter()
const formModalRef = ref<any>(null)
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' },
]

const visibilityOptions = [
  { label: '公开', value: 'PUBLIC' },
  { label: '私有', value: 'PRIVATE' },
]

const bindingModeOptions = [
  { label: '合班上课', value: 'SHARED' },
  { label: '分班开课', value: 'PER_CLASS' },
]

const accessScopeOptions = [
  { label: '公开课', value: 'OPEN' },
  { label: '私有课', value: 'CLASS' },
]

const visibilityLabel: Record<string, string> = {
  PUBLIC: '公开',
  PRIVATE: '私有',
}

const bindingModeLabel: Record<string, string> = {
  SHARED: '合班',
  PER_CLASS: '分班',
}

const accessScopeLabel: Record<string, string> = {
  OPEN: '公开课',
  CLASS: '私有课',
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
  { title: '班级 ID', path: 'class_id', field: 'input' },
  { title: '课程名称', path: 'name', field: 'input' },
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
  {
    title: '课程类型',
    path: 'access_scope',
    field: 'select',
    fieldProps: { options: accessScopeOptions, clearable: true },
  },
  {
    title: '开课模式',
    path: 'binding_mode',
    field: 'select',
    fieldProps: { options: bindingModeOptions, clearable: true },
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
  { type: 'selection', fixed: 'left' },
  { title: '主键', path: 'id', width: 150, ellipsis: { tooltip: true } },
  {
    title: '所属班级',
    key: 'classes',
    width: 200,
    ellipsis: { tooltip: true },
    render: (row) => {
      const classes = Array.isArray(row.classes) ? row.classes : []
      if (classes.length) {
        return classes.map((c: any) => c.name || c.code || c.id).join('、')
      }
      const ids = Array.isArray(row.class_ids) ? row.class_ids : []
      return ids.join('、') || row.class_id || '-'
    },
  },
  { title: '课程名称', path: 'name', width: 150, ellipsis: { tooltip: true } },
  { title: '状态', path: 'status', width: 100 },
  {
    title: '类型',
    path: 'access_scope',
    width: 80,
    render: row => accessScopeLabel[row.access_scope] ?? row.access_scope,
  },
  {
    title: '模式',
    path: 'binding_mode',
    width: 80,
    render: row => (row.access_scope === 'OPEN' ? '-' : (bindingModeLabel[row.binding_mode] ?? row.binding_mode)),
  },
  {
    title: '可见性',
    path: 'visibility',
    width: 90,
    render: row => visibilityLabel[row.visibility] ?? row.visibility,
  },
  { title: '排序', path: 'sort', width: 70 },
  { title: '更新时间', path: 'updated_at', width: 190, render: row => formatDateTime(row.updated_at) },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: row => (
      <NFlex size={12}>
        {hasPermission('biz:course:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => goDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('biz:course:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('biz:course:delete') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

onMounted(() => {
  const classId = route.query.class_id ? String(route.query.class_id) : ''
  if (classId) {
    searchForm.setInitialValues({ class_id: classId })
    state.searchValues = { class_id: classId }
  }
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojCourseApi.page({ current: state.page, size: state.pageSize, ...state.searchValues })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter(key => state.rows.some(item => item.id === key))
  } finally {
    state.loading = false
  }
}

function goDetail(id: string) {
  router.push({ path: '/biz/course/detail', query: { id } })
}

function openCreateModal() {
  const classId = state.searchValues.class_id ?? ''
  formModalRef.value?.openModal(undefined, classId ? { class_id: classId } : {})
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) return
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    content: ids.length > 1 ? `删除 ${ids.length} 条记录?` : '删除该记录?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojCourseApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter(key => !ids.includes(key))
  window.$message.success('删除成功')
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
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="课程管理"
      row-key="id"
      :scroll-x="1200"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('biz:course:create')" type="primary" text @click="openCreateModal">
            <template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template>
          </NButton>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon><NIcon><Icon icon="icon-park-outline:refresh" /></NIcon></template>
          </NButton>
          <NButton v-if="hasPermission('biz:course:delete')" type="error" text :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)">
            <template #icon><NIcon><Icon icon="icon-park-outline:delete" /></NIcon></template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
  </NFlex>
</template>
