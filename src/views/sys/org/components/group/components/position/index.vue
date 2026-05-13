<template>
  <div class="flex flex-col gap-2">
    <div class="flex items-center gap-2">
      <a-button type="text" size="small" @click="goBack">
        <template #icon><ArrowLeftOutlined /></template>
        返回用户组管理
      </a-button>
    </div>
    <AppSearchPanel
      :model="searchForm"
      :perm="['sys:position:page']"
      @search="handleSearch"
      @reset="resetSearch"
    >
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="职位名称" allow-clear />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      perm="sys:position:page"
      :columns="columns"
      :fetch-data="fetchPositionPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:position:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增职位
        </a-button>
        <a-button
          v-if="hasPermission('sys:position:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button v-if="hasPermission('sys:position:import')" @click="importOpen = true">
          <template #icon><UploadOutlined /></template>
          导入
        </a-button>
        <a-button v-if="hasPermission('sys:position:export')" @click="exportOpen = true">
          <template #icon><DownloadOutlined /></template>
          导出
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'category'">
          <a-tag>{{ $dict.label('POSITION_CATEGORY', record.category) }}</a-tag>
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
              v-if="hasPermission('sys:position:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:position:remove')"
              title="确定删除该职位？"
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
      template-text="下载职位导入模板"
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
defineOptions({ name: 'OrgPositionManagement' })
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { useRouter, useRoute } from 'vue-router'
import {
  fetchPositionPage,
  fetchPositionRemove,
  fetchPositionExport,
  fetchPositionTemplate,
  fetchPositionImport,
} from '@/api/position'
import { downloadBlob, confirmDelete } from '@/utils'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const router = useRouter()
const route = useRoute()
const tableRef = ref()
const orgId = ref('')

// ── Search ──
const searchForm = reactive({
  keyword: '',
  group_id: undefined as string | undefined,
})
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => {
    selectedKeys.value = keys
  },
}))

const columns = [
  { title: '职位名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '职位编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '职位类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
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
function openCreate() {
  formRef.value?.doOpen(undefined, searchForm.group_id)
}

function goBack() {
  const query: Record<string, string> = {}
  if (orgId.value) query.org_id = orgId.value
  router.push({ path: '/sys/org/group', query })
}

async function handleDelete(id: string) {
  const { success } = await fetchPositionRemove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    tableRef.value?.refresh()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '职位',
    selectedKeys: selectedKeys.value,
    deleteApi: fetchPositionRemove,
    onSuccess: () => {
      selectedKeys.value = []
      tableRef.value?.refresh()
    },
  })
}

function handleFormSuccess() {
  tableRef.value?.refresh()
}

function handleSearch() {
  tableRef.value?.refresh(true)
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.group_id = undefined
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
    const blob = await fetchPositionTemplate()
    downloadBlob(blob, '职位导入模板.xlsx')
  } catch {
    message.error('下载模板失败')
  } finally {
    templateLoading.value = false
  }
}

async function handleExportWithParams(params: any) {
  try {
    const blob = await fetchPositionExport(params)
    downloadBlob(blob, `职位数据_${new Date().toLocaleDateString()}.xlsx`)
    message.success('导出成功')
    exportOpen.value = false
  } catch {
    message.error('导出失败')
  }
}

async function handleImport(file: File) {
  try {
    const { success, data } = await fetchPositionImport(file)
    if (success && data) {
      importModalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
      message.success('导入成功')
      tableRef.value?.refresh(true)
    }
  } catch {
    importModalRef.value?.setResult({ success: false, message: '导入失败，请检查文件格式' })
  }
}

onMounted(() => {
  // Read params from query (navigated from group management)
  const { org_id, group_id } = route.query as Record<string, string>
  if (org_id) orgId.value = org_id
  if (group_id) {
    searchForm.group_id = group_id
    tableRef.value?.refresh(true)
  }
})
</script>
