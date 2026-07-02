<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { NButton, NFlex, NIcon, NImage, NTag } from 'naive-ui'
import { fileApi } from '@/api'
import { createTagColor, normalizeSearchValues, renderButtonIcon, resolveFileUrl } from '@/utils'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeColor, dictTypeData } from '@/utils/dict'
import { useI18n } from 'vue-i18n'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const state = reactive({
  files: [] as any[],
  total: 0,
  loading: false,
  uploadLoading: false,
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
    title: t('resource.sys.file.original_name'),
    path: 'original_name',
    field: 'input',
  },
  {
    title: t('resource.sys.file.object_name'),
    path: 'object_name',
    field: 'input',
  },
  {
    title: t('resource.sys.file.storage_provider'),
    path: 'storage_provider',
    field: 'select',
    fieldProps: {
      options: dictList('STORAGE_PROVIDER'),
    },
  },
  {
    title: t('resource.sys.file.content_type'),
    path: 'content_type',
    field: 'input',
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
    width: 90,
    path: 'id',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.sys.file.preview'),
    key: 'preview',
    width: 120,
    render: (row) => renderPreview(row),
  },
  {
    title: t('resource.sys.file.original_name'),
    path: 'original_name',
    width: 220,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.sys.file.object_name'),
    path: 'object_name',
    width: 320,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.sys.file.storage_provider'),
    path: 'storage_provider',
    width: 130,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('STORAGE_PROVIDER', row.storage_provider))}
        bordered={false}
      >
        {dictTypeData('STORAGE_PROVIDER', row.storage_provider) || row.storage_provider}
      </NTag>
    ),
  },
  {
    title: t('resource.sys.file.bucket'),
    path: 'bucket',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.sys.file.content_type'),
    path: 'content_type',
    width: 180,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.sys.file.size'),
    path: 'size',
    width: 120,
    render: (row) => formatFileSize(row.size),
  },
  {
    title: t('common.often.updated_at'),
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
          {renderButtonIcon('icon-park-outline:preview-open')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
          {renderButtonIcon('icon-park-outline:edit')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openFile(row)}>
          {renderButtonIcon('icon-park-outline:link')}
        </NButton>
        <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
          {renderButtonIcon('icon-park-outline:delete')}
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
    const response = await fileApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.files = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
  } finally {
    state.loading = false
  }
}

function renderPreview(row: any) {
  const src = resolveFileUrl(row.url)
  if (!src || !isImage(row)) {
    return <NTag bordered={false}>{row.content_type || '-'}</NTag>
  }
  return (
    <NImage
      src={src}
      alt={row.original_name || t('resource.sys.file.preview')}
      width={72}
      height={48}
      objectFit="cover"
    />
  )
}

function isImage(row: any) {
  return String(row.content_type || '').startsWith('image/')
}

function formatFileSize(size?: number | string | null) {
  const value = Number(size ?? 0)
  if (!Number.isFinite(value) || value <= 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let current = value
  let unitIndex = 0
  while (current >= 1024 && unitIndex < units.length - 1) {
    current /= 1024
    unitIndex += 1
  }
  return `${current.toFixed(unitIndex === 0 ? 0 : 2)} ${units[unitIndex]}`
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
}

function triggerUpload() {
  fileInputRef.value?.click()
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  state.uploadLoading = true
  try {
    await fileApi.upload(file)
    window.$message.success(t('resource.sys.file.upload_success'))
    await fetchPage()
  } finally {
    state.uploadLoading = false
  }
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function openFile(row: any) {
  const url = resolveFileUrl(row.url)
  if (!url) {
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) {
    return
  }
  const isBatch = ids.length > 1

  window.$dialog.warning({
    title: isBatch ? t('common.often.batch_delete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('resource.sys.file.batch_delete_confirm', { count: ids.length })
      : t('resource.sys.file.delete_confirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await fileApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success(t('common.often.delete_success'))
  await fetchPage()
  if (!state.files.length && state.total > 0 && state.page > 1) {
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
        :reset-button-props="{ content: t('common.search_form.reset') }"
        :search-button-props="{ content: t('common.search_form.search') }"
        :collapse-button-props="{
          content: searchForm.collapsed.value
            ? t('common.search_form.expand')
            : t('common.search_form.collapse'),
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="t('resource.sys.file.title')"
      row-key="id"
      :scroll-x="1890"
      :columns="tableColumns"
      :data="state.files"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton type="primary" text :title="t('resource.sys.file.upload')" :aria-label="t('resource.sys.file.upload')" :loading="state.uploadLoading" @click="triggerUpload">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:upload" />
              </NIcon>
            </template>
          </NButton>
          <NButton text :title="t('common.reload')" :aria-label="t('common.reload')" :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            type="error"
            text
            :title="t('common.often.batch_delete')"
            :aria-label="t('common.often.batch_delete')"
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

    <input ref="fileInputRef" class="hidden" type="file" @change="handleFileChange" />
    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
