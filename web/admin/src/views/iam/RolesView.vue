<script setup lang="ts">
import { DownOutlined, PlusOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import { computed, onMounted, reactive, ref } from 'vue'

import type { RoleItem } from '@/types/api'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listRoles } from '@/apis/iam'
import { formatDateTime } from '@hei/shared'

const query = reactive<{
  keyword: string
  category?: string
  scope_type?: string
  page: number
  page_size: number
}>({
  keyword: '',
  category: undefined,
  scope_type: undefined,
  page: 1,
  page_size: 10,
})
const data = ref<RoleItem[]>([])
const total = ref(0)
const drawerOpen = ref(false)
const selectedRowKeys = ref<Key[]>([])

const columns: TableColumnsType<RoleItem> = [
  { title: '#', key: 'serial', width: 70 },
  { title: '角色名称', dataIndex: 'name', key: 'name' },
  { title: '编码', dataIndex: 'code', key: 'code' },
  { title: '类型', dataIndex: 'category', key: 'category' },
  { title: '数据范围', dataIndex: 'scope_type', key: 'scope_type' },
  { title: '用户数', dataIndex: 'user_count', key: 'user_count' },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at' },
  { title: '操作', key: 'actions', width: 180 },
]

async function fetchData() {
  const result = await listRoles(query)
  data.value = result.items
  total.value = result.total
}

function resetQuery() {
  query.keyword = ''
  query.category = undefined
  query.scope_type = undefined
  query.page = 1
  fetchData()
}

onMounted(fetchData)

const rowSelection = computed<TableRowSelection<RoleItem>>(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  },
}))
</script>

<template>
  <QueryTable>
    <template #search="{ expanded, toggle }">
      <AForm layout="inline" :model="query">
        <ARow :gutter="[48, 16]" class="w-full">
          <ACol :md="8" :sm="24">
            <AFormItem label="关键词">
              <AInput v-model:value="query.keyword" allow-clear placeholder="角色名称、编码" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem label="角色类型">
              <ASelect v-model:value="query.category" allow-clear placeholder="请选择">
                <ASelectOption value="system">系统</ASelectOption>
                <ASelectOption value="business">业务</ASelectOption>
                <ASelectOption value="audit">审计</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="数据范围">
              <ASelect v-model:value="query.scope_type" allow-clear placeholder="请选择">
                <ASelectOption value="all">全部</ASelectOption>
                <ASelectOption value="dept">本部门</ASelectOption>
                <ASelectOption value="custom">自定义</ASelectOption>
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
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">角色列表</div>
      <ASpace>
        <AButton type="primary" @click="drawerOpen = true">
          <template #icon><PlusOutlined /></template>
          新建角色
        </AButton>
        <ADropdown v-if="selectedRowKeys.length > 0">
          <AButton>批量操作 <DownOutlined /></AButton>
          <template #overlay>
            <AMenu>
              <AMenuItem key="delete">删除</AMenuItem>
              <AMenuItem key="disable">停用</AMenuItem>
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
      :pagination="{ current: query.page, pageSize: query.page_size, total }"
      :row-selection="rowSelection"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'serial'">
          {{ data.findIndex((item) => item.id === record.id) + 1 }}
        </template>
        <template v-if="column.key === 'created_at'">{{
          formatDateTime(record.created_at)
        }}</template>
        <template v-if="column.key === 'actions'">
          <span class="inline-flex flex-wrap gap-2">
            <AButton size="small" type="link">编辑</AButton>
            <AButton size="small" type="link">分配权限</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" title="新建角色" width="560">
      <AForm layout="vertical">
        <AFormItem label="角色名称"><AInput /></AFormItem>
        <AFormItem label="角色编码"><AInput /></AFormItem>
        <AFormItem label="数据范围"><ASelect placeholder="请选择数据范围" /></AFormItem>
        <AFormItem label="资源权限">
          <ATree checkable default-expand-all />
        </AFormItem>
      </AForm>
    </ADrawer>
  </QueryTable>
</template>
