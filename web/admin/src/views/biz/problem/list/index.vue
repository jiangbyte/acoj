<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import type { ProblemOption } from '@/components/selector/ProblemSelector.vue'
import { Icon } from '@iconify/vue/offline'
import ProblemSelector from '@/components/selector/ProblemSelector.vue'
import { ojProblemListApi } from '@/api'
import { formatDateTime, normalizeSearchValues, renderButtonIcon } from '@/utils'
import {
  NButton,
  NFlex,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NSelect,
  NSpin,
  NTag,
} from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'

const formModal = reactive({
  show: false,
  loading: false,
  submitLoading: false,
  showProblemSelector: false,
  id: null as string | null,
  model: {
    code: '',
    title: '',
    summary: '',
    cover_url: '',
    sort: 0,
    status: 'ENABLED',
    visibility: 'PUBLIC',
    problem_ids: [] as string[],
  },
  selectedProblems: [] as ProblemOption[],
})
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
    state.searchValues = normalizeSearchValues(values, {
      code: (value) => String(value).trim(),
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
  { title: '编码', path: 'code', field: 'input' },
  { title: '标题', path: 'title', field: 'input' },
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
  { title: '编码', path: 'code', width: 120, ellipsis: { tooltip: true } },
  { title: '标题', path: 'title', ellipsis: { tooltip: true } },
  { title: '题数', path: 'problem_count', width: 80 },
  { title: '排序', path: 'sort', width: 70 },
  {
    title: '状态',
    path: 'status',
    width: 90,
    render: (row) => (
      <NTag size="small" type={row.status === 'ENABLED' ? 'success' : 'default'}>
        {row.status === 'ENABLED' ? '启用' : row.status === 'DISABLED' ? '禁用' : row.status || '-'}
      </NTag>
    ),
  },
  { title: '更新时间', path: 'updated_at', width: 170, render: (row) => formatDateTime(row.updated_at) },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render: (row) => (
      <NFlex size={8} align="center">
        <NButton type="primary" size="small" text onClick={() => openEdit(row.id)}>
          {renderButtonIcon('icon-park-outline:edit')}
        </NButton>
      </NFlex>
    ),
  },
])

async function fetchPage() {
  state.loading = true
  try {
    const res = await ojProblemListApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
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
  formModal.id = null
  formModal.model = {
    code: '',
    title: '',
    summary: '',
    cover_url: '',
    sort: 0,
    status: 'ENABLED',
    visibility: 'PUBLIC',
    problem_ids: [],
  }
  formModal.selectedProblems = []
  formModal.show = true
}

async function openEdit(id: string) {
  formModal.id = id
  formModal.show = true
  formModal.loading = true
  try {
    const res = await ojProblemListApi.detail({ id })
    const d = res.data ?? {}
    const selected = (d.problems ?? []).map((p: any) => ({
      id: String(p.id),
      code: p.code || '',
      name: p.name || '',
    }))
    formModal.model = {
      code: d.code ?? '',
      title: d.title ?? '',
      summary: d.summary ?? '',
      cover_url: d.cover_url ?? '',
      sort: d.sort ?? 0,
      status: d.status ?? 'ENABLED',
      visibility: d.visibility ?? 'PUBLIC',
      problem_ids: selected.map((p: ProblemOption) => p.id),
    }
    formModal.selectedProblems = selected
  } finally {
    formModal.loading = false
  }
}

function handleProblemsConfirm(problems: ProblemOption[]) {
  formModal.selectedProblems = problems
  formModal.model.problem_ids = problems.map((p) => p.id)
}

function removeSelectedProblem(id: string) {
  formModal.selectedProblems = formModal.selectedProblems.filter((p) => p.id !== id)
  formModal.model.problem_ids = formModal.selectedProblems.map((p) => p.id)
}

async function submitForm() {
  formModal.submitLoading = true
  try {
    if (formModal.id) {
      await ojProblemListApi.update({ id: formModal.id, ...formModal.model })
      window.$message.success('更新成功')
    } else {
      await ojProblemListApi.create(formModal.model)
      window.$message.success('创建成功')
    }
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
  await ojProblemListApi.remove({ ids })
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
    <ProCard content-class="pb-0!">
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
      title="官方题单"
      row-key="id"
      :scroll-x="960"
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

    <NModal
      v-model:show="formModal.show"
      preset="card"
      :title="formModal.id ? '编辑官方题单' : '新增官方题单'"
      style="width: 720px"
      :mask-closable="false"
    >
      <NSpin :show="formModal.loading">
        <NForm label-placement="left" label-width="100" :disabled="formModal.submitLoading">
          <NFormItem label="编码" required>
            <NInput v-model:value="formModal.model.code" />
          </NFormItem>
          <NFormItem label="标题" required>
            <NInput v-model:value="formModal.model.title" />
          </NFormItem>
          <NFormItem label="摘要">
            <NInput v-model:value="formModal.model.summary" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
          </NFormItem>
          <NFormItem label="封面 URL">
            <NInput v-model:value="formModal.model.cover_url" />
          </NFormItem>
          <NFormItem label="题目">
            <div class="w-full">
              <NFlex :size="8" class="mb-8px">
                <NButton type="primary" @click="formModal.showProblemSelector = true">选择题目</NButton>
                <NText depth="3">已选 {{ formModal.selectedProblems.length }} 题</NText>
              </NFlex>
              <NFlex :size="8" style="flex-wrap: wrap">
                <NTag
                  v-for="p in formModal.selectedProblems"
                  :key="p.id"
                  closable
                  @close="removeSelectedProblem(p.id)"
                >
                  {{ p.code }}. {{ p.name }}
                </NTag>
              </NFlex>
            </div>
          </NFormItem>
          <NFormItem label="排序">
            <NInputNumber v-model:value="formModal.model.sort" class="w-full" />
          </NFormItem>
          <NFormItem label="状态">
            <NSelect
              v-model:value="formModal.model.status"
              :options="[
                { label: '启用', value: 'ENABLED' },
                { label: '禁用', value: 'DISABLED' },
              ]"
            />
          </NFormItem>
        </NForm>
      </NSpin>
      <template #footer>
        <NFlex justify="end">
          <NButton @click="formModal.show = false">取消</NButton>
          <NButton type="primary" :loading="formModal.submitLoading" @click="submitForm">保存</NButton>
        </NFlex>
      </template>
    </NModal>

    <ProblemSelector
      v-model:visible="formModal.showProblemSelector"
      mode="multiple"
      public-only
      :selected="formModal.selectedProblems"
      @confirm="handleProblemsConfirm"
    />
  </NFlex>
</template>
