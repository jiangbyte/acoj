<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue'
import { accountApi } from '@/api'
import { createTagColor, normalizeSearchValues } from '@/utils'
import { dictList, dictTypeColor, dictTypeData } from '@/utils/dict'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  accounts: [] as any[],
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
      account: (value) => String(value).trim(),
      name: (value) => String(value).trim(),
      phone: (value) => String(value).trim(),
      email: (value) => String(value).trim(),
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
    title: t('pages.sys.account.account'),
    path: 'account',
    field: 'input',
  },
  {
    title: t('pages.sys.account.name'),
    path: 'name',
    field: 'input',
  },
  {
    title: t('pages.sys.account.phone'),
    path: 'phone',
    field: 'input',
  },
  {
    title: t('pages.sys.account.email'),
    path: 'email',
    field: 'input',
  },
  {
    title: t('pages.sys.account.accountType'),
    path: 'account_type',
    field: 'select',
    fieldProps: {
      options: dictList('ACCOUNT_TYPE'),
    },
  },
  {
    title: t('pages.sys.account.accountStatus'),
    path: 'account_status',
    field: 'select',
    fieldProps: {
      options: dictList('ACCOUNT_STATUS'),
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
    title: t('pages.sys.account.account'),
    path: 'account',
    width: 140,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.account.name'),
    path: 'name',
    width: 130,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.account.nickname'),
    path: 'nickname',
    width: 130,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.account.accountType'),
    path: 'account_type',
    width: 120,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('ACCOUNT_TYPE', row.account_type))} bordered={false}>
        {dictTypeData('ACCOUNT_TYPE', row.account_type)}
      </NTag>
    ),
  },
  {
    title: t('pages.sys.account.accountStatus'),
    path: 'account_status',
    width: 120,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('ACCOUNT_STATUS', row.account_status))}
        bordered={false}
      >
        {dictTypeData('ACCOUNT_STATUS', row.account_status)}
      </NTag>
    ),
  },
  {
    title: t('pages.sys.account.phone'),
    path: 'phone',
    width: 150,
  },
  {
    title: t('pages.sys.account.email'),
    path: 'email',
    width: 220,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.account.isSuperuser'),
    path: 'is_superuser',
    width: 120,
    render: (row) => (row.is_superuser ? t('pages.sys.account.yes') : t('pages.sys.account.no')),
  },
  {
    title: t('pages.sys.account.latestLoginTime'),
    path: 'latest_login_time',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
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
    const response = await accountApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.accounts = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.accounts.some((item) => item.id === key),
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
    title: isBatch ? t('common.often.batchDelete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('pages.sys.account.batchDeleteConfirm', { count: ids.length })
      : t('pages.sys.account.deleteConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await accountApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success(t('common.often.deleteSuccess'))
  await fetchPage()
  if (!state.accounts.length && state.total > 0 && state.page > 1) {
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
      :title="t('pages.sys.account.title')"
      row-key="id"
      :scroll-x="1960"
      :columns="tableColumns"
      :data="state.accounts"
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
