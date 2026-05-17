# 组件体系

框架提供了一系列高级业务组件，封装了后台管理系统的通用交互模式。

## 表格组件

### AppTable

通用 CRUD 表格组件，自动管理分页和数据加载：

```vue
<AppTable
  ref="tableRef"
  :columns="columns"
  :fetch-data="fetchApi"
  :search-form="searchForm"
  :row-selection="rowSelection"
  :perm="'sys:module:page'"
  :fixed-height="true"
>
  <template #toolbar>
    <a-button type="primary" @click="openCreate">新增</a-button>
  </template>
  <template #bodyCell="{ column, record }">
    <!-- 自定义列渲染 -->
  </template>
</AppTable>
```

**Props：**
| Prop | 类型 | 说明 |
|------|------|------|
| columns | array | 表格列定义 |
| data / fetchData | function | 数据获取函数（接收 `{ current, size, ...search }`） |
| searchForm | object | 搜索条件对象，自动过滤空值后传入 data 函数 |
| rowSelection | object | 行选择配置 `{ selectedRowKeys, onChange, type }` |
| perm | string/array | 权限码，无权限时隐藏 |
| fixedHeight | boolean | 固定高度模式，自动计算滚动区域 |
| rowKey | string | 行 key 字段名，默认 `id` |

将 `fetch-data` 和 `search-form` 绑定后，表格自动在初始化、搜索条件变化、分页变化时加载数据。

### AppTreeTable

树形表格组件，适用于组织架构、资源管理等树形数据展示：

```vue
<AppTreeTable
  :columns="columns"
  :data-source="treeData"
  :loading="loading"
  :default-expand-all-rows="true"
  children-column-name="children"
>
  <template #toolbar>
    <a-button type="primary" @click="handleAdd">新增</a-button>
  </template>
</AppTreeTable>
```

与 AppTable 不同，AppTreeTable 的数据由父组件控制（`data-source` prop），不自动加载。

## 表单组件

### AppSearchPanel

搜索面板组件，自动折叠多余搜索项，支持权限控制：

```vue
<AppSearchPanel
  :model="searchForm"
  :collapse-after="4"
  perm="sys:module:page"
  @search="handleSearch"
  @reset="resetSearch"
>
  <a-col :xs="24" :sm="12" :md="8" :lg="6">
    <a-form-item label="关键词" name="keyword">
      <a-input v-model:value="searchForm.keyword" allow-clear />
    </a-form-item>
  </a-col>
</AppSearchPanel>
```

### AppDrawerForm

抽屉表单组件，从右侧滑入的表单，通过 `on-submit` 回调函数处理提交：

```vue
<AppDrawerForm
  :open="open"
  title="添加用户"
  :form="form"
  :rules="rules"
  :on-submit="handleSubmit"
  @close="handleClose"
  @success="handleFormSuccess"
>
  <template #default="{ form }">
    <a-form-item label="用户名" name="username">
      <a-input v-model:value="form.username" />
    </a-form-item>
  </template>
</AppDrawerForm>
```

组件在提交成功后自动调用 `onSubmit` 并显示成功提示，然后关闭抽屉。

### DictSelect

字典选择器，自动从字典缓存中加载选项列表，支持多种展示形式：

```vue
<DictSelect
  v-model="form.status"
  type-code="USER_STATUS"
  option-type="dropdown"
  placeholder="全部"
/>
```

**Props：**
| Prop | 类型 | 说明 |
|------|------|------|
| type-code | string | 字典类型编码（需与后端定义一致） |
| option-type | string | 展示形式：`dropdown` / `radio` / `button` / `checkbox` |
| v-model | any | 绑定值 |
| placeholder | string | 占位文本 |
| allow-clear | boolean | 是否允许清除 |
| disabled | boolean | 是否禁用 |

## 布局组件

### AppSplitPanel

分割面板组件，实现可拖拽调整宽度的左右分栏布局：

```vue
<AppSplitPanel
  :initial-size="260"
  :min-size="200"
  :max-size="400"
  @resize="handleResize"
>
  <template #left>
    <!-- 左侧树形结构 -->
  </template>
  <template #right>
    <!-- 右侧内容 -->
  </template>
</AppSplitPanel>
```

### AppTreePanel

树形面板组件，左侧展示树形结构，右侧展示选中节点的详情或列表。

## CRUD Hook

`useCrud` Hook 封装了列表页的通用操作逻辑：

```typescript
import { useCrud } from '@/hooks/useCrud'
import { fetchUserRemove } from '@/api/user'

const crud = useCrud({
  name: '用户',
  deleteApi: fetchUserRemove,
  onSuccess: () => { /* 刷新后的回调 */ },
})

const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud
```

**Options：**
| 参数 | 类型 | 说明 |
|------|------|------|
| name | string | 实体名称，用于删除确认提示 |
| deleteApi | function | 删除 API：`({ ids }) => Promise<{ success }>` |
| onSuccess | function | 操作成功后的回调（可选） |

**返回值：**
| 返回值 | 说明 |
|--------|------|
| tableRef | 传递给 AppTable 的 ref，用于调用内部方法 |
| selectedKeys | 当前选中的行 ID 列表（响应式） |
| rowSelection | 绑定到 AppTable 的 `row-selection` 的计算属性 |
| handleSearch | 触发表格刷新（保留当前分页） |
| resetSearch | 重置搜索表单并刷新（接收 form 对象和可选的默认值） |
| handleDelete | 单条删除（确认后调用 deleteApi） |
| handleBatchDelete | 批量删除（弹窗确认后调用 deleteApi） |
| handleFormSuccess | 表单操作成功后刷新表格 |

## 使用模式

实际页面中的完整使用示例：

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/store'
import { fetchModulePage, fetchModuleRemove } from '@/api/module'
import { useCrud } from '@/hooks/useCrud'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '模块', deleteApi: fetchModuleRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud

const searchForm = reactive({ keyword: '', status: undefined })
const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = undefined
  tableRef.value?.refresh(true)
}
</script>

<template>
  <AppSearchPanel
    :model="searchForm"
    perm="sys:module:page"
    @search="handleSearch"
    @reset="resetSearch"
  >
    <a-col :xs="24" :sm="12" :md="8" :lg="6">
      <a-form-item label="关键词" name="keyword">
        <a-input v-model:value="searchForm.keyword" allow-clear />
      </a-form-item>
    </a-col>
  </AppSearchPanel>

  <AppTable
    ref="tableRef"
    perm="sys:module:page"
    :columns="columns"
    :fetch-data="fetchModulePage"
    :search-form="searchForm"
    :row-selection="rowSelection"
  >
    <template #toolbar>
      <a-button v-if="hasPermission('sys:module:create')" type="primary">新增</a-button>
      <a-button v-if="hasPermission('sys:module:remove')" danger :disabled="selectedKeys.length === 0" @click="handleBatchDelete">批量删除</a-button>
    </template>
  </AppTable>
</template>
```
