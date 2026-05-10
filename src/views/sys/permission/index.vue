<template>
  <AppTable ref="tableRef" :columns="columns" :fetchData="fetchPermissionPage" :searchForm="searchForm">
    <template #search>
      <a-form-item label="关键词" name="keyword"><a-input v-model:value="searchForm.keyword" /></a-form-item>
    </template>
    <template #toolbar>
      <a-button type="primary" @click="openCreate">新增</a-button>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'action'">
        <a-space>
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
import { fetchPermissionPage, fetchPermissionRemove } from '@/api/permission'
import AppTable from '@/components/AppTable.vue'

const tableRef = ref()
const searchForm = reactive({ keyword: '' })
const columns = [
  { title: '权限名称', dataIndex: 'name' },
  { title: '权限编码', dataIndex: 'code' },
  { title: '模块', dataIndex: 'module' },
  { title: '操作', key: 'action', width: 200 },
]

function openCreate() {}
function openEdit(_id: string) {}
async function handleDelete(id: string) {
  await fetchPermissionRemove({ ids: [id] })
  tableRef.value?.refresh()
}
</script>
