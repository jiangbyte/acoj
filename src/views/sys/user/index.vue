<template>
  <div class="flex flex-col gap-2">
    <!-- Search panel: first item visible, rest auto-collapsed -->
    <AppSearchPanel
      :model="searchForm"
      perm="sys:user:page"
      @search="handleSearch"
      @reset="resetSearch"
    >
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="账号/昵称" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="状态" name="status">
          <DictSelect
            v-model="searchForm.status"
            type-code="USER_STATUS"
            placeholder="全部"
            :allow-clear="true"
          />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <!-- Table panel -->
    <AppTable
      ref="tableRef"
      perm="sys:user:page"
      :columns="columns"
      :fetch-data="fetchUserPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:user:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增用户
        </a-button>
        <a-button
          v-if="hasPermission('sys:user:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'avatar'">
          <a-avatar :size="32" :src="record.avatar" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="hasPermission('sys:user:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-dropdown
              v-if="
                hasPermission('sys:user:grant-role') || hasPermission('sys:user:grant-permission')
              "
            >
              <a-button type="link" size="small">
                授权
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item
                    v-if="hasPermission('sys:user:grant-role')"
                    @click="openGrantRole(record)"
                  >
                    分配角色
                  </a-menu-item>
                  <a-menu-item
                    v-if="hasPermission('sys:user:grant-permission')"
                    @click="openGrantPermission(record)"
                  >
                    授权权限
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-popconfirm
              v-if="hasPermission('sys:user:remove')"
              title="确定删除？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="$dict.color('USER_STATUS', record.status)">
            {{ $dict.label('USER_STATUS', record.status) }}
          </a-tag>
        </template>
      </template>
    </AppTable>

    <!-- Drawers -->
    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
    <GrantRole ref="grantRoleRef" v-model:open="grantRoleOpen" @success="handleFormSuccess" />
    <GrantPermission
      ref="grantPermissionRef"
      v-model:open="grantPermissionOpen"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysUser' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'

import {
  fetchUserPage,
  fetchUserRemove,
} from '@/api/user'
import { useCrud } from '@/hooks/useCrud'
import DictSelect from '@/components/form/DictSelect.vue'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'
import GrantRole from './components/grantRole.vue'
import GrantPermission from './components/grantPermission.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '用户', deleteApi: fetchUserRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud

const searchForm = reactive({ keyword: '', status: undefined })
const columns = [
  { title: '头像', dataIndex: 'avatar', key: 'avatar', width: 70 },
  { title: '账号', dataIndex: 'account', key: 'account', width: 150 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 150 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 200, ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = undefined
  tableRef.value?.refresh(true)
}

// Refs
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

const grantRoleRef = ref()
const grantPermissionRef = ref()
const grantRoleOpen = ref(false)
const grantPermissionOpen = ref(false)

function openGrantRole(record: any) {
  grantRoleRef.value?.doOpen(record)
}
function openGrantPermission(record: any) {
  grantPermissionRef.value?.doOpen(record)
}

</script>
