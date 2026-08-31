<!--
  Author: Charlie

  OJ 标签管理。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojTagApi } from '@/api'
import { readPageMeta } from '@/utils/wire'
import {
  dictList,
  createTagColor,
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
  { title: '名称', path: 'name', field: 'input' },
  {
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: {
      options: dictList('COMMON_STATUS'),
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
  { title: '名称', path: 'name', width: 180, ellipsis: { tooltip: true } },
  {
    title: '状态',
    path: 'status',
    width: 100,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || displayValue(row.status)}
      </NTag>
    ),
  },
  {
    title: '更新时间',
    path: 'updated_at',
    width: 190,
    render: (row) => formatDateTime(row.updated_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:tag:detail') ? (
          <NButton type="info" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:tag:update') ? (
          <NButton type="primary" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('oj:tag:delete') ? (
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
    const response = await ojTagApi.page({
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

function openCreateModal() {
  formModalRef.value?.openModal()
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
  await ojTagApi.remove({ ids })
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
      title="标签管理"
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
            v-if="hasPermission('oj:tag:create')"
            type="primary"
            text
            @click="openCreateModal"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:plus" /></NIcon>
            </template>
          </NButton>
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
            v-if="hasPermission('oj:tag:delete')"
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
      <template
        v-if="!state.loading && !state.rows.length"
        #empty
      >
        <div class="tag-empty">
          暂无标签。请先新增标签，再到「题目管理」编辑题目时绑定。
        </div>
      </template>
    </ProDataTable>

    <ModalDetail ref="detailModalRef" />
    <ModalForm
      ref="formModalRef"
      @saved="fetchPage"
    />
  </NFlex>
</template>

<style scoped>
.tag-empty {
  padding: 24px 12px;
  color: var(--n-text-color-3, #999);
  font-size: 13px;
  text-align: center;
}
</style>
