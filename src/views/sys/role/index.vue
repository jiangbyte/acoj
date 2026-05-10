<template>
  <AppTable ref="tableRef" :columns="columns" :fetchData="fetchRolePage" :searchForm="searchForm">
    <template #search>
      <a-form-item label="关键词" name="keyword"><a-input v-model:value="searchForm.keyword" /></a-form-item>
    </template>
    <template #toolbar>
      <a-button type="primary" @click="openCreate" v-if="hasPermission('sys:role:create')">新增</a-button>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'action'">
        <a-space>
          <a-button type="link" size="small" @click="openDetail(record.id)">详情</a-button>
          <a-button type="link" size="small" @click="openEdit(record.id)" v-if="hasPermission('sys:role:modify')">编辑</a-button>
          <a-button type="link" size="small" @click="openPermission(record.id)" v-if="hasPermission('sys:role:grant-permission')">权限</a-button>
          <a-popconfirm title="确定删除？" @confirm="handleDelete(record.id)" v-if="hasPermission('sys:role:remove')">
            <a-button type="link" danger size="small">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </AppTable>
  <RoleFormDrawer v-model:open="formOpen" :id="currentId" @success="tableRef?.refresh()" />
  <RolePermissionDrawer v-model:open="permOpen" :id="currentId" @success="tableRef?.refresh()" />
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { fetchRolePage, fetchRoleRemove } from '@/service/api/role'
import { useAuthStore } from '@/store/auth'
import AppTable from '@/components/AppTable.vue'
import RoleFormDrawer from './components/RoleFormDrawer.vue'
import RolePermissionDrawer from './components/RolePermissionDrawer.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const tableRef = ref()
const searchForm = reactive({ keyword: '' })
const columns = [
  { title: '角色编码', dataIndex: 'code' },
  { title: '角色名称', dataIndex: 'name' },
  { title: '描述', dataIndex: 'description' },
  { title: '操作', key: 'action', width: 280 },
]

const formOpen = ref(false)
const permOpen = ref(false)
const currentId = ref('')

function openCreate() { currentId.value = ''; formOpen.value = true }
function openEdit(id: string) { currentId.value = id; formOpen.value = true }
function openPermission(id: string) { currentId.value = id; permOpen.value = true }

async function handleDelete(id: string) {
  const { success } = await fetchRoleRemove({ ids: [id] })
  if (success) tableRef.value?.refresh()
}
</script>
