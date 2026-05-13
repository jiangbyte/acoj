<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" perm="sys:notice:page" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="通知标题" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="通知类别" name="category">
          <a-select v-model:value="searchForm.category" placeholder="全部" allow-clear>
            <a-select-option value="NOTICE">通知</a-select-option>
            <a-select-option value="NEWS">新闻</a-select-option>
            <a-select-option value="MESSAGE">消息</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="通知级别" name="level">
          <a-select v-model:value="searchForm.level" placeholder="全部" allow-clear>
            <a-select-option value="URGENT">紧急</a-select-option>
            <a-select-option value="IMPORTANT">重要</a-select-option>
            <a-select-option value="NORMAL">普通</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      perm="sys:notice:page"
      :columns="columns"
      :fetch-data="fetchNoticePage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:notice:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增通知
        </a-button>
        <a-button
          v-if="hasPermission('sys:notice:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button v-if="hasPermission('sys:notice:import')" @click="ieImportOpen = true">
          <template #icon><UploadOutlined /></template>
          导入
        </a-button>
        <a-button v-if="hasPermission('sys:notice:export')" @click="ieExportOpen = true">
          <template #icon><DownloadOutlined /></template>
          导出
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'category'">
          <a-tag>{{ categoryMap[record.category] || record.category || '-' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'level'">
          <a-tag :color="levelColorMap[record.level] || 'default'">
            {{ levelMap[record.level] || record.level || '-' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
            {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'is_top'">
          <a-tag :color="record.is_top === 'YES' ? 'orange' : 'default'">
            {{ record.is_top === 'YES' ? '置顶' : '否' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="hasPermission('sys:notice:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:notice:remove')"
              title="确定删除该通知？"
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
      :open="ieImportOpen"
      template-text="下载通知导入模板"
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
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysNotice' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'

import {
  fetchNoticePage,
  fetchNoticeRemove,
  fetchNoticeExport,
  fetchNoticeTemplate,
  fetchNoticeImport,
} from '@/api/notice'
import { useCrud } from '@/hooks/useCrud'
import { useImportExport } from '@/hooks/useImportExport'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import AppImportModal from '@/components/modal/AppImportModal.vue'
import AppExportModal from '@/components/modal/AppExportModal.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '通知', deleteApi: fetchNoticeRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud

const categoryMap: Record<string, string> = {
  NOTICE: '通知',
  NEWS: '新闻',
  MESSAGE: '消息',
}

const levelMap: Record<string, string> = {
  URGENT: '紧急',
  IMPORTANT: '重要',
  NORMAL: '普通',
}

const levelColorMap: Record<string, string> = {
  URGENT: 'red',
  IMPORTANT: 'orange',
  NORMAL: 'default',
}

// ── Search ──
const searchForm = reactive({
  keyword: '',
  category: undefined as string | undefined,
  level: undefined as string | undefined,
})

const columns = [
  { title: '通知标题', dataIndex: 'title', key: 'title', width: 300, ellipsis: true },
  { title: '通知类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '通知级别', dataIndex: 'level', key: 'level', width: 100 },
  { title: '是否置顶', dataIndex: 'is_top', key: 'is_top', width: 90 },
  { title: '浏览次数', dataIndex: 'view_count', key: 'view_count', width: 90 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.category = undefined
  searchForm.level = undefined
  tableRef.value?.refresh(true)
}

// ── Refs ──
const importModalRef = ref()
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
  handleDownloadTemplate: ieHandleDownloadTemplate,
  handleExportWithParams: ieHandleExportWithParams,
  handleImport: ieHandleImport,
} = useImportExport({
  exportApi: fetchNoticeExport,
  templateApi: fetchNoticeTemplate,
  importApi: fetchNoticeImport,
  fileName: '通知数据',
  templateName: '通知导入模板',
  importModalRef,
  onSuccess: () => tableRef.value?.refresh(true),
})
</script>
