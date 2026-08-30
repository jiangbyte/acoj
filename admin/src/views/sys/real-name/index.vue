<!--
  Author: Charlie

  实名认证审核：待审队列、详情、通过/驳回。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { realNameApi } from '@/api'
import {
  createTagColor,
  formatDateTime,
  hasPermission,
  normalizeSearchValues,
  renderButtonIcon,
} from '@/utils'
import { readPageMeta } from '@/utils/wire'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDetail from './components/ModalDetail.vue'

const detailModalRef = ref<InstanceType<typeof ModalDetail> | null>(null)

const DOCUMENT_TYPE_LABELS: Record<string, string> = {
  ID_CARD: '身份证',
  PASSPORT: '护照',
  EID_CARD: '电子身份证',
  EID: '电子身份证',
}

const BUSINESS_TYPE_LABELS: Record<string, string> = {
  ACCOUNT_VERIFY: '账号实名认证',
  ACCOUNT_RECOVERY: '实名找回账号',
}

const CASE_STATUS_LABELS: Record<string, string> = {
  PENDING: '审核中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

const VERIFY_CHANNEL_LABELS: Record<string, string> = {
  MANUAL: '人工审核',
  THIRD_PARTY: '第三方认证',
}

function labelOf(map: Record<string, string>, value?: string | null) {
  if (!value) return '-'
  return map[value] || value
}

function statusColor(status?: string | null) {
  if (status === 'APPROVED') return '#52c41a'
  if (status === 'PENDING') return '#1677ff'
  if (status === 'REJECTED') return '#ff4d4f'
  return '#d9d9d9'
}

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
      account_id: (value) => String(value).trim(),
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

const statusOptions = [
  { label: '全部', value: '' },
  { label: '审核中', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  { title: '账号 ID', path: 'account_id', field: 'input' },
  {
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: { options: statusOptions, clearable: true },
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
    title: '工单号',
    path: 'case_id',
    width: 180,
    ellipsis: { tooltip: true },
    render: (row) => row.case_id ?? row.caseId ?? '-',
  },
  {
    title: '账号 ID',
    path: 'account_id',
    width: 120,
    ellipsis: { tooltip: true },
    render: (row) => row.account_id ?? row.accountId ?? '-',
  },
  {
    title: '业务类型',
    path: 'business_type',
    width: 120,
    render: (row) => labelOf(BUSINESS_TYPE_LABELS, row.business_type ?? row.businessType),
  },
  {
    title: '证件类型',
    path: 'document_type',
    width: 100,
    render: (row) => labelOf(DOCUMENT_TYPE_LABELS, row.document_type ?? row.documentType),
  },
  {
    title: '姓名',
    path: 'real_name_masked',
    width: 120,
    render: (row) => row.real_name_masked ?? row.realNameMasked ?? '-',
  },
  {
    title: '认证方式',
    path: 'verify_channel',
    width: 110,
    render: (row) =>
      labelOf(VERIFY_CHANNEL_LABELS, row.verify_channel ?? row.verifyChannel),
  },
  {
    title: '状态',
    path: 'status',
    width: 100,
    render: (row) => {
      const color = createTagColor(statusColor(row.status))
      return (
        <NTag size="small" color={color} bordered={false}>
          {labelOf(CASE_STATUS_LABELS, row.status)}
        </NTag>
      )
    },
  },
  {
    title: '提交时间',
    path: 'created_at',
    width: 170,
    render: (row) => formatDateTime(row.created_at ?? row.createdAt),
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: (row) => {
      const caseId = row.case_id ?? row.caseId
      return (
        <NFlex size={12}>
          {hasPermission('sys:realname:verify') ? (
            <NButton
              type="info"
              text
              onClick={() => openDetailModal(caseId)}
            >
              {renderButtonIcon('icon-park-outline:preview-open')}
              <span style="margin-left: 4px">详细</span>
            </NButton>
          ) : null}
        </NFlex>
      )
    },
  },
])

async function fetchPage() {
  state.loading = true
  try {
    const response = await realNameApi.reviewPage({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = pageMeta.size
  } finally {
    state.loading = false
  }
}

function openDetailModal(caseId: string) {
  detailModalRef.value?.openModal(caseId)
}

onMounted(() => {
  fetchPage()
})
</script>

<template>
  <NFlex
    class="h-full min-h-0"
    vertical
  >
    <ProCard>
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: '重置' }"
        :search-button-props="{ content: '搜索' }"
        :collapse-button-props="{
          content: searchForm.collapsed.value ? '展开' : '收起',
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="实名认证审核"
      row-key="case_id"
      :scroll-x="1320"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NFlex align="center">
          <NButton
            text
            title="刷新"
            aria-label="刷新"
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalDetail
      ref="detailModalRef"
      @changed="fetchPage"
    />
  </NFlex>
</template>
