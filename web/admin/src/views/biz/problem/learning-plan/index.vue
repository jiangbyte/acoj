<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import type { ProblemOption } from '@/components/selector/ProblemSelector.vue'
import { Icon } from '@iconify/vue/offline'
import ProblemSelector from '@/components/selector/ProblemSelector.vue'
import { ojLearningPlanApi } from '@/api'
import { formatDateTime, normalizeSearchValues, renderButtonIcon } from '@/utils'
import {
  NButton,
  NDynamicInput,
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

type SectionModel = { title: string; sort: number; problems: ProblemOption[] }

const formModal = reactive({
  show: false,
  loading: false,
  submitLoading: false,
  showProblemSelector: false,
  sectionIndex: -1,
  id: null as string | null,
  model: {
    code: '',
    title: '',
    subtitle: '',
    overview: '',
    cover_url: '',
    category: 'FEATURED',
    sort: 0,
    status: 'ENABLED',
    sections: [] as SectionModel[],
  },
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

const sectionSelectedProblems = computed(() => {
  const idx = formModal.sectionIndex
  if (idx < 0 || idx >= formModal.model.sections.length) return [] as ProblemOption[]
  return formModal.model.sections[idx]?.problems ?? []
})

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
  { title: '编码', path: 'code', width: 140, ellipsis: { tooltip: true } },
  { title: '标题', path: 'title', ellipsis: { tooltip: true } },
  {
    title: '分类',
    path: 'category',
    width: 110,
    render: (row) => (row.category === 'INTERVIEW' ? '面试准备' : row.category === 'FEATURED' ? '精选' : row.category || '-'),
  },
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
    const res = await ojLearningPlanApi.page({
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
    subtitle: '',
    overview: '',
    cover_url: '',
    category: 'FEATURED',
    sort: 0,
    status: 'ENABLED',
    sections: [{ title: '基础', sort: 0, problems: [] }],
  }
  formModal.show = true
}

async function openEdit(id: string) {
  formModal.id = id
  formModal.show = true
  formModal.loading = true
  try {
    const res = await ojLearningPlanApi.detail({ id })
    const d = res.data ?? {}
    formModal.model = {
      code: d.code ?? '',
      title: d.title ?? '',
      subtitle: d.subtitle ?? '',
      overview: d.overview ?? '',
      cover_url: d.cover_url ?? '',
      category: d.category ?? 'FEATURED',
      sort: d.sort ?? 0,
      status: d.status ?? 'ENABLED',
      sections: (d.sections ?? []).map((s: any, i: number) => ({
        title: s.title,
        sort: s.sort ?? i,
        problems: (s.problems ?? []).map((p: any) => ({
          id: String(p.id),
          code: p.code || '',
          name: p.name || '',
        })),
      })),
    }
  } finally {
    formModal.loading = false
  }
}

function openSectionSelector(index: number) {
  formModal.sectionIndex = index
  formModal.showProblemSelector = true
}

function handleSectionProblemsConfirm(problems: ProblemOption[]) {
  const idx = formModal.sectionIndex
  if (idx < 0 || idx >= formModal.model.sections.length) return
  formModal.model.sections[idx].problems = problems
}

function removeSectionProblem(section: SectionModel, id: string) {
  section.problems = section.problems.filter((p) => p.id !== id)
}

async function submitForm() {
  formModal.submitLoading = true
  try {
    const payload = {
      ...formModal.model,
      sections: formModal.model.sections.map((s) => ({
        title: s.title,
        sort: s.sort,
        problem_ids: s.problems.map((p) => p.id),
      })),
    }
    if (formModal.id) {
      await ojLearningPlanApi.update({ id: formModal.id, ...payload })
      window.$message.success('更新成功')
    } else {
      await ojLearningPlanApi.create(payload)
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
  await ojLearningPlanApi.remove({ ids })
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
      title="学习计划"
      row-key="id"
      :scroll-x="1000"
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
      :title="formModal.id ? '编辑学习计划' : '新增学习计划'"
      style="width: 860px"
      :mask-closable="false"
    >
      <NSpin :show="formModal.loading">
        <NForm label-placement="left" label-width="100">
          <NFormItem label="编码" required>
            <NInput v-model:value="formModal.model.code" />
          </NFormItem>
          <NFormItem label="标题" required>
            <NInput v-model:value="formModal.model.title" />
          </NFormItem>
          <NFormItem label="副标题">
            <NInput v-model:value="formModal.model.subtitle" />
          </NFormItem>
          <NFormItem label="概述">
            <NInput v-model:value="formModal.model.overview" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
          </NFormItem>
          <NFormItem label="封面 URL">
            <NInput v-model:value="formModal.model.cover_url" />
          </NFormItem>
          <NFormItem label="分类">
            <NSelect
              v-model:value="formModal.model.category"
              :options="[
                { label: '精选', value: 'FEATURED' },
                { label: '面试准备', value: 'INTERVIEW' },
              ]"
            />
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
          <NFormItem label="分组题目">
            <NDynamicInput
              v-model:value="formModal.model.sections"
              :on-create="() => ({ title: '新分组', sort: formModal.model.sections.length, problems: [] })"
            >
              <template #default="{ value, index }">
                <div class="flex w-full flex-col gap-8px py-8px">
                  <NInput v-model:value="value.title" placeholder="分组标题" />
                  <NFlex :size="8" align="center">
                    <NButton type="primary" size="small" @click="openSectionSelector(index)">
                      选择题目
                    </NButton>
                    <NText depth="3">已选 {{ value.problems.length }} 题</NText>
                  </NFlex>
                  <NFlex :size="8" style="flex-wrap: wrap">
                    <NTag
                      v-for="p in value.problems"
                      :key="p.id"
                      closable
                      size="small"
                      @close="removeSectionProblem(value, p.id)"
                    >
                      {{ p.code }}. {{ p.name }}
                    </NTag>
                  </NFlex>
                </div>
              </template>
            </NDynamicInput>
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
      :selected="sectionSelectedProblems"
      @confirm="handleSectionProblemsConfirm"
    />
  </NFlex>
</template>
