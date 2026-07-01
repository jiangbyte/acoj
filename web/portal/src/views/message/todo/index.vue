<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue'
import { messageApi } from '@/api'
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
  todos: [] as any[],
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
    title: t('resource.message.todo.title_field'),
    path: 'title',
    field: 'input',
  },
  {
    title: t('common.often.status'),
    path: 'status',
    field: 'select',
    fieldProps: {
      options: dictList('TODO_STATUS'),
    },
  },
  {
    title: t('resource.message.todo.target_account_type'),
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
    ellipsis: { tooltip: true },
  },
  {
    title: t('resource.message.todo.title_field'),
    path: 'title',
    width: 220,
    ellipsis: { tooltip: true },
  },
  {
    title: t('resource.message.todo.priority'),
    path: 'priority',
    width: 120,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('TODO_PRIORITY', row.priority))} bordered={false}>
        {dictTypeData('TODO_PRIORITY', row.priority) || row.priority}
      </NTag>
    ),
  },
  {
    title: t('resource.message.todo.target_scope'),
    path: 'target_scope',
    width: 130,
    render: (row) => dictTypeData('MESSAGE_TARGET_SCOPE', row.target_scope) || row.target_scope,
  },
  {
    title: t('resource.message.todo.target_account_type'),
    path: 'target_account_type',
    width: 140,
    render: (row) => dictTypeData('ACCOUNT_TYPE', row.target_account_type) || row.target_account_type || '-',
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 120,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('TODO_STATUS', row.status))} bordered={false}>
        {dictTypeData('TODO_STATUS', row.status) || row.status}
      </NTag>
    ),
  },
  {
    title: t('resource.message.todo.due_at'),
    path: 'due_at',
    width: 190,
    ellipsis: { tooltip: true },
  },
  {
    title: t('common.often.updated_at'),
    path: 'updated_at',
    width: 190,
    ellipsis: { tooltip: true },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 230,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
          {t('common.often.detail')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
          {t('common.often.edit')}
        </NButton>
        <NButton type="warning" size="small" text={true} onClick={() => cancelData(row.id)}>
          {t('resource.message.todo.cancel_task')}
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
    const response = await messageApi.todoPage({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.todos = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.todos.some((item) => item.id === key),
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
    title: isBatch ? t('common.often.batch_delete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('common.often.batch_delete_confirm', { count: ids.length })
      : `${t('common.often.delete_confirm')}${t('common.often.delete_question')}`,
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await messageApi.removeTodo({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success(t('common.often.delete_success'))
  await fetchPage()
}

async function cancelData(id: string) {
  await messageApi.cancelTodoAdmin({ id })
  window.$message.success(t('resource.message.todo.cancel_success'))
  await fetchPage()
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
      :title="t('resource.message.todo.title')"
      row-key="id"
      :scroll-x="1570"
      :columns="tableColumns"
      :data="state.todos"
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
            {{ t('common.often.batch_delete') }}
            {{ t('common.often.total', { count: state.checkedRowKeys.length }) }}
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
