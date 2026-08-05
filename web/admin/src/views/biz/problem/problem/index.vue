<script setup lang="tsx">
import type { PaginationProps, SelectOption } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi, ojProblemGroupApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const groupOptions = ref<SelectOption[]>([])
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

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      code: (value) => String(value).trim(),
      name: (value) => String(value).trim(),
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
  { title: '题目编码', path: 'code', field: 'input' },
  { title: '题目标题', path: 'name', field: 'input' },
  {
    title: '分组',
    path: 'group_id',
    field: 'select',
    fieldProps: {
      options: groupOptions.value,
      filterable: true,
      clearable: true,
    },
  },
  {
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: {
      options: [
        { label: '草稿', value: 'draft' },
        { label: '就绪', value: 'ready' },
        { label: '已发布', value: 'published' },
      ],
      clearable: true,
    },
  },
  {
    title: '公开题库',
    path: 'is_public',
    field: 'select',
    fieldProps: {
      options: [
        { label: '公开', value: true },
        { label: '竞赛专用', value: false },
      ],
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
  { type: 'selection', fixed: 'left' },
  { title: '编码', path: 'code', width: 120, ellipsis: { tooltip: true } },
  { title: '标题', path: 'name', width: 180, ellipsis: { tooltip: true } },
  { title: '分组', path: 'group_name', width: 120, ellipsis: { tooltip: true }, render: row => row.group_name || '-' },
  {
    title: '类型',
    path: 'type_names',
    width: 140,
    ellipsis: { tooltip: true },
    render: row => (Array.isArray(row.type_names) && row.type_names.length ? row.type_names.join(', ') : '-'),
  },
  { title: '时间限制(ms)', path: 'time_limit_ms', width: 110 },
  { title: '内存(KB)', path: 'memory_limit_kb', width: 100 },
  { title: '分值', path: 'points', width: 80 },
  {
    title: '难度',
    path: 'difficulty',
    width: 80,
    render: (row) => {
      const label = dictTypeData('PROBLEM_DIFFICULTY', row.difficulty) || row.difficulty || '-'
      const color = dictTypeColor('PROBLEM_DIFFICULTY', row.difficulty)
      return color ? (
        <NTag size="small" bordered={false} color={{ color: 'transparent', textColor: color }}>
          {label}
        </NTag>
      ) : (
        label
      )
    },
  },
  {
    title: '状态',
    path: 'status',
    width: 90,
    render: (row) => {
      const map: Record<string, string> = { draft: '草稿', ready: '就绪', published: '已发布' }
      return map[row.status] || row.status || '-'
    },
  },
  {
    title: '公开题库',
    path: 'is_public',
    width: 100,
    render: (row) => (
      <NTag size="small" type={row.is_public ? 'success' : 'warning'}>
        {row.is_public ? '公开' : '竞赛专用'}
      </NTag>
    ),
  },
  { title: '更新时间', path: 'updated_at', width: 170, render: row => formatDateTime(row.updated_at) },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    fixed: 'right',
    render: row => (
      <NFlex size={8} align="center">
        {hasPermission('biz:problem:problem:detail') ? (
          <NButton type="info" size="small" text onClick={() => goDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('biz:problem:problem:update') ? (
          <NButton type="primary" size="small" text onClick={() => goEdit(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('biz:problem:problem:delete') ? (
          <NButton type="error" size="small" text onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

onMounted(async () => {
  await loadGroupOptions()
  await fetchPage()
})

async function loadGroupOptions() {
  const response = await ojProblemGroupApi.list()
  const records = response.data ?? []
  groupOptions.value = records.map((item: any) => ({
    label: item.name,
    value: item.id,
  }))
}

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojProblemApi.page({ current: state.page, size: state.pageSize, ...state.searchValues })
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

function goCreate() {
  router.push('/biz/problem/problem/create')
}

function goDetail(id: string) {
  router.push({ path: '/biz/problem/problem/detail', query: { id } })
}

function goEdit(id: string) {
  router.push({ path: '/biz/problem/problem/edit', query: { id, tab: 'basic' } })
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
    content: ids.length > 1 ? `删除 ${ids.length} 条记录?` : '删除该记录?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojProblemApi.remove({ ids })
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
      title="题目"
      row-key="id"
      :scroll-x="960"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('biz:problem:problem:create')" type="primary" text @click="goCreate">
            <template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template>
          </NButton>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon><NIcon><Icon icon="icon-park-outline:refresh" /></NIcon></template>
          </NButton>
          <NButton v-if="hasPermission('biz:problem:problem:delete')" type="error" text :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)">
            <template #icon><NIcon><Icon icon="icon-park-outline:delete" /></NIcon></template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>
  </NFlex>
</template>
