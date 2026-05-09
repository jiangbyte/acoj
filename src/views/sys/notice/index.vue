<template>
  <AppTable ref="tableRef" :columns="columns" :fetchData="fetchNoticePage" :searchForm="searchForm">
    <template #search>
      <a-form-item label="关键词" name="keyword"><a-input v-model:value="searchForm.keyword" /></a-form-item>
    </template>
    <template #toolbar>
      <a-button type="primary" @click="openCreate">新增</a-button>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'action'">
        <a-space>
          <a-button type="link" size="small" @click="openDetail(record.id)">详情</a-button>
          <a-button type="link" size="small" @click="openEdit(record.id)">编辑</a-button>
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
import { fetchNoticePage, fetchNoticeRemove } from '@/service/api/notice'
import AppTable from '@/components/AppTable.vue'

const tableRef = ref()
const searchForm = reactive({ keyword: '' })
const columns = [
  { title: '标题', dataIndex: 'title' },
  { title: '类型', dataIndex: 'type' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', key: 'action', width: 200 },
]

function openCreate() {}
function openDetail(_id: string) {}
function openEdit(_id: string) {}
async function handleDelete(id: string) {
  await fetchNoticeRemove({ ids: [id] })
  tableRef.value?.refresh()
}
</script>
