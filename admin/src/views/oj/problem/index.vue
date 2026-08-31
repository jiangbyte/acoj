<!--
  Author: Charlie

  OJ 题目管理。
-->
<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi } from '@/api'
import {
  createTagColor,
  dictList,
  dictTypeColor,
  dictTypeData,
  displayValue,
  formatDateTime,
  hasPermission,
  normalizeSearchValues,
  renderButtonIcon,
} from '@/utils'
import { NButton, NDropdown, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { readPageMeta } from '@/utils/wire'

const router = useRouter()

const difficultyOptions = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' },
]

const difficultyType: Record<string, 'success' | 'warning' | 'error' | 'default'> = {
  EASY: 'success',
  MEDIUM: 'warning',
  HARD: 'error',
}

const moreActionOptions = computed(() => {
  const options: Array<{ label: string; key: string }> = []
  if (hasPermission('oj:problem:update')) {
    options.push(
      { label: '测例', key: 'cases' },
      { label: '参考答案', key: 'solutions' },
      { label: '试跑', key: 'dry-run' },
      { label: '试跑历史', key: 'dry-runs' },
    )
  }
  return options
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
  { title: '题号', path: 'problem_key', field: 'input' },
  { title: '标题', path: 'title', field: 'input' },
  {
    title: '难度',
    path: 'difficulty',
    field: 'select',
    fieldProps: { options: difficultyOptions },
  },
  {
    title: '状态',
    path: 'status',
    field: 'select',
    fieldProps: { options: dictList('OJ_PROBLEM_STATUS') },
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
  { type: 'selection', fixed: 'left' },
  { title: '题号', path: 'problem_key', width: 110, ellipsis: { tooltip: true } },
  { title: '标题', path: 'title', width: 240, ellipsis: { tooltip: true } },
  {
    title: '难度',
    path: 'difficulty',
    width: 90,
    render: (row) => {
      const label =
        difficultyOptions.find((item) => item.value === row.difficulty)?.label ||
        displayValue(row.difficulty)
      return (
        <NTag size="small" bordered={false} type={difficultyType[row.difficulty] || 'default'}>
          {label}
        </NTag>
      )
    },
  },
  {
    title: '状态',
    path: 'status',
    width: 90,
    render: (row) => (
      <NTag
        size="small"
        color={createTagColor(dictTypeColor('OJ_PROBLEM_STATUS', row.status))}
        bordered={false}
      >
        {dictTypeData('OJ_PROBLEM_STATUS', row.status) || displayValue(row.status)}
      </NTag>
    ),
  },
  {
    title: '标签',
    key: 'tags',
    width: 180,
    ellipsis: { tooltip: true },
    render: (row) => {
      const tags = Array.isArray(row.tags) ? row.tags : []
      if (!tags.length) {
        return '—'
      }
      return (
        <NFlex size={4} wrap>
          {tags.map((tag: any) => (
            <NTag size="small" bordered={false} key={tag.id || tag.name}>
              {tag.name || displayValue(tag.id)}
            </NTag>
          ))}
        </NFlex>
      )
    },
  },
  {
    title: '语言数',
    key: 'language_count',
    width: 90,
    align: 'right',
    render: (row) => {
      const limits = row.language_limits
      return Array.isArray(limits) ? String(limits.length) : '—'
    },
  },
  {
    title: '测例版本',
    path: 'case_version',
    width: 90,
    align: 'right',
  },
  {
    title: '提交/AC',
    key: 'counts',
    width: 100,
    render: (row) => `${displayValue(row.submit_count)} / ${displayValue(row.accept_count)}`,
  },
  {
    title: '更新时间',
    path: 'updated_at',
    width: 170,
    render: (row) => formatDateTime(row.updated_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('oj:problem:detail') ? (
          <NButton type="info" text={true} onClick={() => openDetailPage(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('oj:problem:update') ? (
          <NButton type="primary" text={true} onClick={() => openEditPage(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {moreActionOptions.value.length ? (
          <NDropdown
            trigger="click"
            options={moreActionOptions.value}
            onSelect={(key) => handleMoreAction(String(key), row.id)}
          >
            <NButton type="warning" text={true}>
              {renderButtonIcon('icon-park-outline:more')}
            </NButton>
          </NDropdown>
        ) : null}
        {hasPermission('oj:problem:delete') ? (
          <NButton type="error" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await ojProblemApi.page({
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
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.rows.some((item) => item.id === key),
    )
  } finally {
    state.loading = false
  }
}

function openDetailPage(id: string) {
  router.push({ path: '/oj/problem/detail', query: { id } })
}

function openCreatePage() {
  router.push('/oj/problem/create')
}

function openEditPage(id: string) {
  router.push({ path: '/oj/problem/edit', query: { id } })
}

function openCasesPage(id: string) {
  router.push({ path: '/oj/problem/cases', query: { id } })
}

function openSolutionsPage(id: string) {
  router.push({ path: '/oj/problem/solutions', query: { id } })
}

function openDryRunPage(id: string) {
  router.push({ path: '/oj/problem/dry-run', query: { id } })
}

function openDryRunsPage(id: string) {
  router.push({ path: '/oj/problem/dry-runs', query: { id } })
}

function handleMoreAction(key: string, id: string) {
  if (key === 'cases') {
    openCasesPage(id)
    return
  }
  if (key === 'solutions') {
    openSolutionsPage(id)
    return
  }
  if (key === 'dry-run') {
    openDryRunPage(id)
    return
  }
  if (key === 'dry-runs') {
    openDryRunsPage(id)
  }
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
  await ojProblemApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success('删除成功')
  await fetchPage()
}
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
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="题目管理"
      row-key="id"
      :scroll-x="1480"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton
            v-if="hasPermission('oj:problem:create')"
            type="primary"
            text
            @click="openCreatePage"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            text
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:refresh" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('oj:problem:delete')"
            type="error"
            text
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
  </NFlex>
</template>
