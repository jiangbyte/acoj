<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" perm="sys:file:page" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="文件名称" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="存储引擎" name="engine">
          <a-select v-model:value="searchForm.engine" placeholder="全部" allow-clear>
            <a-select-option value="LOCAL">本地</a-select-option>
            <a-select-option value="MINIO">MinIO</a-select-option>
            <a-select-option value="ALIYUN">阿里云</a-select-option>
            <a-select-option value="TENCENT">腾讯云</a-select-option>
            <a-select-option value="S3">S3</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      perm="sys:file:page"
      :columns="columns"
      :fetch-data="fetchFilePage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:file:upload')" type="primary" @click="openUpload">
          <template #icon><UploadOutlined /></template>
          文件上传
        </a-button>
        <a-button
          v-if="hasPermission('sys:file:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'thumbnail'">
          <img
            v-if="isImage(record.suffix) && record.thumbnail"
            :src="record.thumbnail"
            class="w-10 h-10 object-cover rounded"
          />
          <span v-else class="text-gray-400 text-xs">{{ record.suffix?.toUpperCase() || 'FILE' }}</span>
        </template>
        <template v-else-if="column.key === 'engine'">
          <a-tag :color="engineColorMap[record.engine] || 'default'">
            {{ engineLabelMap[record.engine] || record.engine || '-' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button v-if="hasPermission('sys:file:detail')" type="link" size="small" @click="openDetail(record)">
              详情
            </a-button>
            <a-button v-if="hasPermission('sys:file:download')" type="link" size="small" @click="handleDownload(record)">
              下载
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:file:remove')"
              title="确定删除该文件？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTable>

    <UploadModal ref="uploadRef" @success="handleUploadSuccess" />
    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysFile' })
import { ref, reactive } from 'vue'
import { UploadOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { downloadBlob } from '@/utils'
import { useAuthStore } from '@/store'

import { fetchFilePage, fetchFileRemove, fetchFileDownload } from '@/api/file'
import { useCrud } from '@/hooks/useCrud'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import UploadModal from './components/upload.vue'
import DetailDrawer from './components/detail.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '文件', deleteApi: fetchFileRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete } = crud

const engineLabelMap: Record<string, string> = {
  LOCAL: '本地',
  MINIO: 'MinIO',
  ALIYUN: '阿里云',
  TENCENT: '腾讯云',
  S3: 'S3',
}

const engineColorMap: Record<string, string> = {
  LOCAL: 'green',
  MINIO: 'blue',
  ALIYUN: 'orange',
  TENCENT: 'red',
  S3: 'purple',
}

function isImage(suffix: string | null | undefined): boolean {
  if (!suffix) return false
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'].includes(suffix.toLowerCase().replace('.', ''))
}

const searchForm = reactive({
  keyword: '',
  engine: undefined as string | undefined,
})

const columns = [
  { title: '文件名称', dataIndex: 'name', key: 'name', width: 300, ellipsis: true },
  { title: '缩略图', dataIndex: 'thumbnail', key: 'thumbnail', width: 80 },
  { title: '文件大小', dataIndex: 'size_info', key: 'size_info', width: 100 },
  { title: '后缀', dataIndex: 'suffix', key: 'suffix', width: 70 },
  { title: '存储引擎', dataIndex: 'engine', key: 'engine', width: 100 },
  { title: '上传时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
]

async function handleDownload(record: any) {
  if (!record.download_path) return
  const blob = await fetchFileDownload(record.download_path)
  downloadBlob(blob, record.name || `file.${record.suffix || ''}`)
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.engine = undefined
  tableRef.value?.refresh(true)
}

const uploadRef = ref()
const detailRef = ref()
const detailOpen = ref(false)

function openUpload() { uploadRef.value?.doOpen() }
function openDetail(record: any) { detailRef.value?.doOpen(record) }
function handleUploadSuccess() { tableRef.value?.refresh(true) }
</script>
