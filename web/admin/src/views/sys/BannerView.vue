<script setup lang="ts">
import { DeleteOutlined, DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import dayjs, { type Dayjs } from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import {
  createBanner,
  deleteBanners,
  getBannerDetail,
  listBanners,
  updateBanner,
  type BannerPayload,
} from '@/apis/sys'
import StatusTag from '@/components/common/StatusTag.vue'
import QueryTable from '@/components/pro/QueryTable.vue'
import type { SysBannerItem } from '@/types/api'
import { formatDateTime } from '@hei/shared'

interface OptionItem {
  labelKey: string
  value: string
}

type BannerFormModel = Omit<BannerPayload, 'start_at' | 'end_at'> & {
  id?: string
  url?: string
  summary?: string
  description?: string
  active_time?: [Dayjs, Dayjs]
}

const { t } = useI18n()

const statusOptions: OptionItem[] = [
  { labelKey: 'sys.options.enabled', value: 'ENABLED' },
  { labelKey: 'sys.options.disabled', value: 'DISABLED' },
]
const categoryOptions: OptionItem[] = [
  { labelKey: 'sys.options.home', value: 'HOME' },
  { labelKey: 'sys.options.login', value: 'LOGIN' },
  { labelKey: 'sys.options.workplace', value: 'WORKPLACE' },
  { labelKey: 'sys.options.notice', value: 'NOTICE' },
  { labelKey: 'sys.options.adminDashboard', value: 'ADMIN_DASHBOARD' },
  { labelKey: 'sys.options.systemUpgrade', value: 'SYSTEM_UPGRADE' },
]
const typeOptions: OptionItem[] = [
  { labelKey: 'sys.options.carousel', value: 'CAROUSEL' },
  { labelKey: 'sys.options.hero', value: 'HERO' },
  { labelKey: 'sys.options.noticeBar', value: 'NOTICE' },
  { labelKey: 'sys.options.card', value: 'CARD' },
  { labelKey: 'sys.options.popup', value: 'POPUP' },
  { labelKey: 'sys.options.sidebar', value: 'SIDEBAR' },
]
const positionOptions: OptionItem[] = [
  { labelKey: 'sys.options.homeTop', value: 'HOME_TOP' },
  { labelKey: 'sys.options.homeMiddle', value: 'HOME_MIDDLE' },
  { labelKey: 'sys.options.homeBottom', value: 'HOME_BOTTOM' },
  { labelKey: 'sys.options.loginSide', value: 'LOGIN_SIDE' },
  { labelKey: 'sys.options.workplaceTop', value: 'WORKPLACE_TOP' },
  { labelKey: 'sys.options.noticeArea', value: 'NOTICE_AREA' },
  { labelKey: 'sys.options.adminTop', value: 'ADMIN_TOP' },
  { labelKey: 'sys.options.adminSidebar', value: 'ADMIN_SIDEBAR' },
]
const displayScopeOptions: OptionItem[] = [
  { labelKey: 'sys.options.portal', value: 'PORTAL' },
  { labelKey: 'sys.options.admin', value: 'ADMIN' },
  { labelKey: 'sys.options.app', value: 'APP' },
]
const linkTypeOptions: OptionItem[] = [
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
const saving = ref(false)
const drawerOpen = ref(false)
const selectedRowKeys = ref<Key[]>([])
const query = reactive({
  title: '',
  display_scope: undefined as string | undefined,
  category: undefined as string | undefined,
  type: undefined as string | undefined,
  position: undefined as string | undefined,
  status: undefined as string | undefined,
  page: 1,
  page_size: 10,
})
const data = ref<SysBannerItem[]>([])
const total = ref(0)
const form = reactive<BannerFormModel>(createEmptyForm())

const columns = computed<TableColumnsType<SysBannerItem>>(() => [
  { title: '#', key: 'serial', fixed: 'left', width: 70 },
  { title: t('sys.title'), dataIndex: 'title', key: 'title', fixed: 'left', width: 180 },
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
  { title: t('common.actions'), key: 'actions', fixed: 'right', width: 150 },
])

function toLabelMap(options: OptionItem[]) {
  return Object.fromEntries(options.map((item) => [item.value, t(item.labelKey)]))
}

function createEmptyForm(): BannerFormModel {
  return {
    title: '',
    image: '',
    url: '',
    link_type: 'URL',
    summary: '',
    description: '',
    category: 'HOME',
    type: 'CAROUSEL',
    position: 'HOME_TOP',
    display_scope: 'PORTAL',
    sort: 0,
    status: 'ENABLED',
    active_time: undefined,
  }
}

function resetForm() {
  Object.assign(form, createEmptyForm())
}

function normalizeText(value?: string | null) {
  return value?.trim() || null
}

function toPayload(): BannerPayload {
  const [startAt, endAt] = form.active_time || []
  return {
    id: form.id,
    title: form.title.trim(),
    image: form.image.trim(),
    url: normalizeText(form.url),
    link_type: form.link_type,
    summary: normalizeText(form.summary),
    description: normalizeText(form.description),
    category: form.category,
    type: form.type,
    position: form.position,
    display_scope: form.display_scope,
    sort: Number(form.sort) || 0,
    status: form.status,
    start_at: startAt ? startAt.toISOString() : null,
    end_at: endAt ? endAt.toISOString() : null,
  }
}

function asBannerRecord(record: unknown) {
  return record as SysBannerItem
}

async function fetchData() {
  loading.value = true
  try {
    const result = await listBanners(query)
    data.value = result.items
    total.value = result.total
    query.page = result.page
    query.page_size = result.page_size
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
  query.page = 1
  fetchData()
}

function openCreate() {
  resetForm()
  drawerOpen.value = true
}

async function openEdit(record: SysBannerItem) {
  resetForm()
  drawerOpen.value = true
  const detail = await getBannerDetail(record.id)
  Object.assign(form, {
    ...detail,
    url: detail.url || '',
    summary: detail.summary || '',
    description: detail.description || '',
    active_time: detail.start_at && detail.end_at ? [dayjs(detail.start_at), dayjs(detail.end_at)] : undefined,
  })
}

async function save() {
  if (!form.title.trim() || !form.image.trim()) {
    message.warning(t('sys.bannerRequired'))
    return
  }

  saving.value = true
  try {
    const payload = toPayload()
    if (payload.id) {
      await updateBanner(payload as BannerPayload & { id: string })
      message.success(t('sys.bannerUpdated'))
    } else {
      await createBanner(payload)
      message.success(t('sys.bannerCreated'))
    }
    drawerOpen.value = false
    await fetchData()
  } finally {
    saving.value = false
  }
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('sys.confirmDeleteBanner'),
    content: t('sys.deleteBannerContent', { count: ids.length }),
    okText: t('common.delete'),
    okType: 'danger',
    cancelText: t('common.cancel'),
    async onOk() {
      await deleteBanners(ids)
      selectedRowKeys.value = selectedRowKeys.value.filter((key) => !ids.includes(String(key)))
      message.success(t('sys.deleteSuccess'))
      await fetchData()
    },
  })
}

function handleTableChange(pagination: { current?: number; pageSize?: number }) {
  query.page = pagination.current || 1
  query.page_size = pagination.pageSize || 10
  fetchData()
}

onMounted(fetchData)

const rowSelection = computed<TableRowSelection<SysBannerItem>>(() => ({
  fixed: true,
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
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
      :pagination="{ current: query.page, pageSize: query.page_size, total }"
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
            <AButton size="small" type="link" @click="openEdit(asBannerRecord(record))">{{ t('common.edit') }}</AButton>
            <AButton danger size="small" type="link" @click="confirmDelete([asBannerRecord(record).id])">{{ t('common.delete') }}</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" :title="form.id ? t('sys.editBanner') : t('sys.createBanner')" width="620">
      <AForm layout="vertical" :model="form">
        <AFormItem :label="t('sys.title')" required><AInput v-model:value="form.title" :placeholder="t('sys.bannerTitlePlaceholder')" /></AFormItem>
        <AFormItem :label="t('sys.imageUrl')" required><AInput v-model:value="form.image" placeholder="https://example.com/banner.png" /></AFormItem>
        <AFormItem :label="t('sys.url')"><AInput v-model:value="form.url" :placeholder="t('sys.urlPlaceholder')" /></AFormItem>
        <AFormItem :label="t('sys.linkType')">
          <ASelect v-model:value="form.link_type">
            <ASelectOption v-for="item in linkTypeOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
          </ASelect>
        </AFormItem>
        <AFormItem :label="t('sys.summary')"><AInput v-model:value="form.summary" /></AFormItem>
        <AFormItem :label="t('sys.description')"><ATextarea v-model:value="form.description" :rows="3" /></AFormItem>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem :label="t('sys.category')">
              <ASelect v-model:value="form.category">
                <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem :label="t('common.type')">
              <ASelect v-model:value="form.type">
                <ASelectOption v-for="item in typeOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
        </ARow>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem :label="t('sys.position')">
              <ASelect v-model:value="form.position">
                <ASelectOption v-for="item in positionOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem :label="t('sys.displayScope')">
              <ASelect v-model:value="form.display_scope">
                <ASelectOption v-for="item in displayScopeOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
        </ARow>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem :label="t('sys.sort')"><AInputNumber v-model:value="form.sort" class="w-full" :min="0" /></AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem :label="t('common.status')">
              <ASelect v-model:value="form.status">
                <ASelectOption v-for="item in statusOptions" :key="item.value" :value="item.value">{{ t(item.labelKey) }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
        </ARow>
        <AFormItem :label="t('sys.activeTime')">
          <ARangePicker v-model:value="form.active_time" class="w-full" show-time />
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
