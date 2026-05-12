<template>
  <AppSplitPanel ref="splitRef" v-model:collapsed="splitCollapsed" :initial-size="280" :min-size="200" :max-size="400" :md="0">
    <template #left>
      <a-card size="small" class="h-full flex flex-col max-md:hidden" :body-style="{ flex: '1', overflow: 'auto', padding: '12px' }">
        <a-input-search v-model:value="treeSearchKey" placeholder="搜索组织" allow-clear class="mb-2" />
        <a-spin :spinning="treeLoading">
          <a-tree
            v-if="treeData.length"
            v-model:expanded-keys="expandedKeys"
            :tree-data="treeData"
            :field-names="treeFieldNames"
            :selected-keys="selectedTreeKeys"
            block-node
            show-line
            @select="handleTreeSelect"
          >
          </a-tree>
          <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
        </a-spin>
      </a-card>
    </template>
    <template #right>
      <div class="flex flex-col h-full overflow-auto gap-2">
        <!-- Search -->
        <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
            <a-button type="text" size="small" class="max-md:hidden" @click="splitRef?.toggleCollapse()">
              <template #icon>
                <component :is="splitCollapsed ? DoubleRightOutlined : DoubleLeftOutlined" />
              </template>
            </a-button>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="关键词" name="keyword">
              <a-input v-model:value="searchForm.keyword" placeholder="组织名称" allow-clear />
            </a-form-item>
          </a-col>
            <a-button type="text" size="small" class="md:hidden" @click="mobileTreeOpen = true">
              <template #icon><FolderOutlined /></template>
            </a-button>
        </AppSearchPanel>

        <!-- Table -->
        <AppTable
          ref="tableRef"
          :columns="columns"
          :fetch-data="orgApi.page"
          :search-form="searchForm"
          :row-selection="rowSelection"
        >
          <template #toolbar>
            <a-button v-if="hasPermission('sys:org:create')" type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              新增组织
            </a-button>
            <a-button
              v-if="hasPermission('sys:org:remove')"
              danger
              :disabled="selectedKeys.length === 0"
              @click="handleBatchDelete"
            >
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
            <a-button v-if="hasPermission('sys:org:import')" @click="importOpen = true">
              <template #icon><UploadOutlined /></template>
              导入
            </a-button>
            <a-button v-if="hasPermission('sys:org:export')" @click="exportOpen = true">
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
                  v-if="hasPermission('sys:org:modify')"
                  type="link"
                  size="small"
                  @click="openEdit(record)"
                >
                  编辑
                </a-button>
                <a-dropdown v-if="hasPermission('sys:org:create')">
                  <a-button type="link" size="small">
                    更多
                    <DownOutlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item v-if="hasPermission('sys:org:create')" @click="openCreate(record)">
                        新增子级
                      </a-menu-item>
                      <a-menu-item @click="router.push('/sys/org/group?org_id=' + record.id)">
                        用户组管理
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
                <a-popconfirm
                  v-if="hasPermission('sys:org:remove')"
                  title="确定删除该组织？如有子级将一并删除"
                  @confirm="handleDelete(record.id)"
                >
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </AppTable>

        <!-- Import modal -->
        <AppImportModal
          ref="importModalRef"
          :open="importOpen"
          template-text="下载组织导入模板"
          :template-loading="templateLoading"
          @close="importOpen = false"
          @download-template="handleDownloadTemplate"
          @upload="handleImport"
        />

        <!-- Export modal -->
        <AppExportModal
          :open="exportOpen"
          :selected-keys="selectedKeys"
          @close="exportOpen = false"
          @export="handleExportWithParams"
        />

        <!-- Drawers -->
        <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
        <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
      </div>
    </template>
  </AppSplitPanel>

  <!-- Mobile tree drawer -->
  <a-drawer
    :open="mobileTreeOpen"
    title="组织分类"
    placement="left"
    :width="280"
    destroy-on-close
    @close="mobileTreeOpen = false"
  >
    <a-input-search v-model:value="treeSearchKey" placeholder="搜索组织" allow-clear class="mb-2" />
    <a-spin :spinning="treeLoading">
      <a-tree
        v-if="treeData.length"
        v-model:expanded-keys="expandedKeys"
        :tree-data="treeData"
        :field-names="treeFieldNames"
        :selected-keys="selectedTreeKeys"
        block-node
        show-line
        @select="handleTreeSelect"
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
defineOptions({ name: 'SysOrg' })
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
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { useRouter } from 'vue-router'
import { orgApi } from '@/api/org'
import { downloadBlob, confirmDelete } from '@/utils'
import AppSplitPanel from '@/components/layout/AppSplitPanel.vue'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const router = useRouter()
const tableRef = ref()
const splitRef = ref()
const splitCollapsed = ref(false)
const mobileTreeOpen = ref(false)

const categoryMap: Record<string, string> = {
  COMPANY: '公司',
  DEPT: '部门',
  UNIT: '单位',
  GROUP: '集团',
}

// Tree
const treeLoading = ref(false)
const treeData = ref<any[]>([])
const expandedKeys = ref<string[]>([])
const selectedTreeKeys = ref<string[]>([])
const treeFieldNames = { children: 'children', title: 'name', key: 'id' }
const treeSearchKey = ref('')
const treeDataOrigin = ref<any[]>([])

function filterTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const match = node.name?.includes(keyword)
    const filteredChildren = node.children ? filterTree(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

watch(treeSearchKey, (val) => {
  treeData.value = filterTree(treeDataOrigin.value, val)
  if (val) {
    const getAllKeys = (nodes: any[]): string[] => {
      return nodes.reduce((keys: string[], n) => {
        keys.push(n.id)
        if (n.children) keys.push(...getAllKeys(n.children))
        return keys
      }, [])
    }
    expandedKeys.value = getAllKeys(treeData.value)
  }
})

async function loadTree() {
  treeLoading.value = true
  try {
    const { data } = await orgApi.tree({})
    treeDataOrigin.value = data || []
    treeData.value = data || []
  } finally {
    treeLoading.value = false
  }
}

onMounted(loadTree)

function handleTreeSelect(keys: any[]) {
  selectedTreeKeys.value = keys
  searchForm.parent_id = keys.length > 0 ? keys[0] : undefined
  mobileTreeOpen.value = false
  tableRef.value?.refresh(true)
}

// Search form
const searchForm = reactive({ keyword: '', parent_id: undefined as string | undefined })
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => { selectedKeys.value = keys },
}))

const columns = [
  { title: '组织名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '组织编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '组织类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' },
]

// Drawer refs
const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) {
  detailRef.value?.doOpen(record)
}

function openEdit(record: any) {
  formRef.value?.doOpen(record)
}

function openCreate(parent?: any) {
  formRef.value?.doOpen(undefined, parent?.id || searchForm.parent_id)
}

async function handleDelete(id: string) {
  const { success } = await orgApi.remove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    tableRef.value?.refresh()
    loadTree()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '组织',
    selectedKeys: selectedKeys.value,
    deleteApi: orgApi.remove,
    onSuccess: () => {
      selectedKeys.value = []
      tableRef.value?.refresh()
      loadTree()
    },
  })
}

function handleFormSuccess() {
  tableRef.value?.refresh()
  loadTree()
}

// Search
function handleSearch() {
  tableRef.value?.refresh(true)
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.parent_id = undefined
  selectedTreeKeys.value = []
  tableRef.value?.refresh(true)
}

// Import / Export / Template
const importOpen = ref(false)
const exportOpen = ref(false)
const templateLoading = ref(false)
const importModalRef = ref()

async function handleDownloadTemplate() {
  templateLoading.value = true
  try {
    const blob = await orgApi.template()
    downloadBlob(blob, '组织导入模板.xlsx')
  } catch {
    message.error('下载模板失败')
  } finally {
    templateLoading.value = false
  }
}

async function handleExportWithParams(params: any) {
  try {
    const blob = await orgApi.export(params)
    downloadBlob(blob, `组织数据_${new Date().toLocaleDateString()}.xlsx`)
    message.success('导出成功')
    exportOpen.value = false
  } catch {
    message.error('导出失败')
  }
}

async function handleImport(file: File) {
  try {
    const { success, data } = await orgApi.importFile(file)
    if (success && data) {
      importModalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
      message.success('导入成功')
      tableRef.value?.refresh(true)
      loadTree()
    }
  } catch {
    importModalRef.value?.setResult({ success: false, message: '导入失败，请检查文件格式' })
  }
}
</script>
