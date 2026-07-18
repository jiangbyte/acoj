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
import ModalForm from './components/ModalForm.vue'

const formModalRef = ref<InstanceType<typeof ModalForm> | null>(null)
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
  { title: '节点名称', path: 'name', field: 'input' },
  { title: '状态', path: 'status', field: 'select', fieldProps: { options: ['ENABLED', 'DISABLED', 'BLOCKED'].map(option) } },
  { title: '在线', path: 'online', field: 'select', fieldProps: { options: [{ label: '在线', value: true }, { label: '离线', value: false }] } },
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
  { title: '名称', path: 'name', width: 180 },
  { title: '状态', path: 'status', width: 120, render: (row) => renderTag(row.status, row.status === 'ENABLED' ? '#18a058' : '#d03050') },
  { title: '在线', path: 'online', width: 100, render: (row) => renderTag(row.online ? 'ONLINE' : 'OFFLINE', row.online ? '#18a058' : '#909399') },
  { title: 'tier', path: 'tier', width: 90 },
  { title: 'last_ip', path: 'last_ip', width: 140, render: (row) => row.last_ip || '-' },
  { title: 'last_heartbeat_at', path: 'last_heartbeat_at', width: 180, render: (row) => formatDateTime(row.last_heartbeat_at) },
  { title: 'load', path: 'load', width: 90, render: (row) => row.load ?? '-' },
  { title: 'supported_languages', path: 'supported_languages', width: 220, render: (row) => row.supported_languages?.join(', ') },
  { title: 'supported_modes', path: 'supported_modes', width: 260, render: (row) => row.supported_modes?.join(', ') },
  { title: 'description', path: 'description', width: 200, ellipsis: { tooltip: true }, render: (row) => row.description || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:judgenodes:detail') ? <NButton type="info" size="small" text={true} onClick={() => openDetail(row.id)}>{renderButtonIcon('icon-park-outline:preview-open')}</NButton> : null}
        {hasPermission('oj:judgenodes:update') ? <NButton type="primary" size="small" text={true} onClick={() => openForm(row.id)}>{renderButtonIcon('icon-park-outline:edit')}</NButton> : null}
        {hasPermission('oj:judgenodes:delete') ? <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>{renderButtonIcon('icon-park-outline:delete')}</NButton> : null}
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

onMounted(fetchPage)

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojJudgeApi.node.page({ current: state.page, size: state.pageSize, ...state.searchValues })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
  } finally {
    state.loading = false
  }
}

function openForm(id?: string) {
  formModalRef.value?.openModal(id)
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
    content: ids.length > 1 ? `删除 ${ids.length} 个节点?` : '删除该节点?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojJudgeApi.node.remove({ ids })
      state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
      window.$message.success('删除成功')
      await fetchPage()
    },
  })
}

function renderTag(text: string, color: string) {
  return <NTag color={createTagColor(color)} bordered={false}>{text}</NTag>
}

function option(value: string) {
  return { label: value, value }
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm :form="searchForm" :columns="searchColumns" :reset-button-props="{ content: '重置' }" :search-button-props="{ content: '搜索' }" :collapse-button-props="{ content: searchForm.collapsed.value ? '展开' : '收起' }" />
    </ProCard>
    <ProDataTable class="min-h-0 flex-1" remote title="判题节点" row-key="id" :scroll-x="2000" :columns="tableColumns" :data="state.rows" :loading="state.loading" :pagination="pagination" :checked-row-keys="state.checkedRowKeys" :on-update-checked-row-keys="handleCheckedRowKeys">
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('oj:judgenodes:create')" type="primary" text title="新增" aria-label="新增" @click="openForm()"><template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template></NButton>
          <NButton text title="刷新" aria-label="刷新" :loading="state.loading" @click="fetchPage"><template #icon><NIcon><Icon icon="icon-park-outline:reload" /></NIcon></template></NButton>
          <NButton v-if="hasPermission('oj:judgenodes:delete')" type="error" text title="批量删除" aria-label="批量删除" :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)"><template #icon><NIcon><Icon icon="icon-park-outline:delete" /></NIcon></template></NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
