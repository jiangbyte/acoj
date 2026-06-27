<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue'
import { NButton, NFlex, NIcon, NImage, NTag } from 'naive-ui'
import { bannerApi } from '@/api'
import { createTagColor, normalizeSearchValues } from '@/utils'
import { dictList, dictTypeColor, dictTypeData } from '@/utils/dict'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
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
    title: t('pages.sys.banner.displayScope'),
    path: 'display_scope',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_DISPLAY_SCOPE'),
    },
  },
  {
    title: t('pages.sys.banner.category'),
    path: 'category',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_CATEGORY'),
    },
  },
  {
    title: t('pages.sys.banner.type'),
    path: 'type',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_TYPE'),
    },
  },
  {
    title: t('pages.sys.banner.position'),
    path: 'position',
    field: 'select',
    fieldProps: {
      options: dictList('BANNER_POSITION'),
    },
  },
  {
    title: t('common.often.status'),
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
  prefix: ({ itemCount }) => t('common.often.total', { count: itemCount }),
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
    title: t('common.often.index'),
    width: 80,
    path: 'id',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.banner.titleField'),
    path: 'title',
    width: 180,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.banner.image'),
    key: 'image',
    width: 130,
    render: (row) => (
      <NImage
        src={row.image}
        alt={row.title || t('pages.sys.banner.image')}
        width={96}
        height={42}
        objectFit="cover"
      />
    ),
  },
  {
    title: t('pages.sys.banner.displayScope'),
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
    title: t('pages.sys.banner.category'),
    path: 'category',
    width: 150,
    render: (row) => dictTypeData('BANNER_CATEGORY', row.category),
  },
  {
    title: t('pages.sys.banner.type'),
    path: 'type',
    width: 120,
    render: (row) => dictTypeData('BANNER_TYPE', row.type),
  },
  {
    title: t('pages.sys.banner.position'),
    path: 'position',
    width: 160,
    render: (row) => dictTypeData('BANNER_POSITION', row.position),
  },
  {
    title: t('pages.sys.banner.linkType'),
    path: 'link_type',
    width: 110,
    render: (row) => dictTypeData('BANNER_LINK_TYPE', row.link_type),
  },
  {
    title: t('pages.sys.banner.sort'),
    path: 'sort',
    width: 90,
  },
  {
    title: t('pages.sys.banner.interactionCount'),
    path: 'interaction_count',
    width: 120,
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status)}
      </NTag>
    ),
  },
  {
    title: t('common.often.updatedAt'),
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 170,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
          {t('common.often.detail')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
          {t('common.often.edit')}
        </NButton>
        <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
          {t('common.often.delete')}
        </NButton>
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
    title: isBatch ? t('common.often.batchDelete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('pages.sys.banner.batchDeleteConfirm', { count: ids.length })
      : t('pages.sys.banner.deleteConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await bannerApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success(t('common.often.deleteSuccess'))
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
      <ProSearchForm :form="searchForm" :columns="searchColumns" />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="t('pages.sys.banner.title')"
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
          <NButton type="primary" ghost @click="openCreateModal">
            <template #icon>
              <NIcon>
                <Icon icon="ant-design:plus-outlined" />
              </NIcon>
            </template>
            {{ t('common.often.add') }}
          </NButton>
          <NButton ghost :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="ant-design:reload-outlined" />
              </NIcon>
            </template>
            {{ t('common.reload') }}
          </NButton>
          <NButton
            type="error"
            ghost
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            {{ t('common.often.batchDelete') }}
            {{ t('common.often.total', { count: state.checkedRowKeys.length }) }}
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>

<style scoped></style>
