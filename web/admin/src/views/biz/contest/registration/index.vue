<script setup lang="tsx">
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { ojContestRegistrationApi } from '@/api'
import { formatDateTime } from '@/utils'
import {
  NButton,
  NDataTable,
  NFlex,
  NInput,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
} from 'naive-ui'
import { computed, onMounted, reactive, watch } from 'vue'

const props = defineProps<{ contestId: string }>()
const message = useMessage()
const dialog = useDialog()

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  page: 1,
  pageSize: 20,
  status: null as string | null,
  accountId: '',
  checkedRowKeys: [] as string[],
  addAccountId: '',
  addLoading: false,
})

const statusOptions = [
  { label: '全部', value: null },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' },
]

const statusMap: Record<string, { type: 'default' | 'success' | 'warning' | 'error' | 'info'; label: string }> = {
  PENDING: { type: 'warning', label: '待审核' },
  APPROVED: { type: 'success', label: '已通过' },
  REJECTED: { type: 'error', label: '已拒绝' },
  CANCELLED: { type: 'default', label: '已取消' },
}

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage: (p) => {
    state.page = p
    void fetchPage()
  },
  onUpdatePageSize: (s) => {
    state.pageSize = s
    state.page = 1
    void fetchPage()
  },
}))

const columns = computed<DataTableColumns<any>>(() => [
  { type: 'selection' },
  { title: '账户ID', key: 'account_id', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const meta = statusMap[row.status] || { type: 'default' as const, label: row.status }
      return <NTag type={meta.type} size="small">{meta.label}</NTag>
    },
  },
  { title: '来源', key: 'source', width: 80 },
  {
    title: '申请时间',
    key: 'applied_at',
    width: 180,
    render: (row) => formatDateTime(row.applied_at),
  },
  {
    title: '备注',
    key: 'remark',
    ellipsis: { tooltip: true },
  },
])

async function fetchPage() {
  if (!props.contestId) return
  state.loading = true
  try {
    const res = await ojContestRegistrationApi.page(props.contestId, {
      current: state.page,
      size: state.pageSize,
      account_id: state.accountId || undefined,
      status: state.status || undefined,
    })
    state.rows = res.data?.records ?? []
    state.total = res.data?.total ?? 0
  } finally {
    state.loading = false
  }
}

async function handleAdd() {
  if (!state.addAccountId.trim()) {
    message.warning('请输入账户 ID')
    return
  }
  state.addLoading = true
  try {
    await ojContestRegistrationApi.add(props.contestId, { account_id: state.addAccountId.trim() })
    message.success('已添加（直接通过）')
    state.addAccountId = ''
    await fetchPage()
  } finally {
    state.addLoading = false
  }
}

async function handleApprove() {
  if (!state.checkedRowKeys.length) return
  await ojContestRegistrationApi.approve(props.contestId, { ids: state.checkedRowKeys })
  message.success('已通过')
  state.checkedRowKeys = []
  await fetchPage()
}

async function handleReject() {
  if (!state.checkedRowKeys.length) return
  dialog.warning({
    title: '拒绝报名',
    content: '确认拒绝选中的报名记录？',
    positiveText: '拒绝',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojContestRegistrationApi.reject(props.contestId, {
        ids: state.checkedRowKeys,
        remark: '管理员拒绝',
      })
      message.success('已拒绝')
      state.checkedRowKeys = []
      await fetchPage()
    },
  })
}

async function handleCancel() {
  if (!state.checkedRowKeys.length) return
  await ojContestRegistrationApi.cancel(props.contestId, { ids: state.checkedRowKeys })
  message.success('已取消')
  state.checkedRowKeys = []
  await fetchPage()
}

watch(
  () => props.contestId,
  () => {
    state.page = 1
    void fetchPage()
  },
)

onMounted(() => void fetchPage())
</script>

<template>
  <div class="flex flex-col gap-12px">
    <NFlex justify="space-between" align="center" :wrap="true">
      <NSpace>
        <NSelect
          v-model:value="state.status"
          :options="statusOptions"
          clearable
          placeholder="状态"
          style="width: 140px"
          @update:value="() => { state.page = 1; fetchPage() }"
        />
        <NInput
          v-model:value="state.accountId"
          clearable
          placeholder="账户 ID"
          style="width: 200px"
          @keyup.enter="() => { state.page = 1; fetchPage() }"
        />
        <NButton @click="() => { state.page = 1; fetchPage() }">
          查询
        </NButton>
      </NSpace>
      <NSpace>
        <NButton type="primary" :disabled="!state.checkedRowKeys.length" @click="handleApprove">
          通过
        </NButton>
        <NButton type="warning" :disabled="!state.checkedRowKeys.length" @click="handleReject">
          拒绝
        </NButton>
        <NButton type="error" :disabled="!state.checkedRowKeys.length" @click="handleCancel">
          移除
        </NButton>
      </NSpace>
    </NFlex>

    <NFlex align="center" :size="8">
      <NInput
        v-model:value="state.addAccountId"
        placeholder="手动添加账户 ID（直接通过）"
        style="width: 280px"
        @keyup.enter="handleAdd"
      />
      <NButton type="primary" :loading="state.addLoading" @click="handleAdd">
        添加选手
      </NButton>
    </NFlex>

    <NDataTable
      v-model:checked-row-keys="state.checkedRowKeys"
      :columns="columns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :row-key="(row) => row.id"
      size="small"
      :bordered="false"
    />
  </div>
</template>
