<script setup lang="ts">
import { DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import { computed, onMounted, reactive, ref } from 'vue'

import type { AccountItem } from '@/types/api'
import StatusTag from '@/components/common/StatusTag.vue'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listAccounts } from '@/apis/iam'
import { formatDateTime } from '@hei/shared'

const loading = ref(false)
const drawerOpen = ref(false)
const query = reactive<{
  keyword: string
  status?: string
  account_type?: string
  page: number
  page_size: number
}>({ keyword: '', status: undefined, account_type: undefined, page: 1, page_size: 10 })
const data = ref<AccountItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<Key[]>([])

const columns: TableColumnsType<AccountItem> = [
  { title: '#', key: 'serial', fixed: 'left', width: 70 },
  { title: '账号', dataIndex: 'account', key: 'account', fixed: 'left', width: 140 },
  { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
  { title: '部门', dataIndex: 'dept_name', key: 'dept_name', width: 140 },
  { title: '角色', dataIndex: 'role_names', key: 'role_names', width: 180 },
  { title: '状态', dataIndex: 'account_status', key: 'account_status', width: 100 },
  { title: '手机', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 180 },
  { title: '更新时间', dataIndex: 'updated_at', key: 'updated_at', width: 160 },
  { title: '操作', key: 'actions', fixed: 'right', width: 150 },
]

async function fetchData() {
  loading.value = true
  try {
    const result = await listAccounts(query)
    data.value = result.items
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.status = undefined
  query.account_type = undefined
  query.page = 1
  fetchData()
}

onMounted(fetchData)

const rowSelection = computed<TableRowSelection<AccountItem>>(() => ({
  fixed: true,
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  },
}))
</script>

<template>
  <div>
    <QueryTable>
      <template #search="{ expanded, toggle }">
        <AForm layout="inline" :model="query">
          <ARow :gutter="[48, 16]" class="w-full">
            <ACol :md="8" :sm="24">
              <AFormItem label="关键词">
                <AInput v-model:value="query.keyword" allow-clear placeholder="账号、姓名、邮箱" @press-enter="fetchData" />
              </AFormItem>
            </ACol>
            <ACol :md="8" :sm="24">
              <AFormItem label="账号类型">
                <ASelect v-model:value="query.account_type" allow-clear placeholder="请选择">
                  <ASelectOption value="admin">管理端</ASelectOption>
                  <ASelectOption value="portal">门户端</ASelectOption>
                </ASelect>
              </AFormItem>
            </ACol>
            <ACol v-show="expanded" :md="8" :sm="24">
              <AFormItem label="状态">
                <ASelect v-model:value="query.status" allow-clear placeholder="请选择">
                  <ASelectOption value="enabled">启用</ASelectOption>
                  <ASelectOption value="disabled">停用</ASelectOption>
                  <ASelectOption value="locked">锁定</ASelectOption>
                </ASelect>
              </AFormItem>
            </ACol>
            <ACol :md="expanded ? 24 : 8" :sm="24">
              <span class="inline-flex flex-wrap gap-2" :class="{ 'is-expanded': expanded }">
                <AButton type="link" @click="toggle">
                  {{ expanded ? '收起' : '展开' }}
                  <UpOutlined v-if="expanded" />
                  <DownOutlined v-else />
                </AButton>
                <AButton type="primary" @click="fetchData">查询</AButton>
                <AButton class="ml-2" @click="resetQuery">重置</AButton>
              </span>
            </ACol>
          </ARow>
        </AForm>
      </template>

      <template #toolbar>
        <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">账号列表</div>
        <ASpace>
          <AButton @click="fetchData">
            <template #icon><ReloadOutlined /></template>
            刷新
          </AButton>
          <AButton type="primary" @click="drawerOpen = true">
            <template #icon><PlusOutlined /></template>
            新建账号
          </AButton>
          <ADropdown v-if="selectedRowKeys.length > 0">
            <AButton>批量操作 <DownOutlined /></AButton>
            <template #overlay>
              <AMenu>
                <AMenuItem key="disable">停用</AMenuItem>
                <AMenuItem key="lock">锁定</AMenuItem>
              </AMenu>
            </template>
          </ADropdown>
        </ASpace>
      </template>

      <template #alert>
        <AAlert show-icon type="info">
          <template #message>
            已选择 <a>{{ selectedRowKeys.length }}</a> 项
            <a class="ml-3" @click="selectedRowKeys = []">清空</a>
          </template>
        </AAlert>
      </template>

      <ATable
        :columns="columns"
        :data-source="data"
        :loading="loading"
        :pagination="{ current: query.page, pageSize: query.page_size, total }"
        :row-selection="rowSelection"
        :scroll="{ x: 1260 }"
        row-key="id"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'serial'">
            {{ data.findIndex((item) => item.id === record.id) + 1 }}
          </template>
          <template v-if="column.key === 'role_names'">
            <ASpace wrap>
              <ATag v-for="role in record.role_names" :key="role">{{ role }}</ATag>
            </ASpace>
          </template>
          <template v-if="column.key === 'account_status'">
            <StatusTag :status="record.account_status" />
          </template>
          <template v-if="column.key === 'updated_at'">
            {{ formatDateTime(record.updated_at) }}
          </template>
          <template v-if="column.key === 'actions'">
            <span class="inline-flex flex-wrap gap-2">
              <AButton size="small" type="link">编辑</AButton>
              <AButton size="small" type="link">分配角色</AButton>
            </span>
          </template>
        </template>
      </ATable>
    </QueryTable>

    <ADrawer v-model:open="drawerOpen" title="新建账号" width="520">
      <AForm layout="vertical">
        <AFormItem label="账号"><AInput placeholder="请输入登录账号" /></AFormItem>
        <AFormItem label="姓名"><AInput placeholder="请输入姓名" /></AFormItem>
        <AFormItem label="手机号"><AInput placeholder="请输入手机号" /></AFormItem>
        <AFormItem label="邮箱"><AInput placeholder="请输入邮箱" /></AFormItem>
        <AFormItem label="角色"><ASelect mode="multiple" placeholder="请选择角色" /></AFormItem>
      </AForm>
      <template #footer>
        <ASpace>
          <AButton @click="drawerOpen = false">取消</AButton>
          <AButton type="primary" @click="drawerOpen = false">保存</AButton>
        </ASpace>
      </template>
    </ADrawer>
  </div>
</template>
