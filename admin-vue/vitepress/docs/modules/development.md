# 模块开发规范

## 页面结构规范

每个业务模块遵循以下文件结构：

```
views/sys/<module>/
├── index.vue              # 列表页
├── components/
│   ├── form.vue           # 新增/编辑表单弹窗
│   └── detail.vue         # 详情弹窗
```

### 列表页（index.vue）

列表页通常包含以下区域：

1. 搜索区域（`AppSearchPanel`）
2. 操作按钮区域（新增、批量删除等）
3. 数据表格（`AppTable` 或 `AppTreeTable`）
4. 表单弹窗（`AppDrawerForm`）
5. 详情弹窗

### 表单弹窗（form.vue）

用于新增和编辑操作，接收 `record` 属性区分新增与编辑：

```vue
<script setup lang="ts">
const props = defineProps<{ record: any }>()  // null 表示新增，有值表示编辑

if (props.record) {
  // 编辑模式：回填表单数据
  form.value = { ...props.record }
}
</script>
```

## 开发示例

### 第一步：创建 API

在 `src/api/` 下创建接口定义：

```typescript
// src/api/<module>.ts
import { request } from '@/utils'

export function fetchPage(params: any) {
  return request.Get('/api/v1/sys/<module>/page', { params })
}
export function fetchCreate(data: any) {
  return request.Post('/api/v1/sys/<module>/create', data)
}
export function fetchModify(data: any) {
  return request.Post('/api/v1/sys/<module>/modify', data)
}
export function fetchRemove(data: any) {
  return request.Post('/api/v1/sys/<module>/remove', data)
}
export function fetchDetail(params: any) {
  return request.Get('/api/v1/sys/<module>/detail', { params })
}
```

### 第二步：创建页面

在 `src/views/sys/` 下创建对应的页面目录和文件。

使用 `useCrud` Hook 快速实现 CRUD 功能：

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/store'
import { fetchPage, fetchRemove } from '@/api/<module>'
import { useCrud } from '@/hooks/useCrud'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '模块', deleteApi: fetchRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud

const searchForm = reactive({ keyword: '', status: undefined })
const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]
</script>

<template>
  <AppSearchPanel
    :model="searchForm"
    @search="handleSearch"
    @reset="() => { searchForm.keyword = ''; searchForm.status = undefined; tableRef.value?.refresh(true) }"
  >
    <a-col :xs="24" :sm="12" :md="8" :lg="6">
      <a-form-item label="关键词" name="keyword">
        <a-input v-model:value="searchForm.keyword" allow-clear />
      </a-form-item>
    </a-col>
  </AppSearchPanel>

  <AppTable
    ref="tableRef"
    :columns="columns"
    :fetch-data="fetchPage"
    :search-form="searchForm"
    :row-selection="rowSelection"
  >
    <template #toolbar>
      <a-button v-if="hasPermission('sys:<module>:create')" type="primary">新增</a-button>
    </template>
  </AppTable>
</template>
```

### 第三步：注册菜单

在数据库中添加菜单记录，框架会自动读取菜单配置并生成路由。需要提供：

| 字段 | 说明 | 示例 |
|------|------|------|
| code | 权限码/路由名 | `sys:<module>:list` |
| name | 菜单名称 | 模块管理 |
| route_path | 路由路径 | `/sys/<module>` |
| component_path | 组件路径 | `sys/<module>/index` |
| type | 菜单类型 | `MENU` |
| icon | 图标 | `icon-xxx` |
| sort_code | 排序号 | `10` |
