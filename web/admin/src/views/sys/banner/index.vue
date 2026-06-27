<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { bannerApi } from '@/api'
import {
  createProSearchForm,
  ProCard,
  ProDataTable,
  ProSearchForm,
} from 'pro-naive-ui'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  categoryLabelKeyMap,
  categoryOptions,
  displayScopeLabelKeyMap,
  displayScopeOptions,
  displayScopeTagTypeMap,
  linkTypeLabelKeyMap,
  positionLabelKeyMap,
  positionOptions,
  statusLabelKeyMap,
  statusOptions,
  statusTagTypeMap,
  typeLabelKeyMap,
  typeOptions,
} from './constants'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
const banners = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const searchValues = ref<any>({})
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const checkedRowKeys = ref<string[]>([])
const page = ref(1)
const pageSize = ref(20)

const searchForm = createProSearchForm<any>({
  onSubmit(values) {
    searchValues.value = normalizeSearchValues(values)
    page.value = 1
    fetchBannerPage()
  },
  onReset() {
    searchValues.value = {}
    page.value = 1
    fetchBannerPage()
  },
})

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  {
    title: t('pages.sys.banner.displayScope'),
    path: 'display_scope',
    field: 'select',
    fieldProps: {
      options: translateOptions(displayScopeOptions),
    },
  },
  {
    title: t('pages.sys.banner.category'),
    path: 'category',
    field: 'select',
    fieldProps: {
      options: translateOptions(categoryOptions),
    },
  },
  {
    title: t('pages.sys.banner.type'),
    path: 'type',
    field: 'select',
    fieldProps: {
      options: translateOptions(typeOptions),
    },
  },
  {
    title: t('pages.sys.banner.position'),
    path: 'position',
    field: 'select',
    fieldProps: {
      options: translateOptions(positionOptions),
    },
  },
  {
    title: t('common.often.status'),
    path: 'status',
    field: 'select',
    fieldProps: {
      options: translateOptions(statusOptions),
    },
  },
])

const pagination = computed<PaginationProps>(() => ({
  page: page.value,
  pageSize: pageSize.value,
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => t('common.often.total', { count: itemCount }),
  onUpdatePage: (value) => {
    page.value = value
    fetchBannerPage()
  },
  onUpdatePageSize: (value) => {
    pageSize.value = value
    page.value = 1
    fetchBannerPage()
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
      <img
        class="banner-table-image"
        src={row.image}
        alt={row.title || t('pages.sys.banner.image')}
      />
    ),
  },
  {
    title: t('pages.sys.banner.displayScope'),
    path: 'display_scope',
    width: 120,
    render: (row) => (
      <NTag type={displayScopeTagTypeMap[row.display_scope] ?? 'default'} bordered={false}>
        {displayLabel(displayScopeLabelKeyMap, row.display_scope)}
      </NTag>
    ),
  },
  {
    title: t('pages.sys.banner.category'),
    path: 'category',
    width: 150,
    render: (row) => displayLabel(categoryLabelKeyMap, row.category),
  },
  {
    title: t('pages.sys.banner.type'),
    path: 'type',
    width: 120,
    render: (row) => displayLabel(typeLabelKeyMap, row.type),
  },
  {
    title: t('pages.sys.banner.position'),
    path: 'position',
    width: 160,
    render: (row) => displayLabel(positionLabelKeyMap, row.position),
  },
  {
    title: t('pages.sys.banner.linkType'),
    path: 'link_type',
    width: 110,
    render: (row) => displayLabel(linkTypeLabelKeyMap, row.link_type),
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
      <NTag type={statusTagTypeMap[row.status] ?? 'default'} bordered={false}>
        {displayLabel(statusLabelKeyMap, row.status)}
      </NTag>
    ),
  },
  {
    title: t('common.often.updateTime'),
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
        <NButton type="error" size="small" text={true} onClick={() => confirmDeleteBanners(row.id)}>
          {t('common.often.delete')}
        </NButton>
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => checkedRowKeys.value.length > 0)

onMounted(() => {
  fetchBannerPage()
})

async function fetchBannerPage() {
  loading.value = true
  try {
    const response = await bannerApi.page({
      current: page.value,
      size: pageSize.value,
      ...searchValues.value,
    })
    const data = response.data ?? {}
    banners.value = data.records ?? []
    total.value = data.total ?? 0
    page.value = data.current ?? page.value
    pageSize.value = data.size ?? pageSize.value
  } finally {
    loading.value = false
  }
}

function normalizeSearchValues(values: any) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== ''),
  )
}

function translateOptions(options: Array<{ labelKey: string; value: string }>) {
  return options.map((item) => ({
    label: t(item.labelKey),
    value: item.value,
  }))
}

function displayLabel(map: Record<string, string>, value?: string | null) {
  if (!value) {
    return '-'
  }
  const labelKey = map[value]
  return labelKey ? t(labelKey) : value
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
  checkedRowKeys.value = keys.map(String)
}

function confirmDeleteBanners(value: string | string[]) {
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
    onPositiveClick: () => deleteBanners(ids),
  })
}

async function deleteBanners(ids: string[]) {
  await bannerApi.remove({ ids })
  checkedRowKeys.value = checkedRowKeys.value.filter((key) => !ids.includes(key))

  window.$message.success(t('common.often.deleteSuccess'))
  await fetchBannerPage()
  if (!banners.value.length && total.value > 0 && page.value > 1) {
    page.value -= 1
    await fetchBannerPage()
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
      :title="t('pages.sys.banner.title')"
      row-key="id"
      :scroll-x="1780"
      :columns="tableColumns"
      :data="banners"
      :loading="loading"
      :pagination="pagination"
      :checked-row-keys="checkedRowKeys"
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
          <NButton
            type="error"
            ghost
            :disabled="!hasCheckedRows"
            @click="confirmDeleteBanners(checkedRowKeys)"
          >
            {{ t('common.often.batchDelete') }}
            {{ t('common.often.total', { count: checkedRowKeys.length }) }}
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchBannerPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>

<style scoped>
.banner-table-image {
  width: 96px;
  height: 42px;
  object-fit: cover;
  border-radius: 6px;
  background: var(--n-color-embedded);
}
</style>
