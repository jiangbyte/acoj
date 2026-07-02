<script setup lang="tsx">
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { resourceApi, resourceModuleApi } from '@/api'
import { createTagColor, normalizeSearchValues, renderButtonIcon, translateLocale } from '@/utils'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const state = reactive({
  resources: [] as any[],
  moduleOptions: [] as any[],
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
})

const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)
const filteredResources = computed(() => filterResourceTree(state.resources, state.searchValues))

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      code: (value) => String(value).trim(),
      name: (value) => String(value).trim(),
      module_id: (value) => String(value).trim(),
      parent_id: (value) => String(value).trim(),
    })
  },
  onReset() {
    state.searchValues = {}
  },
})

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  {
    title: t('resource.iam.resource.code'),
    path: 'code',
    field: 'input',
  },
  {
    title: t('resource.iam.resource.name'),
    path: 'name',
    field: 'input',
  },
  {
    title: t('resource.iam.resource.resource_type'),
    path: 'resource_type',
    field: 'select',
    fieldProps: {
      options: dictList('RESOURCE_TYPE'),
    },
  },
  {
    title: t('resource.iam.resource.module'),
    path: 'module_id',
    field: 'select',
    fieldProps: {
      options: state.moduleOptions,
    },
  },
  {
    title: t('resource.iam.resource.parent_id'),
    path: 'parent_id',
    field: 'input',
  },
  {
    title: t('common.often.status'),
    path: 'status',
    field: 'select',
    fieldProps: {
      options: dictList('COMMON_STATUS'),
    },
  },
])

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  {
    type: 'selection',
    fixed: 'left',
  },
  {
    title: t('resource.iam.resource.name'),
    path: 'name',
    width: 220,
    render: (row) => translateLocale(row.locale_key, row.name),
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.locale_key'),
    path: 'locale_key',
    width: 220,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.resource.code'),
    path: 'code',
    width: 180,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.resource.resource_type'),
    path: 'resource_type',
    width: 130,
    render: (row) => dictTypeData('RESOURCE_TYPE', row.resource_type) || row.resource_type,
  },
  {
    title: t('resource.iam.resource.module'),
    path: 'module_id_name',
    width: 130,
    ellipsis: {
      tooltip: true,
    },
    render: (row) => row.module_id_name || row.module_id || '-',
  },
  {
    title: t('resource.iam.resource.path'),
    path: 'path',
    width: 210,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.resource.component'),
    path: 'component',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.resource.sort'),
    path: 'sort',
    width: 90,
  },
  {
    title: t('resource.iam.resource.is_visible'),
    path: 'is_visible',
    width: 90,
    render: (row) =>
      row.is_visible ? t('resource.iam.resource.yes') : t('resource.iam.resource.no'),
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || row.status}
      </NTag>
    ),
  },
  {
    title: t('common.often.updated_at'),
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
          {renderButtonIcon('icon-park-outline:preview-open')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
          {renderButtonIcon('icon-park-outline:edit')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openCreateModal(row.id)}>
          {renderButtonIcon('icon-park-outline:plus')}
        </NButton>
        <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
          {renderButtonIcon('icon-park-outline:delete')}
        </NButton>
      </NFlex>
    ),
  },
])

onMounted(() => {
  fetchModules()
  fetchTree()
})

async function fetchTree() {
  state.loading = true
  try {
    const response = await resourceApi.tree()
    state.resources = response.data ?? []
    const existingIds = new Set(flattenResourceTree(state.resources).map((item) => item.id))
    state.checkedRowKeys = state.checkedRowKeys.filter((key) => existingIds.has(key))
  } finally {
    state.loading = false
  }
}

async function fetchModules() {
  const response = await resourceModuleApi.selector()
  state.moduleOptions = (response.data ?? []).map((item: any) => ({
    label: translateLocale(item.locale_key, item.name),
    value: item.id,
  }))
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function openCreateModal(parentId?: string) {
  formModalRef.value?.openModal(undefined, parentId)
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) {
    return
  }
  const isBatch = ids.length > 1

  window.$dialog.warning({
    title: isBatch ? t('common.often.batch_delete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('resource.iam.resource.batch_delete_confirm', { count: ids.length })
      : t('resource.iam.resource.delete_confirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await resourceApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success(t('common.often.delete_success'))
  await fetchTree()
}

function filterResourceTree(items: any[], searchValues: any): any[] {
  return items
    .map((item) => {
      const children = filterResourceTree(item.children ?? [], searchValues)
      if (matchesResource(item, searchValues) || children.length) {
        return {
          ...item,
          children,
        }
      }
      return null
    })
    .filter(Boolean)
}

function matchesResource(item: any, searchValues: any) {
  return (
    containsValue(item.code, searchValues.code) &&
    containsValue(item.name, searchValues.name) &&
    equalsValue(item.module_id, searchValues.module_id) &&
    containsValue(item.parent_id, searchValues.parent_id) &&
    equalsValue(item.resource_type, searchValues.resource_type) &&
    equalsValue(item.status, searchValues.status)
  )
}

function containsValue(source: unknown, target: unknown) {
  if (target === undefined || target === null || target === '') {
    return true
  }
  return String(source ?? '')
    .toLowerCase()
    .includes(String(target).toLowerCase())
}

function equalsValue(source: unknown, target: unknown) {
  if (target === undefined || target === null || target === '') {
    return true
  }
  return String(source ?? '') === String(target)
}

function flattenResourceTree(items: any[]) {
  const result: any[] = []
  const walk = (nodes: any[]) => {
    nodes.forEach((node) => {
      result.push(node)
      walk(node.children ?? [])
    })
  }
  walk(items)
  return result
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: t('common.search_form.reset') }"
        :search-button-props="{ content: t('common.search_form.search') }"
        :collapse-button-props="{
          content: searchForm.collapsed.value
            ? t('common.search_form.expand')
            : t('common.search_form.collapse'),
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      :title="t('resource.iam.resource.title')"
      row-key="id"
      :scroll-x="1800"
      :columns="tableColumns"
      :data="filteredResources"
      :loading="state.loading"
      :pagination="false"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
      default-expand-all
    >
      <template #toolbar>
        <NFlex>
          <NButton type="primary" text :title="t('common.often.add')" :aria-label="t('common.often.add')" @click="openCreateModal()">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton text :title="t('common.reload')" :aria-label="t('common.reload')" :loading="state.loading" @click="fetchTree">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            type="error"
            text
            :title="t('common.often.batch_delete')"
            :aria-label="t('common.often.batch_delete')"
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

    <ModalForm ref="formModalRef" @saved="fetchTree" />
    <ModalDetail ref="detailModalRef" />
  </NFlex>
</template>

<style scoped></style>
