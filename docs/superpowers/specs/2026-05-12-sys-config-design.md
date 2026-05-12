# 系统配置管理页面设计

> **目标：** 为 HEI 管理系统创建系统配置管理页面，引用 Snowy 的配置管理模式，支持按分类的表单批量编辑 + 用户自定义配置的表格 CRUD。

**架构：** 前端使用 Ant Design Vue `a-card` + `tab-list` 作为主容器，每个 tab 渲染表单字段（按分类）或标准分页表格（用户自定义）。表单 tab 通过 `list-by-category` 加载配置数据，通过 `edit-batch` 批量保存。后端模块已完整实现，只需创建前端文件。

**技术栈：** Vue 3 Composition API / Ant Design Vue / FastAPI / SQLAlchemy

---

## 数据模型

`sys_config` 表字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(32) | 主键 |
| `config_key` | VARCHAR(255) | 配置键 |
| `config_value` | Text | 配置值 |
| `category` | VARCHAR(255) | 分类编码 |
| `remark` | VARCHAR(500) | 备注 |
| `sort_code` | Integer | 排序码 |
| `ext_json` | Text | 扩展信息 |

现有种子数据分类：

| 分类 | 说明 | 配置项 |
|------|------|--------|
| `SYS_BASE` | 系统基础配置 | 默认文件引擎、Snowflake ID、默认密码、用户初始密码 |
| `SYS_SECURITY` | 安全配置 | 登录失败次数、锁定时间、JWT Token 过期 |
| `FILE_LOCAL` | 本地文件存储 | Windows/Unix 存储路径 |
| `BIZ_DEFINE` | 用户自定义 | （空，由用户通过表格 CRUD 管理） |

## 页面结构

```
src/views/sys/config/
  index.vue                  # 主页面
  api/
    config.ts                # API 封装
  components/
    sysBase.vue              # Tab1 - 系统基础配置 (SYS_BASE)
    security.vue             # Tab2 - 安全配置 (SYS_SECURITY)
    fileConfig/
      index.vue              # Tab3 - 文件配置 容器
      localForm.vue          # 本地文件存储 (FILE_LOCAL)
    bizConfig/
      index.vue              # Tab4 - 其他配置 (BIZ_DEFINE 表格 CRUD)
      form.vue               # 新增/编辑弹窗
      detail.vue             # 详情抽屉
```

## 组件设计

### 主页面 `index.vue`

使用 `a-card` 的 `tab-list` 属性渲染 4 个 tab：

```vue
<a-card :tab-list="tabList" :active-tab-key="activeKey" @tab-change="(k) => activeKey = k">
  <sys-base v-if="activeKey === 'sysBase'" />
  <security v-if="activeKey === 'security'" />
  <file-config v-if="activeKey === 'fileConfig'" />
  <biz-config v-if="activeKey === 'bizConfig'" />
</a-card>
```

tabList 定义：
```ts
const tabList = [
  { key: 'sysBase', tab: '系统基础配置' },
  { key: 'security', tab: '安全配置' },
  { key: 'fileConfig', tab: '文件配置' },
  { key: 'bizConfig', tab: '其他配置' },
]
```

### 表单式 tab 组件（sysBase / security / fileConfig）

**通用模式：**

1. **加载：** `onMounted` 时调用 `fetchConfigListByCategory({ category: 'SYS_BASE' })`，返回的数组按 `config_key` 映射到表单字段对象
2. **表单字段：** 每个配置项根据其 config_key 硬编码对应的输入控件类型（input / input-number / select / switch）
3. **Boolean 转换：** 从后端获取的 `"true"/"false"` 字符串转为 boolean 供 `a-switch` 使用，保存时转回字符串
4. **保存：** 收集所有表单值 → 构造成 `{config_key, config_value}[]` → `fetchConfigEditBatch({ configs: [...] })`
5. **加载状态：** `a-spin` 包裹表单
6. **布局：** `a-row` + `a-col` 栅格，`xs:24 sm:12` 响应式

**sysBase.vue 配置项映射：**

| config_key | 标签 | 控件 | 选项 |
|-----------|------|------|------|
| SYS_DEFAULT_FILE_ENGINE | 默认文件引擎 | a-select | LOCAL / ALIYUN / MINIO |
| SYS_SNOWFLAKE_WORKER_ID | Snowflake 工作节点ID | a-input-number | 0-31 |
| SYS_SNOWFLAKE_DATACENTER_ID | Snowflake 数据中心ID | a-input-number | 0-31 |
| SYS_DEFAULT_PASSWORD | 默认密码 | a-input | 文本 |
| SYS_USER_INIT_PASSWORD | 用户初始密码 | a-input | 文本 |

**security.vue 配置项映射：**

| config_key | 标签 | 控件 | 说明 |
|-----------|------|------|------|
| SYS_MAX_LOGIN_RETRIES | 最大登录失败次数 | a-input-number | 0-99 |
| SYS_LOGIN_LOCK_MINUTES | 登录锁定时间(分钟) | a-input-number | 0-999 |
| SYS_JWT_TOKEN_EXPIRE | JWT Token 过期时间(秒) | a-input-number | 0-864000 |

**fileConfig 结构：**
- `fileConfig/index.vue` — 使用 `a-tabs tab-position="left"` 作为容器
- `fileConfig/localForm.vue` — 本地存储表单

| config_key | 标签 | 控件 |
|-----------|------|------|
| SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS | 本地路径(Windows) | a-input |
| SYS_FILE_LOCAL_FOLDER_FOR_UNIX | 本地路径(Unix) | a-input |

### 表格 CRUD 组件（bizConfig）

**bizConfig/index.vue：**
- 与现有 notice 管理相同的模式
- `AppSearchPanel` 关键词搜索
- `AppTable` 分页表格，`fetch-data` 绑定 `fetchConfigPage`
- 列：配置键、配置值、备注、排序、创建时间、操作
- 新增/编辑通过 `AppDrawerForm` 弹窗
- 删除通过 `fetchConfigRemove`

**bizConfig/form.vue：**
- 字段：config_key（必填）、config_value（必填）、remark、sort_code
- 创建时自动设置 `category = 'BIZ_DEFINE'`

**bizConfig/detail.vue：**
- 与现有 detail 抽屉相同模式

## API 封装 `api/config.ts`

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

注意：URL 使用 kebab-case（`list-by-category`、`edit-batch`），禁止驼峰。

## 数据流

### 表单 tab 数据流

```
onMounted
  → fetchConfigListByCategory({ category: 'SYS_BASE' })
  → 返回 [{config_key, config_value, remark, ...}]
  → 映射到 formData: Record<string, string> (keyed by config_key)
  → 渲染表单字段

保存
  → 收集 formData 所有值
  → 构建 Array<{config_key, config_value}>
  → fetchConfigEditBatch({ configs: [...] })
  → 刷新
```

### Boolean 处理

```typescript
// 加载时: "true" → true, "false" → false
function toBoolean(val: string): boolean {
  return val === 'true'
}
// 保存时: true → "true", false → "false"
function fromBoolean(val: boolean): string {
  return val ? 'true' : 'false'
}
```

## 文件清单

**创建：**
- `src/api/config.ts` — API 函数
- `src/views/sys/config/index.vue` — 主页面
- `src/views/sys/config/components/sysBase.vue` — 系统基础配置表单
- `src/views/sys/config/components/security.vue` — 安全配置表单
- `src/views/sys/config/components/fileConfig/index.vue` — 文件配置容器
- `src/views/sys/config/components/fileConfig/localForm.vue` — 本地文件表单
- `src/views/sys/config/components/bizConfig/index.vue` — 用户自定义配置表格
- `src/views/sys/config/components/bizConfig/form.vue` — 配置表单弹窗
- `src/views/sys/config/components/bizConfig/detail.vue` — 配置详情抽屉

**后端无需修改（已有）。**
