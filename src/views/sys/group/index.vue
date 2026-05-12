<template>
  <AppSplitPanel ref="splitRef" v-model:collapsed="splitCollapsed" :initial-size="280" :min-size="200" :max-size="400" :md="0">
    <template #left>
      <a-card size="small" class="h-full flex flex-col max-md:hidden" :body-style="{ flex: '1', overflow: 'auto', padding: '12px' }">
        <a-input-search v-model:value="orgSearchKey" placeholder="搜索组织" allow-clear class="mb-2" />
        <a-spin :spinning="orgTreeLoading">
          <a-tree
            v-if="orgTreeData.length"
            v-model:expanded-keys="orgExpandedKeys"
            :tree-data="orgTreeData"
            :field-names="{ children: 'children', title: 'name', key: 'id' }"
            :selected-keys="selectedOrgKeys"
            block-node
            show-line
            class="mb-3"
            @select="handleOrgSelect"
          >
          </a-tree>
          <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
        </a-spin>
      </a-card>
    </template>
    <template #right>
      <div class="flex flex-col h-full overflow-auto gap-2">
        <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
            <a-button type="text" size="small" class="max-md:hidden" @click="splitRef?.toggleCollapse()">
              <template #icon>
                <component :is="splitCollapsed ? DoubleRightOutlined : DoubleLeftOutlined" />
              </template>
            </a-button>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="关键词" name="keyword">
              <a-input v-model:value="searchForm.keyword" placeholder="用户组名称" allow-clear />
            </a-form-item>
          </a-col>
            <a-button type="text" size="small" class="md:hidden" @click="mobileTreeOpen = true">
              <template #icon><FolderOutlined /></template>
            </a-button>
        </AppSearchPanel>

        <AppTreeTable
          ref="treeTableRef"
          :columns="columns"
          :data-source="treeTableData"
          :loading="treeTableLoading"
          :row-selection="rowSelection"
          default-expand-all
          @refresh="loadGroupTree"
        >
          <template #toolbar>
            <a-button v-if="hasPermission('sys:group:create')" type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              新增用户组
            </a-button>
            <a-button
              v-if="hasPermission('sys:group:remove')"
              danger
              :disabled="selectedKeys.length === 0"
              @click="handleBatchDelete"
            >
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
            <a-button v-if="hasPermission('sys:group:import')" @click="importOpen = true">
              <template #icon><UploadOutlined /></template>
              导入
            </a-button>
            <a-button v-if="hasPermission('sys:group:export')" @click="exportOpen = true">
              <template #icon><DownloadOutlined /></template>
              导出
            </a-button>
          </template>

          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'category'">
              <a-tag>{{ categoryMap[record.category] || record.category || '-' }}</a-tag>
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
                <a-popconfirm
                  v-if="hasPermission('sys:group:remove')"
                  title="确定删除该用户组？如有子级将一并删除"
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
          template-text="下载用户组导入模板"
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
  </AppSplitPanel>

  <a-drawer
    :open="mobileTreeOpen"
    title="组织"
    placement="left"
    :width="300"
    destroy-on-close
    @close="mobileTreeOpen = false"
  >
    <a-input-search v-model:value="orgSearchKey" placeholder="搜索组织" allow-clear class="mb-2" />
    <a-spin :spinning="orgTreeLoading">
      <a-tree
        v-if="orgTreeData.length"
        v-model:expanded-keys="orgExpandedKeys"
        :tree-data="orgTreeData"
        :field-names="{ children: 'children', title: 'name', key: 'id' }"
        :selected-keys="selectedOrgKeys"
        block-node
        show-line
        class="mb-3"
        @select="handleOrgSelect"
      >
        <template #switcherIcon="{ expanded }">
          <CaretDownOutlined :class="expanded ? '' : '-rotate-90'" class="text-[12px]" />
        </template>
        <template #icon>
          <BankOutlined class="text-[var(--primary-color)]" />
        </template>
      </a-tree>
      <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysGroup' })
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  DoubleLeftOutlined,
  DoubleRightOutlined,
  FolderOutlined,
  BankOutlined,
  CaretDownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import {
  fetchGroupTree,
  fetchGroupRemove,
  fetchGroupExport,
  fetchGroupTemplate,
  fetchGroupImport,
} from '@/api/group'
import { fetchOrgTree } from '@/api/org'
import { downloadBlob, confirmDelete } from '@/utils'
import AppSplitPanel from '@/components/layout/AppSplitPanel.vue'
import AppTreeTable from '@/components/table/AppTreeTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const treeTableRef = ref()
const splitRef = ref()
const splitCollapsed = ref(false)
const mobileTreeOpen = ref(false)

const categoryMap: Record<string, string> = {
  ROLE: '角色组',
  DEPT: '部门组',
  PROJECT: '项目组',
  OTHER: '其他',
}

// ── Org tree ──
const orgTreeLoading = ref(false)
const orgTreeData = ref<any[]>([])
const orgTreeDataOrigin = ref<any[]>([])
const orgExpandedKeys = ref<string[]>([])
const selectedOrgKeys = ref<string[]>([])
const orgSearchKey = ref('')

function filterOrgTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const match = node.name?.includes(keyword)
    const filteredChildren = node.children ? filterOrgTree(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

watch(orgSearchKey, (val) => {
  orgTreeData.value = filterOrgTree(orgTreeDataOrigin.value, val)
  if (val) {
    const getAllKeys = (nodes: any[]): string[] => nodes.reduce((keys: string[], n) => {
      keys.push(n.id)
      if (n.children) keys.push(...getAllKeys(n.children))
      return keys
    }, [])
    orgExpandedKeys.value = getAllKeys(orgTreeData.value)
  }
})

async function loadOrgTree() {
  orgTreeLoading.value = true
  try {
    const { data } = await fetchOrgTree({})
    orgTreeDataOrigin.value = data || []
    orgTreeData.value = data || []
  } finally {
    orgTreeLoading.value = false
  }
}

// ── Tree table ──
const treeTableLoading = ref(false)
const treeTableData = ref<any[]>([])
const treeTableDataOrigin = ref<any[]>([])

function filterGroupTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const match = node.name?.includes(keyword)
    const filteredChildren = node.children ? filterGroupTree(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

async function loadGroupTree() {
  treeTableLoading.value = true
  try {
    const params: any = {}
    if (searchForm.org_id) params.org_id = searchForm.org_id
    const { data } = await fetchGroupTree(params)
    treeTableDataOrigin.value = data || []
    treeTableData.value = filterGroupTree(data || [], searchForm.keyword)
  } finally {
    treeTableLoading.value = false
  }
}

// ── Search form ──
const searchForm = reactive({
  keyword: '',
  org_id: undefined as string | undefined,
})

// Keyword search — filter tree on client side
watch(() => searchForm.keyword, (val) => {
  treeTableData.value = filterGroupTree(treeTableDataOrigin.value, val)
})

// ── Org selection ──
function handleOrgSelect(keys: any[]) {
  selectedOrgKeys.value = keys
  if (keys.length > 0) {
    searchForm.org_id = keys[0]
  } else {
    searchForm.org_id = undefined
  }
  mobileTreeOpen.value = false
  loadGroupTree()
}
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => { selectedKeys.value = keys },
}))

const columns = [
  { title: '用户组名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '用户组编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '用户组类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' },
]

// ── Drawers ──
const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) { detailRef.value?.doOpen(record) }
function openEdit(record: any) { formRef.value?.doOpen(record) }
function openCreate(parent?: any) {
  formRef.value?.doOpen(undefined, parent?.id, parent?.org_id || searchForm.org_id)
}

async function handleDelete(id: string) {
  const { success } = await fetchGroupRemove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    loadGroupTree()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '用户组',
    selectedKeys: selectedKeys.value,
    deleteApi: fetchGroupRemove,
    onSuccess: () => {
      selectedKeys.value = []
      loadGroupTree()
    },
  })
}

function handleFormSuccess() {
  loadGroupTree()
}

function handleSearch() { loadGroupTree() }

function resetSearch() {
  searchForm.keyword = ''
  searchForm.org_id = undefined
  selectedOrgKeys.value = []
  treeTableRef.value?.clearSelected()
  loadOrgTree()
  loadGroupTree()
}

// ── Import / Export / Template ──
const importOpen = ref(false)
const exportOpen = ref(false)
const templateLoading = ref(false)
const importModalRef = ref()

async function handleDownloadTemplate() {
  templateLoading.value = true
  try {
    const blob = await fetchGroupTemplate()
    downloadBlob(blob, '用户组导入模板.xlsx')
  } catch { message.error('下载模板失败') }
  finally { templateLoading.value = false }
}

async function handleExportWithParams(params: any) {
  try {
    const blob = await fetchGroupExport(params)
    downloadBlob(blob, `用户组数据_${new Date().toLocaleDateString()}.xlsx`)
    message.success('导出成功')
    exportOpen.value = false
  } catch { message.error('导出失败') }
}

async function handleImport(file: File) {
  try {
    const { success, data } = await fetchGroupImport(file)
    if (success && data) {
      importModalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
      message.success('导入成功')
      loadGroupTree()
    }
  } catch {
    importModalRef.value?.setResult({ success: false, message: '导入失败，请检查文件格式' })
  }
}

onMounted(() => {
  loadOrgTree()
  loadGroupTree()
})
</script>
