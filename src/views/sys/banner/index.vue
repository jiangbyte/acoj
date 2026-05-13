<template>
  <div class="flex flex-col gap-2">
    <!-- Search panel -->
    <AppSearchPanel
      :model="searchForm"
      perm="sys:banner:page"
      @search="handleSearch"
      @reset="resetSearch"
    >
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="标题" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="类别" name="category">
          <a-select v-model:value="searchForm.category" placeholder="全部" allow-clear style="width: 100%">
            <a-select-option value="HOME">首页</a-select-option>
            <a-select-option value="PAGE">页面</a-select-option>
            <a-select-option value="APP">应用</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="类型" name="type">
          <a-select v-model:value="searchForm.type" placeholder="全部" allow-clear style="width: 100%">
            <a-select-option value="IMAGE">图片</a-select-option>
            <a-select-option value="VIDEO">视频</a-select-option>
            <a-select-option value="TEXT">文字</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="位置" name="position">
          <a-select v-model:value="searchForm.position" placeholder="全部" allow-clear style="width: 100%">
            <a-select-option value="TOP">顶部</a-select-option>
            <a-select-option value="CENTER">中间</a-select-option>
            <a-select-option value="BOTTOM">底部</a-select-option>
            <a-select-option value="SIDEBAR">侧栏</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      perm="sys:banner:page"
      :columns="columns"
      :fetch-data="fetchBannerPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:banner:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增轮播图
        </a-button>
        <a-button
          v-if="hasPermission('sys:banner:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button v-if="hasPermission('sys:banner:import')" @click="ieImportOpen = true">
          <template #icon><UploadOutlined /></template>
          导入
        </a-button>
        <a-button v-if="hasPermission('sys:banner:export')" @click="ieExportOpen = true">
          <template #icon><DownloadOutlined /></template>
          导出
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'image'">
          <img v-if="record.image" :src="record.image" class="w-16 h-10 object-cover rounded" />
          <span v-else class="text-gray-400">无</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="hasPermission('sys:banner:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:banner:remove')"
              title="确定删除？"
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
      template-text="下载轮播图导入模板"
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
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="tableRef?.refresh()" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysBanner' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import {
  fetchBannerPage,
  fetchBannerRemove,
  fetchBannerExport,
  fetchBannerTemplate,
  fetchBannerImport,
} from '@/api/banner'
import { useCrud } from '@/hooks/useCrud'
import { useImportExport } from '@/hooks/useImportExport'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'
import { useAuthStore } from '@/store'


const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '轮播图', deleteApi: fetchBannerRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete } = crud

const searchForm = reactive({ keyword: '', category: undefined, type: undefined, position: undefined })
const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 200, ellipsis: true },
  { title: '图片', dataIndex: 'image', key: 'image', width: 100 },
  { title: '类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '位置', dataIndex: 'position', key: 'position', width: 100 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.category = undefined
  searchForm.type = undefined
  searchForm.position = undefined
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
function openCreate() {
  formRef.value?.doOpen()
}

const {
  importOpen: ieImportOpen,
  exportOpen: ieExportOpen,
  templateLoading: ieTemplateLoading,
  handleDownloadTemplate: ieHandleDownloadTemplate,
  handleExportWithParams: ieHandleExportWithParams,
  handleImport: ieHandleImport,
} = useImportExport({
  exportApi: fetchBannerExport,
  templateApi: fetchBannerTemplate,
  importApi: fetchBannerImport,
  fileName: '轮播图数据',
  templateName: '轮播图导入模板',
  importModalRef,
  onSuccess: () => tableRef.value?.refresh(true),
})
</script>
