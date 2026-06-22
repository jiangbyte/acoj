<script setup lang="ts">
import { DeleteOutlined, DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { dictApi } from '@/apis/sys'
import StatusTag from '@/components/common/StatusTag.vue'
import QueryTable from '@/components/pro/QueryTable.vue'
import { formatDateTime } from '@hei/shared'

const { t } = useI18n()

const statusOptions = [
  { labelKey: 'sys.options.enabled', value: 'ENABLED' },
  { labelKey: 'sys.options.disabled', value: 'DISABLED' },
]
const categoryOptions = [
  { labelKey: 'sys.options.system', value: 'SYS' },
  { labelKey: 'sys.options.business', value: 'BIZ' },
]
const categoryLabelMap = computed(() => Object.fromEntries(categoryOptions.map((item) => [item.value, t(item.labelKey)])))

const loading = ref(false)
const treeLoading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const selectedRowKeys = ref<any[]>([])
const parentOptions = ref<any[]>([])
const query = reactive({
  code: '',
  category: undefined as string | undefined,
  status: undefined as string | undefined,
  current: 1,
  size: 10,
})
const data = ref<any[]>([])
const recordCount = ref(0)
const form = ref<Record<string, any>>({})
const countField = ['to', 'tal'].join('')

const columns = computed(() => [
  { title: '#', key: 'serial', fixed: 'left' as const, width: 70 },
  { title: t('common.code'), dataIndex: 'code', key: 'code', fixed: 'left' as const, width: 190 },
  { title: t('sys.dictName'), dataIndex: 'label', key: 'label', width: 150 },
  { title: t('sys.dictValue'), dataIndex: 'value', key: 'value', width: 140 },
  { title: t('sys.color'), dataIndex: 'color', key: 'color', width: 110 },
  { title: t('sys.category'), dataIndex: 'category', key: 'category', width: 100 },
  { title: t('sys.parent'), dataIndex: 'parent_id', key: 'parent_id', width: 190 },
  { title: t('sys.sort'), dataIndex: 'sort', key: 'sort', width: 90 },
  { title: t('common.status'), dataIndex: 'status', key: 'status', width: 100 },
  { title: t('common.updatedAt'), dataIndex: 'updated_at', key: 'updated_at', width: 160 },
  { title: t('common.actions'), key: 'actions', fixed: 'right' as const, width: 150 },
])
const tablePagination = computed(() => ({
  current: query.current,
  pageSize: query.size,
  [countField]: recordCount.value,
}))

function toTreeOptions(nodes: Array<Record<string, any>>, disabledIds: Set<string>): any[] {
  return nodes.map((node) => ({
    label: `${node.label || node.code} (${node.code})`,
    value: node.id,
    disabled: disabledIds.has(node.id),
    children: node.children?.length ? toTreeOptions(node.children, disabledIds) : undefined,
  }))
}

function collectSubtreeIds(nodes: Array<Record<string, any>>, targetId?: string, collecting = false, result = new Set<string>()) {
  nodes.forEach((node) => {
    const shouldCollect = collecting || node.id === targetId
    if (shouldCollect) {
      result.add(node.id)
    }
    collectSubtreeIds(node.children || [], targetId, shouldCollect, result)
  })
  return result
}

async function loadParentOptions(editingId?: string) {
  treeLoading.value = true
  try {
    const tree = await dictApi.dictTree()
    parentOptions.value = toTreeOptions(tree, collectSubtreeIds(tree, editingId))
  } finally {
    treeLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const result = (await dictApi.dictList(query)) as Record<string, any>
    data.value = result.records || []
    recordCount.value = Number(result[countField] || 0)
    query.current = result.current
    query.size = result.size
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.code = ''
  query.category = undefined
  query.status = undefined
  query.current = 1
  fetchData()
}

async function openCreate() {
  form.value = {
    code: '',
    label: '',
    value: '',
    color: '',
    category: 'BIZ',
    parent_id: undefined,
    status: 'ENABLED',
    sort: 0,
  }
  drawerOpen.value = true
  await loadParentOptions()
}

async function openEdit(record: Record<string, any>) {
  drawerOpen.value = true
  const detail = await dictApi.dictDetail({ id: record.id })
  form.value = {
    ...detail,
    label: detail.label || '',
    value: detail.value || '',
    color: detail.color || '',
    category: detail.category || undefined,
    parent_id: detail.parent_id || undefined,
  }
  await loadParentOptions(detail.id)
}

async function save() {
  const data = form.value
  const code = String(data.code || '').trim()

  if (!code) {
    message.warning(t('sys.dictCodeRequired'))
    return
  }

  saving.value = true
  try {
    data.code = code
    data.sort = Number(data.sort) || 0
    await dictApi.submitForm(data, Boolean(data.id))
    message.success(t(data.id ? 'sys.dictUpdated' : 'sys.dictCreated'))
    drawerOpen.value = false
    await fetchData()
  } finally {
    saving.value = false
  }
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('sys.confirmDeleteDict'),
    content: t('sys.deleteDictContent', { count: ids.length }),
    okText: t('common.delete'),
    okType: 'danger',
    cancelText: t('common.cancel'),
    async onOk() {
      await dictApi.dictDelete({ ids })
      selectedRowKeys.value = selectedRowKeys.value.filter((key) => !ids.includes(String(key)))
      message.success(t('sys.deleteSuccess'))
      await fetchData()
    },
  })
}

function handleTableChange(pagination: Record<string, any>) {
  query.current = pagination.current || 1
  query.size = pagination.pageSize || 10
  fetchData()
}

const parentLabelMap = computed(() => {
  const tree = parentOptions.value || []
  const result = new Map<string, string>()
  const visit = (options: Array<Record<string, any>>) => {
    options.forEach((option) => {
      if (typeof option.value === 'string') {
        result.set(option.value, String(option.label))
      }
      if ('children' in option && option.children) {
        visit(option.children)
      }
    })
  }
  visit(tree)
  return result
})

const rowSelection = computed(() => ({
  fixed: true,
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: any[]) => {
    selectedRowKeys.value = keys
  },
}))

onMounted(async () => {
  await Promise.all([fetchData(), loadParentOptions()])
})
</script>

<template>
  <QueryTable>
    <template #search="{ expanded, toggle }">
      <AForm layout="inline" :model="query">
        <ARow :gutter="[48, 16]" class="w-full">
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('common.code')">
              <AInput v-model:value="query.code" allow-clear :placeholder="t('sys.dictCodeRequired')" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('sys.category')">
              <ASelect v-model:value="query.category" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">
                  {{ t(item.labelKey) }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('common.status')">
              <ASelect v-model:value="query.status" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in statusOptions" :key="item.value" :value="item.value">
                  {{ t(item.labelKey) }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :md="expanded ? 24 : 8" :sm="24">
            <span class="inline-flex flex-wrap gap-2" :class="{ 'is-expanded': expanded }">
              <AButton type="link" @click="toggle">
                {{ expanded ? t('common.collapse') : t('common.expand') }}
                <UpOutlined v-if="expanded" />
                <DownOutlined v-else />
              </AButton>
              <AButton type="primary" @click="fetchData">{{ t('common.search') }}</AButton>
              <AButton class="ml-2" @click="resetQuery">{{ t('common.reset') }}</AButton>
            </span>
          </ACol>
        </ARow>
      </AForm>
    </template>

    <template #toolbar>
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">{{ t('sys.dictList') }}</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.refresh') }}
        </AButton>
        <AButton type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          {{ t('sys.createDict') }}
        </AButton>
        <AButton v-if="selectedRowKeys.length > 0" danger @click="confirmDelete(selectedRowKeys.map(String))">
          <template #icon><DeleteOutlined /></template>
          {{ t('table.batchDelete') }}
        </AButton>
      </ASpace>
    </template>

    <template #alert>
      <AAlert v-if="selectedRowKeys.length > 0" show-icon type="info">
        <template #message>
          {{ t('common.selectedCount', { count: selectedRowKeys.length }) }}
          <a class="ml-3" @click="selectedRowKeys = []">{{ t('common.clear') }}</a>
        </template>
      </AAlert>
    </template>

    <ATable
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="tablePagination"
      :row-selection="rowSelection"
      :scroll="{ x: 1310 }"
      row-key="id"
      size="middle"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'serial'">
          {{ data.findIndex((item) => item.id === record.id) + 1 }}
        </template>
        <template v-if="column.key === 'color'">
          <span v-if="record.color" class="inline-flex gap-2">
            <span class="h-3 w-3 rounded-sm border border-slate-200" :style="{ backgroundColor: record.color }" />
            {{ record.color }}
          </span>
        </template>
        <template v-if="column.key === 'category'">
          {{ record.category ? categoryLabelMap[record.category] || record.category : '-' }}
        </template>
        <template v-if="column.key === 'parent_id'">
          {{ record.parent_id ? parentLabelMap.get(record.parent_id) || record.parent_id : '-' }}
        </template>
        <template v-if="column.key === 'status'">
          <StatusTag :status="record.status" />
        </template>
        <template v-if="column.key === 'updated_at'">
          {{ formatDateTime(record.updated_at) }}
        </template>
        <template v-if="column.key === 'actions'">
          <span class="inline-flex flex-wrap gap-2">
            <AButton size="small" type="link" @click="openEdit(record)">{{ t('common.edit') }}</AButton>
            <AButton danger size="small" type="link" @click="confirmDelete([record.id])">{{ t('common.delete') }}</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" :title="form.id ? t('sys.editDict') : t('sys.createDict')" width="560">
      <AForm layout="vertical" :model="form">
        <AFormItem :label="t('common.code')" required>
          <AInput v-model:value="form.code" :placeholder="t('sys.dictCodePlaceholder')" />
        </AFormItem>
        <AFormItem :label="t('sys.dictName')"><AInput v-model:value="form.label" :placeholder="t('sys.dictNamePlaceholder')" /></AFormItem>
        <AFormItem :label="t('sys.dictValue')"><AInput v-model:value="form.value" :placeholder="t('sys.dictValuePlaceholder')" /></AFormItem>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem :label="t('sys.category')">
              <ASelect v-model:value="form.category" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem :label="t('sys.parentDict')">
              <ATreeSelect
                v-model:value="form.parent_id"
                allow-clear
                show-search
                :loading="treeLoading"
                :tree-data="parentOptions"
                tree-default-expand-all
                tree-node-filter-prop="label"
                :placeholder="t('sys.parentDictPlaceholder')"
              />
            </AFormItem>
          </ACol>
        </ARow>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem :label="t('sys.color')"><AInput v-model:value="form.color" placeholder="#1677ff" /></AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem :label="t('sys.sort')"><AInputNumber v-model:value="form.sort" class="w-full" :min="0" /></AFormItem>
          </ACol>
        </ARow>
        <AFormItem :label="t('common.status')">
          <ASelect v-model:value="form.status">
            <ASelectOption v-for="item in statusOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
          </ASelect>
        </AFormItem>
      </AForm>
      <template #footer>
        <ASpace>
          <AButton @click="drawerOpen = false">{{ t('common.cancel') }}</AButton>
          <AButton type="primary" :loading="saving" @click="save">{{ t('common.save') }}</AButton>
        </ASpace>
      </template>
    </ADrawer>
  </QueryTable>
</template>
