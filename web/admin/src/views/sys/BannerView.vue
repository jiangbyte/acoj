<script setup lang="ts">
import { DeleteOutlined, DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import dayjs, { type Dayjs } from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'

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
  label: string
  value: string
}

type BannerFormModel = Omit<BannerPayload, 'start_at' | 'end_at'> & {
  id?: string
  url?: string
  summary?: string
  description?: string
  active_time?: [Dayjs, Dayjs]
}

const statusOptions: OptionItem[] = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
]
const categoryOptions: OptionItem[] = [
  { label: '首页', value: 'HOME' },
  { label: '登录页', value: 'LOGIN' },
  { label: '工作台', value: 'WORKPLACE' },
  { label: '公告', value: 'NOTICE' },
  { label: '管理控制台', value: 'ADMIN_DASHBOARD' },
  { label: '系统升级', value: 'SYSTEM_UPGRADE' },
]
const typeOptions: OptionItem[] = [
  { label: '轮播', value: 'CAROUSEL' },
  { label: '横幅', value: 'HERO' },
  { label: '公告条', value: 'NOTICE' },
  { label: '卡片', value: 'CARD' },
  { label: '弹窗', value: 'POPUP' },
  { label: '侧栏', value: 'SIDEBAR' },
]
const positionOptions: OptionItem[] = [
  { label: '首页顶部', value: 'HOME_TOP' },
  { label: '首页中部', value: 'HOME_MIDDLE' },
  { label: '首页底部', value: 'HOME_BOTTOM' },
  { label: '登录页侧栏', value: 'LOGIN_SIDE' },
  { label: '工作台顶部', value: 'WORKPLACE_TOP' },
  { label: '公告区域', value: 'NOTICE_AREA' },
  { label: '管理端顶部', value: 'ADMIN_TOP' },
  { label: '管理端侧栏', value: 'ADMIN_SIDEBAR' },
]
const displayScopeOptions: OptionItem[] = [
  { label: '门户端', value: 'PORTAL' },
  { label: '管理端', value: 'ADMIN' },
  { label: '移动端', value: 'APP' },
]
const linkTypeOptions: OptionItem[] = [
  { label: '外链', value: 'URL' },
  { label: '路由', value: 'ROUTE' },
  { label: '无跳转', value: 'NONE' },
]

const optionLabelMaps = {
  category: toLabelMap(categoryOptions),
  type: toLabelMap(typeOptions),
  position: toLabelMap(positionOptions),
  display_scope: toLabelMap(displayScopeOptions),
  link_type: toLabelMap(linkTypeOptions),
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

const columns: TableColumnsType<SysBannerItem> = [
  { title: '#', key: 'serial', fixed: 'left', width: 70 },
  { title: '标题', dataIndex: 'title', key: 'title', fixed: 'left', width: 180 },
  { title: '图片', dataIndex: 'image', key: 'image', width: 120 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 110 },
  { title: '位置', dataIndex: 'position', key: 'position', width: 140 },
  { title: '显示端', dataIndex: 'display_scope', key: 'display_scope', width: 110 },
  { title: '链接类型', dataIndex: 'link_type', key: 'link_type', width: 110 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 90 },
  { title: '交互数', dataIndex: 'interaction_count', key: 'interaction_count', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updated_at', key: 'updated_at', width: 160 },
  { title: '操作', key: 'actions', fixed: 'right', width: 150 },
]

function toLabelMap(options: OptionItem[]) {
  return Object.fromEntries(options.map((item) => [item.value, item.label]))
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
    message.warning('请填写标题和图片地址')
    return
  }

  saving.value = true
  try {
    const payload = toPayload()
    if (payload.id) {
      await updateBanner(payload as BannerPayload & { id: string })
      message.success('Banner 已更新')
    } else {
      await createBanner(payload)
      message.success('Banner 已创建')
    }
    drawerOpen.value = false
    await fetchData()
  } finally {
    saving.value = false
  }
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: '确认删除 Banner？',
    content: `将删除 ${ids.length} 条 Banner，删除后不可恢复。`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteBanners(ids)
      selectedRowKeys.value = selectedRowKeys.value.filter((key) => !ids.includes(String(key)))
      message.success('删除成功')
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
            <AFormItem label="标题">
              <AInput v-model:value="query.title" allow-clear placeholder="请输入 Banner 标题" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem label="显示端">
              <ASelect v-model:value="query.display_scope" allow-clear placeholder="请选择">
                <ASelectOption v-for="item in displayScopeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem label="位置">
              <ASelect v-model:value="query.position" allow-clear placeholder="请选择">
                <ASelectOption v-for="item in positionOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="分类">
              <ASelect v-model:value="query.category" allow-clear placeholder="请选择">
                <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="类型">
              <ASelect v-model:value="query.type" allow-clear placeholder="请选择">
                <ASelectOption v-for="item in typeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="状态">
              <ASelect v-model:value="query.status" allow-clear placeholder="请选择">
                <ASelectOption v-for="item in statusOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :md="expanded ? 24 : 8" :sm="24">
            <span class="inline-flex flex-wrap gap-2" :class="{ 'is-expanded': expanded }">
              <AButton type="link" @click="toggle">
                {{ expanded ? '收起' : '展开' }}
                <UpOutlined v-if="expanded" />
                <DownOutlined v-else />
              </AButton>
              <AButton type="primary" @click="fetchData">查询</AButton>
              <AButton class="ml-2" @click="resetQuery">重置</AButton>
            </span>
          </ACol>
        </ARow>
      </AForm>
    </template>

    <template #toolbar>
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">Banner 列表</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </AButton>
        <AButton type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新建 Banner
        </AButton>
        <AButton v-if="selectedRowKeys.length > 0" danger @click="confirmDelete(selectedRowKeys.map(String))">
          <template #icon><DeleteOutlined /></template>
          批量删除
        </AButton>
      </ASpace>
    </template>

    <template #alert>
      <AAlert v-if="selectedRowKeys.length > 0" show-icon type="info">
        <template #message>
          已选择 <a>{{ selectedRowKeys.length }}</a> 项
          <a class="ml-3" @click="selectedRowKeys = []">清空</a>
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
          {{ optionLabelMaps.category[record.category] || record.category }}
        </template>
        <template v-if="column.key === 'type'">
          {{ optionLabelMaps.type[record.type] || record.type }}
        </template>
        <template v-if="column.key === 'position'">
          {{ optionLabelMaps.position[record.position] || record.position }}
        </template>
        <template v-if="column.key === 'display_scope'">
          {{ optionLabelMaps.display_scope[record.display_scope] || record.display_scope }}
        </template>
        <template v-if="column.key === 'link_type'">
          {{ optionLabelMaps.link_type[record.link_type] || record.link_type }}
        </template>
        <template v-if="column.key === 'status'">
          <StatusTag :status="record.status" />
        </template>
        <template v-if="column.key === 'updated_at'">
          {{ formatDateTime(record.updated_at) }}
        </template>
        <template v-if="column.key === 'actions'">
          <span class="inline-flex flex-wrap gap-2">
            <AButton size="small" type="link" @click="openEdit(asBannerRecord(record))">编辑</AButton>
            <AButton danger size="small" type="link" @click="confirmDelete([asBannerRecord(record).id])">删除</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" :title="form.id ? '编辑 Banner' : '新建 Banner'" width="620">
      <AForm layout="vertical" :model="form">
        <AFormItem label="标题" required><AInput v-model:value="form.title" placeholder="请输入 Banner 标题" /></AFormItem>
        <AFormItem label="图片地址" required><AInput v-model:value="form.image" placeholder="https://example.com/banner.png" /></AFormItem>
        <AFormItem label="跳转地址"><AInput v-model:value="form.url" placeholder="URL 或路由地址" /></AFormItem>
        <AFormItem label="链接类型">
          <ASelect v-model:value="form.link_type">
            <ASelectOption v-for="item in linkTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</ASelectOption>
          </ASelect>
        </AFormItem>
        <AFormItem label="摘要"><AInput v-model:value="form.summary" /></AFormItem>
        <AFormItem label="描述"><ATextarea v-model:value="form.description" :rows="3" /></AFormItem>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem label="分类">
              <ASelect v-model:value="form.category">
                <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem label="类型">
              <ASelect v-model:value="form.type">
                <ASelectOption v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
        </ARow>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem label="位置">
              <ASelect v-model:value="form.position">
                <ASelectOption v-for="item in positionOptions" :key="item.value" :value="item.value">{{ item.label }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem label="显示端">
              <ASelect v-model:value="form.display_scope">
                <ASelectOption v-for="item in displayScopeOptions" :key="item.value" :value="item.value">{{ item.label }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
        </ARow>
        <ARow :gutter="16">
          <ACol :span="12">
            <AFormItem label="排序"><AInputNumber v-model:value="form.sort" class="w-full" :min="0" /></AFormItem>
          </ACol>
          <ACol :span="12">
            <AFormItem label="状态">
              <ASelect v-model:value="form.status">
                <ASelectOption v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
        </ARow>
        <AFormItem label="展示时间">
          <ARangePicker v-model:value="form.active_time" class="w-full" show-time />
        </AFormItem>
      </AForm>
      <template #footer>
        <ASpace>
          <AButton @click="drawerOpen = false">取消</AButton>
          <AButton type="primary" :loading="saving" @click="save">保存</AButton>
        </ASpace>
      </template>
    </ADrawer>
  </QueryTable>
</template>
