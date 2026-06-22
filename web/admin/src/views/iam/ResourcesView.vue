<script setup lang="ts">
import { DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { onMounted, reactive, ref } from 'vue'

import type { ResourceTreeNode } from '@/types/route'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listResourceTree } from '@/apis/iam'

const query = reactive<{
  keyword: string
  resource_type?: string
  module?: string
}>({
  keyword: '',
  resource_type: undefined,
  module: undefined,
})
const data = ref<ResourceTreeNode[]>([])
const drawerOpen = ref(false)

const columns: TableColumnsType<ResourceTreeNode> = [
  { title: '资源名称', dataIndex: 'name', key: 'name' },
  { title: '编码', dataIndex: 'code', key: 'code' },
  { title: '类型', dataIndex: 'resource_type', key: 'resource_type' },
  { title: '模块', dataIndex: 'module', key: 'module' },
  { title: '路径', dataIndex: 'path', key: 'path' },
  { title: '组件', dataIndex: 'component', key: 'component' },
  { title: '操作', key: 'actions', width: 160 },
]

async function fetchData() {
  data.value = await listResourceTree()
}

function resetQuery() {
  query.keyword = ''
  query.resource_type = undefined
  query.module = undefined
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <QueryTable>
    <template #search="{ expanded, toggle }">
      <AForm layout="inline" :model="query">
        <ARow :gutter="[48, 16]" class="w-full">
          <ACol :md="8" :sm="24">
            <AFormItem label="关键词">
              <AInput v-model:value="query.keyword" allow-clear placeholder="资源名称、编码、路径" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem label="资源类型">
              <ASelect v-model:value="query.resource_type" allow-clear placeholder="请选择">
                <ASelectOption value="CATALOG">目录</ASelectOption>
                <ASelectOption value="MENU">菜单</ASelectOption>
                <ASelectOption value="PAGE">页面</ASelectOption>
                <ASelectOption value="BUTTON">按钮</ASelectOption>
                <ASelectOption value="API_GROUP">接口分组</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="模块">
              <ASelect v-model:value="query.module" allow-clear placeholder="请选择">
                <ASelectOption value="dashboard">dashboard</ASelectOption>
                <ASelectOption value="iam">iam</ASelectOption>
                <ASelectOption value="file">file</ASelectOption>
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
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">资源树</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </AButton>
        <AButton type="primary" @click="drawerOpen = true">
          <template #icon><PlusOutlined /></template>
          新建资源
        </AButton>
      </ASpace>
    </template>

    <ATable :columns="columns" :data-source="data" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'resource_type'">
          <ATag
            :color="
              record.resource_type === 'CATALOG' || record.resource_type === 'MENU'
                ? 'blue'
                : record.resource_type === 'BUTTON'
                  ? 'gold'
                  : 'purple'
            "
          >
            {{ record.resource_type }}
          </ATag>
        </template>
        <template v-if="column.key === 'actions'">
          <span class="inline-flex flex-wrap gap-2">
            <AButton size="small" type="link">编辑</AButton>
            <AButton size="small" type="link">绑定权限</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" title="新建资源" width="520">
      <AForm layout="vertical">
        <AFormItem label="资源名称"><AInput /></AFormItem>
        <AFormItem label="资源编码"><AInput /></AFormItem>
        <AFormItem label="资源类型"><ASelect /></AFormItem>
        <AFormItem label="访问路径"><AInput /></AFormItem>
        <AFormItem label="权限点"><AInput placeholder="iam:resource:list" /></AFormItem>
      </AForm>
    </ADrawer>
  </QueryTable>
</template>
