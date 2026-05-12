# 系统配置管理页面 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create frontend pages for system config management (tabbed form + table CRUD).

**Architecture:** Main `a-card` with 4 tabs: 系统基础配置 (form) / 安全配置 (form) / 文件配置 (left-tab forms) / 其他配置 (table CRUD). All use existing backend APIs.

**Tech Stack:** Vue 3 Composition API / Ant Design Vue / TypeScript

---

### Task 1: API layer — `src/api/config.ts`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\api\config.ts`

```typescript
import { request } from '@/utils'
import type { Service } from '@/utils/http/types'

export function fetchConfigPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/config/page', { params })
}
export function fetchConfigListByCategory(params: { category: string }) {
  return request.Get<Service.ResponseResult<any[]>>('/api/v1/sys/config/list-by-category', { params })
}
export function fetchConfigCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/create', data)
}
export function fetchConfigModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/modify', data)
}
export function fetchConfigRemove(data: { ids: string[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/remove', data)
}
export function fetchConfigDetail(params: { id: string }) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/config/detail', { params })
}
export function fetchConfigEditBatch(data: { configs: { config_key: string; config_value: string }[] }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/config/edit-batch', data)
}
```

### Task 2: Main page — `src/views/sys/config/index.vue`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\index.vue`

```vue
<template>
  <a-card :tab-list="tabList" :active-tab-key="activeKey" @tab-change="(k) => activeKey = k">
    <template #customTab="{ item, event }">
      <span @click="event?.onClick">{{ item.tab }}</span>
    </template>
    <sys-base v-if="activeKey === 'sysBase'" />
    <security v-if="activeKey === 'security'" />
    <file-config v-if="activeKey === 'fileConfig'" />
    <biz-config v-if="activeKey === 'bizConfig'" />
  </a-card>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysConfig' })
import { ref } from 'vue'
import SysBase from './components/sysBase.vue'
import Security from './components/security.vue'
import FileConfig from './components/fileConfig/index.vue'
import BizConfig from './components/bizConfig/index.vue'

const activeKey = ref('sysBase')
const tabList = [
  { key: 'sysBase', tab: '系统基础配置' },
  { key: 'security', tab: '安全配置' },
  { key: 'fileConfig', tab: '文件配置' },
  { key: 'bizConfig', tab: '其他配置' },
]
</script>
```

### Task 3: System base form — `sysBase.vue`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\sysBase.vue`

```vue
<template>
  <a-spin :spinning="loading">
    <a-form layout="vertical">
      <a-alert message="系统基础配置" type="info" show-icon class="mb-4" />
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="默认文件引擎" name="SYS_DEFAULT_FILE_ENGINE">
            <a-select v-model:value="formData.SYS_DEFAULT_FILE_ENGINE">
              <a-select-option value="LOCAL">本地存储</a-select-option>
              <a-select-option value="ALIYUN">阿里云OSS</a-select-option>
              <a-select-option value="MINIO">MinIO</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Snowflake 工作节点ID" name="SYS_SNOWFLAKE_WORKER_ID">
            <a-input-number v-model:value="formData.SYS_SNOWFLAKE_WORKER_ID" :min="0" :max="31" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Snowflake 数据中心ID" name="SYS_SNOWFLAKE_DATACENTER_ID">
            <a-input-number v-model:value="formData.SYS_SNOWFLAKE_DATACENTER_ID" :min="0" :max="31" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="默认密码" name="SYS_DEFAULT_PASSWORD">
            <a-input v-model:value="formData.SYS_DEFAULT_PASSWORD" placeholder="新增用户时使用的默认密码" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="用户初始密码" name="SYS_USER_INIT_PASSWORD">
            <a-input v-model:value="formData.SYS_USER_INIT_PASSWORD" placeholder="用户首次登录密码" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-space class="mt-4">
        <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </a-form>
  </a-spin>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { fetchConfigListByCategory, fetchConfigEditBatch } from '@/api/config'

const loading = ref(false)
const saving = ref(false)
const initialData: Record<string, any> = {}
const formData = reactive<Record<string, any>>({})

function toNumber(val: any): number | undefined {
  if (val === undefined || val === null || val === '') return undefined
  const n = Number(val)
  return isNaN(n) ? undefined : n
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchConfigListByCategory({ category: 'SYS_BASE' })
    if (data) {
      Object.keys(formData).forEach(k => delete formData[k])
      data.forEach((item: any) => {
        formData[item.config_key] = item.config_value
        initialData[item.config_key] = item.config_value
      })
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const configs = Object.entries(formData).map(([key, value]) => ({
      config_key: key,
      config_value: String(value ?? ''),
    }))
    const { success } = await fetchConfigEditBatch({ configs })
    if (success) {
      message.success('保存成功')
      Object.keys(formData).forEach(k => { initialData[k] = formData[k] })
    }
  } finally {
    saving.value = false
  }
}

function handleReset() {
  Object.keys(initialData).forEach(k => {
    formData[k] = initialData[k]
  })
}

onMounted(loadData)
</script>
```

### Task 4: Security config form — `security.vue`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\security.vue`

```vue
<template>
  <a-spin :spinning="loading">
    <a-form layout="vertical">
      <a-alert message="安全配置" type="info" show-icon class="mb-4" />
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="最大登录失败次数" name="SYS_MAX_LOGIN_RETRIES">
            <a-input-number v-model:value="formData.SYS_MAX_LOGIN_RETRIES" :min="0" :max="99" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="登录锁定时间（分钟）" name="SYS_LOGIN_LOCK_MINUTES">
            <a-input-number v-model:value="formData.SYS_LOGIN_LOCK_MINUTES" :min="0" :max="999" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="JWT Token 过期时间（秒）" name="SYS_JWT_TOKEN_EXPIRE">
            <a-input-number v-model:value="formData.SYS_JWT_TOKEN_EXPIRE" :min="0" :max="864000" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-space class="mt-4">
        <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </a-form>
  </a-spin>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { fetchConfigListByCategory, fetchConfigEditBatch } from '@/api/config'

const loading = ref(false)
const saving = ref(false)
const initialData: Record<string, any> = {}
const formData = reactive<Record<string, any>>({})

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchConfigListByCategory({ category: 'SYS_SECURITY' })
    if (data) {
      Object.keys(formData).forEach(k => delete formData[k])
      data.forEach((item: any) => {
        formData[item.config_key] = item.config_value
        initialData[item.config_key] = item.config_value
      })
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const configs = Object.entries(formData).map(([key, value]) => ({
      config_key: key,
      config_value: String(value ?? ''),
    }))
    const { success } = await fetchConfigEditBatch({ configs })
    if (success) {
      message.success('保存成功')
      Object.keys(formData).forEach(k => { initialData[k] = formData[k] })
    }
  } finally {
    saving.value = false
  }
}

function handleReset() {
  Object.keys(initialData).forEach(k => { formData[k] = initialData[k] })
}

onMounted(loadData)
</script>
```

### Task 5: File config container + local form

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\fileConfig\index.vue`

```vue
<template>
  <a-tabs v-model:active-key="activeKey" tab-position="left">
    <a-tab-pane key="local" tab="本地存储">
      <local-form />
    </a-tab-pane>
  </a-tabs>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LocalForm from './localForm.vue'

const activeKey = ref('local')
</script>
```

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\fileConfig\localForm.vue`

```vue
<template>
  <a-spin :spinning="loading">
    <a-form layout="vertical">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12">
          <a-form-item label="本地存储路径（Windows）" name="SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS">
            <a-input v-model:value="formData.SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS" placeholder="D:/hei-file-upload" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12">
          <a-form-item label="本地存储路径（Unix）" name="SYS_FILE_LOCAL_FOLDER_FOR_UNIX">
            <a-input v-model:value="formData.SYS_FILE_LOCAL_FOLDER_FOR_UNIX" placeholder="/data/hei-file-upload" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-space class="mt-4">
        <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </a-form>
  </a-spin>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { fetchConfigListByCategory, fetchConfigEditBatch } from '@/api/config'

const loading = ref(false)
const saving = ref(false)
const initialData: Record<string, any> = {}
const formData = reactive<Record<string, any>>({})

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchConfigListByCategory({ category: 'FILE_LOCAL' })
    if (data) {
      Object.keys(formData).forEach(k => delete formData[k])
      data.forEach((item: any) => {
        formData[item.config_key] = item.config_value
        initialData[item.config_key] = item.config_value
      })
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const configs = Object.entries(formData).map(([key, value]) => ({
      config_key: key,
      config_value: String(value ?? ''),
    }))
    const { success } = await fetchConfigEditBatch({ configs })
    if (success) {
      message.success('保存成功')
      Object.keys(formData).forEach(k => { initialData[k] = formData[k] })
    }
  } finally {
    saving.value = false
  }
}

function handleReset() {
  Object.keys(initialData).forEach(k => { formData[k] = initialData[k] })
}

onMounted(loadData)
</script>
```

### Task 6: Biz config table CRUD — `bizConfig/index.vue`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\bizConfig\index.vue`

```vue
<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="配置键/备注" allow-clear />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      :columns="columns"
      :fetch-data="fetchConfigPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:config:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增配置
        </a-button>
        <a-button
          v-if="hasPermission('sys:config:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button v-if="hasPermission('sys:config:modify')" type="link" size="small" @click="openEdit(record)">
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:config:remove')"
              title="确定删除该配置？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTable>

    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { fetchConfigPage, fetchConfigRemove } from '@/api/config'
import { confirmDelete } from '@/utils'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import DetailDrawer from './detail.vue'
import FormDrawer from './form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const tableRef = ref()

const searchForm = reactive({ keyword: '' })
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => { selectedKeys.value = keys },
}))

const columns = [
  { title: '配置键', dataIndex: 'config_key', key: 'config_key', width: 250, ellipsis: true },
  { title: '配置值', dataIndex: 'config_value', key: 'config_value', width: 300, ellipsis: true },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 200, ellipsis: true },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) { detailRef.value?.doOpen(record) }
function openEdit(record: any) { formRef.value?.doOpen(record) }
function openCreate() { formRef.value?.doOpen() }

async function handleDelete(id: string) {
  const { success } = await fetchConfigRemove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    tableRef.value?.refresh()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '配置',
    selectedKeys: selectedKeys.value,
    deleteApi: fetchConfigRemove,
    onSuccess: () => {
      selectedKeys.value = []
      tableRef.value?.refresh()
    },
  })
}

function handleFormSuccess() { tableRef.value?.refresh() }
function handleSearch() { tableRef.value?.refresh(true) }
function resetSearch() {
  searchForm.keyword = ''
  tableRef.value?.refresh(true)
}
</script>
```

### Task 7: Biz config form drawer — `bizConfig/form.vue`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\bizConfig\form.vue`

```vue
<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑配置' : '新增配置'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="配置键" name="config_key" :rules="[{ required: true, message: '请输入配置键' }]">
        <a-input v-model:value="form.config_key" placeholder="请输入配置键" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="配置值" name="config_value" :rules="[{ required: true, message: '请输入配置值' }]">
        <a-input v-model:value="form.config_value" placeholder="请输入配置值" />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-input v-model:value="form.remark" placeholder="备注说明" />
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchConfigDetail, fetchConfigCreate, fetchConfigModify } from '@/api/config'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  config_key: '',
  config_value: '',
  remark: '',
  sort_code: 0,
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchConfigDetail({ id: row.id })
    if (data) Object.assign(form, data)
  } else {
    isEdit.value = false
    currentId.value = null
    Object.assign(form, initialForm())
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await fetchConfigModify({ ...f, id: currentId.value })
  } else {
    return await fetchConfigCreate({ ...f, category: 'BIZ_DEFINE' })
  }
}

function handleClose() { emit('update:open', false) }

defineExpose({ doOpen })
</script>
```

### Task 8: Biz config detail drawer — `bizConfig/detail.vue`

**File:** Create `E:\DevProjects\hei\hei-admin-vue\src\views\sys\config\components\bizConfig\detail.vue`

```vue
<template>
  <a-drawer :open="open" title="配置详情" placement="right" :width="480" @close="handleClose">
    <a-spin :spinning="loading">
      <template v-if="data">
        <a-card title="基本信息" size="small" class="mb-3">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="配置键">{{ data.config_key || '-' }}</a-descriptions-item>
            <a-descriptions-item label="配置值">
              <div class="whitespace-pre-wrap break-all">{{ data.config_value || '-' }}</div>
            </a-descriptions-item>
            <a-descriptions-item label="分类">{{ data.category || '-' }}</a-descriptions-item>
            <a-descriptions-item label="备注">{{ data.remark || '-' }}</a-descriptions-item>
            <a-descriptions-item label="排序">{{ data.sort_code ?? '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
        <a-card title="系统信息" size="small">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="创建时间">{{ data.created_at || '-' }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ data.updated_at || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </template>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { fetchConfigDetail } from '@/api/config'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  try {
    const res = await fetchConfigDetail({ id: row.id })
    data.value = res?.data || null
  } finally {
    loading.value = false
  }
  emit('update:open', true)
}

function handleClose() { emit('update:open', false) }

defineExpose({ doOpen })
</script>
```

## Self-Review Checklist

- [x] Every spec requirement has a corresponding task
  - Tab 系统基础配置 → Task 3
  - Tab 安全配置 → Task 4
  - Tab 文件配置 → Task 5
  - Tab 其他配置 → Tasks 6-8
  - API layer → Task 1
  - Main index.vue → Task 2
- [x] No placeholders or TODOs
- [x] Type consistency across tasks
- [x] All API URLs use kebab-case (list-by-category, edit-batch)
- [x] Follows existing project patterns (AppTable, AppDrawerForm, AppSearchPanel)
