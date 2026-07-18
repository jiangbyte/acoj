<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { configApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  activeTab: 'other',
  configs: [] as any[],
  total: 0,
  loading: false,
  loaded: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
})

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      config_key: (value) => String(value).trim(),
      category: (value) => String(value).trim(),
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
    title: '配置键',
    path: 'config_key',
    field: 'input',
  },
  {
    title: '分类',
    path: 'category',
    field: 'input',
  },
])

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => `${itemCount} 条`,
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
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '配置键',
    path: 'config_key',
    width: 240,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '配置值',
    path: 'config_value',
    width: 300,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '分类',
    path: 'category',
    width: 160,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '备注',
    path: 'remark',
    width: 220,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '排序码',
    path: 'sort_code',
    width: 100,
  },
  {
    title: '更新时间',
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
    render: (row) => formatDateTime(row.updated_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('sys:config:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('sys:config:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('sys:config:delete') ? (
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
    const response = await configApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.configs = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.configs.some((item) => item.id === key),
    )
    state.loaded = true
  } finally {
    state.loading = false
  }
}

function handleTabUpdate(value: string | number) {
  state.activeTab = String(value)
  if (state.activeTab === 'other' && !state.loaded) {
    fetchPage()
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
    title: isBatch ? '批量删除' : '删除',
    draggable: true,
    maskClosable: false,
    content: isBatch ? `删除 ${ids.length} 个系统配置?` : '删除该系统配置?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await configApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success('删除成功')
  await fetchPage()
  if (!state.configs.length && state.total > 0 && state.page > 1) {
    state.page -= 1
    await fetchPage()
  }
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard class="min-h-0 flex-1" content-class="h-full min-h-0 overflow-auto">
      <div class="min-h-full flex flex-col gap-16px">
        <NTabs :value="state.activeTab" type="line" animated @update:value="handleTabUpdate">
          <NTabPane name="login" :tab="'登录配置'" />
          <NTabPane name="register" :tab="'注册配置'" />
          <NTabPane name="other" :tab="'其他配置Key'" />
        </NTabs>

        <ProSearchForm
          v-if="state.activeTab === 'other'"
          :form="searchForm"
          :columns="searchColumns"
          :reset-button-props="{ content: '重置' }"
          :search-button-props="{ content: '搜索' }"
          :collapse-button-props="{
            content: searchForm.collapsed.value ? '展开' : '收起',
          }"
        />

        <ProDataTable
          v-if="state.activeTab === 'other'"
          class="config-table"
          remote
          :title="'系统配置'"
          row-key="id"
          :scroll-x="1420"
          :columns="tableColumns"
          :data="state.configs"
          :loading="state.loading"
          :pagination="pagination"
          :checked-row-keys="state.checkedRowKeys"
          :on-update-checked-row-keys="handleCheckedRowKeys"
        >
          <template #toolbar>
            <NFlex>
              <NButton
                v-if="hasPermission('sys:config:create')"
                type="primary"
                text
                :title="'新增'"
                :aria-label="'新增'"
                @click="openCreateModal"
              >
                <template #icon>
                  <NIcon>
                    <Icon icon="icon-park-outline:plus" />
                  </NIcon>
                </template>
              </NButton>
              <NButton
                text
                :title="'刷新'"
                :aria-label="'刷新'"
                :loading="state.loading"
                @click="fetchPage"
              >
                <template #icon>
                  <NIcon>
                    <Icon icon="icon-park-outline:reload" />
                  </NIcon>
                </template>
              </NButton>
              <NButton
                v-if="hasPermission('sys:config:delete')"
                type="error"
                text
                :title="'批量删除'"
                :aria-label="'批量删除'"
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

        <div v-else class="min-h-320px flex items-center justify-center">
          <NEmpty :description="'暂空'" />
        </div>
      </div>
    </ProCard>

    <ModalForm v-if="state.activeTab === 'other'" ref="formModalRef" @saved="fetchPage" />
    <ModalDetail v-if="state.activeTab === 'other'" ref="detailModalRef" />
  </NFlex>
</template>

<style scoped>
.config-table {
  min-height: 520px;
}
</style>
