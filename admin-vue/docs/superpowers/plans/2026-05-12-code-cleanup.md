# Code Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce codebase redundancy by ~20% — delete dead code, extract CRUD/import-export composables, create API factory, unify duplicated logic

**Architecture:** Seven independent cleanup categories, each with its own files (composables, API factory, tree component). Views get updated to use new abstractions. No behavioral changes — only structural simplification.

**Tech Stack:** Vue 3 + TypeScript + Pinia + Alova + Ant Design Vue

---

### Task 1: Delete Dead Code

**Files:**
- Modify: `src/utils/http/handle.ts` — remove `handleServiceResult`
- Delete: `src/hooks/usePermission.ts`
- Delete: `src/hooks/index.ts`
- Modify: `src/views/sys/user/index.vue` — remove empty `<style scoped></style>` block

- [ ] **Remove handleServiceResult from handle.ts**

```typescript
// Delete lines 45-57 (the handleServiceResult function)
// After: file ends at line 43 with handleBusinessError
```

- [ ] **Delete hooks/usePermission.ts and hooks/index.ts**

```bash
rm src/hooks/usePermission.ts src/hooks/index.ts
```

- [ ] **Remove empty style block from user/index.vue**

```bash
# Remove line 283: <style scoped></style>
```

- [ ] **Run build to verify no breakage**

Run: `npx vue-tsc --noEmit 2>&1 | head -50`
Expected: No type errors

- [ ] **Commit**

```bash
git add -A
git commit -m "refactor: remove dead code (unused handleServiceResult, usePermission hook, empty style)"
```

---

### Task 2: API Factory — createCrudApi

**Files:**
- Create: `src/utils/http/crud.ts`
- Modify: `src/types/service.d.ts` — add `File` param type hint if needed

- [ ] **Create crud.ts API factory**

```typescript
import { request } from './index'

interface CrudConfig {
  basePath: string
  hasTree?: boolean
}

export function createCrudApi(config: CrudConfig) {
  const { basePath, hasTree } = config

  return {
    page(params: any) {
      return request.Get<Service.ResponseResult<Service.PageResult>>(`${basePath}/page`, { params })
    },
    create(data: any) {
      return request.Post<Service.ResponseResult>(`${basePath}/create`, data)
    },
    modify(data: any) {
      return request.Post<Service.ResponseResult>(`${basePath}/modify`, data)
    },
    remove(data: { ids: string[] }) {
      return request.Post<Service.ResponseResult>(`${basePath}/remove`, data)
    },
    detail(params: { id: string }) {
      return request.Get<Service.ResponseResult>(`${basePath}/detail`, { params })
    },
    export(params: any) {
      return request.Get(`${basePath}/export`, { params, meta: { isBlob: true } }) as Promise<Blob>
    },
    template() {
      return request.Get(`${basePath}/template`, { meta: { isBlob: true } }) as Promise<Blob>
    },
    importFile(file: File) {
      const formData = new FormData()
      formData.append('file', file)
      return request.Post<Service.ResponseResult>(`${basePath}/import`, formData)
    },
    ...(hasTree
      ? {
          tree(params: any = {}) {
            return request.Get<Service.ResponseResult>(`${basePath}/tree`, { params })
          },
        }
      : {}),
  }
}
```

- [ ] **Commit**

```bash
git add src/utils/http/crud.ts
git commit -m "feat: add createCrudApi factory for CRUD API endpoints"
```

---

### Task 3: Refactor API Files to Use Factory

**Files:**
- Modify: `src/api/user.ts`
- Modify: `src/api/role.ts`
- Modify: `src/api/org.ts`
- Modify: `src/api/group.ts`
- Modify: `src/api/dict.ts`
- Modify: `src/api/notice.ts`
- Modify: `src/api/banner.ts`
- Modify: `src/api/resource.ts`
- Modify: `src/api/position.ts`
- Modify: `src/api/config.ts`

- [ ] **Refactor user.ts — keep only non-CRUD functions**

```typescript
import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const userApi = createCrudApi({ basePath: '/api/v1/sys/user' })

// Keep non-standard functions
export function fetchUserGrantRole(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-role', data)
}
export function fetchUserOwnRoles(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-roles', { params })
}
export function fetchUserGrantGroup(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-group', data)
}
export function fetchUserOwnGroups(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-groups', { params })
}
export function fetchUserGrantPermission(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-permission', data)
}
export function fetchUserOwnPermissionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-permission-detail', { params })
}
```

- [ ] **Refactor role.ts**

```typescript
import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const roleApi = createCrudApi({ basePath: '/api/v1/sys/role' })

export function fetchRoleGrantPermission(data: any) { ... }
export function fetchRoleOwnPermissionDetail(params: any) { ... }
export function fetchRoleGrantResource(data: any) { ... }
export function fetchRoleOwnPermission(params: any) { ... }
export function fetchRoleOwnResource(params: any) { ... }
```

- [ ] **Refactor org.ts (hasTree: true)**

```typescript
import { createCrudApi } from '@/utils/http/crud'

export const orgApi = createCrudApi({ basePath: '/api/v1/sys/org', hasTree: true })
// No extra functions needed
```

- [ ] **Refactor group.ts (hasTree: true)**

```typescript
import { createCrudApi } from '@/utils/http/crud'

export const groupApi = createCrudApi({ basePath: '/api/v1/sys/group', hasTree: true })
```

- [ ] **Refactor dict.ts (hasTree: true)**

```typescript
import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const dictApi = createCrudApi({ basePath: '/api/v1/sys/dict', hasTree: true })

export function fetchDictList(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/dict/list', { params })
}
export function fetchDictGetChildren(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/dict/get-children', { params })
}
```

- [ ] **Refactor notice.ts**

```typescript
import { createCrudApi } from '@/utils/http/crud'

export const noticeApi = createCrudApi({ basePath: '/api/v1/sys/notice' })
```

- [ ] **Refactor banner.ts**

```typescript
import { createCrudApi } from '@/utils/http/crud'

export const bannerApi = createCrudApi({ basePath: '/api/v1/sys/banner' })
```

- [ ] **Refactor position.ts**

```typescript
import { createCrudApi } from '@/utils/http/crud'

export const positionApi = createCrudApi({ basePath: '/api/v1/sys/position' })
```

- [ ] **Refactor config.ts**

```typescript
import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const configApi = createCrudApi({ basePath: '/api/v1/sys/config' })

export function fetchConfigListByCategory(params: { category: string }) { ... }
export function fetchConfigEditBatch(data: any) { ... }
export function fetchConfigEditByCategory(data: any) { ... }
```

- [ ] **Refactor resource.ts**

```typescript
import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const moduleApi = createCrudApi({ basePath: '/api/v1/sys/module' })
export const resourceApi = createCrudApi({ basePath: '/api/v1/sys/resource' })

export function fetchResourceTree() { ... }
```

- [ ] **Update all view imports to use new API names**

Example: In `views/sys/user/index.vue`:
```typescript
// Before:
import { fetchUserPage, fetchUserRemove, fetchUserExport, fetchUserTemplate, fetchUserImport } from '@/api/user'

// After:
import { userApi } from '@/api/user'
// Then: userApi.page(...), userApi.remove(...), etc.
```

- [ ] **Run build to verify no breakage**

Run: `npx vue-tsc --noEmit 2>&1 | head -80`
Expected: No type errors

- [ ] **Commit**

```bash
git add -A
git commit -m "refactor: compress CRUD API files with createCrudApi factory"
```

---

### Task 4: Create useCrud Composable

**Files:**
- Create: `src/hooks/useCrud.ts`

- [ ] **Create hooks/useCrud.ts**

```typescript
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { confirmDelete } from '@/utils'

interface UseCrudOptions {
  name: string
  deleteApi: (params: { ids: string[] }) => Promise<{ success: boolean }>
  onSuccess?: () => void
}

export function useCrud(options: UseCrudOptions) {
  const tableRef = ref()
  const selectedKeys = ref<string[]>([])

  const rowSelection = computed(() => ({
    selectedRowKeys: selectedKeys.value,
    onChange: (keys: string[]) => { selectedKeys.value = keys },
  }))

  function handleSearch() {
    tableRef.value?.refresh(true)
  }

  function resetSearch(form: Record<string, any>, extra?: Record<string, any>) {
    Object.keys(form).forEach(k => { form[k] = undefined })
    if (extra) {
      Object.entries(extra).forEach(([k, v]) => { form[k] = v })
    }
    tableRef.value?.refresh(true)
  }

  async function handleDelete(id: string) {
    const { success } = await options.deleteApi({ ids: [id] })
    if (success) {
      message.success('删除成功')
      tableRef.value?.refresh()
      options.onSuccess?.()
    }
  }

  function handleBatchDelete() {
    confirmDelete({
      name: options.name,
      selectedKeys: selectedKeys.value,
      deleteApi: options.deleteApi,
      onSuccess: () => {
        selectedKeys.value = []
        tableRef.value?.refresh()
        options.onSuccess?.()
      },
    })
  }

  function handleFormSuccess() {
    tableRef.value?.refresh()
    options.onSuccess?.()
  }

  return {
    tableRef,
    selectedKeys,
    rowSelection,
    handleSearch,
    resetSearch,
    handleDelete,
    handleBatchDelete,
    handleFormSuccess,
  }
}
```

- [ ] **Commit**

```bash
git add src/hooks/useCrud.ts
git commit -m "feat: add useCrud composable for CRUD page boilerplate"
```

---

### Task 5: Create useImportExport Composable

**Files:**
- Create: `src/hooks/useImportExport.ts`

- [ ] **Create hooks/useImportExport.ts**

```typescript
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { downloadBlob } from '@/utils'

interface UseImportExportOptions {
  exportApi: (params: any) => Promise<Blob>
  templateApi: () => Promise<Blob>
  importApi: (file: File) => Promise<any>
  fileName: string
  templateName: string
  onSuccess?: () => void
}

export function useImportExport(options: UseImportExportOptions) {
  const importOpen = ref(false)
  const exportOpen = ref(false)
  const templateLoading = ref(false)
  const importModalRef = ref()

  async function handleDownloadTemplate() {
    templateLoading.value = true
    try {
      const blob = await options.templateApi()
      downloadBlob(blob, `${options.templateName}.xlsx`)
    } catch {
      message.error('下载模板失败')
    } finally {
      templateLoading.value = false
    }
  }

  async function handleExportWithParams(params: any) {
    try {
      const blob = await options.exportApi(params)
      downloadBlob(blob, `${options.fileName}_${new Date().toLocaleDateString()}.xlsx`)
      message.success('导出成功')
      exportOpen.value = false
    } catch {
      message.error('导出失败')
    }
  }

  async function handleImport(file: File) {
    try {
      const { success, data } = await options.importApi(file)
      if (success && data) {
        importModalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
        message.success('导入成功')
        options.onSuccess?.()
      }
    } catch {
      importModalRef.value?.setResult({ success: false, message: '导入失败，请检查文件格式' })
    }
  }

  return {
    importOpen,
    exportOpen,
    templateLoading,
    importModalRef,
    handleDownloadTemplate,
    handleExportWithParams,
    handleImport,
  }
}
```

- [ ] **Commit**

```bash
git add src/hooks/useImportExport.ts
git commit -m "feat: add useImportExport composable for import/export boilerplate"
```

---

### Task 6: Create AppTreePanel Component

**Files:**
- Create: `src/components/layout/AppTreePanel.vue`

- [ ] **Create AppTreePanel.vue**

```vue
<template>
  <AppSplitPanel
    ref="splitRef"
    v-model:collapsed="collapsed"
    :initial-size="280"
    :min-size="200"
    :max-size="400"
    :md="0"
  >
    <template #left>
      <a-card
        size="small"
        class="h-full flex flex-col max-md:hidden"
        :body-style="{ flex: '1', overflow: 'auto', padding: '12px' }"
      >
        <a-input-search
          v-model:value="searchKey"
          :placeholder="`搜索${title}`"
          allow-clear
          class="mb-2"
        />
        <a-spin :spinning="loading">
          <a-tree
            v-if="data.length"
            v-model:expanded-keys="expandedKeys"
            :tree-data="data"
            :field-names="fieldNames"
            :selected-keys="selectedKeys"
            block-node
            show-line
            @select="handleSelect"
          />
          <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
        </a-spin>
      </a-card>
    </template>
    <template #right>
      <slot name="right" :parent-id="currentParentId" :refresh-tree="refresh" />
    </template>
  </AppSplitPanel>

  <a-drawer
    :open="mobileOpen"
    :title="title"
    placement="left"
    :width="280"
    destroy-on-close
    @close="mobileOpen = false"
  >
    <a-input-search
      v-model:value="searchKey"
      :placeholder="`搜索${title}`"
      allow-clear
      class="mb-2"
    />
    <a-spin :spinning="loading">
      <a-tree
        v-if="data.length"
        v-model:expanded-keys="expandedKeys"
        :tree-data="data"
        :field-names="fieldNames"
        :selected-keys="selectedKeys"
        block-node
        show-line
        @select="handleSelect"
      >
        <template #switcherIcon="{ expanded }">
          <CaretDownOutlined :class="expanded ? '' : '-rotate-90'" class="text-[12px]" />
        </template>
        <template #icon>
          <component :is="icon" class="text-[var(--primary-color)]" />
        </template>
      </a-tree>
      <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { CaretDownOutlined } from '@ant-design/icons-vue'
import AppSplitPanel from './AppSplitPanel.vue'

const props = withDefaults(defineProps<{
  title?: string
  fetchTree: (params?: any) => Promise<any>
  fieldNames?: { children: string; title: string; key: string }
  icon?: any
}>(), {
  title: '',
  fieldNames: () => ({ children: 'children', title: 'name', key: 'id' }),
  icon: undefined,
})

const emit = defineEmits<{
  select: [parentId: string | undefined]
}>()

const splitRef = ref()
const collapsed = ref(false)
const mobileOpen = ref(false)
const loading = ref(false)
const data = ref<any[]>([])
const originData = ref<any[]>([])
const expandedKeys = ref<string[]>([])
const selectedKeys = ref<string[]>([])
const searchKey = ref('')

function filterTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const label = node[props.fieldNames.title]
    const match = label?.includes(keyword)
    const filteredChildren = node.children ? filterTree(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

function getAllKeys(nodes: any[]): string[] {
  return nodes.reduce((keys: string[], n) => {
    keys.push(n[props.fieldNames.key])
    if (n.children) keys.push(...getAllKeys(n.children))
    return keys
  }, [])
}

watch(searchKey, (val) => {
  data.value = filterTree(originData.value, val)
  if (val) expandedKeys.value = getAllKeys(data.value)
})

const currentParentId = ref<string | undefined>()

function handleSelect(keys: any[]) {
  selectedKeys.value = keys
  currentParentId.value = keys.length > 0 ? keys[0] : undefined
  mobileOpen.value = false
  emit('select', currentParentId.value)
}

async function refresh() {
  loading.value = true
  try {
    const res = await props.fetchTree()
    originData.value = res.data || []
    data.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(refresh)

defineExpose({ refresh, collapsed, splitRef })
</script>
```

- [ ] **Commit**

```bash
git add src/components/layout/AppTreePanel.vue
git commit -m "feat: add AppTreePanel component for tree + split-panel pages"
```

---

### Task 7: Refactor View Pages with Composables

**Files:**
- Modify: `src/views/sys/user/index.vue`
- Modify: `src/views/sys/role/index.vue`
- Modify: `src/views/sys/notice/index.vue`
- Modify: `src/views/sys/banner/index.vue`
- Modify: `src/views/sys/config/components/bizConfig/index.vue`
- Modify: `src/views/sys/org/index.vue` (also uses AppTreePanel)
- Modify: `src/views/sys/dict/index.vue` (also uses AppTreePanel)

- [ ] **Refactor user/index.vue** — apply useCrud + useImportExport, update API imports

```typescript
// Script section after refactor:
import { ref } from 'vue'
import { PlusOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { userApi } from '@/api/user'
import { useCrud } from '@/hooks/useCrud'
import { useImportExport } from '@/hooks/useImportExport'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '用户', deleteApi: userApi.remove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete } = crud

const ie = useImportExport({
  exportApi: userApi.export,
  templateApi: userApi.template,
  importApi: userApi.importFile,
  fileName: '用户数据',
  templateName: '用户导入模板',
  onSuccess: () => tableRef.value?.refresh(true),
})
// Use ie.importOpen, ie.exportOpen, ie.importModalRef, etc. in template
```

- [ ] **Refactor role/index.vue** — same pattern

- [ ] **Refactor notice/index.vue** — same pattern

- [ ] **Refactor banner/index.vue** — same pattern

- [ ] **Refactor config/bizConfig/index.vue** — useCrud only (no import/export)

- [ ] **Refactor org/index.vue** — use AppTreePanel + useCrud

Template structure:
```vue
<AppTreePanel ref="treePanel" :fetch-tree="orgApi.tree" title="组织" @select="handleTreeSelect">
  <template #right="{ parentId, refreshTree }">
    <div class="flex flex-col h-full overflow-auto gap-2">
      <AppSearchPanel ...>
        ...
      </AppSearchPanel>
      <AppTable ...>
        ...
      </AppTable>
      ...
    </div>
  </template>
</AppTreePanel>
```

- [ ] **Refactor dict/index.vue** — same pattern as org

Also fix the duplicated lines in handleTreeSelect:
```typescript
// Before (3 lines repeated):
selectedTreeKeys.value = keys
searchForm.parent_id = keys.length > 0 ? keys[0] : undefined
tableRef.value?.refresh(true)

// After (single set):
function handleTreeSelect(keys: any[]) {
  selectedTreeKeys.value = keys
  searchForm.parent_id = keys.length > 0 ? keys[0] : undefined
  tableRef.value?.refresh(true)
}
```

- [ ] **Run build to verify**

Run: `npx vue-tsc --noEmit 2>&1 | head -80`
Expected: No type errors

- [ ] **Commit**

```bash
git add -A
git commit -m "refactor: apply useCrud, useImportExport, AppTreePanel to view pages"
```

---

### Task 8: Unify isMobile Detection

**Files:**
- Modify: `src/store/app.ts`
- Modify: `src/components/form/AppDrawerForm.vue`
- Modify: `src/components/layout/AppSplitPanel.vue`

- [ ] **Fix store/app.ts** — remove redundant isMobileRef pattern

```typescript
// Before (lines 8-14):
const isMobileRef = ref(window.matchMedia('(max-width: 700px)').matches)
if (typeof window !== 'undefined') {
  const mql = window.matchMedia('(max-width: 700px)')
  mql.addEventListener('change', e => {
    isMobileRef.value = e.matches
  })
}

// After:
const mql = window.matchMedia('(max-width: 700px)')
const isMobileRef = ref(mql.matches)
mql.addEventListener('change', (e: MediaQueryListEvent) => {
  isMobileRef.value = e.matches
})
```

- [ ] **Fix AppDrawerForm.vue** — use store's isMobile instead of local implementation

```typescript
// Before (lines 39-46):
const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

// After:
import { useAppStore } from '@/store'
const appStore = useAppStore()
const drawerWidth = computed(() => (appStore.isMobile ? '100%' : (props.width ?? 560)))
```

Also remove unused `onMounted` and `onBeforeUnmount` imports.

- [ ] **Fix AppSplitPanel.vue** — use store's isMobile

```typescript
// Before (lines 62-69):
const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

// After:
import { useAppStore } from '@/store'
const appStore = useAppStore()
// Then replace all `isMobile.value` references with `appStore.isMobile`
```

- [ ] **Run build to verify**

Run: `npx vue-tsc --noEmit 2>&1 | head -30`
Expected: No type errors

- [ ] **Commit**

```bash
git add -A
git commit -m "refactor: unify isMobile detection through app store"
```

---

### Task 9: Fix Minor Code Quality Issues

**Files:**
- Modify: `src/utils/http/alova.ts`
- (dict fix already done in Task 7)

- [ ] **Fix alova.ts type assertion** — remove unnecessary cast

```typescript
// Before:
onError: ((error: any, method: any) => {
  ...
}) as (error: any, method: any) => void,

// After:
onError: (error: any, method: any) => {
  ...
},
```

- [ ] **Run build to verify**

Run: `npx vue-tsc --noEmit 2>&1 | head -30`
Expected: No type errors

- [ ] **Commit**

```bash
git add src/utils/http/alova.ts
git commit -m "refactor: remove unnecessary type assertion in alova.ts"
```

---

### Task 10: Final Verification

- [ ] **Full type check**

Run: `npx vue-tsc --noEmit`
Expected: No errors

- [ ] **Run build**

Run: `pnpm build`
Expected: Build succeeds

- [ ] **Verify git status is clean**

Run: `git status`
Expected: No uncommitted changes (except maybe lockfile)
