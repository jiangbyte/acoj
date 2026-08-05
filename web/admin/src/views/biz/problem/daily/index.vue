<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns } from 'pro-naive-ui'
import type { ProblemOption } from '@/components/selector/ProblemSelector.vue'
import { Icon } from '@iconify/vue/offline'
import ProblemSelector from '@/components/selector/ProblemSelector.vue'
import { ojDailyApi } from '@/api'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { NButton, NDatePicker, NFlex, NForm, NFormItem, NIcon, NInput, NInputGroup, NModal, NTag } from 'naive-ui'
import { ProDataTable } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'

const formModal = reactive({
  show: false,
  submitLoading: false,
  showProblemSelector: false,
  day_date: null as number | null,
  problem_id: null as string | null,
  problemLabel: '',
})
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

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
  { title: '日期', path: 'day_date', width: 130 },
  {
    title: '题目',
    key: 'problem',
    ellipsis: { tooltip: true },
    render: (row) => `${row.problem_code ?? '-'}. ${row.problem_name ?? ''}`,
  },
  {
    title: '难度',
    path: 'difficulty',
    width: 90,
    render: (row) => {
      const label = dictTypeData('PROBLEM_DIFFICULTY', row.difficulty) || row.difficulty || '-'
      const color = dictTypeColor('PROBLEM_DIFFICULTY', row.difficulty)
      return color ? (
        <NTag size="small" bordered={false} color={{ color: 'transparent', textColor: color }}>
          {label}
        </NTag>
      ) : (
        label
      )
    },
  },
  {
    title: '通过率',
    path: 'ac_rate',
    width: 90,
    render: (row) => `${Number(row.ac_rate || 0).toFixed(1)}%`,
  },
])

async function fetchPage() {
  state.loading = true
  try {
    const res = await ojDailyApi.page({ current: state.page, size: state.pageSize })
    const data = res.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) => state.rows.some((item) => item.id === key))
  } finally {
    state.loading = false
  }
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function openCreate() {
  formModal.day_date = Date.now()
  formModal.problem_id = null
  formModal.problemLabel = ''
  formModal.show = true
}

function handleProblemSelect(problem: ProblemOption) {
  formModal.problem_id = problem.id
  formModal.problemLabel = `${problem.code} - ${problem.name}`
}

function clearProblem() {
  formModal.problem_id = null
  formModal.problemLabel = ''
}

async function submitForm() {
  if (!formModal.day_date || !formModal.problem_id) {
    window.$message.warning('请选择日期和题目')
    return
  }
  formModal.submitLoading = true
  try {
    const d = new Date(formModal.day_date)
    const day = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    await ojDailyApi.upsert({ day_date: day, problem_id: formModal.problem_id })
    window.$message.success('保存成功')
    formModal.show = false
    await fetchPage()
  } finally {
    formModal.submitLoading = false
  }
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) return
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    content: ids.length > 1 ? `删除 ${ids.length} 条记录?` : '删除该记录?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await ojDailyApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success('删除成功')
  await fetchPage()
}

onMounted(async () => {
  await fetchPage()
})
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="每日一题"
      row-key="id"
      :scroll-x="800"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton type="primary" text @click="openCreate">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:plus" /></NIcon>
            </template>
          </NButton>
          <NButton text :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
            </template>
          </NButton>
          <NButton type="error" text :disabled="!hasCheckedRows" @click="confirmDelete(state.checkedRowKeys)">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:delete" /></NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <NModal v-model:show="formModal.show" preset="card" title="配置每日一题" style="width: 520px">
      <NForm label-placement="left" label-width="90">
        <NFormItem label="日期" required>
          <NDatePicker v-model:value="formModal.day_date" type="date" class="w-full" />
        </NFormItem>
        <NFormItem label="题目" required>
          <NInputGroup>
            <NInput :value="formModal.problemLabel" readonly placeholder="请选择题目" />
            <NButton type="primary" @click="formModal.showProblemSelector = true">
              选择
            </NButton>
            <NButton :disabled="!formModal.problem_id" @click="clearProblem">
              清除
            </NButton>
          </NInputGroup>
        </NFormItem>
      </NForm>
      <template #footer>
        <NFlex justify="end">
          <NButton @click="formModal.show = false">取消</NButton>
          <NButton type="primary" :loading="formModal.submitLoading" @click="submitForm">保存</NButton>
        </NFlex>
      </template>
    </NModal>

    <ProblemSelector
      v-model:visible="formModal.showProblemSelector"
      public-only
      @select="handleProblemSelect"
    />
  </NFlex>
</template>
