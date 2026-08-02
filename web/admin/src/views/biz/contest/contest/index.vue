<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojContestApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
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

const lifecycleTagType: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
  SCHEDULED: 'info',
  RUNNING: 'success',
  ENDED: 'warning',
  LOCKED: 'error',
}

const lifecycleLabel: Record<string, string> = {
  SCHEDULED: '未开始',
  RUNNING: '进行中',
  ENDED: '已结束',
  LOCKED: '已锁定',
}

const formatLabel: Record<string, string> = {
  default: 'Default',
  acm: 'ACM',
  icpc: 'ICPC',
  atcoder: 'AtCoder',
  oi: 'OI',
  ioi: 'IOI',
}

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      key: (value) => String(value).trim(),
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
  { title: '竞赛标识', path: 'key', field: 'input' },
  { title: '竞赛名称', path: 'name', field: 'input' },
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
  { title: '标识', path: 'key', width: 120, ellipsis: { tooltip: true } },
  { title: '名称', path: 'name', width: 180, ellipsis: { tooltip: true } },
  {
    title: '状态',
    path: 'lifecycle_status',
    width: 90,
    render: (row) => (
      <NTag size="small" type={lifecycleTagType[row.lifecycle_status] || 'default'}>
        {lifecycleLabel[row.lifecycle_status] || row.lifecycle_status || '-'}
      </NTag>
    ),
  },
  {
    title: '赛制',
    path: 'format_name',
    width: 90,
    render: row => formatLabel[row.format_name] || row.format_name || '-',
  },
  {
    title: 'Rated',
    path: 'is_rated',
    width: 70,
    render: (row) => (
      <NTag size="small" type={row.is_rated ? 'success' : 'default'} bordered={false}>
        {row.is_rated ? '是' : '否'}
      </NTag>
    ),
  },
  {
    title: '标签',
    path: 'tag_names',
    width: 140,
    ellipsis: { tooltip: true },
    render: row => (Array.isArray(row.tag_names) && row.tag_names.length ? row.tag_names.join(', ') : '-'),
  },
  { title: '开始时间', path: 'start_time', width: 170, render: row => formatDateTime(row.start_time) },
  { title: '结束时间', path: 'end_time', width: 170, render: row => formatDateTime(row.end_time) },
  {
    title: '公开',
    path: 'is_visible',
    width: 70,
    render: (row) => (
      <NTag size="small" type={row.is_visible ? 'success' : 'default'} bordered={false}>
        {row.is_visible ? '是' : '否'}
      </NTag>
    ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: row => (
      <NFlex size={8} align="center">
        {hasPermission('biz:contest:contest:detail') ? (
          <NButton type="info" size="small" text title="详情" onClick={() => goDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('biz:contest:contest:update') ? (
          <NButton type="primary" size="small" text title="编辑" onClick={() => goEdit(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('biz:contest:contest:create') ? (
          <NButton size="small" text title="克隆" onClick={() => handleClone(row.id)}>
            {renderButtonIcon('icon-park-outline:copy')}
          </NButton>
        ) : null}
        {hasPermission('biz:contest:contest:update') ? (
          row.lifecycle_status === 'LOCKED'
            ? (
                <NButton size="small" text title="解锁" onClick={() => handleUnlock(row.id)}>
                  {renderButtonIcon('icon-park-outline:unlock')}
                </NButton>
              )
            : (
                <NButton size="small" text title="锁定" onClick={() => handleLock(row.id)}>
                  {renderButtonIcon('icon-park-outline:lock')}
                </NButton>
              )
        ) : null}
        {hasPermission('biz:contest:contest:delete') ? (
          <NButton type="error" size="small" text title="删除" onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
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
    const response = await ojContestApi.page({ current: state.page, size: state.pageSize, ...state.searchValues })
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
  router.push('/biz/contest/contest/create')
}

function goDetail(id: string) {
  router.push({ path: '/biz/contest/contest/detail', query: { id } })
}

function goEdit(id: string) {
  router.push({ path: '/biz/contest/contest/edit', query: { id, tab: 'basic' } })
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

async function handleClone(id: string) {
  window.$dialog.warning({
    title: '克隆竞赛',
    content: '将复制该竞赛配置与题目，是否继续？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      const response = await ojContestApi.clone({ contest_id: id, copy_staff: false })
      window.$message.success('克隆成功')
      const newId = response.data
      if (newId) {
        goEdit(String(newId))
      } else {
        await fetchPage()
      }
    },
  })
}

async function handleLock(id: string) {
  await ojContestApi.lock({ contest_id: id })
  window.$message.success('已锁定')
  await fetchPage()
}

async function handleUnlock(id: string) {
  await ojContestApi.unlock({ contest_id: id })
  window.$message.success('已解锁')
  await fetchPage()
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
  await ojContestApi.remove({ ids })
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
      title="竞赛"
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
          <NButton v-if="hasPermission('biz:contest:contest:create')" type="primary" text @click="goCreate">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:plus" /></NIcon>
            </template>
          </NButton>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
            </template>
          </NButton>
          <NButton v-if="hasPermission('biz:contest:contest:delete')" type="error" text :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:delete" /></NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>
  </NFlex>
</template>
