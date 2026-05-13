<template>
  <AppSplitPanel
    ref="splitRef"
    v-model:collapsed="splitCollapsed"
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
          v-model:value="groupSearchKey"
          placeholder="搜索用户组"
          allow-clear
          class="mb-2"
        />
        <a-spin :spinning="groupTreeLoading">
          <a-tree
            v-if="groupTreeData.length"
            v-model:expanded-keys="groupExpandedKeys"
            :tree-data="groupTreeData"
            :field-names="{ children: 'children', title: 'name', key: 'id' }"
            :selected-keys="selectedGroupKeys"
            block-node
            show-line
            class="mb-3"
            @select="handleGroupSelect"
          ></a-tree>
          <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
        </a-spin>
      </a-card>
    </template>
    <template #right>
      <div class="flex flex-col h-full overflow-auto gap-2">
        <div class="flex items-center gap-2 mb-1">
          <a-button type="text" size="small" @click="router.push('/sys/org')">
            <template #icon><ArrowLeftOutlined /></template>
            返回组织管理
          </a-button>
        </div>
        <AppSearchPanel
          :model="searchForm"
          :perm="['sys:group:page', 'sys:group:tree']"
          @search="handleSearch"
          @reset="resetSearch"
        >
          <a-button
            type="text"
            size="small"
            class="max-md:hidden"
            @click="splitRef?.toggleCollapse()"
          >
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
          :perm="['sys:group:page', 'sys:group:tree']"
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
              <a-tag>{{ $dict.label('GROUP_CATEGORY', record.category) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tooltip title="禁用后仅不可被选择，不影响已绑定的数据">
                <a-tag :color="$dict.color('SYS_STATUS', record.status)">
                  {{ $dict.label('SYS_STATUS', record.status) }}
                </a-tag>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
                <a-button
                  v-if="hasPermission('sys:group:modify')"
                  type="link"
                  size="small"
                  @click="openEdit(record)"
                >
                  编辑
                </a-button>
                <a-dropdown v-if="hasPermission('sys:group:create')">
                  <a-button type="link" size="small">
                    更多
                    <DownOutlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item
                        v-if="hasPermission('sys:group:create')"
                        @click="openCreate(record)"
                      >
                        新增子级
                      </a-menu-item>
                      <a-menu-item @click="goToPosition(record)">职位管理</a-menu-item>
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
    title="用户组"
    placement="left"
    :width="300"
    destroy-on-close
    @close="mobileTreeOpen = false"
  >
    <a-input-search
      v-model:value="groupSearchKey"
      placeholder="搜索用户组"
      allow-clear
      class="mb-2"
    />
    <a-spin :spinning="groupTreeLoading">
      <a-tree
        v-if="groupTreeData.length"
        v-model:expanded-keys="groupExpandedKeys"
        :tree-data="groupTreeData"
        :field-names="{ children: 'children', title: 'name', key: 'id' }"
        :selected-keys="selectedGroupKeys"
        block-node
        show-line
        class="mb-3"
        @select="handleGroupSelect"
      >
        <template #switcherIcon="{ expanded }">
          <CaretDownOutlined :class="expanded ? '' : '-rotate-90'" class="text-[12px]" />
        </template>
      </a-tree>
      <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'OrgGroupManagement' })
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
  CaretDownOutlined,
  ArrowLeftOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { useRouter, useRoute } from 'vue-router'
import {
  fetchGroupTree,
  fetchGroupRemove,
  fetchGroupExport,
  fetchGroupTemplate,
  fetchGroupImport,
} from '@/api/group'
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
const router = useRouter()
const route = useRoute()
const treeTableRef = ref()
const splitRef = ref()
const splitCollapsed = ref(false)
const mobileTreeOpen = ref(false)

const orgId = ref('')

// ── Left group tree ──
const groupTreeLoading = ref(false)
const allGroupTreeData = ref<any[]>([]) // full tree for left panel
const groupTreeData = ref<any[]>([]) // filtered for display
const groupExpandedKeys = ref<string[]>([])
const selectedGroupKeys = ref<string[]>([])
const groupSearchKey = ref('')

function filterGroupTreeByKeyword(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const match = node.name?.includes(keyword)
    const filteredChildren = node.children ? filterGroupTreeByKeyword(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

watch(groupSearchKey, val => {
  groupTreeData.value = filterGroupTreeByKeyword(allGroupTreeData.value, val)
  if (val) {
    const getAllKeys = (nodes: any[]): string[] =>
      nodes.reduce((keys: string[], n) => {
        keys.push(n.id)
        if (n.children) keys.push(...getAllKeys(n.children))
        return keys
      }, [])
    groupExpandedKeys.value = getAllKeys(groupTreeData.value)
  }
})

function extractSubtree(nodes: any[], id: string): any[] {
  if (!id) return nodes
  for (const node of nodes) {
    if (node.id === id) return [{ ...node }]
    if (node.children) {
      const result = extractSubtree(node.children, id)
      if (result.length > 0) return result
    }
  }
  return []
}

async function loadLeftGroupTree() {
  groupTreeLoading.value = true
  try {
    const { data } = await fetchGroupTree({ org_id: orgId.value || undefined })
    allGroupTreeData.value = data || []
    groupTreeData.value = data || []
  } finally {
    groupTreeLoading.value = false
  }
}

// ── Tree table ──
const treeTableLoading = ref(false)
const treeTableData = ref<any[]>([])
const selectedGroupNode = ref<any>(null)

function filterTreeTableByKeyword(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const match = node.name?.includes(keyword)
    const filteredChildren = node.children ? filterTreeTableByKeyword(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

async function loadGroupTree() {
  treeTableLoading.value = true
  try {
    const { data } = await fetchGroupTree({ org_id: orgId.value || undefined })
    const fullTree = data || []
    if (selectedGroupKeys.value.length > 0) {
      treeTableData.value = filterTreeTableByKeyword(
        extractSubtree(fullTree, selectedGroupKeys.value[0]),
        searchForm.keyword
      )
    } else {
      treeTableData.value = filterTreeTableByKeyword(fullTree, searchForm.keyword)
    }
  } finally {
    treeTableLoading.value = false
  }
}

// ── Search form ──
const searchForm = reactive({
  keyword: '',
})

// Keyword search — filter tree on client side
watch(
  () => searchForm.keyword,
  val => {
    loadGroupTree()
  }
)

// ── Group selection ──
function handleGroupSelect(keys: any[]) {
  selectedGroupKeys.value = keys
  selectedGroupNode.value = keys.length > 0 ? null : null
  mobileTreeOpen.value = false
  loadGroupTree()
  // Preserve state in URL for refresh
  const query: Record<string, string> = {}
  if (orgId.value) query.org_id = orgId.value
  if (keys.length > 0) query.group_id = keys[0]
  router.replace({ query })
}

function goToPosition(record: any) {
  const query: Record<string, string> = { group_id: record.id }
  if (record.org_id) {
    query.org_id = record.org_id
  } else if (orgId.value) {
    query.org_id = orgId.value
  }
  router.push({ path: '/sys/org/group/position', query })
}
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => {
    selectedKeys.value = keys
  },
}))

const columns = [
  { title: '用户组名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '用户组编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '用户组类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' },
]

// ── Drawers ──
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
  formRef.value?.doOpen(undefined, parent?.id, parent?.org_id || orgId.value || undefined)
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

function handleSearch() {
  loadGroupTree()
}

function resetSearch() {
  searchForm.keyword = ''
  selectedGroupKeys.value = []
  treeTableRef.value?.clearSelected()
  loadLeftGroupTree()
  loadGroupTree()
  const query: Record<string, string> = {}
  if (orgId.value) query.org_id = orgId.value
  router.replace({ query })
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
  } catch {
    message.error('下载模板失败')
  } finally {
    templateLoading.value = false
  }
}

async function handleExportWithParams(params: any) {
  try {
    const blob = await fetchGroupExport(params)
    downloadBlob(blob, `用户组数据_${new Date().toLocaleDateString()}.xlsx`)
    message.success('导出成功')
    exportOpen.value = false
  } catch {
    message.error('导出失败')
  }
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
  const { org_id, group_id } = route.query as Record<string, string>
  if (org_id) orgId.value = org_id
  if (group_id) selectedGroupKeys.value = [group_id]
  loadLeftGroupTree()
  loadGroupTree()
})
</script>
