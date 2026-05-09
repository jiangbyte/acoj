<template>
  <AppTable ref="tableRef" :columns="columns" :fetchData="fetchUserPage" :searchForm="searchForm">
    <template #search>
      <a-form-item label="关键词" name="keyword">
        <a-input v-model:value="searchForm.keyword" placeholder="账号/昵称" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-select v-model:value="searchForm.status" placeholder="全部" allowClear style="width:120px">
          <a-select-option value="ACTIVE">启用</a-select-option>
          <a-select-option value="INACTIVE">禁用</a-select-option>
        </a-select>
      </a-form-item>
    </template>
    <template #toolbar>
      <a-button type="primary" @click="openCreate" v-if="hasPermission('sys:user:create')">新增</a-button>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'action'">
        <a-space>
          <a-button type="link" size="small" @click="openDetail(record.id)">详情</a-button>
          <a-button type="link" size="small" @click="openEdit(record.id)" v-if="hasPermission('sys:user:modify')">编辑</a-button>
          <a-popconfirm title="确定删除？" @confirm="handleDelete(record.id)" v-if="hasPermission('sys:user:remove')">
            <a-button type="link" danger size="small">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
      <template v-else-if="column.key === 'status'">
        <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">{{ record.status === 'ACTIVE' ? '启用' : '禁用' }}</a-tag>
      </template>
    </template>
  </AppTable>

  <UserDetailDrawer v-model:open="detailOpen" :id="currentId" />
  <UserFormDrawer v-model:open="formOpen" :id="currentId" @success="tableRef?.refresh()" />
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/store/auth'
import { fetchUserPage, fetchUserRemove } from '@/service/api/user'
import AppTable from '@/components/AppTable.vue'
import UserDetailDrawer from './components/UserDetailDrawer.vue'
import UserFormDrawer from './components/UserFormDrawer.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const tableRef = ref()

const searchForm = reactive({ keyword: '', status: undefined })
const columns = [
  { title: '账号', dataIndex: 'account', key: 'account' },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at' },
  { title: '操作', key: 'action', width: 200 },
]

const detailOpen = ref(false)
const formOpen = ref(false)
const currentId = ref('')

function openDetail(id: string) { currentId.value = id; detailOpen.value = true }
function openEdit(id: string) { currentId.value = id; formOpen.value = true }
function openCreate() { currentId.value = ''; formOpen.value = true }

async function handleDelete(id: string) {
  const { isSuccess } = await fetchUserRemove({ ids: [id] })
  if (isSuccess) tableRef.value?.refresh()
}
</script>
