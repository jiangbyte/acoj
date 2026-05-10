<template>
  <AppTable ref="tableRef" :columns="columns" :fetchData="fetchFilePage" :searchForm="searchForm">
    <template #search>
      <a-form-item label="关键词" name="keyword"><a-input v-model:value="searchForm.keyword" /></a-form-item>
    </template>
    <template #toolbar>
      <a-upload :beforeUpload="handleUpload" :showUploadList="false">
        <a-button type="primary">上传文件</a-button>
      </a-upload>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'action'">
        <a-space>
          <a-button type="link" size="small" @click="openDetail(record.id)">详情</a-button>
          <a-popconfirm title="确定删除？" @confirm="handleDelete(record.id)">
            <a-button type="link" danger size="small">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </AppTable>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { fetchFilePage, fetchFileRemove, uploadFile } from '@/api/file'
import AppTable from '@/components/AppTable.vue'

const tableRef = ref()
const searchForm = reactive({ keyword: '' })
const columns = [
  { title: '文件名', dataIndex: 'original_name' },
  { title: '大小', dataIndex: 'size' },
  { title: '引擎', dataIndex: 'engine' },
  { title: '操作', key: 'action', width: 200 },
]

function openDetail(_id: string) {}

async function handleDelete(id: string) {
  await fetchFileRemove({ ids: [id] })
  tableRef.value?.refresh()
}

async function handleUpload(file: File) {
  const { success } = await uploadFile(file)
  if (success) {
    message.success('上传成功')
    tableRef.value?.refresh()
  }
  return false
}
</script>
