<template>
  <AppTreePanel
    ref="treePanel"
    :fetch-tree="fetchDictTree"
    title="字典"
    :field-names="{ children: 'children', title: 'label', key: 'id' }"
    :icon="BookOutlined"
    @select="handleTreeSelect"
  >
    <template #right>
      <div class="flex flex-col h-full overflow-auto gap-2">
        <!-- Search -->
        <AppSearchPanel
          :model="searchForm"
          perm="sys:dict:page"
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
              <a-input v-model:value="searchForm.keyword" placeholder="字典标签" allow-clear />
            </a-form-item>
          </a-col>
        </AppSearchPanel>

        <!-- Table -->
        <AppTable
          ref="tableRef"
          perm="sys:dict:page"
          :columns="columns"
          :fetch-data="fetchDictPage"
          :search-form="searchForm"
          :row-selection="rowSelection"
        >
          <template #toolbar>
            <a-button v-if="hasPermission('sys:dict:create')" type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              新增字典
            </a-button>
            <a-button
              v-if="hasPermission('sys:dict:remove')"
              danger
              :disabled="selectedKeys.length === 0"
              @click="handleBatchDelete"
            >
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
            <a-button v-if="hasPermission('sys:dict:import')" @click="ieImportOpen = true">
              <template #icon><UploadOutlined /></template>
              导入
            </a-button>
            <a-button v-if="hasPermission('sys:dict:export')" @click="ieExportOpen = true">
              <template #icon><DownloadOutlined /></template>
              导出
            </a-button>
          </template>

          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'color'">
              <a-tag v-if="record.color" :color="record.color">{{ record.color }}</a-tag>
              <span v-else class="text-gray-400">-</span>
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
                  v-if="hasPermission('sys:dict:modify')"
                  type="link"
                  size="small"
                  @click="openEdit(record)"
                >
                  编辑
                </a-button>
                <a-dropdown v-if="hasPermission('sys:dict:create')">
                  <a-button type="link" size="small">
                    更多
                    <DownOutlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="openCreate(record)">新增子级</a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
                <a-popconfirm
                  v-if="hasPermission('sys:dict:remove')"
                  title="确定删除该字典？如有子级将一并删除"
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
          template-text="下载字典导入模板"
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
defineOptions({ name: 'SysDict' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  DoubleLeftOutlined,
  DoubleRightOutlined,
  BookOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore, useDictStore } from '@/store'

import {
  fetchDictPage,
  fetchDictTree,
  fetchDictRemove,
  fetchDictExport,
  fetchDictTemplate,
  fetchDictImport,
} from '@/api/dict'
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
const treePanel = ref()

const crud = useCrud({
  name: '字典',
  deleteApi: fetchDictRemove,
  onSuccess: () => {
    treePanel.value?.refresh()
    useDictStore().refreshDict()
  },
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
  { title: '字典标签', dataIndex: 'label', key: 'label', width: 180 },
  { title: '字典值', dataIndex: 'value', key: 'value', width: 150, ellipsis: true },
  { title: '编码', dataIndex: 'code', key: 'code', width: 150, ellipsis: true },
  { title: '颜色', dataIndex: 'color', key: 'color', width: 100 },
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
  exportApi: fetchDictExport,
  templateApi: fetchDictTemplate,
  importApi: fetchDictImport,
  fileName: '字典数据',
  templateName: '字典导入模板',
  importModalRef,
  onSuccess: () => {
    tableRef.value?.refresh(true)
    treePanel.value?.refresh()
    useDictStore().refreshDict()
  },
})
</script>
