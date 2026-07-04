<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { NButton, NFlex, NIcon, NImage, NTag } from 'naive-ui'
import { bannerApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeData, dictTypeColor } from '@/utils/dict'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  banners: [] as any[],
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
  {
    title: 'Display Scope',
    path: 'display_scope',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_DISPLAY_SCOPE'),
    },
  },
  {
    title: 'Category',
    path: 'category',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_CATEGORY'),
    },
  },
  {
    title: 'Type',
    path: 'type',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_TYPE'),
    },
  },
  {
    title: 'Position',
    path: 'position',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_POSITION'),
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
    title: 'Title',
    path: 'title',
    width: 180,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Image',
    key: 'image',
    width: 130,
    render: (row) => (
      <NImage
        src={row.image}
        alt={row.title || 'Image'}
        width={96}
        height={42}
        objectFit="cover"
      />
    ),
  },
  {
    title: 'Display Scope',
    path: 'display_scope',
    width: 120,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('BANNER_DISPLAY_SCOPE', row.display_scope))}
        bordered={false}
      >
        {dictTypeData('BANNER_DISPLAY_SCOPE', row.display_scope)}
      </NTag>
    ),
  },
  {
    title: 'Category',
    path: 'category',
    width: 150,
    render: (row) => dictTypeData('BANNER_CATEGORY', row.category),
  },
  {
    title: 'Type',
    path: 'type',
    width: 120,
    render: (row) => dictTypeData('BANNER_TYPE', row.type),
  },
  {
    title: 'Position',
    path: 'position',
    width: 160,
    render: (row) => dictTypeData('BANNER_POSITION', row.position),
  },
  {
    title: 'Link Type',
    path: 'link_type',
    width: 110,
    render: (row) => dictTypeData('BANNER_LINK_TYPE', row.link_type),
  },
  {
    title: 'Sort',
    path: 'sort',
    width: 90,
  },
  {
    title: 'Interactions',
    path: 'interaction_count',
    width: 120,
  },
  {
    title: 'Status',
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status)}
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
        {hasPermission('sys:banner:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('sys:banner:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('sys:banner:delete') ? (
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
    const response = await bannerApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.banners = data.records ?? []
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
      ? `Delete ${ids.length} selected display images?`
      : 'Delete this display image?',
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await bannerApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success('Deleted successfully')
  await fetchPage()
  if (!state.banners.length && state.total > 0 && state.page > 1) {
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
      :title="'Display Image Page'"
      row-key="id"
      :scroll-x="1780"
      :columns="tableColumns"
      :data="state.banners"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('sys:banner:create')" type="primary" text :title="'Add'" :aria-label="'Add'" @click="openCreateModal">
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
            v-if="hasPermission('sys:banner:delete')"
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
