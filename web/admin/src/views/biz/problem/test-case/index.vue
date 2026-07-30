<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi, ojProblemTestCaseApi } from '@/api'
import { formatDateTime, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'
import TrialJudgeModal from './components/TrialJudgeModal.vue'

const route = useRoute()
const router = useRouter()
const props = defineProps<{ problemId?: string, embedded?: boolean }>()
const problemId = computed(() => String(props.problemId ?? route.query.id ?? ''))
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const trialJudgeModalRef = ref<any>(null)
const problemTitle = ref('')

const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

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
  { title: '数据模式', path: 'data_mode', field: 'select', fieldProps: {
    options: [
      { label: 'file', value: 'file' },
      { label: 'inline', value: 'inline' },
    ],
    clearable: true,
  } },
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
  { title: '编号', path: 'case_no', width: 80 },
  { title: '顺序', path: 'sort', width: 80 },
  { title: '类型', path: 'case_type', width: 110 },
  {
    title: '数据模式',
    path: 'data_mode',
    width: 90,
    render: row => (
      <NTag size="small" type={row.data_mode === 'inline' ? 'info' : 'default'}>
        {row.data_mode ?? 'file'}
      </NTag>
    ),
  },
  { title: '分值', path: 'points', width: 80 },
  { title: 'pretest', path: 'is_pretest', width: 80, render: row => (row.is_pretest ? '是' : '否') },
  { title: '更新时间', path: 'updated_at', width: 170, render: row => formatDateTime(row.updated_at) },
  {
    title: '操作',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: row => (
      <NFlex size={12}>
        {hasPermission('biz:problem:testcase:detail') ? (
          <NButton type="info" size="small" text onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('biz:problem:testcase:update') ? (
          <NButton type="primary" size="small" text onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('biz:problem:testcase:delete') ? (
          <NButton type="error" size="small" text onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

watch(problemId, () => {
  void loadProblemTitle()
  state.page = 1
  fetchPage()
})

onMounted(async () => {
  await loadProblemTitle()
  await fetchPage()
})

async function loadProblemTitle() {
  if (!problemId.value) {
    problemTitle.value = ''
    return
  }
  try {
    const response = await ojProblemApi.detail({ id: problemId.value })
    const data = response.data ?? {}
    problemTitle.value = data.name ? `${data.code} · ${data.name}` : problemId.value
  } catch {
    problemTitle.value = problemId.value
  }
}

async function fetchPage() {
  if (!problemId.value) {
    return
  }
  state.loading = true
  try {
    const response = await ojProblemTestCaseApi.page(problemId.value, {
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter(key => state.rows.some(item => item.id === key))
  } finally {
    state.loading = false
  }
}

function goBack() {
  router.push('/biz/problem/problem')
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

function openTrialJudgeModal() {
  trialJudgeModalRef.value?.openModal()
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
    content: ids.length > 1 ? `删除 ${ids.length} 条记录?` : '删除该记录?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojProblemTestCaseApi.remove(problemId.value, { ids })
  state.checkedRowKeys = state.checkedRowKeys.filter(key => !ids.includes(key))
  window.$message.success('删除成功')
  await fetchPage()
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard v-if="!props.embedded">
      <NFlex align="center" justify="space-between">
        <NFlex align="center" :size="12">
          <NButton text @click="goBack">返回题目列表</NButton>
          <span class="font-medium">测试用例</span>
          <span v-if="problemTitle" class="text-gray-500">{{ problemTitle }}</span>
          <span class="text-gray-400 text-sm">每行一条；试判按行发给 worker</span>
        </NFlex>
        <NButton v-if="hasPermission('biz:problem:problem:update')" type="primary" @click="openTrialJudgeModal">
          试判
        </NButton>
      </NFlex>
    </ProCard>

    <ProCard v-if="!props.embedded" content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: '重置' }"
        :search-button-props="{ content: '搜索' }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="props.embedded ? '测试用例' : '测试用例（oj_problem_test_case）'"
      row-key="id"
      :scroll-x="900"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('biz:problem:testcase:create')" type="primary" text @click="openCreateModal">
            <template #icon><NIcon><Icon icon="icon-park-outline:plus" /></NIcon></template>
            {{ props.embedded ? '新增 inline' : '' }}
          </NButton>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon><NIcon><Icon icon="icon-park-outline:refresh" /></NIcon></template>
          </NButton>
          <NButton v-if="hasPermission('biz:problem:testcase:delete')" type="error" text :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)">
            <template #icon><NIcon><Icon icon="icon-park-outline:delete" /></NIcon></template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalDetail ref="detailModalRef" :problem-id="problemId" />
    <ModalForm ref="formModalRef" :problem-id="problemId" @saved="fetchPage" />
    <TrialJudgeModal :problem-id="problemId" ref="trialJudgeModalRef" />
  </NFlex>
</template>
