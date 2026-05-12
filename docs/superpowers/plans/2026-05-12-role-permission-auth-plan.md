# Role, Permission & Authorization Management — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add permission CRUD page, role/user/group authorization grant drawers, and wire up all grant APIs.

**Architecture:** Follows existing page patterns (AppTable/AppSearchPanel/AppDrawerForm/AppTreeTable). Each grant function is a standalone drawer component imported by the parent list page. API layer uses existing `request.Post/Get` wrapper.

**Tech Stack:** Vue 3 (Composition API + `<script setup>`), Ant Design Vue, Pinia, TypeScript

---

## File Structure

### New files (9):
- `src/api/permission.ts` — Permission API calls
- `src/views/sys/permission/index.vue` — Permission list page
- `src/views/sys/permission/components/form.vue` — Permission create/edit drawer
- `src/views/sys/permission/components/detail.vue` — Permission detail drawer
- `src/views/sys/role/components/grantPermission.vue` — Role grant permission drawer
- `src/views/sys/role/components/grantResource.vue` — Role grant resource drawer
- `src/views/sys/user/components/grantRole.vue` — User grant role drawer
- `src/views/sys/user/components/grantGroup.vue` — User grant group drawer
- `src/views/sys/group/components/grantRole.vue` — Group grant role drawer

### Modified files (5):
- `src/api/user.ts` — Add grant-group + own-groups endpoints
- `src/api/group.ts` — Add grant-role + own-roles endpoints
- `src/views/sys/role/index.vue` — Add 「授权」dropdown menu
- `src/views/sys/user/index.vue` — Add 「授权」dropdown menu
- `src/views/sys/group/index.vue` — Add 「授权」dropdown menu

---

### Task 1: Create permission API file

**Files:**
- Create: `src/api/permission.ts`

- [ ] **Create `src/api/permission.ts`**

```typescript
import { request } from '@/utils'

export function fetchPermissionPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/permission/page', {
    params,
  })
}
export function fetchPermissionCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/create', data)
}
export function fetchPermissionModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/modify', data)
}
export function fetchPermissionRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/remove', data)
}
export function fetchPermissionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/permission/detail', { params })
}
export function fetchPermissionExport(params: any) {
  return request.Get('/api/v1/sys/permission/export', {
    params,
    meta: { isBlob: true },
  }) as Promise<Blob>
}
export function fetchPermissionTemplate() {
  return request.Get('/api/v1/sys/permission/template', {
    meta: { isBlob: true },
  }) as Promise<Blob>
}
export function fetchPermissionImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.Post<Service.ResponseResult>('/api/v1/sys/permission/import', formData)
}
export function fetchPermissionModules() {
  return request.Get<Service.ResponseResult<string[]>>('/api/v1/sys/permission/modules')
}
export function fetchPermissionByModule(params: { module: string }) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/permission/by-module', { params })
}
```

---

### Task 2: Create permission management page

**Files:**
- Create: `src/views/sys/permission/index.vue`
- Create: `src/views/sys/permission/components/form.vue`
- Create: `src/views/sys/permission/components/detail.vue`

Following the exact pattern of `src/views/sys/role/`.

- [ ] **Create `src/views/sys/permission/components/detail.vue`**

```vue
<template>
  <a-drawer
    :open="open"
    title="权限详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">权限名称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">权限编码</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.code || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">所属模块</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.module || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">权限分类</div>
            <div class="text-sm">
              <a-tag>{{ data.category === 'BACKEND' ? '后端权限' : '前端权限' }}</a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.sort_code ?? '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm">
              <a-tag :color="data.status === 'ENABLED' ? 'green' : 'red'">
                {{ data.status === 'ENABLED' ? '启用' : '禁用' }}
              </a-tag>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_at || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.updated_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.updated_at || '-' }}</div>
          </a-col>
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'PermissionDetail' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

function doOpen(row: any) {
  loading.value = true
  data.value = null
  try {
    data.value = row
    emit('update:open', true)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

- [ ] **Create `src/views/sys/permission/components/form.vue`**

```vue
<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑权限' : '新增权限'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="权限编码" name="code" :rules="[{ required: true, message: '请输入权限编码' }]">
        <a-input v-model:value="form.code" placeholder="如 sys:user:page" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="权限名称" name="name" :rules="[{ required: true, message: '请输入权限名称' }]">
        <a-input v-model:value="form.name" placeholder="请输入权限名称" />
      </a-form-item>
      <a-form-item label="所属模块" name="module">
        <a-input v-model:value="form.module" placeholder="如 sys:user" />
      </a-form-item>
      <a-form-item label="权限分类" name="category" :rules="[{ required: true, message: '请选择权限分类' }]">
        <a-select v-model:value="form.category" placeholder="请选择权限分类">
          <a-select-option value="BACKEND">后端权限</a-select-option>
          <a-select-option value="FRONTEND">前端权限</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-select v-model:value="form.status" placeholder="请选择状态">
          <a-select-option value="ENABLED">启用</a-select-option>
          <a-select-option value="DISABLED">禁用</a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
defineOptions({ name: 'PermissionForm' })
import { reactive, ref } from 'vue'
import { fetchPermissionDetail, fetchPermissionCreate, fetchPermissionModify } from '@/api/permission'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  code: '',
  name: '',
  module: '',
  category: 'BACKEND',
  sort_code: 0,
  status: 'ENABLED',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchPermissionDetail({ id: row.id })
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
    return await fetchPermissionModify({ ...f, id: currentId.value })
  } else {
    return await fetchPermissionCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

- [ ] **Create `src/views/sys/permission/index.vue`**

```vue
<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="权限名称/编码" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="模块" name="module">
          <a-input v-model:value="searchForm.module" placeholder="所属模块" allow-clear />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      :columns="columns"
      :fetch-data="fetchPermissionPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:permission:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增权限
        </a-button>
        <a-button
          v-if="hasPermission('sys:permission:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button v-if="hasPermission('sys:permission:import')" @click="importOpen = true">
          <template #icon><UploadOutlined /></template>
          导入
        </a-button>
        <a-button v-if="hasPermission('sys:permission:export')" @click="exportOpen = true">
          <template #icon><DownloadOutlined /></template>
          导出
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'category'">
          <a-tag>{{ record.category === 'BACKEND' ? '后端权限' : '前端权限' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
            {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="hasPermission('sys:permission:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:permission:remove')"
              title="确定删除该权限？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTable>

    <AppImportModal
      ref="importModalRef"
      :open="importOpen"
      template-text="下载权限导入模板"
      :template-loading="templateLoading"
      @close="importOpen = false"
      @download-template="handleDownloadTemplate"
      @upload="handleImport"
    />

    <AppExportModal
      :open="exportOpen"
      :selected-keys="selectedKeys"
      @close="exportOpen = false"
      @export="handleExportWithParams"
    />

    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysPermission' })
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import {
  fetchPermissionPage,
  fetchPermissionRemove,
  fetchPermissionExport,
  fetchPermissionTemplate,
  fetchPermissionImport,
} from '@/api/permission'
import { downloadBlob, confirmDelete } from '@/utils'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const tableRef = ref()

// ── Search ──
const searchForm = reactive({
  keyword: '',
  module: undefined as string | undefined,
})
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => { selectedKeys.value = keys },
}))

const columns = [
  { title: '权限名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '权限编码', dataIndex: 'code', key: 'code', width: 200, ellipsis: true },
  { title: '所属模块', dataIndex: 'module', key: 'module', width: 150 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 100 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
]

// ── Drawers ──
const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) { detailRef.value?.doOpen(record) }
function openEdit(record: any) { formRef.value?.doOpen(record) }
function openCreate() { formRef.value?.doOpen() }

async function handleDelete(id: string) {
  const { success } = await fetchPermissionRemove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    tableRef.value?.refresh()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '权限',
    selectedKeys: selectedKeys.value,
    deleteApi: fetchPermissionRemove,
    onSuccess: () => {
      selectedKeys.value = []
      tableRef.value?.refresh()
    },
  })
}

function handleFormSuccess() {
  tableRef.value?.refresh()
}

function handleSearch() { tableRef.value?.refresh(true) }

function resetSearch() {
  searchForm.keyword = ''
  searchForm.module = undefined
  tableRef.value?.refresh(true)
}

// ── Import / Export / Template ──
const importOpen = ref(false)
const exportOpen = ref(false)
const templateLoading = ref(false)
const importModalRef = ref()

async function handleDownloadTemplate() {
  templateLoading.value = true
  try {
    const blob = await fetchPermissionTemplate()
    downloadBlob(blob, '权限导入模板.xlsx')
  } catch { message.error('下载模板失败') }
  finally { templateLoading.value = false }
}

async function handleExportWithParams(params: any) {
  try {
    const blob = await fetchPermissionExport(params)
    downloadBlob(blob, `权限数据_${new Date().toLocaleDateString()}.xlsx`)
    message.success('导出成功')
    exportOpen.value = false
  } catch { message.error('导出失败') }
}

async function handleImport(file: File) {
  try {
    const { success, data } = await fetchPermissionImport(file)
    if (success && data) {
      importModalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
      message.success('导入成功')
      tableRef.value?.refresh(true)
    }
  } catch {
    importModalRef.value?.setResult({ success: false, message: '导入失败，请检查文件格式' })
  }
}
</script>
```

### Task 3: Update user.ts and group.ts API files

**Files:**
- Modify: `src/api/user.ts`
- Modify: `src/api/group.ts`

- [ ] **Add grant-group and own-groups to `src/api/user.ts`**

Append before the closing of the file:

```typescript
export function fetchUserGrantGroup(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-group', data)
}
export function fetchUserOwnGroups(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-groups', { params })
}
```

- [ ] **Add grant-role and own-roles to `src/api/group.ts`**

Append before the closing of the file:

```typescript
export function fetchGroupGrantRole(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/group/grant-role', data)
}
export function fetchGroupOwnRoles(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/group/own-roles', { params })
}
```

### Task 4: Create role grant-permission drawer

**Files:**
- Create: `src/views/sys/role/components/grantPermission.vue`

- [ ] **Create `src/views/sys/role/components/grantPermission.vue`**

```vue
<template>
  <a-drawer
    :open="open"
    title="授权权限"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-alert message="选择权限并为每个权限设置数据范围。数据范围更改后需重新登录方可生效。" type="warning" closable class="mb-3" />

    <a-spin :spinning="loading">
      <!-- Module selector -->
      <a-select
        v-model:value="currentModule"
        placeholder="选择模块"
        allow-clear
        style="width: 240px"
        class="mb-3"
        @change="loadPermissions"
      >
        <a-select-option v-for="m in modules" :key="m" :value="m">{{ m }}</a-select-option>
      </a-select>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        size="small"
        bordered
        :scroll="{ x: 'max-content' }"
      >
        <template #headerCell="{ column }">
          <template v-if="column.key === 'code'">
            <a-checkbox :checked="allChecked" :indeterminate="indeterminate" @change="handleAllCheck">
              权限编码
            </a-checkbox>
          </template>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <a-checkbox :checked="record.checked" @change="(e: any) => handleCheck(record, e.target.checked)">
              {{ record.code }}
            </a-checkbox>
          </template>
          <template v-else-if="column.key === 'name'">
            {{ record.name }}
          </template>
          <template v-else-if="column.key === 'scope'">
            <a-radio-group
              v-if="record.checked"
              :value="record.scope"
              size="small"
              @change="(e: any) => record.scope = e.target.value"
            >
              <a-radio-button value="ALL">全部</a-radio-button>
              <a-radio-button value="SELF">仅自己</a-radio-button>
              <a-radio-button value="ORG">所属组织</a-radio-button>
              <a-radio-button value="ORG_AND_BELOW">组织及以下</a-radio-button>
            </a-radio-group>
            <span v-else class="text-gray-400 text-xs">未选中</span>
          </template>
        </template>
      </a-table>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'RoleGrantPermission' })
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchRoleGrantPermission, fetchRoleOwnPermission } from '@/api/role'
import { fetchPermissionModules, fetchPermissionByModule } from '@/api/permission'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 800))

const currentRoleId = ref('')
const modules = ref<string[]>([])
const currentModule = ref<string | undefined>(undefined)
const allPermissions = ref<any[]>([])
const tableData = ref<any[]>([])
const loading = ref(false)
const submitLoading = ref(false)

const allChecked = computed(() => tableData.value.length > 0 && tableData.value.every(p => p.checked))
const indeterminate = computed(() => {
  const checked = tableData.value.filter(p => p.checked)
  return checked.length > 0 && checked.length < tableData.value.length
})

const columns = [
  { key: 'code', title: '权限编码', dataIndex: 'code', width: 280 },
  { key: 'name', title: '权限名称', dataIndex: 'name', width: 180 },
  { key: 'scope', title: '数据范围', dataIndex: 'scope', width: 400 },
]

async function loadModules() {
  const { data } = await fetchPermissionModules()
  modules.value = data || []
}

async function loadPermissions() {
  if (!currentModule.value) {
    tableData.value = []
    return
  }
  loading.value = true
  try {
    const [permsRes, ownRes] = await Promise.all([
      fetchPermissionByModule({ module: currentModule.value }),
      fetchRoleOwnPermission({ role_id: currentRoleId.value }),
    ])
    const ownIds: string[] = ownRes?.data || []

    tableData.value = (permsRes?.data || []).map((p: any) => ({
      id: p.id,
      code: p.code,
      name: p.name,
      checked: ownIds.includes(p.id),
      scope: 'ALL',
    }))
  } finally {
    loading.value = false
  }
}

function handleAllCheck(e: any) {
  const checked = e.target.checked
  tableData.value.forEach(p => { p.checked = checked })
}

function handleCheck(record: any, checked: boolean) {
  record.checked = checked
}

async function handleSubmit() {
  const checkedItems = tableData.value.filter(p => p.checked)
  submitLoading.value = true
  try {
    const { success } = await fetchRoleGrantPermission({
      role_id: currentRoleId.value,
      permission_ids: checkedItems.map(p => p.id),
    })
    if (success) {
      message.success('授权成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function doOpen(role: any) {
  currentRoleId.value = role.id
  loadModules()
  emit('update:open', true)
}

function handleClose() {
  currentModule.value = undefined
  tableData.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

### Task 5: Create role grant-resource drawer

**Files:**
- Create: `src/views/sys/role/components/grantResource.vue`

- [ ] **Create `src/views/sys/role/components/grantResource.vue`**

```vue
<template>
  <a-drawer
    :open="open"
    title="授权资源"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        size="small"
        bordered
        :scroll="{ x: 'max-content' }"
        :row-key="(r: any) => r.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-checkbox
              v-if="record.type === 'MENU' || record.type === 'DIRECTORY'"
              :checked="record.checked"
              :indeterminate="isIndeterminate(record)"
              @change="(e: any) => handleMenuCheck(record, e.target.checked)"
            >
              {{ record.name }}
            </a-checkbox>
            <span v-else>{{ record.name }}</span>
          </template>
          <template v-else-if="column.key === 'button'">
            <template v-if="record.buttonList?.length">
              <a-checkbox
                v-for="btn in record.buttonList"
                :key="btn.id"
                :checked="btn.checked"
                class="mr-2"
                @change="(e: any) => btn.checked = e.target.checked"
              >
                {{ btn.name }}
              </a-checkbox>
            </template>
          </template>
        </template>
      </a-table>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'RoleGrantResource' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchRoleGrantResource, fetchRoleOwnResource } from '@/api/role'
import { fetchResourceTree } from '@/api/resource'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 800))

const currentRoleId = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const treeData = ref<any[]>([])
const tableData = ref<any[]>([])

const columns = [
  { key: 'name', title: '菜单/目录', dataIndex: 'name', width: 300 },
  { key: 'button', title: '按钮权限', dataIndex: 'button', width: 400 },
]

function flattenTree(nodes: any[], ownIds: string[]): any[] {
  const result: any[] = []
  const traverse = (items: any[], parent?: any) => {
    items?.forEach((node: any) => {
      const buttons = node.children?.filter((c: any) => c.type === 'BUTTON') || []
      const row: any = {
        id: node.id,
        name: node.name,
        type: node.type,
        checked: ownIds.includes(node.id),
        buttonList: buttons.map((b: any) => ({ id: b.id, name: b.name, checked: ownIds.includes(b.id) })),
        children: [],
      }
      result.push(row)
      const menuChildren = node.children?.filter((c: any) => c.type !== 'BUTTON') || []
      if (menuChildren.length > 0) {
        row.children = traverse(menuChildren, row)
      }
    })
  }
  traverse(nodes)
  return result
}

function isIndeterminate(record: any): boolean {
  if (!record.children?.length) return false
  const all = getAllDescendants(record)
  const checked = all.filter((n: any) => n.checked)
  return checked.length > 0 && checked.length < all.length
}

function getAllDescendants(node: any): any[] {
  const result: any[] = []
  const walk = (n: any) => {
    n.children?.forEach((child: any) => {
      result.push(child)
      walk(child)
    })
  }
  walk(node)
  return result
}

function handleMenuCheck(record: any, checked: boolean) {
  record.checked = checked
  const descendants = getAllDescendants(record)
  descendants.forEach((n: any) => { n.checked = checked })
  if (record.buttonList) {
    record.buttonList.forEach((b: any) => { b.checked = checked })
  }
}

async function loadData() {
  loading.value = true
  try {
    const [treeRes, ownRes] = await Promise.all([
      fetchResourceTree(),
      fetchRoleOwnResource({ role_id: currentRoleId.value }),
    ])
    const ownIds: string[] = ownRes?.data || []
    tableData.value = flattenTree(treeRes?.data || [], ownIds)
  } finally {
    loading.value = false
  }
}

function doOpen(role: any) {
  currentRoleId.value = role.id
  loadData()
  emit('update:open', true)
}

async function handleSubmit() {
  const allIds: string[] = []
  const collect = (items: any[]) => {
    items?.forEach((item: any) => {
      if (item.checked) allIds.push(item.id)
      if (item.buttonList) {
        item.buttonList.forEach((b: any) => {
          if (b.checked) allIds.push(b.id)
        })
      }
      collect(item.children)
    })
  }
  collect(tableData.value)

  submitLoading.value = true
  try {
    const { success } = await fetchRoleGrantResource({
      role_id: currentRoleId.value,
      resource_ids: allIds,
    })
    if (success) {
      message.success('授权成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function handleClose() {
  tableData.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

### Task 6: Update role index page — add 授权 dropdown

**Files:**
- Modify: `src/views/sys/role/index.vue`

- [ ] **Update role index page to add 授权 dropdown**

Changes needed:
1. Import grantPermission and grantResource components
2. Add refs and open handlers for both drawers
3. Replace the action column simple buttons with a dropdown menu containing 授权 submenu

In the `<template>`, replace the action column section with:

```vue
<template v-else-if="column.key === 'action'">
  <a-space>
    <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
    <a-button
      v-if="hasPermission('sys:role:modify')"
      type="link"
      size="small"
      @click="openEdit(record)"
    >
      编辑
    </a-button>
    <a-dropdown v-if="hasPermission('sys:role:grantPermission') || hasPermission('sys:role:grantResource')">
      <a-button type="link" size="small">
        授权
        <DownOutlined />
      </a-button>
      <template #overlay>
        <a-menu>
          <a-menu-item v-if="hasPermission('sys:role:grantPermission')" @click="openGrantPermission(record)">
            授权权限
          </a-menu-item>
          <a-menu-item v-if="hasPermission('sys:role:grantResource')" @click="openGrantResource(record)">
            授权资源
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
    <a-popconfirm
      v-if="hasPermission('sys:role:remove')"
      title="确定删除该角色？"
      @confirm="handleDelete(record.id)"
    >
      <a-button type="link" danger size="small">删除</a-button>
    </a-popconfirm>
  </a-space>
</template>
```

Add after `</FormDrawer>` closing tag:

```vue
<GrantPermission ref="grantPermissionRef" v-model:open="grantPermissionOpen" @success="tableRef?.refresh()" />
<GrantResource ref="grantResourceRef" v-model:open="grantResourceOpen" @success="tableRef?.refresh()" />
```

In the `<script>`, add imports:

```typescript
import { DownOutlined } from '@ant-design/icons-vue'
import GrantPermission from './components/grantPermission.vue'
import GrantResource from './components/grantResource.vue'
```

Add these refs and handlers:

```typescript
const grantPermissionRef = ref()
const grantResourceRef = ref()
const grantPermissionOpen = ref(false)
const grantResourceOpen = ref(false)

function openGrantPermission(record: any) {
  grantPermissionRef.value?.doOpen(record)
}
function openGrantResource(record: any) {
  grantResourceRef.value?.doOpen(record)
}
```

### Task 7: Create user grant-role drawer

**Files:**
- Create: `src/views/sys/user/components/grantRole.vue`

- [ ] **Create `src/views/sys/user/components/grantRole.vue`**

```vue
<template>
  <a-drawer
    :open="open"
    title="分配角色"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-transfer
        v-model:target-keys="targetKeys"
        :data-source="dataSource"
        :titles="['可选角色', '已选角色']"
        :render="(item: any) => item.title"
        :row-key="(item: any) => item.key"
        :list-style="{ width: '100%', height: 420 }"
        show-search
        :filter-option="(inputValue: string, item: any) => item.title.includes(inputValue)"
      />
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserGrantRole' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchUserGrantRole, fetchUserOwnRoles } from '@/api/user'
import { fetchRolePage } from '@/api/role'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

const currentUserId = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<any[]>([])
const targetKeys = ref<string[]>([])

async function loadData() {
  loading.value = true
  try {
    const [rolesRes, ownRes] = await Promise.all([
      fetchRolePage({ size: 9999 }),
      fetchUserOwnRoles({ user_id: currentUserId.value }),
    ])
    dataSource.value = (rolesRes?.data?.records || []).map((r: any) => ({
      key: r.id,
      title: `${r.name} (${r.code})`,
    }))
    targetKeys.value = ownRes?.data || []
  } finally {
    loading.value = false
  }
}

function doOpen(user: any) {
  currentUserId.value = user.id
  loadData()
  emit('update:open', true)
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const { success } = await fetchUserGrantRole({
      user_id: currentUserId.value,
      role_ids: targetKeys.value,
    })
    if (success) {
      message.success('分配成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function handleClose() {
  dataSource.value = []
  targetKeys.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

### Task 8: Create user grant-group drawer

**Files:**
- Create: `src/views/sys/user/components/grantGroup.vue`

- [ ] **Create `src/views/sys/user/components/grantGroup.vue`**

```vue
<template>
  <a-drawer
    :open="open"
    title="分配用户组"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-transfer
        v-model:target-keys="targetKeys"
        :data-source="dataSource"
        :titles="['可选用户组', '已选用户组']"
        :render="(item: any) => item.title"
        :row-key="(item: any) => item.key"
        :list-style="{ width: '100%', height: 420 }"
        show-search
        :filter-option="(inputValue: string, item: any) => item.title.includes(inputValue)"
      />
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserGrantGroup' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchUserGrantGroup, fetchUserOwnGroups } from '@/api/user'
import { fetchGroupTree } from '@/api/group'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

const currentUserId = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<any[]>([])
const targetKeys = ref<string[]>([])

function flattenTree(nodes: any[]): any[] {
  const result: any[] = []
  const walk = (items: any[]) => {
    items?.forEach((n: any) => {
      result.push({ key: n.id, title: n.name })
      if (n.children) walk(n.children)
    })
  }
  walk(nodes)
  return result
}

async function loadData() {
  loading.value = true
  try {
    const [groupRes, ownRes] = await Promise.all([
      fetchGroupTree({}),
      fetchUserOwnGroups({ user_id: currentUserId.value }),
    ])
    dataSource.value = flattenTree(groupRes?.data || [])
    targetKeys.value = ownRes?.data || []
  } finally {
    loading.value = false
  }
}

function doOpen(user: any) {
  currentUserId.value = user.id
  loadData()
  emit('update:open', true)
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const { success } = await fetchUserGrantGroup({
      user_id: currentUserId.value,
      group_ids: targetKeys.value,
    })
    if (success) {
      message.success('分配成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function handleClose() {
  dataSource.value = []
  targetKeys.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

### Task 9: Update user index page — add 授权 dropdown

**Files:**
- Modify: `src/views/sys/user/index.vue`

- [ ] **Update user index page**

Changes:
1. Import `DownOutlined` icon
2. Import `GrantRole` and `GrantGroup` components
3. Add drawer refs and open handlers
4. Replace action column with dropdown (same pattern as role page)

Add action column dropdown:

```vue
<template v-else-if="column.key === 'action'">
  <a-space>
    <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
    <a-button
      v-if="hasPermission('sys:user:modify')"
      type="link"
      size="small"
      @click="openEdit(record)"
    >
      编辑
    </a-button>
    <a-dropdown v-if="hasPermission('sys:user:grant-role') || hasPermission('sys:user:grant-group')">
      <a-button type="link" size="small">
        授权
        <DownOutlined />
      </a-button>
      <template #overlay>
        <a-menu>
          <a-menu-item v-if="hasPermission('sys:user:grant-role')" @click="openGrantRole(record)">
            分配角色
          </a-menu-item>
          <a-menu-item v-if="hasPermission('sys:user:grant-group')" @click="openGrantGroup(record)">
            分配用户组
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
    <a-popconfirm
      v-if="hasPermission('sys:user:remove')"
      title="确定删除？"
      @confirm="handleDelete(record.id)"
    >
      <a-button type="link" danger size="small">删除</a-button>
    </a-popconfirm>
  </a-space>
</template>
```

Add drawer components after `</FormDrawer>`:

```vue
<GrantRole ref="grantRoleRef" v-model:open="grantRoleOpen" @success="tableRef?.refresh()" />
<GrantGroup ref="grantGroupRef" v-model:open="grantGroupOpen" @success="tableRef?.refresh()" />
```

Add imports:

```typescript
import { DownOutlined } from '@ant-design/icons-vue'
import GrantRole from './components/grantRole.vue'
import GrantGroup from './components/grantGroup.vue'
```

Add refs and handlers:

```typescript
const grantRoleRef = ref()
const grantGroupRef = ref()
const grantRoleOpen = ref(false)
const grantGroupOpen = ref(false)

function openGrantRole(record: any) { grantRoleRef.value?.doOpen(record) }
function openGrantGroup(record: any) { grantGroupRef.value?.doOpen(record) }
```

### Task 10: Create group grant-role drawer

**Files:**
- Create: `src/views/sys/group/components/grantRole.vue`

- [ ] **Create `src/views/sys/group/components/grantRole.vue`**

Same pattern as user grant-role but uses group APIs.

```vue
<template>
  <a-drawer
    :open="open"
    title="分配角色"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-transfer
        v-model:target-keys="targetKeys"
        :data-source="dataSource"
        :titles="['可选角色', '已选角色']"
        :render="(item: any) => item.title"
        :row-key="(item: any) => item.key"
        :list-style="{ width: '100%', height: 420 }"
        show-search
        :filter-option="(inputValue: string, item: any) => item.title.includes(inputValue)"
      />
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'GroupGrantRole' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchGroupGrantRole, fetchGroupOwnRoles } from '@/api/group'
import { fetchRolePage } from '@/api/role'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

const currentGroupId = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<any[]>([])
const targetKeys = ref<string[]>([])

async function loadData() {
  loading.value = true
  try {
    const [rolesRes, ownRes] = await Promise.all([
      fetchRolePage({ size: 9999 }),
      fetchGroupOwnRoles({ group_id: currentGroupId.value }),
    ])
    dataSource.value = (rolesRes?.data?.records || []).map((r: any) => ({
      key: r.id,
      title: `${r.name} (${r.code})`,
    }))
    targetKeys.value = ownRes?.data || []
  } finally {
    loading.value = false
  }
}

function doOpen(group: any) {
  currentGroupId.value = group.id
  loadData()
  emit('update:open', true)
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const { success } = await fetchGroupGrantRole({
      group_id: currentGroupId.value,
      role_ids: targetKeys.value,
    })
    if (success) {
      message.success('分配成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function handleClose() {
  dataSource.value = []
  targetKeys.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
```

### Task 11: Update group index page — add 授权 dropdown

**Files:**
- Modify: `src/views/sys/group/index.vue`

- [ ] **Update group index page**

Changes:
1. Import `DownOutlined` icon
2. Import `GrantRole` component
3. Add drawer ref and open handler
4. Add 分配角色 to action column

Add action column dropdown:

```vue
<template v-else-if="column.key === 'action'">
  <a-space>
    <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
    <a-button
      v-if="hasPermission('sys:group:create')"
      type="link"
      size="small"
      @click="openCreate(record)"
    >
      新增子级
    </a-button>
    <a-button
      v-if="hasPermission('sys:group:modify')"
      type="link"
      size="small"
      @click="openEdit(record)"
    >
      编辑
    </a-button>
    <a-dropdown v-if="hasPermission('sys:group:grant-role')">
      <a-button type="link" size="small">
        授权
        <DownOutlined />
      </a-button>
      <template #overlay>
        <a-menu>
          <a-menu-item @click="openGrantRole(record)">分配角色</a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
    <a-popconfirm
      v-if="hasPermission('sys:group:remove')"
      title="确定删除该用户组？如有子级将一并删除"
      @confirm="handleDelete(record.id)"
    >
      <a-button type="link" danger size="small">删除</a-button>
    </a-popconfirm>
  </a-space>
</template>
```

Add drawer component after `</FormDrawer>`:

```vue
<GrantRole ref="grantRoleRef" v-model:open="grantRoleOpen" @success="handleFormSuccess" />
```

Add imports:

```typescript
import { DownOutlined } from '@ant-design/icons-vue'
import GrantRole from './components/grantRole.vue'
```

Add refs and handlers:

```typescript
const grantRoleRef = ref()
const grantRoleOpen = ref(false)

function openGrantRole(record: any) { grantRoleRef.value?.doOpen(record) }
```

---

## Self-Review

1. **Spec coverage**: All 6 modules from the spec have corresponding tasks (perm CRUD page, role grant perm, role grant resource, user grant role, user grant group, group grant role). All page modifications are covered.
2. **Placeholder scan**: No TBD/TODO/fill-in-later patterns. Every file has complete code.
3. **Type consistency**: All component names, ref names, API function names are consistent across tasks. `doOpen` exposed via `defineExpose` matches how existing components work.
