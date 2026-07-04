<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { messageApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { dictList, dictTypeColor, dictTypeData } from '@/utils/dict'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  notifications: [] as any[],
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
      title: (value) => String(value).trim(),
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
    title: 'Title',
    path: 'title',
    field: 'input',
  },
  {
    title: 'Status',
    path: 'status',
    field: 'select',
    fieldProps: {
      options: dictList('NOTIFICATION_STATUS'),
    },
  },
  {
    title: 'Target Account Type',
    path: 'target_account_type',
    field: 'select',
    fieldProps: {
      options: dictList('ACCOUNT_TYPE'),
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
    width: 90,
    path: 'id',
    ellipsis: { tooltip: true },
  },
  {
    title: 'Title',
    path: 'title',
    width: 220,
    ellipsis: { tooltip: true },
  },
  {
    title: 'Severity',
    path: 'severity',
    width: 120,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('NOTIFICATION_SEVERITY', row.severity))}
        bordered={false}
      >
        {dictTypeData('NOTIFICATION_SEVERITY', row.severity) || row.severity}
      </NTag>
    ),
  },
  {
    title: 'Target Scope',
    path: 'target_scope',
    width: 130,
    render: (row) => dictTypeData('MESSAGE_TARGET_SCOPE', row.target_scope) || row.target_scope,
  },
  {
    title: 'Target Account Type',
    path: 'target_account_type',
    width: 140,
    render: (row) =>
      dictTypeData('ACCOUNT_TYPE', row.target_account_type) || row.target_account_type || '-',
  },
  {
    title: 'Status',
    path: 'status',
    width: 120,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('NOTIFICATION_STATUS', row.status))}
        bordered={false}
      >
        {dictTypeData('NOTIFICATION_STATUS', row.status) || row.status}
      </NTag>
    ),
  },
  {
    title: 'Published At',
    path: 'publish_at',
    width: 190,
    ellipsis: { tooltip: true },
  },
  {
    title: 'Updated At',
    path: 'updated_at',
    width: 190,
    ellipsis: { tooltip: true },
  },
  {
    title: 'Operation',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('message:notification:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('message:notification:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('message:notification:publish') ? (
          <NButton type="success" size="small" text={true} onClick={() => publishData(row.id)}>
            {renderButtonIcon('icon-park-outline:send-one')}
          </NButton>
        ) : null}
        {hasPermission('message:notification:revoke') ? (
          <NButton type="warning" size="small" text={true} onClick={() => revokeData(row.id)}>
            {renderButtonIcon('icon-park-outline:back')}
          </NButton>
        ) : null}
        {hasPermission('message:notification:delete') ? (
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
    const response = await messageApi.notificationPage({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.notifications = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.notifications.some((item) => item.id === key),
    )
  } finally {
    state.loading = false
  }
}

function openCreateModal() {
  formModalRef.value?.openModal()
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
}

function openDetailModal(id: string) {
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
  const isBatch = ids.length > 1
  window.$dialog.warning({
    title: isBatch ? 'Batch Delete' : 'Delete',
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? `Delete ${ids.length} selected users?`
      : `${'Delete '}${'?'}`,
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await messageApi.removeNotification({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success('Deleted successfully')
  await fetchPage()
}

async function publishData(id: string) {
  await messageApi.publishNotification({ id })
  window.$message.success('Published successfully')
  await fetchPage()
}

async function revokeData(id: string) {
  await messageApi.revokeNotification({ id })
  window.$message.success('Revoked successfully')
  await fetchPage()
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
      :title="'Notifications'"
      row-key="id"
      :scroll-x="1540"
      :columns="tableColumns"
      :data="state.notifications"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('message:notification:create')" type="primary" text :title="'Add'" :aria-label="'Add'" @click="openCreateModal">
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
            v-if="hasPermission('message:notification:delete')"
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
