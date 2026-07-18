<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojJudgeApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon } from 'naive-ui'
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
  { title: '判题节点ID', path: 'judge_node_id', field: 'input' },
  { title: '语言ID', path: 'language_id', field: 'input' },
  { title: '运行时名称', path: 'runtime_name', field: 'input' },
  { title: '运行时版本', path: 'runtime_version', field: 'input' },
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
  { title: 'judge_node_id', path: 'judge_node_id', width: 180 },
  { title: 'language_id', path: 'language_id', width: 160 },
  { title: 'runtime_name', path: 'runtime_name', width: 180 },
  { title: 'runtime_version', path: 'runtime_version', width: 180, render: (row) => row.runtime_version || '-' },
  { title: 'priority', path: 'priority', width: 100 },
  { title: 'created_at', path: 'created_at', width: 180, render: (row) => formatDateTime(row.created_at) },
  { title: 'updated_at', path: 'updated_at', width: 180, render: (row) => formatDateTime(row.updated_at) },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:runtimeversions:detail') ? <NButton type="info" size="small" text={true} onClick={() => openDetail(row.id)}>{renderButtonIcon('icon-park-outline:preview-open')}</NButton> : null}
        {hasPermission('oj:runtimeversions:update') ? <NButton type="primary" size="small" text={true} onClick={() => openForm(row.id)}>{renderButtonIcon('icon-park-outline:edit')}</NButton> : null}
        {hasPermission('oj:runtimeversions:delete') ? <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>{renderButtonIcon('icon-park-outline:delete')}</NButton> : null}
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

onMounted(fetchPage)

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojJudgeApi.runtimeVersion.page({ current: state.page, size: state.pageSize, ...state.searchValues })
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
    content: ids.length > 1 ? `删除 ${ids.length} 个运行时版本?` : '删除该运行时版本?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojJudgeApi.runtimeVersion.remove({ ids })
      state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
      window.$message.success('删除成功')
      await fetchPage()
    },
  })
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm :form="searchForm" :columns="searchColumns" :reset-button-props="{ content: '重置' }" :search-button-props="{ content: '搜索' }" :collapse-button-props="{ content: searchForm.collapsed.value ? '展开' : '收起' }" />
    </ProCard>
    <ProDataTable class="min-h-0 flex-1" remote title="运行时版本" row-key="id" :scroll-x="1450" :columns="tableColumns" :data="state.rows" :loading="state.loading" :pagination="pagination" :checked-row-keys="state.checkedRowKeys" :on-update-checked-row-keys="handleCheckedRowKeys">
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('oj:runtimeversions:create')" type="primary" text title="新增" aria-label="新增" @click="openForm()"><template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template></NButton>
          <NButton text title="刷新" aria-label="刷新" :loading="state.loading" @click="fetchPage"><template #icon><NIcon><Icon icon="icon-park-outline:reload" /></NIcon></template></NButton>
          <NButton v-if="hasPermission('oj:runtimeversions:delete')" type="error" text title="批量删除" aria-label="批量删除" :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)"><template #icon><NIcon><Icon icon="icon-park-outline:delete" /></NIcon></template></NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
