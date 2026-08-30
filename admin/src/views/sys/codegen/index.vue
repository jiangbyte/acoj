<!-- Author: Charlie -->

<script setup lang="tsx">
import type { FormRules, PaginationProps } from 'naive-ui'
import type { SelectMixedOption } from 'naive-ui/es/select/src/interface'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { codegenApi, resourceModuleApi } from '@/api'
import IconSelect from '@/components/common/IconSelect.vue'
import MonacoPreview from '@/components/editor/MonacoPreview.vue'
import {
  createRequiredRule,
  dictDataAll,
  formatDateTime,
  hasPermission,
  normalizeSearchValues,
  refreshDict,
  renderButtonIcon,
  wireBool,
  wireInt,
} from '@/utils'
import { readPageMeta } from '@/utils/wire'
import {
  NAlert,
  NButton,
  NCheckbox,
  NEllipsis,
  NFlex,
  NInput,
  NInputNumber,
  NSelect,
  NTag,
  NTreeSelect,
} from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useDraggable } from 'vue-draggable-plus'

const valueTypeLabels: Record<string, string> = {
  str: '字符串',
  int: '整数',
  float: '浮点数',
  bool: '布尔',
  datetime: '日期时间',
  dict: '字典',
}

const uiTypeLabels: Record<string, string> = {
  string: '字符串',
  number: '数字',
  boolean: '布尔',
  'Record<string, any>': '对象',
}

function formatValueType(value: string | null | undefined) {
  if (!value) {
    return '-'
  }
  return valueTypeLabels[value] ? `${valueTypeLabels[value]} (${value})` : value
}

function formatUiType(value: string | null | undefined) {
  if (!value) {
    return '-'
  }
  return uiTypeLabels[value] ? `${uiTypeLabels[value]} (${value})` : value
}

function renderCellEllipsis(text: string | null | undefined, tooltip?: string) {
  const value = text || '-'
  const tip = tooltip ?? (value === '-' ? undefined : value)
  if (!tip || tip === value) {
    return (
      <NEllipsis
        class="codegen-cell-ellipsis"
        tooltip={tip ? true : false}
      >
        {value}
      </NEllipsis>
    )
  }
  return (
    <NEllipsis class="codegen-cell-ellipsis">
      {{
        default: () => value,
        tooltip: () => tip,
      }}
    </NEllipsis>
  )
}

function formatGenTypeLabel(value: string | null | undefined) {
  if (!value) {
    return '-'
  }
  return genTypeOptions.find((item) => item.value === value)?.label ?? value
}

function normalizeFieldSort(rows: any[]) {
  rows.forEach((row, index) => {
    row.sort = index + 1
  })
}

function normalizeCodegenFieldRow(row: any) {
  row.in_table = wireBool(row.in_table ?? false)
  row.in_form = wireBool(row.in_form ?? false)
  row.in_detail = wireBool(row.in_detail ?? false)
  row.in_query = wireBool(row.in_query ?? false)
  row.required = wireBool(row.required ?? false)
  row.primary_key = wireBool(row.primary_key ?? false)
  row.unique_flag = wireBool(row.unique_flag ?? false)
  row.nullable = wireBool(row.nullable ?? true)
  if (row.sort !== undefined && row.sort !== null && row.sort !== '') {
    row.sort = Number(row.sort)
  }
  return row
}

function onFieldDragEnd() {
  normalizeFieldSort(state.fieldRows)
}

/** 模块路径 → 通用输出预览（不绑定具体语言包名） */
function modulePathPreview(modulePath: string) {
  const parts = modulePath
    .trim()
    .replaceAll('\\', '/')
    .split('/')
    .filter((part) => part && part !== '.')
    .map((part) => part.replaceAll('-', '_').toLowerCase())
  if (!parts.length) {
    return {
      moduleRoot: 'module/{模块}',
      featurePath: '{模块}/{功能}',
    }
  }
  const module = parts[0]
  return {
    moduleRoot: `module/${module}`,
    featurePath: parts.join('/'),
  }
}

const genTypeHelp: Record<string, string> = {
  TABLE: '生成单表 CRUD 后端接口 + 前端列表页与菜单 SQL。',
  TREE: '在单表基础上增加树形接口 /tree 与 list 权限，前端为树形维护页。',
  LEFT_TREE_TABLE: '主表左树（含 /tree），子表挂在主资源的 /children/*，前端左树右表。',
  MASTER_DETAIL: '主表列表 + 子表 /children/*，无树接口；前端主从表联动。',
}

const defaultForm = {
  name: '',
  gen_type: 'TABLE',
  author: '',
  description: '',
  table_name: '',
  pk_column: 'id',
  entity_name: '',
  module_path: '',
  business_name: '',
  api_prefix: '',
  permission_prefix: '',
  resource_module_id: null as string | null,
  parent_resource_id: null as string | null,
  menu_name: '',
  menu_path: '',
  component_path: '',
  icon: 'icon-park-outline:code',
  sort: 99,
  tree_parent_field: '',
  tree_label_field: '',
  sub_table: '',
  sub_pk: '',
  sub_foreign_key: '',
  sub_entity_name: '',
  sub_business_name: '',
}

const genTypeOptions = [
  { label: '普通表 — 单表 CRUD 列表', value: 'TABLE' },
  { label: '树表 — 树形维护（含 /tree）', value: 'TREE' },
  { label: '左树右表 — 左树筛选 + 右表明细', value: 'LEFT_TREE_TABLE' },
  { label: '主子表 — 主表列表 + 子表明细', value: 'MASTER_DETAIL' },
]

const genTypeShortLabels: Record<string, string> = {
  TABLE: '普通表',
  TREE: '树表',
  LEFT_TREE_TABLE: '左树右表',
  MASTER_DETAIL: '主子表',
}

const widgetOptions = [
  { label: '输入框', value: 'input' },
  { label: '多行文本', value: 'textarea' },
  { label: '数字', value: 'number' },
  { label: '开关', value: 'switch' },
  { label: '字典', value: 'dict' },
  { label: '日期时间', value: 'datetime' },
  { label: '富文本', value: 'richtext' },
  { label: 'Markdown', value: 'markdown' },
  { label: '代码', value: 'code' },
  { label: 'Icon', value: 'icon' },
]

const operatorOptions = [
  { label: '不查询', value: null },
  { label: '等于', value: 'EQ' },
  { label: '模糊', value: 'LIKE' },
] as SelectMixedOption[]

interface ColumnOption {
  label: string
  value: string
  isPrimaryKey: boolean
}

const formRef = ref<any>(null)
const fieldTableWrapRef = ref<HTMLElement | null>(null)
const state = reactive({
  rows: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
  showForm: false,
  formLoading: false,
  submitLoading: false,
  editingId: null as string | null,
  form: { ...defaultForm },
  tableOptions: [] as any[],
  mainColumnOptions: [] as ColumnOption[],
  subColumnOptions: [] as ColumnOption[],
  moduleOptions: [] as any[],
  parentOptions: [] as any[],
  showFields: false,
  fieldPlanId: '',
  fieldRows: [] as any[],
  fieldLoading: false,
  fieldSaving: false,
  showPreview: false,
  previewFiles: [] as any[],
  previewPath: '',
  previewGroup: 'backend' as 'backend' | 'frontend' | 'sql',
  previewLoading: false,
  downloadingId: '',
})

const fieldRowsModel = computed({
  get: () => state.fieldRows,
  set: (value) => {
    state.fieldRows = value
    normalizeFieldSort(state.fieldRows)
  },
})

const fieldDrag = useDraggable(fieldTableWrapRef, fieldRowsModel, {
  immediate: false,
  animation: 150,
  handle: '.codegen-field-drag-handle',
  draggable: '.n-data-table-tr',
  ghostClass: 'sortable-ghost',
  onEnd: onFieldDragEnd,
})

async function initFieldDrag() {
  fieldDrag.destroy()
  if (!state.showFields || state.fieldLoading || !state.fieldRows.length) {
    return
  }
  await nextTick()
  await new Promise<void>((resolve) => window.requestAnimationFrame(() => resolve()))
  const tbody = fieldTableWrapRef.value?.querySelector('.n-data-table-tbody')
  if (tbody instanceof HTMLElement) {
    fieldDrag.start(tbody)
  }
}

watch(
  () => [state.showFields, state.fieldLoading, state.fieldRows.length] as const,
  () => {
    void initFieldDrag()
  },
  { flush: 'post' },
)

watch(
  () => state.showFields,
  (show) => {
    if (!show) {
      fieldDrag.destroy()
    }
  },
)

const needsTree = computed(() => ['TREE', 'LEFT_TREE_TABLE'].includes(state.form.gen_type))
const needsSub = computed(() => ['LEFT_TREE_TABLE', 'MASTER_DETAIL'].includes(state.form.gen_type))
const packagePreview = computed(() => modulePathPreview(state.form.module_path || ''))
const currentGenTypeHelp = computed(() => genTypeHelp[state.form.gen_type] ?? '')
const previewEditorOptions = computed(() => ({
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  wordWrap: 'off' as const,
  automaticLayout: true,
}))
function previewGroupOf(path: string): 'backend' | 'frontend' | 'sql' {
  if (path.endsWith('.sql') || path.startsWith('scripts/')) {
    return 'sql'
  }
  if (path.startsWith('hei-admin/')) {
    return 'frontend'
  }
  return 'backend'
}
function previewLanguageLabel(language: string | undefined, path: string) {
  if (language) {
    return language
  }
  if (path.endsWith('.java')) {
    return 'java'
  }
  if (path.endsWith('.vue')) {
    return 'vue'
  }
  if (path.endsWith('.ts') || path.endsWith('.append')) {
    return 'typescript'
  }
  if (path.endsWith('.sql')) {
    return 'sql'
  }
  return 'text'
}
const previewGroupedFiles = computed(() =>
  state.previewFiles.filter((file) => previewGroupOf(file.path) === state.previewGroup),
)
const previewGroupCounts = computed(() => {
  const counts = { backend: 0, frontend: 0, sql: 0 }
  for (const file of state.previewFiles) {
    counts[previewGroupOf(file.path)] += 1
  }
  return counts
})
const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)
const dictCodeOptions = computed(() => toDictCodeTreeOptions(dictDataAll()))
const formRules = computed<FormRules>(() => {
  const rules: FormRules = {
    name: createRequiredRule('方案名称', 'input'),
    author: createRequiredRule('作者', 'input'),
    gen_type: createRequiredRule('生成类型', 'change'),
    table_name: createRequiredRule('主表', 'change'),
    pk_column: createRequiredRule('主键', 'change'),
    entity_name: createRequiredRule('主实体名', 'input'),
    module_path: createRequiredRule('模块路径', 'input'),
    business_name: createRequiredRule('业务名称', 'input'),
    api_prefix: createRequiredRule('接口前缀', 'input'),
    permission_prefix: createRequiredRule('权限前缀', 'input'),
    menu_name: createRequiredRule('菜单名称', 'input'),
    menu_path: createRequiredRule('菜单路径', 'input'),
    component_path: createRequiredRule('组件路径', 'input'),
  }
  if (needsTree.value) {
    rules.tree_parent_field = createRequiredRule('父级字段', 'change')
    rules.tree_label_field = createRequiredRule('展示字段', 'change')
  }
  if (needsSub.value) {
    rules.sub_table = createRequiredRule('子表', 'change')
    rules.sub_pk = createRequiredRule('子表主键', 'change')
    rules.sub_foreign_key = createRequiredRule('子表外键', 'change')
    rules.sub_entity_name = createRequiredRule('子实体名', 'input')
    rules.sub_business_name = createRequiredRule('子业务名称', 'input')
  }
  return rules
})

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values)
    state.page = 1
    fetchPage()
  },
  onReset() {
    state.searchValues = {}
    state.page = 1
    fetchPage()
  },
})

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  { title: '方案名称', path: 'name', field: 'input' },
  { title: '主表', path: 'table_name', field: 'input' },
  { title: '生成类型', path: 'gen_type', field: 'select', fieldProps: { options: genTypeOptions } },
])

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => `${itemCount} 条`,
  onUpdatePage: (value) => {
    state.page = value
    fetchPage()
  },
  onUpdatePageSize: (value) => {
    state.pageSize = value
    state.page = 1
    fetchPage()
  },
}))

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  { type: 'selection', fixed: 'left' },
  {
    title: '方案名称',
    path: 'name',
    width: 140,
    render: (row) => renderCellEllipsis(row.name),
  },
  {
    title: '生成类型',
    path: 'gen_type',
    width: 96,
    render: (row) =>
      renderCellEllipsis(
        genTypeShortLabels[row.gen_type] ?? row.gen_type,
        formatGenTypeLabel(row.gen_type),
      ),
  },
  {
    title: '作者',
    path: 'author',
    width: 100,
    render: (row) => renderCellEllipsis(row.author),
  },
  {
    title: '主表',
    path: 'table_name',
    width: 150,
    render: (row) => renderCellEllipsis(row.table_name),
  },
  {
    title: '子表',
    path: 'sub_table',
    width: 150,
    render: (row) => renderCellEllipsis(row.sub_table),
  },
  {
    title: '模块路径',
    path: 'module_path',
    width: 180,
    render: (row) => renderCellEllipsis(row.module_path),
  },
  {
    title: '权限前缀',
    path: 'permission_prefix',
    width: 150,
    render: (row) => renderCellEllipsis(row.permission_prefix),
  },
  {
    title: '更新时间',
    path: 'updated_at',
    width: 190,
    render: (row) => formatDateTime(row.updated_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 230,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('sys:codegen:update') ? (
          <NButton type="primary" text={true} onClick={() => openForm(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('sys:codegen:tables') ? (
          <NButton type="info" text={true} onClick={() => openFields(row.id)}>
            {renderButtonIcon('icon-park-outline:list-view')}
          </NButton>
        ) : null}
        {hasPermission('sys:codegen:preview') ? (
          <NButton type="info" text={true} onClick={() => openPreview(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('sys:codegen:download') ? (
          <NButton
            type="primary"
            text={true}
            loading={state.downloadingId === row.id}
            onClick={() => download(row.id)}
          >
            {renderButtonIcon('icon-park-outline:download')}
          </NButton>
        ) : null}
        {hasPermission('sys:codegen:delete') ? (
          <NButton type="error" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const fieldColumns = computed(() => [
  {
    title: '',
    key: 'drag',
    width: 44,
    render: () => (
      <span class="codegen-field-drag-handle" title="拖拽排序">
        <Icon icon="icon-park-outline:drag" />
      </span>
    ),
  },
  {
    title: '序号',
    key: 'sort',
    width: 64,
    render: (_row: any, index: number) => index + 1,
  },
  { title: '表', key: 'table_role', width: 72 },
  { title: '字段', key: 'column_name', width: 120, render: (row: any) => renderCellEllipsis(row.column_name) },
  {
    title: '注释',
    key: 'label',
    width: 130,
    render: (row: any) => (
      <NInput
        value={row.label}
        onUpdateValue={(value: string) => (row.label = value)}
      />
    ),
  },
  {
    title: '数据库类型',
    key: 'db_type',
    width: 110,
    render: (row: any) => renderCellEllipsis(row.db_type || '-', row.db_type || undefined),
  },
  {
    title: '语言类型',
    key: 'value_type',
    width: 120,
    render: (row: any) => renderCellEllipsis(formatValueType(row.value_type), row.value_type || undefined),
  },
  {
    title: '前端类型',
    key: 'ui_type',
    width: 120,
    render: (row: any) => renderCellEllipsis(formatUiType(row.ui_type), row.ui_type || undefined),
  },
  {
    title: '表单控件',
    key: 'widget',
    width: 150,
    render: (row: any) => (
      <NSelect
        value={row.widget}
        options={widgetOptions}
        onUpdateValue={(value: string) => handleWidgetUpdate(row, value)}
      />
    ),
  },
  {
    title: '字典',
    key: 'dict_code',
    width: 220,
    render: (row: any) => (
      <NTreeSelect
        value={row.dict_code || null}
        options={dictCodeOptions.value}
        clearable={true}
        filterable={true}
        disabled={row.widget !== 'dict'}
        placeholder="选择字典"
        onUpdateValue={(value: string | number | Array<string | number> | null) =>
          handleDictCodeUpdate(row, value)
        }
      />
    ),
  },
  {
    title: '查询',
    key: 'query_operator',
    width: 120,
    render: (row: any) => (
      <NSelect
        value={row.query_operator}
        options={operatorOptions}
        onUpdateValue={(value: string | null) => (row.query_operator = value)}
      />
    ),
  },
  {
    title: '表格',
    key: 'in_table',
    width: 80,
    render: (row: any) => (
      <NCheckbox
        checked={wireBool(row.in_table)}
        onUpdateChecked={(value: boolean) => (row.in_table = value)}
      />
    ),
  },
  {
    title: '表单',
    key: 'in_form',
    width: 80,
    render: (row: any) => (
      <NCheckbox
        checked={wireBool(row.in_form)}
        onUpdateChecked={(value: boolean) => (row.in_form = value)}
      />
    ),
  },
  {
    title: '详情',
    key: 'in_detail',
    width: 80,
    render: (row: any) => (
      <NCheckbox
        checked={wireBool(row.in_detail)}
        onUpdateChecked={(value: boolean) => (row.in_detail = value)}
      />
    ),
  },
  {
    title: '检索',
    key: 'in_query',
    width: 80,
    render: (row: any) => (
      <NCheckbox
        checked={wireBool(row.in_query)}
        onUpdateChecked={(value: boolean) => (row.in_query = value)}
      />
    ),
  },
  {
    title: '必填',
    key: 'required',
    width: 72,
    render: (row: any) => (
      <NCheckbox
        checked={wireBool(row.required)}
        onUpdateChecked={(value: boolean) => (row.required = value)}
      />
    ),
  },
])

onMounted(async () => {
  const tasks: Promise<void>[] = [fetchPage(), fetchModules(), fetchParentResources(), refreshDict()]
  if (hasPermission('sys:codegen:tables')) {
    tasks.push(fetchTables())
  }
  await Promise.all(tasks)
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await codegenApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.rows = data.records ?? []
    state.page = pageMeta.current
    state.pageSize = pageMeta.size
    state.total = pageMeta.total
  } finally {
    state.loading = false
  }
}

async function fetchTables() {
  if (!hasPermission('sys:codegen:tables')) {
    state.tableOptions = []
    return
  }
  const response = await codegenApi.tables()
  state.tableOptions = (response.data ?? []).map((item: any) => ({
    label: item.table_comment ? `${item.table_name} - ${item.table_comment}` : item.table_name,
    value: item.table_name,
  }))
}

async function fetchModules() {
  const response = await resourceModuleApi.selector({ client: 'ADMIN' })
  state.moduleOptions = (response.data ?? []).map((item: any) => ({
    label: item.name,
    value: String(item.id),
  }))
}

async function fetchParentResources(moduleId = state.form.resource_module_id) {
  const response = await codegenApi.parentResources({ module_id: moduleId || undefined })
  state.parentOptions = toTreeOptions(response.data ?? [])
}

async function fetchColumns(tableName: string, target: 'main' | 'sub') {
  if (!tableName) {
    if (target === 'main') {
      state.mainColumnOptions = []
    } else {
      state.subColumnOptions = []
    }
    return
  }
  const response = await codegenApi.tableColumns({ table_name: tableName })
  const options = (response.data ?? []).map((item: any) => ({
    label: `${item.column_name}${wireBool(item.primary_key) ? ' (主键)' : ''}${item.label ? ` - ${item.label}` : ''}`,
    value: item.column_name,
    isPrimaryKey: wireBool(item.primary_key),
  }))
  if (target === 'main') {
    state.mainColumnOptions = options
  } else {
    state.subColumnOptions = options
  }
}

function openCreateForm() {
  state.editingId = null
  state.form = { ...defaultForm }
  state.mainColumnOptions = []
  state.subColumnOptions = []
  state.showForm = true
}

async function openForm(id: string) {
  state.editingId = id
  state.showForm = true
  state.formLoading = true
  try {
    const response = await codegenApi.detail({ id })
    const data = response.data ?? {}
    state.form = {
      ...defaultForm,
      ...data,
      sort:
        data.sort !== undefined && data.sort !== null && data.sort !== ''
          ? wireInt(String(data.sort))
          : defaultForm.sort,
    }
    await Promise.all([
      fetchColumns(state.form.table_name, 'main'),
      state.form.sub_table ? fetchColumns(state.form.sub_table, 'sub') : Promise.resolve(),
      fetchParentResources(state.form.resource_module_id),
    ])
  } finally {
    state.formLoading = false
  }
}

async function handleMainTableUpdate(value: string) {
  state.form.table_name = value
  await fetchColumns(value, 'main')
  state.form.pk_column = resolvePrimaryColumn('main', state.form.pk_column)
  if (!state.editingId) {
    const entity = toPascalCase(value)
    const backendPath = value.replaceAll('-', '_')
    const routePath = value.replaceAll('_', '-')
    state.form.entity_name = entity
    state.form.business_name = entity
    state.form.module_path = `biz/${backendPath}`
    state.form.api_prefix = `/biz/${routePath}`
    state.form.permission_prefix = `biz:${routePath.replaceAll('-', '')}`
    state.form.menu_name = entity
    state.form.menu_path = `/biz/${routePath}`
    state.form.component_path = `biz/${routePath}/index.vue`
  }
}

async function handleSubTableUpdate(value: string) {
  state.form.sub_table = value
  await fetchColumns(value, 'sub')
  state.form.sub_pk = resolvePrimaryColumn('sub', state.form.sub_pk)
  if (!state.editingId) {
    state.form.sub_entity_name = toPascalCase(value)
    state.form.sub_business_name = toPascalCase(value)
  }
}

async function handleResourceModuleUpdate(value: string | null) {
  state.form.resource_module_id = value
  state.form.parent_resource_id = null
  await fetchParentResources(value)
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = { ...state.form }
    if (!needsTree.value) {
      payload.tree_parent_field = null as any
      payload.tree_label_field = null as any
    }
    if (!needsSub.value) {
      payload.sub_table = null as any
      payload.sub_pk = null as any
      payload.sub_foreign_key = null as any
      payload.sub_entity_name = null as any
      payload.sub_business_name = null as any
    }
    if (state.editingId) {
      await codegenApi.update({ ...payload, id: state.editingId })
      window.$message.success('更新成功')
    } else {
      await codegenApi.create(payload)
      window.$message.success('创建成功')
    }
    state.showForm = false
    await fetchPage()
  } finally {
    state.submitLoading = false
  }
}

async function openFields(id: string) {
  state.fieldPlanId = id
  state.showFields = true
  state.fieldLoading = true
  try {
    const response = await codegenApi.fields({ plan_id: id })
    const rows = [...(response.data ?? [])]
      .map((row: any) => normalizeCodegenFieldRow(row))
      .sort((a: any, b: any) => (a.sort ?? 0) - (b.sort ?? 0))
    normalizeFieldSort(rows)
    state.fieldRows = rows
  } finally {
    state.fieldLoading = false
  }
}

async function saveFields() {
  state.fieldSaving = true
  try {
    normalizeFieldSort(state.fieldRows)
    await codegenApi.updateFieldsBatch({
      plan_id: state.fieldPlanId,
      fields: state.fieldRows,
    })
    window.$message.success('字段配置已保存')
    state.showFields = false
  } finally {
    state.fieldSaving = false
  }
}

async function openPreview(id: string) {
  state.showPreview = true
  state.previewLoading = true
  state.previewFiles = []
  state.previewPath = ''
  state.previewGroup = 'backend'
  try {
    const response = await codegenApi.preview({ id })
    state.previewFiles = response.data?.files ?? []
    const first =
      state.previewFiles.find((file) => previewGroupOf(file.path) === state.previewGroup) ??
      state.previewFiles[0]
    if (first) {
      state.previewGroup = previewGroupOf(first.path)
      state.previewPath = first.path
    }
  } finally {
    state.previewLoading = false
  }
}

function selectPreviewGroup(group: 'backend' | 'frontend' | 'sql') {
  state.previewGroup = group
  const first = state.previewFiles.find((file) => previewGroupOf(file.path) === group)
  state.previewPath = first?.path ?? ''
}

async function download(id: string) {
  state.downloadingId = id
  try {
    await codegenApi.downloadZip(id)
  } finally {
    state.downloadingId = ''
  }
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) {
    return
  }
  window.$dialog.warning({
    title: ids.length > 1 ? '批量删除' : '删除',
    content: ids.length > 1 ? `删除 ${ids.length} 个生成方案?` : '删除该生成方案?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => deleteRows(ids),
  })
}

async function deleteRows(ids: string[]) {
  await codegenApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))
  window.$message.success('删除成功')
  await fetchPage()
}

function findColumn(target: 'main' | 'sub', name: string) {
  const options = target === 'main' ? state.mainColumnOptions : state.subColumnOptions
  return options.find((item) => item.value === name)?.value
}

function resolvePrimaryColumn(target: 'main' | 'sub', currentValue?: string | null) {
  const options = target === 'main' ? state.mainColumnOptions : state.subColumnOptions
  const primaryOption = options.find((item) => item.isPrimaryKey)
  if (primaryOption) {
    return primaryOption.value
  }
  if (currentValue && options.some((item) => item.value === currentValue)) {
    return currentValue
  }
  return findColumn(target, 'id') || options[0]?.value || ''
}

function previewTabLabel(path: string) {
  const parts = path.split('/').filter(Boolean)
  if (path.startsWith('module/')) {
    return parts.slice(-2).join('/')
  }
  return parts.slice(-2).join('/') || path
}

function toPascalCase(value: string) {
  return value
    .split(/[_\-\s]+/)
    .filter(Boolean)
    .map((item) => item.charAt(0).toUpperCase() + item.slice(1))
    .join('')
}

function toTreeOptions(items: any[]): any[] {
  return items.map((item) => ({
    label: item.name,
    key: String(item.id),
    children: item.children?.length ? toTreeOptions(item.children) : undefined,
  }))
}

function toDictCodeTreeOptions(items: any[]): any[] {
  return items.map((item) => ({
    label: toDictOptionLabel(item),
    key: item.code,
    disabled: item.status !== undefined && item.status !== null && item.status !== 'ENABLED',
    children: item.children?.length
      ? toDisabledDictItemOptions(item.children, item.code)
      : undefined,
  }))
}

function toDisabledDictItemOptions(items: any[], parentCode: string): any[] {
  return items.map((item) => ({
    label: toDictOptionLabel(item),
    key: `${parentCode}:${item.code}`,
    disabled: true,
    children: item.children?.length
      ? toDisabledDictItemOptions(item.children, `${parentCode}:${item.code}`)
      : undefined,
  }))
}

function toDictOptionLabel(item: any) {
  const label = item.label || item.name || item.code
  return label && label !== item.code ? `${label} (${item.code})` : item.code
}

function handleWidgetUpdate(row: any, value: string) {
  row.widget = value
  if (value !== 'dict') {
    row.dict_code = null
  }
}

function handleDictCodeUpdate(row: any, value: string | number | Array<string | number> | null) {
  row.dict_code = Array.isArray(value) ? null : value === null ? null : String(value)
}
</script>

<template>
  <NFlex
    class="h-full min-h-0"
    vertical
  >
    <ProCard>
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: '重置' }"
        :search-button-props="{ content: '搜索' }"
        :collapse-button-props="{ content: searchForm.collapsed.value ? '展开' : '收起' }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      title="代码生成"
      row-key="id"
      :scroll-x="1320"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton
            v-if="hasPermission('sys:codegen:create')"
            type="primary"
            text
            title="新增"
            @click="openCreateForm"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:plus" /></NIcon>
            </template>
          </NButton>
          <NButton
            text
            title="刷新"
            :loading="state.loading"
            @click="fetchPage"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:reload" /></NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('sys:codegen:delete')"
            type="error"
            text
            title="批量删除"
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:delete" /></NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <NModal
      v-model:show="state.showForm"
      preset="card"
      draggable
      :mask-closable="false"
      :title="state.editingId ? '编辑生成方案' : '新增生成方案'"
      style="width: min(980px, 96vw)"
      :segmented="{ content: true, action: true }"
    >
      <NSpin :show="state.formLoading">
        <NScrollbar class="hei-modal-scroll">
          <NForm
            ref="formRef"
            :model="state.form"
            :rules="formRules"
            label-placement="left"
            label-width="116"
          >
            <NAlert
              v-if="currentGenTypeHelp"
              type="default"
              class="mb-16px"
              :bordered="false"
            >
              {{ currentGenTypeHelp }}
            </NAlert>
            <NGrid
              :cols="2"
              :x-gap="16"
            >
              <NGi>
                <NFormItem
                  label="方案名称"
                  path="name"
                >
                  <NInput v-model:value="state.form.name" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="作者"
                  path="author"
                >
                  <NInput v-model:value="state.form.author" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="生成类型"
                  path="gen_type"
                >
                  <NSelect
                    v-model:value="state.form.gen_type"
                    :options="genTypeOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="主表"
                  path="table_name"
                >
                  <NSelect
                    v-model:value="state.form.table_name"
                    filterable
                    :options="state.tableOptions"
                    @update:value="handleMainTableUpdate"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="主键"
                  path="pk_column"
                >
                  <NSelect
                    v-model:value="state.form.pk_column"
                    filterable
                    :options="state.mainColumnOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="主实体名"
                  path="entity_name"
                >
                  <NInput
                    v-model:value="state.form.entity_name"
                    placeholder="如 Order / CgTestOrder"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="业务名称"
                  path="business_name"
                >
                  <NInput v-model:value="state.form.business_name" />
                </NFormItem>
              </NGi>
              <NGi :span="2">
                <NFormItem
                  label="模块路径"
                  path="module_path"
                >
                  <NFlex
                    vertical
                    class="w-full"
                    :size="8"
                  >
                    <NInput
                      v-model:value="state.form.module_path"
                      placeholder="biz/order（首段=业务模块，其后=功能路径）"
                    />
                    <div
                      v-if="state.form.module_path"
                      class="codegen-package-preview"
                    >
                      <div>功能路径：{{ packagePreview.featurePath }}</div>
                      <div>模块输出根：{{ packagePreview.moduleRoot }}</div>
                    </div>
                  </NFlex>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="接口前缀"
                  path="api_prefix"
                >
                  <NInput v-model:value="state.form.api_prefix" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="权限前缀"
                  path="permission_prefix"
                >
                  <NInput v-model:value="state.form.permission_prefix" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="菜单名称"
                  path="menu_name"
                >
                  <NInput v-model:value="state.form.menu_name" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="菜单路径"
                  path="menu_path"
                >
                  <NInput v-model:value="state.form.menu_path" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="组件路径"
                  path="component_path"
                >
                  <NInput v-model:value="state.form.component_path" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="资源模块">
                  <NSelect
                    v-model:value="state.form.resource_module_id"
                    clearable
                    :options="state.moduleOptions"
                    @update:value="handleResourceModuleUpdate"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="父级菜单">
                  <NTreeSelect
                    v-model:value="state.form.parent_resource_id"
                    clearable
                    filterable
                    :options="state.parentOptions"
                    key-field="key"
                    label-field="label"
                    children-field="children"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="图标">
                  <IconSelect v-model:value="state.form.icon" />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="排序">
                  <NInputNumber
                    v-model:value="state.form.sort"
                    class="w-full"
                    :min="0"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsTree">
                <NFormItem
                  label="父级字段"
                  path="tree_parent_field"
                >
                  <NSelect
                    v-model:value="state.form.tree_parent_field"
                    filterable
                    :options="state.mainColumnOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsTree">
                <NFormItem
                  label="展示字段"
                  path="tree_label_field"
                >
                  <NSelect
                    v-model:value="state.form.tree_label_field"
                    filterable
                    :options="state.mainColumnOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsSub">
                <NFormItem
                  label="子表"
                  path="sub_table"
                >
                  <NSelect
                    v-model:value="state.form.sub_table"
                    filterable
                    :options="state.tableOptions"
                    @update:value="handleSubTableUpdate"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsSub">
                <NFormItem
                  label="子表主键"
                  path="sub_pk"
                >
                  <NSelect
                    v-model:value="state.form.sub_pk"
                    filterable
                    :options="state.subColumnOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsSub">
                <NFormItem
                  label="子表外键"
                  path="sub_foreign_key"
                >
                  <NSelect
                    v-model:value="state.form.sub_foreign_key"
                    filterable
                    :options="state.subColumnOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsSub">
                <NFormItem
                  label="子实体名"
                  path="sub_entity_name"
                >
                  <NInput
                    v-model:value="state.form.sub_entity_name"
                    placeholder="如 OrderItem"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="needsSub">
                <NFormItem
                  label="子业务名称"
                  path="sub_business_name"
                >
                  <NInput v-model:value="state.form.sub_business_name" />
                </NFormItem>
              </NGi>
            </NGrid>
            <NFormItem label="描述">
              <NInput
                v-model:value="state.form.description"
                type="textarea"
              />
            </NFormItem>
          </NForm>
        </NScrollbar>
      </NSpin>
      <template #action>
        <NSpace justify="end">
          <NButton @click="state.showForm = false">
            取消
          </NButton>
          <NButton
            type="primary"
            :loading="state.submitLoading"
            @click="submitForm"
          >
            确认
          </NButton>
        </NSpace>
      </template>
    </NModal>

    <NDrawer
      v-model:show="state.showFields"
      width="min(1280px, 96vw)"
    >
      <NDrawerContent title="字段配置">
        <NAlert
          type="info"
          class="mb-12px"
          :bordered="false"
        >
          「数据库类型 / 语言类型 / 前端类型」为只读映射；拖拽左侧手柄调整顺序，序号会自动更新并随保存写入 sort。
        </NAlert>
        <div ref="fieldTableWrapRef">
          <NDataTable
            class="codegen-field-table"
            :row-key="(row: any) => row.id || `${row.table_role}-${row.column_name}`"
            :columns="fieldColumns"
            :data="state.fieldRows"
            :loading="state.fieldLoading"
            :scroll-x="1560"
            :pagination="false"
          />
        </div>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="state.showFields = false">
              取消
            </NButton>
            <NButton
              type="primary"
              :loading="state.fieldSaving"
              @click="saveFields"
            >
              保存
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <NDrawer
      v-model:show="state.showPreview"
      width="min(1320px, 96vw)"
    >
      <NDrawerContent
        title="代码预览"
        closable
        body-content-class="codegen-preview-drawer"
      >
        <NSpin :show="state.previewLoading">
          <div class="codegen-preview">
            <NFlex
              v-if="state.previewFiles.length"
              vertical
              :size="12"
            >
              <NFlex :size="8">
                <NButton
                  size="small"
                  :type="state.previewGroup === 'backend' ? 'primary' : 'default'"
                  @click="selectPreviewGroup('backend')"
                >
                  后端 ({{ previewGroupCounts.backend }})
                </NButton>
                <NButton
                  size="small"
                  :type="state.previewGroup === 'frontend' ? 'primary' : 'default'"
                  @click="selectPreviewGroup('frontend')"
                >
                  前端 ({{ previewGroupCounts.frontend }})
                </NButton>
                <NButton
                  size="small"
                  :type="state.previewGroup === 'sql' ? 'primary' : 'default'"
                  @click="selectPreviewGroup('sql')"
                >
                  SQL ({{ previewGroupCounts.sql }})
                </NButton>
              </NFlex>
              <NTabs
                v-if="previewGroupedFiles.length"
                v-model:value="state.previewPath"
                type="card"
                size="small"
                animated
              >
                <NTabPane
                  v-for="file in previewGroupedFiles"
                  :key="file.path"
                  :name="file.path"
                >
                  <template #tab>
                    <NFlex
                      :size="6"
                      align="center"
                    >
                      <NTag
                        size="tiny"
                        :bordered="false"
                      >
                        {{ previewLanguageLabel(file.language, file.path) }}
                      </NTag>
                      <span>{{ previewTabLabel(file.path) }}</span>
                    </NFlex>
                  </template>
                  <div
                    v-if="state.previewPath === file.path"
                    class="codegen-preview__file"
                  >
                    <div
                      class="codegen-preview__path"
                      :title="file.path"
                    >
                      {{ file.path }}
                    </div>
                    <MonacoPreview
                      :value="file.content"
                      :language="file.language"
                      height="calc(100vh - 220px)"
                      :options="previewEditorOptions"
                    />
                  </div>
                </NTabPane>
              </NTabs>
              <NEmpty
                v-else
                description="当前分组暂无文件"
              />
            </NFlex>
            <NEmpty
              v-else
              description="暂无预览文件"
            />
          </div>
        </NSpin>
      </NDrawerContent>
    </NDrawer>
  </NFlex>
</template>

<style scoped>
.codegen-package-preview {
  padding: 8px 12px;
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.03);
  color: var(--n-text-color-3);
  font-family: var(--n-font-family-mono);
  font-size: 12px;
  line-height: 1.6;
}

.codegen-preview {
  min-height: calc(100vh - 120px);
}

:deep(.codegen-preview-drawer) {
  padding: 16px;
}

.codegen-preview :deep(.n-tabs) {
  height: 100%;
}

.codegen-preview :deep(.n-tabs-nav) {
  overflow: hidden;
}

.codegen-preview :deep(.n-tab-pane) {
  padding: 0;
}

.codegen-preview__file {
  min-width: 0;
}

.codegen-preview__path {
  min-width: 0;
  padding: 8px 12px;
  overflow: hidden;
  border-right: 1px solid var(--n-border-color);
  border-left: 1px solid var(--n-border-color);
  background: rgba(0, 0, 0, 0.04);
  color: var(--n-text-color);
  font-family: var(--n-font-family-mono);
  font-size: 12px;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.codegen-cell-ellipsis {
  max-width: 100%;
}

:deep(.codegen-field-table .codegen-cell-ellipsis) {
  display: block;
}

.codegen-field-drag-handle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--n-text-color-3);
  cursor: grab;
  border-radius: 4px;
}

.codegen-field-drag-handle:active {
  cursor: grabbing;
}

.codegen-field-drag-handle:hover {
  color: var(--n-text-color-2);
  background: rgba(0, 0, 0, 0.04);
}

:deep(.codegen-field-table .sortable-ghost) {
  opacity: 0.45;
}

:deep(.codegen-field-table .sortable-drag) {
  background: var(--n-color-hover);
}
</style>
