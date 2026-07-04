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
import ModalGroupForm from './components/ModalGroupForm.vue'

const activeTab = ref<'threads' | 'groups'>('threads')
const formModalRef = ref<any>(null)
const groupFormModalRef = ref<any>(null)
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

const availableTabs = computed(() =>
  [
    {
      name: 'threads',
      tab: 'Threads',
      permission: 'message:thread:page',
    },
    {
      name: 'groups',
      tab: 'Groups',
      permission: 'message:group:page',
    },
  ].filter((item) => hasPermission(item.permission)),
)

const searchColumns = computed<ProSearchFormColumns<any>>(() =>
  activeTab.value === 'threads'
    ? [
        {
          title: 'Thread Type',
          path: 'thread_type',
          field: 'select',
          fieldProps: {
            options: dictList('MESSAGE_THREAD_TYPE'),
          },
        },
      ]
    : [
        {
          title: 'Group Name',
          path: 'name',
          field: 'input',
        },
        {
          title: 'Status',
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

const threadColumns = computed<ProDataTableColumns<any>>(() => [
  {
    title: 'ID',
    width: 100,
    path: 'id',
    ellipsis: { tooltip: true },
  },
  {
    title: 'Thread Title',
    path: 'title',
    width: 220,
    ellipsis: { tooltip: true },
    render: (row) => row.title || '-',
  },
  {
    title: 'Thread Type',
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
    title: 'Group ID',
    path: 'group_id',
    width: 160,
    ellipsis: { tooltip: true },
    render: (row) => row.group_id || '-',
  },
  {
    title: 'Last Message At',
    path: 'last_message_at',
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
    width: 95,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('message:thread:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('message:thread:send') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openSystemMessage(row.id)}>
            {renderButtonIcon('icon-park-outline:send')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const groupColumns = computed<ProDataTableColumns<any>>(() => [
  {
    title: 'ID',
    width: 60,
    path: 'id',
    ellipsis: { tooltip: true },
  },
  {
    title: 'Group Name',
    path: 'name',
    width: 220,
    ellipsis: { tooltip: true },
  },
  {
    title: 'Group Avatar',
    path: 'avatar',
    width: 120,
    ellipsis: { tooltip: true },
    render: (row) => row.avatar || '-',
  },
  {
    title: 'Members',
    path: 'member_count',
    width: 110,
  },
  {
    title: 'Status',
    path: 'status',
    width: 120,
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
    ellipsis: { tooltip: true },
  },
  {
    title: 'Operation',
    key: 'actions',
    width: 140,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('message:group:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('message:group:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openGroupForm(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('message:group:delete') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmDeleteGroup(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const tableColumns = computed<ProDataTableColumns<any>>(() =>
  activeTab.value === 'threads' ? threadColumns.value : groupColumns.value,
)

onMounted(() => {
  if (!availableTabs.value.some((item) => item.name === activeTab.value)) {
    activeTab.value = (availableTabs.value[0]?.name as 'threads' | 'groups') ?? 'threads'
  }
  fetchPage()
})

async function fetchPage() {
  if (!availableTabs.value.some((item) => item.name === activeTab.value)) {
    state.rows = []
    state.total = 0
    return
  }
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
  if (!availableTabs.value.some((item) => item.name === value)) {
    return
  }
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

function openGroupForm(id?: string) {
  groupFormModalRef.value?.openModal(id)
}

function confirmDeleteGroup(id: string) {
  window.$dialog.warning({
    title: 'Delete',
    draggable: true,
    maskClosable: false,
    content: 'Delete this group?',
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: async () => {
      await messageApi.removeGroup({ ids: [id] })
      window.$message.success('Deleted successfully')
      await fetchPage()
    },
  })
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <NFlex vertical>
        <NTabs :value="activeTab" type="line" @update:value="handleTabUpdate">
          <NTabPane v-for="item in availableTabs" :key="item.name" :name="item.name" :tab="item.tab" />
        </NTabs>
        <ProSearchForm
          :key="activeTab"
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
      </NFlex>
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="'Messages'"
      row-key="id"
      :scroll-x="activeTab === 'threads' ? 1150 : 760"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NFlex>
          <NButton
            v-if="activeTab === 'groups' && hasPermission('message:group:create')"
            type="primary"
            text
            :title="'Add Group'"
            :aria-label="'Add Group'"
            @click="openGroupForm()"
          >
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
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalGroupForm ref="groupFormModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
