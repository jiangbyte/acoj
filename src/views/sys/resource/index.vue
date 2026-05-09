<template>
  <a-card>
    <a-space class="mb-4">
      <a-button type="primary" @click="openCreate">新增</a-button>
    </a-space>
    <a-table
      :dataSource="dataSource"
      :columns="columns"
      :loading="loading"
      rowKey="id"
      :pagination="false"
    >
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
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchResourcePage, fetchResourceRemove } from '@/service/api/resource'

const dataSource = ref<any[]>([])
const loading = ref(false)
const columns = [
  { title: '资源名称', dataIndex: 'name' },
  { title: '编码', dataIndex: 'code' },
  { title: '类型', dataIndex: 'type' },
  { title: '路由', dataIndex: 'route_path' },
  { title: '排序', dataIndex: 'sort_code' },
  { title: '操作', key: 'action', width: 200 },
]

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchResourcePage({ current: 1, size: 999 })
    if (data?.records) dataSource.value = data.records
  } finally {
    loading.value = false
  }
}

function openCreate() {}
function openEdit(_id: string) {}
async function handleDelete(id: string) {
  await fetchResourceRemove({ ids: [id] })
  loadData()
}

onMounted(loadData)
</script>
