<script setup lang="ts">
import { DeleteOutlined, DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { bannerApi } from '@/apis/sys'
import StatusTag from '@/components/common/StatusTag.vue'
import QueryTable from '@/components/pro/QueryTable.vue'
import { formatDateTime } from '@hei/shared'
import Form from './form.vue'

const { t } = useI18n()

const statusOptions = [
  { labelKey: 'sys.options.enabled', value: 'ENABLED' },
  { labelKey: 'sys.options.disabled', value: 'DISABLED' },
]
const categoryOptions = [
  { labelKey: 'sys.options.home', value: 'HOME' },
  { labelKey: 'sys.options.login', value: 'LOGIN' },
  { labelKey: 'sys.options.workplace', value: 'WORKPLACE' },
  { labelKey: 'sys.options.notice', value: 'NOTICE' },
  { labelKey: 'sys.options.adminDashboard', value: 'ADMIN_DASHBOARD' },
  { labelKey: 'sys.options.systemUpgrade', value: 'SYSTEM_UPGRADE' },
]
const typeOptions = [
  { labelKey: 'sys.options.carousel', value: 'CAROUSEL' },
  { labelKey: 'sys.options.hero', value: 'HERO' },
  { labelKey: 'sys.options.noticeBar', value: 'NOTICE' },
  { labelKey: 'sys.options.card', value: 'CARD' },
  { labelKey: 'sys.options.popup', value: 'POPUP' },
  { labelKey: 'sys.options.sidebar', value: 'SIDEBAR' },
]
const positionOptions = [
  { labelKey: 'sys.options.homeTop', value: 'HOME_TOP' },
  { labelKey: 'sys.options.homeMiddle', value: 'HOME_MIDDLE' },
  { labelKey: 'sys.options.homeBottom', value: 'HOME_BOTTOM' },
  { labelKey: 'sys.options.loginSide', value: 'LOGIN_SIDE' },
  { labelKey: 'sys.options.workplaceTop', value: 'WORKPLACE_TOP' },
  { labelKey: 'sys.options.noticeArea', value: 'NOTICE_AREA' },
  { labelKey: 'sys.options.adminTop', value: 'ADMIN_TOP' },
  { labelKey: 'sys.options.adminSidebar', value: 'ADMIN_SIDEBAR' },
]
const displayScopeOptions = [
  { labelKey: 'sys.options.portal', value: 'PORTAL' },
  { labelKey: 'sys.options.admin', value: 'ADMIN' },
  { labelKey: 'sys.options.app', value: 'APP' },
]
const linkTypeOptions = [
  { labelKey: 'sys.options.url', value: 'URL' },
  { labelKey: 'sys.options.route', value: 'ROUTE' },
  { labelKey: 'sys.options.none', value: 'NONE' },
]
const optionLabelMaps = {
  category: computed(() => toLabelMap(categoryOptions)),
  type: computed(() => toLabelMap(typeOptions)),
  position: computed(() => toLabelMap(positionOptions)),
  display_scope: computed(() => toLabelMap(displayScopeOptions)),
  link_type: computed(() => toLabelMap(linkTypeOptions)),
}

const loading = ref(false)
const formRef = ref<InstanceType<typeof Form>>()
const selectedRowKeys = ref<any[]>([])
const query = reactive({
  title: '',
  display_scope: undefined as string | undefined,
  category: undefined as string | undefined,
  type: undefined as string | undefined,
  position: undefined as string | undefined,
  status: undefined as string | undefined,
  current: 1,
  size: 10,
})
const data = ref<any[]>([])
const recordCount = ref(0)
const countField = ['to', 'tal'].join('')

const columns = computed(() => [
  { title: '#', key: 'serial', fixed: 'left' as const, width: 70 },
  { title: t('sys.title'), dataIndex: 'title', key: 'title', fixed: 'left' as const, width: 180 },
  { title: t('sys.image'), dataIndex: 'image', key: 'image', width: 120 },
  { title: t('sys.category'), dataIndex: 'category', key: 'category', width: 120 },
  { title: t('common.type'), dataIndex: 'type', key: 'type', width: 110 },
  { title: t('sys.position'), dataIndex: 'position', key: 'position', width: 140 },
  { title: t('sys.displayScope'), dataIndex: 'display_scope', key: 'display_scope', width: 110 },
  { title: t('sys.linkType'), dataIndex: 'link_type', key: 'link_type', width: 110 },
  { title: t('sys.sort'), dataIndex: 'sort', key: 'sort', width: 90 },
  { title: t('sys.interactions'), dataIndex: 'interaction_count', key: 'interaction_count', width: 100 },
  { title: t('common.status'), dataIndex: 'status', key: 'status', width: 100 },
  { title: t('common.updatedAt'), dataIndex: 'updated_at', key: 'updated_at', width: 160 },
  { title: t('common.actions'), key: 'actions', fixed: 'right' as const, width: 150 },
])
const tablePagination = computed(() => ({
  current: query.current,
  pageSize: query.size,
  [countField]: recordCount.value,
}))

function toLabelMap(options: Array<Record<string, string>>) {
  return Object.fromEntries(options.map((item) => [item.value, t(item.labelKey)]))
}

async function fetchData() {
  loading.value = true
  try {
    const result = (await bannerApi.bannerList(query)) as Record<string, any>
    data.value = result.records || []
    recordCount.value = Number(result[countField] || 0)
    query.current = result.current
    query.size = result.size
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.title = ''
  query.display_scope = undefined
  query.category = undefined
  query.type = undefined
  query.position = undefined
  query.status = undefined
  query.current = 1
  fetchData()
}

function openCreate() {
  formRef.value?.onOpen()
}

function openEdit(record: Record<string, any>) {
  formRef.value?.onOpen(record)
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('sys.confirmDeleteBanner'),
    content: t('sys.deleteBannerContent', { count: ids.length }),
    okText: t('common.delete'),
    okType: 'danger',
    cancelText: t('common.cancel'),
    async onOk() {
      await bannerApi.bannerDelete({ ids })
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

onMounted(fetchData)

const rowSelection = computed(() => ({
  fixed: true,
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: any[]) => {
    selectedRowKeys.value = keys
  },
}))
</script>

<template>
  <QueryTable>
    <template #search="{ expanded, toggle }">
      <AForm layout="inline" :model="query">
        <ARow :gutter="[48, 16]" class="w-full">
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('sys.title')">
              <AInput v-model:value="query.title" allow-clear :placeholder="t('sys.bannerTitlePlaceholder')" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('sys.displayScope')">
              <ASelect v-model:value="query.display_scope" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in displayScopeOptions" :key="item.value" :value="item.value">
                  {{ t(item.labelKey) }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('sys.position')">
              <ASelect v-model:value="query.position" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in positionOptions" :key="item.value" :value="item.value">
                  {{ t(item.labelKey) }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('sys.category')">
              <ASelect v-model:value="query.category" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">
                  {{ t(item.labelKey) }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('common.type')">
              <ASelect v-model:value="query.type" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption v-for="item in typeOptions" :key="item.value" :value="item.value">
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
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">{{ t('sys.bannerList') }}</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.refresh') }}
        </AButton>
        <AButton type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          {{ t('sys.createBanner') }}
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
      :scroll="{ x: 1450 }"
      row-key="id"
      size="middle"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'serial'">
          {{ data.findIndex((item) => item.id === record.id) + 1 }}
        </template>
        <template v-if="column.key === 'image'">
          <AImage :src="record.image" :width="72" :height="40" class="object-cover" />
        </template>
        <template v-if="column.key === 'category'">
          {{ optionLabelMaps.category.value[record.category] || record.category }}
        </template>
        <template v-if="column.key === 'type'">
          {{ optionLabelMaps.type.value[record.type] || record.type }}
        </template>
        <template v-if="column.key === 'position'">
          {{ optionLabelMaps.position.value[record.position] || record.position }}
        </template>
        <template v-if="column.key === 'display_scope'">
          {{ optionLabelMaps.display_scope.value[record.display_scope] || record.display_scope }}
        </template>
        <template v-if="column.key === 'link_type'">
          {{ optionLabelMaps.link_type.value[record.link_type] || record.link_type }}
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

    <Form ref="formRef" @successful="fetchData" />
  </QueryTable>
</template>
