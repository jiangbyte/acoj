<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { positionApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeData, dictTypeColor } from '@/utils/dict'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  positions: [] as any[],
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
    state.searchValues = normalizeSearchValues(values, {
      name: (value) => String(value).trim(),
      code: (value) => String(value).trim(),
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
  {
    title: 'Position Name',
    path: 'name',
    field: 'input',
  },
  {
    title: 'Position Code',
    path: 'code',
    field: 'input',
  },
  {
    title: 'Position Category',
    path: 'category',
    field: 'select',
    fieldProps: {
      options: dictList('POSITION_CATEGORY'),
    },
  },
  {
    title: 'Status',
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
  prefix: ({ itemCount }) => `${itemCount} total`,
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
  {
    type: 'selection',
    fixed: 'left',
  },
  {
    title: 'ID',
    width: 80,
    path: 'id',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Position Name',
    path: 'name',
    width: 160,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Position Code',
    path: 'code',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Position Category',
    path: 'category',
    width: 130,
    render: (row) => dictTypeData('POSITION_CATEGORY', row.category) || row.category,
  },
  {
    title: 'Sort',
    path: 'sort',
    width: 90,
  },
  {
    title: 'Virtual Position',
    path: 'is_virtual',
    width: 110,
    render: (row) =>
      row.is_virtual ? 'Yes' : 'No',
  },
  {
    title: 'Status',
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || row.status}
      </NTag>
    ),
  },
  {
    title: 'Updated At',
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Operation',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('iam:position:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('iam:position:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('iam:position:delete') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await positionApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.positions = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.positions.some((item) => item.id === key),
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
  const isBatch = ids.length > 1

  window.$dialog.warning({
    title: isBatch ? 'Batch Delete' : 'Delete',
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? `Delete ${ids.length} selected positions?`
      : 'Delete this position?',
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await positionApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success('Deleted successfully')
  await fetchPage()
  if (!state.positions.length && state.total > 0 && state.page > 1) {
    state.page -= 1
    await fetchPage()
  }
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: 'Reset' }"
        :search-button-props="{ content: 'Search' }"
        :collapse-button-props="{
          content: searchForm.collapsed.value
            ? 'Expand'
            : 'Collapse',
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="'Position Management'"
      row-key="id"
      :scroll-x="1320"
      :columns="tableColumns"
      :data="state.positions"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('iam:position:create')" type="primary" text :title="'Add'" :aria-label="'Add'" @click="openCreateModal">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton text :title="'Reload'" :aria-label="'Reload'" :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('iam:position:delete')"
            type="error"
            text
            :title="'Batch Delete'"
            :aria-label="'Batch Delete'"
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

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>

<style scoped></style>
