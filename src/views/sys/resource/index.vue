<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="资源名称" allow-clear />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTreeTable
      ref="treeTableRef"
      :columns="columns"
      :data-source="treeData"
      :loading="loading"
      default-expand-all
      @refresh="loadTree"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:resource:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增资源
        </a-button>
        <a-button
          v-if="hasPermission('sys:resource:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button v-if="hasPermission('sys:resource:import')" @click="importOpen = true">
          <template #icon><UploadOutlined /></template>
          导入
        </a-button>
        <a-button v-if="hasPermission('sys:resource:export')" @click="exportOpen = true">
          <template #icon><DownloadOutlined /></template>
          导出
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'category'">
          <a-tag>{{ categoryMap[record.category] || record.category || '-' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'type'">
          <a-tag>{{ typeMap[record.type] || record.type || '-' }}</a-tag>
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
              v-if="hasPermission('sys:resource:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-dropdown v-if="hasPermission('sys:resource:create') && record.type === 'DIRECTORY' || (hasPermission('sys:resource:modify') && record.type === 'MENU')">
              <a-button type="link" size="small">
                更多
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item v-if="hasPermission('sys:resource:create') && record.type === 'DIRECTORY'" @click="openCreate(record)">
                    新增子级
                  </a-menu-item>
                  <a-menu-item
                    v-if="hasPermission('sys:resource:modify') && record.type === 'MENU'"
                    @click="openButtonManager(record)"
                  >
                    权限按钮
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-popconfirm
              v-if="hasPermission('sys:resource:remove')"
              title="确定删除该资源？子级将一并删除"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTreeTable>

    <AppImportModal
      ref="importModalRef"
      :open="importOpen"
      template-text="下载资源导入模板"
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
    <ButtonManager ref="buttonManagerRef" v-model:open="buttonManagerOpen" @success="loadTree" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysResource' })
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { resourceApi, fetchResourceTree } from '@/api/resource'
import { downloadBlob, confirmDelete } from '@/utils'
import AppTreeTable from '@/components/table/AppTreeTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'
import ButtonManager from './components/buttonManager.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const treeTableRef = ref()

const categoryMap: Record<string, string> = {
  BACKEND_MENU: '后台菜单',
  FRONTEND_MENU: '前台菜单',
  BACKEND_BUTTON: '后台按钮',
  FRONTEND_BUTTON: '前台按钮',
}

const typeMap: Record<string, string> = {
  DIRECTORY: '目录',
  MENU: '菜单',
  BUTTON: '按钮',
  INTERNAL_LINK: '内链',
  EXTERNAL_LINK: '外链',
}

// ── Tree data ──
const loading = ref(false)
const treeData = ref<any[]>([])
const treeDataOrigin = ref<any[]>([])

function stripButtons(nodes: any[]): any[] {
  return nodes.reduce((acc: any[], node: any) => {
    if (node.type === 'BUTTON') return acc
    const children = node.children ? stripButtons(node.children) : []
    acc.push({ ...node, children })
    return acc
  }, [])
}

function filterTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node: any) => {
    const match = node.name?.includes(keyword) || node.code?.includes(keyword)
    const filteredChildren = node.children ? filterTree(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

async function loadTree() {
  loading.value = true
  try {
    const { data } = await fetchResourceTree()
    treeDataOrigin.value = data || []
    treeData.value = filterTree(stripButtons(data || []), searchForm.keyword)
  } finally {
    loading.value = false
  }
}

// ── Search ──
const searchForm = reactive({ keyword: '' })
const selectedKeys = ref<string[]>([])

watch(() => searchForm.keyword, (val) => {
  treeData.value = filterTree(stripButtons(treeDataOrigin.value), val)
})

const columns = [
  { title: '资源名称', dataIndex: 'name', key: 'name', width: 220 },
  { title: '资源编码', dataIndex: 'code', key: 'code', width: 160, ellipsis: true },
  { title: '资源分类', dataIndex: 'category', key: 'category', width: 110 },
  { title: '资源类型', dataIndex: 'type', key: 'type', width: 90 },
  { title: '路由路径', dataIndex: 'route_path', key: 'route_path', width: 200, ellipsis: true },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' },
]

// ── Drawers ──
const detailRef = ref()
const formRef = ref()
const buttonManagerRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)
const buttonManagerOpen = ref(false)

function openDetail(record: any) { detailRef.value?.doOpen(record) }
function openEdit(record: any) { formRef.value?.doOpen(record) }
function openCreate(parent?: any) { formRef.value?.doOpen(undefined, parent?.id) }
function openButtonManager(record: any) { buttonManagerRef.value?.doOpen(record) }

async function handleDelete(id: string) {
  const { success } = await resourceApi.remove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    loadTree()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '资源',
    selectedKeys: selectedKeys.value,
    deleteApi: resourceApi.remove,
    onSuccess: () => {
      selectedKeys.value = []
      loadTree()
    },
  })
}

function handleFormSuccess() {
  loadTree()
}

function handleSearch() { loadTree() }

function resetSearch() {
  searchForm.keyword = ''
  loadTree()
}

// ── Import / Export / Template ──
const importOpen = ref(false)
const exportOpen = ref(false)
const templateLoading = ref(false)
const importModalRef = ref()

async function handleDownloadTemplate() {
  templateLoading.value = true
  try {
    const blob = await resourceApi.template()
    downloadBlob(blob, '资源导入模板.xlsx')
  } catch { message.error('下载模板失败') }
  finally { templateLoading.value = false }
}

async function handleExportWithParams(params: any) {
  try {
    const blob = await resourceApi.export(params)
    downloadBlob(blob, `资源数据_${new Date().toLocaleDateString()}.xlsx`)
    message.success('导出成功')
    exportOpen.value = false
  } catch { message.error('导出失败') }
}

async function handleImport(file: File) {
  try {
    const { success, data } = await resourceApi.importFile(file)
    if (success && data) {
      importModalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
      message.success('导入成功')
      loadTree()
    }
  } catch {
    importModalRef.value?.setResult({ success: false, message: '导入文件格式错误' })
  }
}

onMounted(() => {
  loadTree()
})
</script>
