<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojSubmissionApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon, resolveFileUrl } from '@/utils'
import { NAvatar, NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import ModalDetail from './components/ModalDetail.vue'

const props = defineProps<{
  problemId?: string
  contestId?: string
  embedded?: boolean
}>()

const detailModalRef = ref<any>(null)

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  rejudgeLoading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)
const scopedProblemId = computed(() => props.problemId || undefined)
const scopedContestId = computed(() => props.contestId || undefined)

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

const searchColumns = computed<ProSearchFormColumns<any>>(() => {
  const cols: ProSearchFormColumns<any> = [
    { title: '用户ID', path: 'user_id', field: 'input' },
    {
      title: '类型',
      path: 'kind',
      field: 'select',
      fieldProps: {
        options: [
          { label: '正式', value: 'OFFICIAL' },
          { label: '试测', value: 'TRIAL' },
        ],
        clearable: true,
      },
    },
    {
      title: '状态',
      path: 'status',
      field: 'select',
      fieldProps: {
        options: [
          { label: 'QUEUED', value: 'QUEUED' },
          { label: 'JUDGING', value: 'JUDGING' },
          { label: 'COMPLETED', value: 'COMPLETED' },
          { label: 'FAILED', value: 'FAILED' },
        ],
        clearable: true,
      },
    },
    {
      title: '结果',
      path: 'result',
      field: 'select',
      fieldProps: {
        options: ['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'OLE', 'SE', 'IE'].map(v => ({ label: v, value: v })),
        clearable: true,
      },
    },
    { title: '语言', path: 'language_key', field: 'input' },
  ]
  if (!scopedProblemId.value) {
    cols.unshift(
      { title: '题目ID', path: 'problem_id', field: 'input' },
      { title: '题码', path: 'problem_code', field: 'input' },
    )
  }
  if (!scopedContestId.value) {
    cols.push({ title: '竞赛ID', path: 'contest_id', field: 'input' })
  }
  return cols
})

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

const resultType = (value?: string) => {
  if (value === 'AC')
    return 'success'
  if (['WA', 'RE', 'CE', 'SE', 'IE'].includes(String(value)))
    return 'error'
  if (['TLE', 'MLE', 'OLE'].includes(String(value)))
    return 'warning'
  return 'default'
}

const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

function renderUserCell(row: any) {
  const name = row.user_nickname || '-'
  const avatar = resolveFileUrl(row.user_avatar)
  return (
    <NFlex align="center" size={8} wrap={false}>
      <NAvatar round size={28} src={avatar || undefined} imgProps={avatarImgProps}>
        {avatar
          ? undefined
          : String(name)
              .slice(0, 1)
              .toUpperCase()}
      </NAvatar>
      <span class="min-w-0 truncate" title={name}>{name}</span>
    </NFlex>
  )
}

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  { type: 'selection', fixed: 'left' },
  { title: 'ID', path: 'id', width: 150, ellipsis: { tooltip: true } },
  {
    title: '用户',
    path: 'user_nickname',
    width: 160,
    ellipsis: { tooltip: true },
    render: row => renderUserCell(row),
  },
  {
    title: '题目',
    path: 'problem_code',
    width: 140,
    ellipsis: { tooltip: true },
    render: row => row.problem_code || row.problem_id,
  },
  {
    title: '竞赛',
    path: 'contest_name',
    width: 140,
    ellipsis: { tooltip: true },
    render: row => row.contest_name || row.contest_key || '-',
  },
  { title: '类型', path: 'kind', width: 90 },
  { title: '语言', path: 'language_key', width: 90 },
  { title: '状态', path: 'status', width: 100 },
  {
    title: '结果',
    path: 'result',
    width: 80,
    render: row => (
      <NTag size="small" type={resultType(row.result)} bordered={false}>
        {row.result || '-'}
      </NTag>
    ),
  },
  { title: '得分', path: 'score', width: 70 },
  {
    title: '时间',
    path: 'time_ms',
    width: 80,
    render: row => `${row.time_ms ?? 0}ms`,
  },
  {
    title: '内存',
    path: 'memory_kb',
    width: 90,
    render: row => `${row.memory_kb ?? 0}KB`,
  },
  {
    title: '提交时间',
    path: 'created_at',
    width: 170,
    render: row => formatDateTime(row.created_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: row => (
      <NFlex size={12}>
        {hasPermission('biz:submission:submission:detail')
          ? (
              <NButton type="info" size="small" text onClick={() => openDetailModal(row.id)}>
                {renderButtonIcon('icon-park-outline:preview-open')}
              </NButton>
            )
          : null}
        {hasPermission('biz:submission:submission:rejudge')
          ? (
              <NButton
                type="warning"
                size="small"
                text
                disabled={!!row.locked_at}
                onClick={() => confirmRejudge([row.id])}
              >
                {renderButtonIcon('icon-park-outline:refresh')}
              </NButton>
            )
          : null}
        {hasPermission('biz:submission:submission:delete')
          ? (
              <NButton type="error" size="small" text onClick={() => confirmDelete(row.id)}>
                {renderButtonIcon('icon-park-outline:delete')}
              </NButton>
            )
          : null}
      </NFlex>
    ),
  },
])

watch(
  () => [props.problemId, props.contestId],
  () => {
    state.page = 1
    fetchPage()
  },
)

onMounted(() => {
  void fetchPage()
})

async function fetchPage() {
  if (!hasPermission('biz:submission:submission:page')) {
    state.rows = []
    state.total = 0
    return
  }
  state.loading = true
  try {
    const response = await ojSubmissionApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
      ...(scopedProblemId.value ? { problem_id: scopedProblemId.value } : {}),
      ...(scopedContestId.value ? { contest_id: scopedContestId.value } : {}),
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter(key => state.rows.some(item => item.id === key))
  }
  finally {
    state.loading = false
  }
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length)
    return
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    content: ids.length > 1 ? `删除 ${ids.length} 条提交?` : '删除该提交?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojSubmissionApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter(key => !ids.includes(key))
  window.$message.success('删除成功')
  await fetchPage()
}

function confirmRejudge(ids: string[]) {
  if (!ids.length)
    return
  if (ids.length > 20) {
    window.$message.warning('一次最多重判 20 条')
    return
  }
  window.$dialog.warning({
    title: ids.length > 1 ? '批量重判' : '重判',
    content: ids.length > 1 ? `重判 ${ids.length} 条提交?` : '按当前全部测例重判该提交?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => runRejudge(ids),
  })
}

async function runRejudge(ids: string[]) {
  state.rejudgeLoading = true
  try {
    const response = await ojSubmissionApi.rejudge({ ids })
    const data = response.data ?? {}
    if (data.failed) {
      window.$message.warning(`入队 ${data.queued ?? 0}，失败 ${data.failed}`)
    }
    else {
      window.$message.success(`已入队重判 ${data.queued ?? ids.length} 条`)
    }
    // Single-id: wait via SSE; batch: refresh shortly (callbacks write DB).
    if (ids.length === 1) {
      try {
        await ojSubmissionApi.watchSubmissionEvents(ids[0], {
          maxWaitSec: 120,
          onUpdate: () => {},
        })
      }
      catch {
        await ojSubmissionApi.pollSubmissionUntilDone(ids[0], {
          maxWaitSec: 120,
          fetchDetail: async id => (await ojSubmissionApi.detail({ id })).data ?? {},
          onUpdate: () => {},
        })
      }
    }
    else {
      await new Promise(resolve => setTimeout(resolve, 1500))
    }
    await fetchPage()
  }
  finally {
    state.rejudgeLoading = false
  }
}
</script>

<template>
  <NFlex :class="props.embedded ? 'min-h-0' : 'h-full min-h-0'" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: '重置' }"
        :search-button-props="{ content: '搜索' }"
      />
    </ProCard>

    <ProDataTable
      :class="props.embedded ? undefined : 'min-h-0 flex-1'"
      :flex-height="!props.embedded"
      remote
      :title="props.embedded ? '提交' : '提交管理'"
      row-key="id"
      :scroll-x="1400"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('biz:submission:submission:rejudge')"
            type="warning"
            text
            :disabled="!hasCheckedRows"
            :loading="state.rejudgeLoading"
            @click="confirmRejudge(state.checkedRowKeys)"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
            </template>
            批量重判
          </NButton>
          <NButton
            v-if="hasPermission('biz:submission:submission:delete')"
            type="error"
            text
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:delete" /></NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalDetail ref="detailModalRef" @rejudged="fetchPage" />
  </NFlex>
</template>
