<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import type { DictCategory, DictFormModel, SysDict } from './types'
import { Icon } from '@iconify/vue'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import {
  createProSearchForm,
  ProCard,
  ProDataTable,
  ProSearchForm,
} from 'pro-naive-ui'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  categoryLabelKeyMap,
  statusLabelKeyMap,
  statusOptions,
  statusTagTypeMap,
} from './constants'
import { createMockDicts } from './mock'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
const dicts = ref<SysDict[]>(createMockDicts())
const loading = ref(false)
const category = ref<DictCategory>('SYS')
const treeSearchKey = ref('')
const selectedTreeKeys = ref<string[]>([])
const checkedRowKeys = ref<string[]>([])
const searchValues = ref<any>({})
const formModalRef = ref<any>(null)
const page = ref(1)
const pageSize = ref(20)

const selectedParentId = computed(() => selectedTreeKeys.value[0] ?? null)
const hasCheckedRows = computed(() => checkedRowKeys.value.length > 0)

const searchForm = createProSearchForm<any>({
  onSubmit(values) {
    searchValues.value = normalizeSearchValues(values)
    page.value = 1
  },
  onReset() {
    searchValues.value = {}
    page.value = 1
  },
})

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  {
    title: t('pages.sys.dict.code'),
    path: 'code',
    field: 'input',
  },
  {
    title: t('common.often.status'),
    path: 'status',
    field: 'select',
    fieldProps: {
      options: translateOptions(statusOptions),
    },
  },
])

const categoryDicts = computed(() =>
  dicts.value
    .filter((item) => item.category === category.value)
    .sort(sortDicts),
)

const treeData = computed(() => {
  const keyword = treeSearchKey.value.trim().toLowerCase()
  return buildTreeNodes(categoryDicts.value, keyword)
})

const tableData = computed(() => {
  const rows = categoryDicts.value.filter((item) => {
    if (selectedParentId.value && item.parent_id !== selectedParentId.value) {
      return false
    }

    if (searchValues.value.code && !item.code.includes(String(searchValues.value.code).toUpperCase())) {
      return false
    }

    if (searchValues.value.status && item.status !== searchValues.value.status) {
      return false
    }

    return true
  })

  return rows
})

const pagedTableData = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const pagination = computed<PaginationProps>(() => ({
  page: page.value,
  pageSize: pageSize.value,
  itemCount: tableData.value.length,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => t('common.often.total', { count: itemCount }),
  onUpdatePage: (value) => {
    page.value = value
  },
  onUpdatePageSize: (value) => {
    pageSize.value = value
    page.value = 1
  },
}))

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  {
    type: 'selection',
    fixed: 'left',
  },
  {
    title: t('common.often.index'),
    width: 80,
    key: 'index',
    render: (_row, index) => (page.value - 1) * pageSize.value + index + 1,
  },
  {
    title: t('pages.sys.dict.code'),
    path: 'code',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.dict.label'),
    path: 'label',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.dict.value'),
    path: 'value',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.sys.dict.color'),
    path: 'color',
    width: 120,
    render: (row) =>
      row.color ? (
        <NTag type={normalizeTagType(row.color)} bordered={false}>
          {row.color}
        </NTag>
      ) : (
        '-'
      ),
  },
  {
    title: t('pages.sys.dict.category'),
    path: 'category',
    width: 120,
    render: (row) => displayLabel(categoryLabelKeyMap, row.category),
  },
  {
    title: t('pages.sys.dict.parent'),
    path: 'parent_id',
    width: 180,
    ellipsis: {
      tooltip: true,
    },
    render: (row) => displayParent(row.parent_id),
  },
  {
    title: t('pages.sys.dict.sort'),
    path: 'sort',
    width: 90,
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag type={statusTagTypeMap[row.status] ?? 'default'} bordered={false}>
        {displayLabel(statusLabelKeyMap, row.status)}
      </NTag>
    ),
  },
  {
    title: t('common.often.updateTime'),
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
          {t('common.often.edit')}
        </NButton>
        <NButton type="error" size="small" text={true} onClick={() => confirmDeleteDicts(row.id)}>
          {t('common.often.delete')}
        </NButton>
      </NFlex>
    ),
  },
])

function normalizeSearchValues(values: any) {
  const result = Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== ''),
  )
  if (result.code) {
    result.code = String(result.code).trim().toUpperCase()
  }
  return result
}

function translateOptions(options: Array<{ labelKey: string; value: string }>) {
  return options.map((item) => ({
    label: t(item.labelKey),
    value: item.value,
  }))
}

function displayLabel(map: Record<string, string>, value?: string | null) {
  if (!value) {
    return '-'
  }
  const labelKey = map[value]
  return labelKey ? t(labelKey) : value
}

function displayParent(parentId?: string | null) {
  if (!parentId) {
    return t('pages.sys.dict.topLevel')
  }
  const parent = dicts.value.find((item) => item.id === parentId)
  return parent ? `${parent.label || parent.code}` : '-'
}

function normalizeTagType(value: string) {
  const types: Record<string, 'success' | 'info' | 'warning' | 'error' | 'default'> = {
    success: 'success',
    info: 'info',
    warning: 'warning',
    error: 'error',
    primary: 'info',
  }
  return types[value] ?? 'default'
}

function handleCategoryUpdate(value: DictCategory) {
  category.value = value
  treeSearchKey.value = ''
  selectedTreeKeys.value = []
  checkedRowKeys.value = []
  page.value = 1
}

function handleTreeSelect(keys: Array<string | number>) {
  selectedTreeKeys.value = keys.map(String)
  checkedRowKeys.value = []
  page.value = 1
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  checkedRowKeys.value = keys.map(String)
}

function openCreateModal() {
  formModalRef.value?.openModal(undefined, {
    category: category.value,
    parentId: selectedParentId.value,
  })
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
}

function handleSaveDict(values: DictFormModel) {
  if (isDuplicateCode(values)) {
    window.$message.error(t('pages.sys.dict.codeExists'))
    return
  }

  if (values.id) {
    updateDict(values)
    window.$message.success(t('common.often.updateSuccess'))
  } else {
    createDict(values)
    window.$message.success(t('common.often.createSuccess'))
  }

  page.value = 1
}

function createDict(values: DictFormModel) {
  const now = currentTime()
  dicts.value.unshift({
    ...values,
    id: createId(),
    created_at: now,
    updated_at: now,
  })
}

function updateDict(values: DictFormModel) {
  const index = dicts.value.findIndex((item) => item.id === values.id)
  if (index < 0) {
    return
  }

  dicts.value[index] = {
    ...dicts.value[index],
    ...values,
    id: values.id!,
    updated_at: currentTime(),
  }
}

function isDuplicateCode(values: DictFormModel) {
  return dicts.value.some((item) => item.code === values.code && item.id !== values.id)
}

function refreshDicts() {
  loading.value = true
  try {
    dicts.value = createMockDicts()
    checkedRowKeys.value = []
    if (selectedParentId.value && !dicts.value.some((item) => item.id === selectedParentId.value)) {
      selectedTreeKeys.value = []
    }
    page.value = 1
  } finally {
    loading.value = false
  }
}

function confirmDeleteDicts(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) {
    return
  }
  const deleteIds = collectDeleteIds(ids)
  const isBatch = ids.length > 1

  window.$dialog.warning({
    title: isBatch ? t('common.often.batchDelete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('pages.sys.dict.batchDeleteConfirm', { count: deleteIds.length })
      : t('pages.sys.dict.deleteConfirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteDicts(deleteIds),
  })
}

function deleteDicts(ids: string[]) {
  dicts.value = dicts.value.filter((item) => !ids.includes(item.id))
  checkedRowKeys.value = checkedRowKeys.value.filter((key) => !ids.includes(key))
  if (selectedParentId.value && ids.includes(selectedParentId.value)) {
    selectedTreeKeys.value = []
  }
  window.$message.success(t('common.often.deleteSuccess'))

  const maxPage = Math.max(1, Math.ceil(tableData.value.length / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }
}

function collectDeleteIds(ids: string[]) {
  const result = new Set(ids)
  const walk = (parentId: string) => {
    dicts.value
      .filter((item) => item.parent_id === parentId)
      .forEach((item) => {
        result.add(item.id)
        walk(item.id)
      })
  }
  ids.forEach(walk)
  return Array.from(result)
}

function buildTreeNodes(items: SysDict[], keyword: string) {
  const nodeMap = new Map(
    items.map((item) => [
      item.id,
      {
        key: item.id,
        label: item.label || item.code,
        children: [] as any[],
        raw: item,
      },
    ]),
  )
  const roots: any[] = []

  nodeMap.forEach((node) => {
    const parentId = node.raw.parent_id
    const parent = parentId ? nodeMap.get(parentId) : null
    if (parent) {
      parent.children.push(node)
    } else {
      roots.push(node)
    }
  })

  return sortAndFilterTree(roots, keyword)
}

function sortAndFilterTree(nodes: any[], keyword: string): any[] {
  return nodes
    .sort((a, b) => sortDicts(a.raw, b.raw))
    .map((node) => ({
      ...node,
      children: sortAndFilterTree(node.children, keyword),
    }))
    .filter((node) => {
      if (!keyword) {
        return true
      }
      const raw = node.raw as SysDict
      return (
        raw.code.toLowerCase().includes(keyword) ||
        String(raw.label ?? '').toLowerCase().includes(keyword) ||
        node.children.length > 0
      )
    })
    .map((node) => ({
      key: node.key,
      label: node.label,
      children: node.children,
    }))
}

function sortDicts(a: SysDict, b: SysDict) {
  return a.sort === b.sort ? b.id.localeCompare(a.id) : a.sort - b.sort
}

function createId() {
  return String(Date.now())
}

function currentTime() {
  return new Date().toISOString().replace(/\.\d{3}Z$/, 'Z')
}
</script>

<template>
  <div class="dict-page">
    <ProCard class="dict-tree-card" content-class="h-full min-h-0">
      <NFlex class="h-full min-h-0" vertical :size="12">
        <NInput
          v-model:value="treeSearchKey"
          clearable
          :placeholder="t('pages.sys.dict.searchTree')"
        >
          <template #prefix>
            <NIcon>
              <Icon icon="ant-design:search-outlined" />
            </NIcon>
          </template>
        </NInput>
        <NTabs
          :value="category"
          type="line"
          animated
          justify-content="space-evenly"
          @update:value="handleCategoryUpdate"
        >
          <NTabPane name="SYS" :tab="t('pages.sys.dict.categories.sys')" />
          <NTabPane name="BIZ" :tab="t('pages.sys.dict.categories.biz')" />
        </NTabs>
        <NScrollbar class="min-h-0 flex-1">
          <NTree
            block-line
            block-node
            show-line
            default-expand-all
            :data="treeData"
            :selected-keys="selectedTreeKeys"
            key-field="key"
            label-field="label"
            children-field="children"
            @update:selected-keys="handleTreeSelect"
          />
        </NScrollbar>
      </NFlex>
    </ProCard>

    <NFlex class="min-w-0 min-h-0 h-full" vertical>
      <ProCard content-class="pb-0!">
        <ProSearchForm :form="searchForm" :columns="searchColumns" />
      </ProCard>

      <ProDataTable
        class="min-h-0 flex-1"
        :title="t('pages.sys.dict.title')"
        row-key="id"
        :scroll-x="1540"
        :columns="tableColumns"
        :data="pagedTableData"
        :loading="loading"
        :pagination="pagination"
        :checked-row-keys="checkedRowKeys"
        :on-update-checked-row-keys="handleCheckedRowKeys"
      >
        <template #toolbar>
          <NFlex>
            <NButton type="primary" ghost @click="openCreateModal">
              <template #icon>
                <NIcon>
                  <Icon icon="ant-design:plus-outlined" />
                </NIcon>
              </template>
              {{ t('pages.sys.dict.addDict') }}
            </NButton>
            <NButton ghost :loading="loading" @click="refreshDicts">
              <template #icon>
                <NIcon>
                  <Icon icon="ant-design:reload-outlined" />
                </NIcon>
              </template>
              {{ t('common.reload') }}
            </NButton>
            <NButton
              type="error"
              ghost
              :disabled="!hasCheckedRows"
              @click="confirmDeleteDicts(checkedRowKeys)"
            >
              {{ t('common.often.batchDelete') }}
              {{ t('common.often.total', { count: checkedRowKeys.length }) }}
            </NButton>
          </NFlex>
        </template>
      </ProDataTable>
    </NFlex>

    <ModalForm ref="formModalRef" :dicts="dicts" @saved="handleSaveDict" />
  </div>
</template>

<style scoped>
.dict-page {
  display: grid;
  grid-template-columns: minmax(260px, 320px) minmax(0, 1fr);
  gap: 16px;
  height: 100%;
  min-height: 0;
}

.dict-tree-card {
  min-height: 0;
}

@media (max-width: 900px) {
  .dict-page {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(280px, 36vh) minmax(0, 1fr);
  }
}
</style>
