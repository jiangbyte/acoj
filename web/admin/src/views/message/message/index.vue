<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
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
const activeTab = ref<'threads' | 'groups'>('threads')
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  page: 1,
  pageSize: 20,
})

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      name: (value) => String(value).trim(),
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

const searchColumns = computed<ProSearchFormColumns<any>>(() =>
  activeTab.value === 'threads'
    ? [
        {
          title: t('resource.message.message.thread_type'),
          path: 'thread_type',
          field: 'select',
          fieldProps: {
            options: dictList('MESSAGE_THREAD_TYPE'),
          },
        },
      ]
    : [
        {
          title: t('resource.message.message.group_name'),
          path: 'name',
          field: 'input',
        },
        {
          title: t('common.often.status'),
          path: 'status',
          field: 'select',
          fieldProps: {
            options: dictList('COMMON_STATUS'),
          },
        },
      ],
)

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

const threadColumns = computed<ProDataTableColumns<any>>(() => [
  {
    title: t('common.often.index'),
    width: 100,
    path: 'id',
    ellipsis: { tooltip: true },
  },
  {
    title: t('resource.message.message.thread_title'),
    path: 'title',
    width: 220,
    ellipsis: { tooltip: true },
    render: (row) => row.title || '-',
  },
  {
    title: t('resource.message.message.thread_type'),
    path: 'thread_type',
    width: 130,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('MESSAGE_THREAD_TYPE', row.thread_type))}
        bordered={false}
      >
        {dictTypeData('MESSAGE_THREAD_TYPE', row.thread_type) || row.thread_type}
      </NTag>
    ),
  },
  {
    title: t('resource.message.message.group_id'),
    path: 'group_id',
    width: 160,
    ellipsis: { tooltip: true },
    render: (row) => row.group_id || '-',
  },
  {
    title: t('resource.message.message.last_message_at'),
    path: 'last_message_at',
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
    width: 210,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row)}>
          {t('common.often.detail')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openSystemMessage(row.id)}>
          {t('resource.message.message.send_system')}
        </NButton>
      </NFlex>
    ),
  },
])

const groupColumns = computed<ProDataTableColumns<any>>(() => [
  {
    title: t('common.often.index'),
    width: 100,
    path: 'id',
    ellipsis: { tooltip: true },
  },
  {
    title: t('resource.message.message.group_name'),
    path: 'name',
    width: 220,
    ellipsis: { tooltip: true },
  },
  {
    title: t('resource.message.message.member_count'),
    path: 'member_count',
    width: 110,
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 120,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || row.status}
      </NTag>
    ),
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
    width: 100,
    fixed: 'right',
    render: (row) => (
      <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row)}>
        {t('common.often.detail')}
      </NButton>
    ),
  },
])

const tableColumns = computed<ProDataTableColumns<any>>(() =>
  activeTab.value === 'threads' ? threadColumns.value : groupColumns.value,
)

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const api = activeTab.value === 'threads' ? messageApi.threadPage : messageApi.groupPage
    const response = await api({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
  } finally {
    state.loading = false
  }
}

function handleTabUpdate(value: string) {
  activeTab.value = value as 'threads' | 'groups'
  state.searchValues = {}
  state.page = 1
  fetchPage()
}

function openSystemMessage(threadId: string) {
  formModalRef.value?.openModal(threadId)
}

function openDetailModal(row: any) {
  detailModalRef.value?.openModal(row, activeTab.value)
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <NFlex vertical>
        <NTabs :value="activeTab" type="line" @update:value="handleTabUpdate">
          <NTabPane name="threads" :tab="t('resource.message.message.threads')" />
          <NTabPane name="groups" :tab="t('resource.message.message.groups')" />
        </NTabs>
        <ProSearchForm :key="activeTab" :form="searchForm" :columns="searchColumns" />
      </NFlex>
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="t('resource.message.message.title')"
      row-key="id"
      :scroll-x="activeTab === 'threads' ? 1150 : 760"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NFlex>
          <NButton ghost :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="ant-design:reload-outlined" />
              </NIcon>
            </template>
            {{ t('common.reload') }}
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
