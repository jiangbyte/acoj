<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="角色名称" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="角色类别" name="category">
          <a-select v-model:value="searchForm.category" placeholder="全部" allow-clear>
            <a-select-option value="ADMIN">管理</a-select-option>
            <a-select-option value="NORMAL">普通</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      :columns="columns"
      :fetch-data="fetchRolePage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:role:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增角色
        </a-button>
        <a-button
          v-if="hasPermission('sys:role:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button v-if="hasPermission('sys:role:import')" @click="ieImportOpen = true">
          <template #icon><UploadOutlined /></template>
          导入
        </a-button>
        <a-button v-if="hasPermission('sys:role:export')" @click="ieExportOpen = true">
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
              v-if="hasPermission('sys:role:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-dropdown v-if="hasPermission('sys:role:grantPermission') || hasPermission('sys:role:grantResource')">
              <a-button type="link" size="small">
                授权
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item v-if="hasPermission('sys:role:grantPermission')" @click="openGrantPermission(record)">
                    授权权限
                  </a-menu-item>
                  <a-menu-item v-if="hasPermission('sys:role:grantResource')" @click="openGrantResource(record)">
                    授权资源
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-popconfirm
              v-if="hasPermission('sys:role:remove')"
              title="确定删除该角色？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTable>

    <AppImportModal
      :ref="(el) => { ieImportModalRef.value = el }"
      :open="ieImportOpen"
      template-text="下载角色导入模板"
      :template-loading="ieTemplateLoading"
      @close="ieImportOpen = false"
      @download-template="ieHandleDownloadTemplate"
      @upload="ieHandleImport"
    />

    <AppExportModal
      :open="ieExportOpen"
      :selected-keys="selectedKeys"
      @close="ieExportOpen = false"
      @export="ieHandleExportWithParams"
    />

    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
    <GrantPermission ref="grantPermissionRef" v-model:open="grantPermissionOpen" @success="tableRef?.refresh()" />
    <GrantResource ref="grantResourceRef" v-model:open="grantResourceOpen" @success="tableRef?.refresh()" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysRole' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import {
  fetchRolePage,
  fetchRoleRemove,
  fetchRoleExport,
  fetchRoleTemplate,
  fetchRoleImport,
} from '@/api/role'
import { useCrud } from '@/hooks/useCrud'
import { useImportExport } from '@/hooks/useImportExport'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'
import GrantPermission from './components/grantPermission.vue'
import GrantResource from './components/grantResource.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '角色', deleteApi: fetchRoleRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud

const categoryMap: Record<string, string> = {
  ADMIN: '管理',
  NORMAL: '普通',
  OTHER: '其他',
}

// ── Search ──
const searchForm = reactive({
  keyword: '',
  category: undefined as string | undefined,
})

const columns = [
  { title: '角色名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '角色编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '角色类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.category = undefined
  tableRef.value?.refresh(true)
}

// ── Drawers ──
const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) { detailRef.value?.doOpen(record) }
function openEdit(record: any) { formRef.value?.doOpen(record) }
function openCreate() { formRef.value?.doOpen() }

const {
  importOpen: ieImportOpen,
  exportOpen: ieExportOpen,
  templateLoading: ieTemplateLoading,
  importModalRef: ieImportModalRef,
  handleDownloadTemplate: ieHandleDownloadTemplate,
  handleExportWithParams: ieHandleExportWithParams,
  handleImport: ieHandleImport,
} = useImportExport({
  exportApi: fetchRoleExport,
  templateApi: fetchRoleTemplate,
  importApi: fetchRoleImport,
  fileName: '角色数据',
  templateName: '角色导入模板',
  onSuccess: () => tableRef.value?.refresh(true),
})

const grantPermissionRef = ref()
const grantResourceRef = ref()
const grantPermissionOpen = ref(false)
const grantResourceOpen = ref(false)

function openGrantPermission(record: any) {
  grantPermissionRef.value?.doOpen(record)
}
function openGrantResource(record: any) {
  grantResourceRef.value?.doOpen(record)
}
</script>
