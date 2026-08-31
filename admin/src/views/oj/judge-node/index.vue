<!--
  Author: Charlie

  OJ 执行机管理。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojJudgeNodeApi } from '@/api'
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
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const runtimeStatusType: Record<string, 'success' | 'warning' | 'error' | 'default'> = {
  ONLINE: 'success',
  OFFLINE: 'default',
  UNHEALTHY: 'error',
}

const circuitStateType: Record<string, 'success' | 'warning' | 'error' | 'default'> = {
  CLOSED: 'success',
  OPEN: 'error',
  HALF_OPEN: 'warning',
}

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  page: 1,
  pageSize: 20,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

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
  { title: '编码', path: 'code', field: 'input' },
  { title: '名称', path: 'name', field: 'input' },
  {
    title: '管理状态',
    path: 'admin_status',
    field: 'select',
    fieldProps: { options: dictList('OJ_JUDGE_ADMIN_STATUS') },
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
  { title: '名称', path: 'name', width: 140, ellipsis: { tooltip: true } },
  { title: '地址', path: 'base_url', width: 220, ellipsis: { tooltip: true } },
  {
    title: '管理状态',
    path: 'admin_status',
    width: 100,
    render: (row) => (
      <NTag
        size="small"
        color={createTagColor(dictTypeColor('OJ_JUDGE_ADMIN_STATUS', row.admin_status))}
        bordered={false}
      >
        {dictTypeData('OJ_JUDGE_ADMIN_STATUS', row.admin_status) || displayValue(row.admin_status)}
      </NTag>
    ),
  },
  {
    title: '运行状态',
    path: 'runtime_status',
    width: 100,
    render: (row) => (
      <NTag size="small" bordered={false} type={runtimeStatusType[row.runtime_status] || 'default'}>
        {displayValue(row.runtime_status)}
      </NTag>
    ),
  },
  {
    title: '熔断',
    path: 'circuit_state',
    width: 100,
    render: (row) => (
      <NTag size="small" bordered={false} type={circuitStateType[row.circuit_state] || 'default'}>
        {displayValue(row.circuit_state)}
      </NTag>
    ),
  },
  {
    title: '在途',
    path: 'inflight_count',
    width: 80,
    align: 'right',
    render: (row) => displayValue(row.inflight_count),
  },
  {
    title: '权重',
    path: 'weight',
    width: 70,
    align: 'right',
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
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:judgenode:detail') ? (
          <NButton type="info" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:judgenode:update') ? (
          <NButton type="primary" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('oj:judgenode:delete') ? (
          <NButton type="error" text={true} onClick={() => confirmDelete(row.id)}>
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
    const response = await ojJudgeNodeApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = pageMeta.size
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.rows.some((item) => item.id === key),
    )
  } finally {
    state.loading = false
  }
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
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
  await ojJudgeNodeApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success('删除成功')
  await fetchPage()
}
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
      title="执行机管理"
      row-key="id"
      :scroll-x="1400"
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
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('oj:judgenode:delete')"
            type="error"
            text
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:delete" /></NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalDetail ref="detailModalRef" />
    <ModalForm
      ref="formModalRef"
      @saved="fetchPage"
    />
  </NFlex>
</template>
