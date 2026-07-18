<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojContestApi } from '@/api'
import { createTagColor, formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import FormPanel from './components/FormPanel.vue'
import ModalDetail from './components/ModalDetail.vue'

const formatOptions = ['ICPC', 'IOI', 'OI', 'ACM', 'CUSTOM'].map(labelValue)
const visibilityOptions = ['PUBLIC', 'PRIVATE', 'ORG_ONLY'].map(labelValue)
const scoreboardOptions = ['VISIBLE', 'AFTER_CONTEST', 'AFTER_PARTICIPATION', 'HIDDEN'].map(labelValue)
const statusOptions = ['ENABLED', 'DISABLED'].map(labelValue)

const detailModalRef = ref<InstanceType<typeof ModalDetail> | null>(null)

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
  formVisible: false,
  formId: null as string | null,
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
  { title: '比赛编码', path: 'key', field: 'input' },
  { title: '比赛名称', path: 'name', field: 'input' },
  { title: '赛制', path: 'contest_format', field: 'select', fieldProps: { options: formatOptions } },
  { title: '可见性', path: 'visibility', field: 'select', fieldProps: { options: visibilityOptions } },
  { title: '榜单可见性', path: 'scoreboard_visibility', field: 'select', fieldProps: { options: scoreboardOptions } },
  { title: '状态', path: 'status', field: 'select', fieldProps: { options: statusOptions } },
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
  { type: 'selection', fixed: 'left' },
  { title: '编码', path: 'key', width: 150, fixed: 'left', ellipsis: { tooltip: true } },
  { title: '名称', path: 'name', width: 240, ellipsis: { tooltip: true } },
  { title: '赛制', path: 'contest_format', width: 110, render: (row) => renderTag(row.contest_format, '#2080f0') },
  { title: '可见性', path: 'visibility', width: 130, render: (row) => renderTag(row.visibility, row.visibility === 'PUBLIC' ? '#18a058' : '#f0a020') },
  { title: '榜单', path: 'scoreboard_visibility', width: 180, render: (row) => renderTag(row.scoreboard_visibility, '#64748b') },
  { title: '开始时间', path: 'start_at', width: 180, render: (row) => formatDateTime(row.start_at) },
  { title: '结束时间', path: 'end_at', width: 180, render: (row) => formatDateTime(row.end_at) },
  { title: '评级', path: 'is_rated', width: 100, render: (row) => renderTag(row.is_rated ? 'RATED' : 'UNRATED', row.is_rated ? '#722ed1' : '#909399') },
  { title: '虚拟参赛', path: 'allow_virtual', width: 110, render: (row) => renderTag(row.allow_virtual ? '允许' : '禁止', row.allow_virtual ? '#18a058' : '#909399') },
  { title: '状态', path: 'status', width: 110, render: (row) => renderTag(row.status, row.status === 'ENABLED' ? '#18a058' : '#909399') },
  { title: '更新时间', path: 'updated_at', width: 180, render: (row) => formatDateTime(row.updated_at), ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:contests:detail') ? (
          <NButton type="info" size="small" text={true} onClick={() => openDetail(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:contests:update') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openForm(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('oj:contests:delete') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

onMounted(fetchPage)

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojContestApi.page({
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

function openForm(id?: string) {
  state.formId = id ?? null
  state.formVisible = true
}

function openDetail(id: string) {
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
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    draggable: true,
    maskClosable: false,
    content: ids.length > 1 ? `删除 ${ids.length} 场竞赛?` : '删除该竞赛?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojContestApi.remove({ ids })
      state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
      window.$message.success('删除成功')
      await fetchPage()
    },
  })
}

function renderTag(text: string, color: string) {
  return <NTag color={createTagColor(color)} bordered={false}>{text}</NTag>
}

function labelValue(value: string) {
  return { label: value, value }
}

async function handleFormSaved() {
  state.formVisible = false
  state.formId = null
  await fetchPage()
}

function closeForm() {
  state.formVisible = false
  state.formId = null
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <FormPanel
      v-if="state.formVisible"
      :key="state.formId || 'create'"
      :id="state.formId"
      @saved="handleFormSaved"
      @cancel="closeForm"
    />

    <template v-else>
      <ProCard content-class="pb-0!">
        <ProSearchForm
          :form="searchForm"
          :columns="searchColumns"
          :reset-button-props="{ content: '重置' }"
          :search-button-props="{ content: '搜索' }"
          :collapse-button-props="{ content: searchForm.collapsed.value ? '展开' : '收起' }"
        />
      </ProCard>

      <ProDataTable
        class="min-h-0 flex-1"
        remote
        title="竞赛管理"
        row-key="id"
        :scroll-x="1900"
        :columns="tableColumns"
        :data="state.rows"
        :loading="state.loading"
        :pagination="pagination"
        :checked-row-keys="state.checkedRowKeys"
        :on-update-checked-row-keys="handleCheckedRowKeys"
      >
        <template #toolbar>
          <NFlex>
            <NButton v-if="hasPermission('oj:contests:create')" type="primary" text title="新增" aria-label="新增" @click="openForm()">
              <template #icon>
                <NIcon>
                  <Icon icon="icon-park-outline:plus" />
                </NIcon>
              </template>
            </NButton>
            <NButton text title="刷新" aria-label="刷新" :loading="state.loading" @click="fetchPage">
              <template #icon>
                <NIcon>
                  <Icon icon="icon-park-outline:reload" />
                </NIcon>
              </template>
            </NButton>
            <NButton
              v-if="hasPermission('oj:contests:delete')"
              type="error"
              text
              title="批量删除"
              aria-label="批量删除"
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
    </template>

    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>
