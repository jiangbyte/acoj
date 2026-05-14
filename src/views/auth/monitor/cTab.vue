<template>
  <AppTable
    ref="tableRef"
    perm="sys:session:page"
    :columns="columns"
    :fetch-data="fetchClientSessionPage"
    :search-form="searchForm"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'avatar'">
        <a-avatar :src="record.avatar" :size="32">
          {{ record.nickname?.[0] || record.account?.[0] }}
        </a-avatar>
      </template>
      <template v-if="column.key === 'status'">
        <a-tag :color="$dict.color('USER_STATUS', record.status)">
          {{ $dict.label('USER_STATUS', record.status) }}
        </a-tag>
      </template>
      <template v-if="column.key === 'action'">
        <a-popconfirm title="确定强制下线该用户？" @confirm="handleExit(record)">
          <a-button type="link" size="small" danger>强退</a-button>
        </a-popconfirm>
      </template>
    </template>
  </AppTable>
</template>

<script setup lang="ts">
defineOptions({ name: 'CSessionTab' })
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { fetchClientSessionPage, fetchClientSessionExit } from '@/api/monitor'
import AppTable from '@/components/table/AppTable.vue'

const searchForm = reactive({ keyword: '' })
const tableRef = ref()

async function handleExit(record: any) {
  const { success } = await fetchClientSessionExit(record.user_id)
  if (success) {
    message.success('已强制下线')
    tableRef.value?.refresh(true)
  }
}

const columns = [
  { title: '头像', key: 'avatar', width: 60 },
  { title: '账号', dataIndex: 'account', key: 'account', width: 140 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 140 },
  { title: '状态', key: 'status', dataIndex: 'status', width: 80 },
  { title: '登录IP', dataIndex: 'last_login_ip', key: 'last_login_ip', width: 140 },
  { title: '登录地址', dataIndex: 'last_login_address', key: 'last_login_address', width: 140 },
  { title: '登录时间', dataIndex: 'last_login_time', key: 'last_login_time', width: 170 },
  { title: '会话时长', dataIndex: 'session_timeout', key: 'session_timeout', width: 130 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
]
</script>
