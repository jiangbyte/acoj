<template>
  <AppTreePanel ref="treePanel" :fetch-tree="fetchOrgTree" title="组织" @select="handleTreeSelect">
    <template #right>
      <div class="flex flex-col h-full overflow-auto gap-2">
        <!-- Search -->
        <AppSearchPanel
          :model="searchForm"
          perm="sys:org:page"
          @search="handleSearch"
          @reset="resetSearch"
        >
          <a-button
            type="text"
            size="small"
            class="max-md:hidden"
            @click="treePanel?.splitRef?.toggleCollapse()"
          >
            <template #icon>
              <component :is="treePanel?.collapsed ? DoubleRightOutlined : DoubleLeftOutlined" />
            </template>
          </a-button>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="关键词" name="keyword">
              <a-input v-model:value="searchForm.keyword" placeholder="组织名称" allow-clear />
            </a-form-item>
          </a-col>
        </AppSearchPanel>

        <!-- Table -->
        <AppTable
          ref="tableRef"
          perm="sys:org:page"
          :columns="columns"
          :fetch-data="fetchOrgPage"
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
            <a-button v-if="hasPermission('sys:org:import')" @click="ieImportOpen = true">
              <template #icon><UploadOutlined /></template>
              导入
            </a-button>
            <a-button v-if="hasPermission('sys:org:export')" @click="ieExportOpen = true">
              <template #icon><DownloadOutlined /></template>
              导出
            </a-button>
          </template>

          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'category'">
              <a-tag>{{ $dict.label('ORG_CATEGORY', record.category) }}</a-tag>
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
                      <a-menu-item
                        v-if="hasPermission('sys:org:create')"
                        @click="openCreate(record)"
                      >
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
          :open="ieImportOpen"
          template-text="下载组织导入模板"
          :template-loading="ieTemplateLoading"
          @close="ieImportOpen = false"
          @download-template="ieHandleDownloadTemplate"
          @upload="ieHandleImport"
        />

        <!-- Export modal -->
        <AppExportModal
          :open="ieExportOpen"
          :selected-keys="selectedKeys"
          @close="ieExportOpen = false"
          @export="ieHandleExportWithParams"
        />

        <!-- Drawers -->
        <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
        <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
      </div>
    </template>
  </AppTreePanel>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysOrg' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  DoubleLeftOutlined,
  DoubleRightOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { useRouter } from 'vue-router'

import {
  fetchOrgPage,
  fetchOrgTree,
  fetchOrgRemove,
  fetchOrgExport,
  fetchOrgTemplate,
  fetchOrgImport,
} from '@/api/org'
import { useCrud } from '@/hooks/useCrud'
import { useImportExport } from '@/hooks/useImportExport'
import AppTreePanel from '@/components/layout/AppTreePanel.vue'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const router = useRouter()
const treePanel = ref()

const crud = useCrud({
  name: '组织',
  deleteApi: fetchOrgRemove,
  onSuccess: () => treePanel.value?.refresh(),
})
const {
  tableRef,
  selectedKeys,
  rowSelection,
  handleSearch,
  handleDelete,
  handleBatchDelete,
  handleFormSuccess,
} = crud

// Search form
const searchForm = reactive({ keyword: '', parent_id: undefined as string | undefined })

const columns = [
  { title: '组织名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '组织编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '组织类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.parent_id = undefined
  tableRef.value?.refresh(true)
}

// Drawer refs
const importModalRef = ref()
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

function handleTreeSelect(parentId: string | undefined) {
  searchForm.parent_id = parentId
  tableRef.value?.refresh(true)
}

const {
  importOpen: ieImportOpen,
  exportOpen: ieExportOpen,
  templateLoading: ieTemplateLoading,
  handleDownloadTemplate: ieHandleDownloadTemplate,
  handleExportWithParams: ieHandleExportWithParams,
  handleImport: ieHandleImport,
} = useImportExport({
  exportApi: fetchOrgExport,
  templateApi: fetchOrgTemplate,
  importApi: fetchOrgImport,
  fileName: '组织数据',
  templateName: '组织导入模板',
  importModalRef,
  onSuccess: () => {
    tableRef.value?.refresh(true)
    treePanel.value?.refresh()
  },
})
</script>
