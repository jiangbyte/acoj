<script setup lang="ts">
import { DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type { ResourceTreeNode } from '@/types/route'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listResourceTree } from '@/apis/iam'

const { t } = useI18n()
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

const columns = computed<TableColumnsType<ResourceTreeNode>>(() => [
  { title: t('table.resourceName'), dataIndex: 'name', key: 'name' },
  { title: t('common.code'), dataIndex: 'code', key: 'code' },
  { title: t('common.type'), dataIndex: 'resource_type', key: 'resource_type' },
  { title: t('common.module'), dataIndex: 'module', key: 'module' },
  { title: t('common.path'), dataIndex: 'path', key: 'path' },
  { title: t('common.component'), dataIndex: 'component', key: 'component' },
  { title: t('common.actions'), key: 'actions', width: 160 },
])

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
            <AFormItem :label="t('common.keyword')">
              <AInput v-model:value="query.keyword" allow-clear :placeholder="t('table.resourceKeyword')" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('table.resourceType')">
              <ASelect v-model:value="query.resource_type" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption value="CATALOG">{{ t('table.catalog') }}</ASelectOption>
                <ASelectOption value="MENU">{{ t('table.menu') }}</ASelectOption>
                <ASelectOption value="PAGE">{{ t('table.page') }}</ASelectOption>
                <ASelectOption value="BUTTON">{{ t('table.button') }}</ASelectOption>
                <ASelectOption value="API_GROUP">{{ t('table.apiGroup') }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('common.module')">
              <ASelect v-model:value="query.module" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption value="dashboard">dashboard</ASelectOption>
                <ASelectOption value="iam">iam</ASelectOption>
                <ASelectOption value="file">file</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol :md="expanded ? 24 : 8" :sm="24">
            <span class="inline-flex flex-wrap gap-2" :class="{ 'is-expanded': expanded }">
              <AButton type="link" @click="toggle">
                {{ expanded ? t('common.collapse') : t('common.expand') }}
                <UpOutlined v-if="expanded" />
                <DownOutlined v-else />
              </AButton>
              <AButton type="primary" @click="fetchData">{{ t('common.search') }}</AButton>
              <AButton class="ml-2" @click="resetQuery">{{ t('common.reset') }}</AButton>
            </span>
          </ACol>
        </ARow>
      </AForm>
    </template>

    <template #toolbar>
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">{{ t('table.resourceTree') }}</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.refresh') }}
        </AButton>
        <AButton type="primary" @click="drawerOpen = true">
          <template #icon><PlusOutlined /></template>
          {{ t('table.createResource') }}
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
            <AButton size="small" type="link">{{ t('common.edit') }}</AButton>
            <AButton size="small" type="link">{{ t('table.bindPermission') }}</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" :title="t('table.createResource')" width="520">
      <AForm layout="vertical">
        <AFormItem :label="t('table.resourceName')"><AInput /></AFormItem>
        <AFormItem :label="t('table.resourceCode')"><AInput /></AFormItem>
        <AFormItem :label="t('table.resourceType')"><ASelect /></AFormItem>
        <AFormItem :label="t('table.accessPath')"><AInput /></AFormItem>
        <AFormItem :label="t('table.permissionPoint')"><AInput placeholder="iam:resource:list" /></AFormItem>
      </AForm>
    </ADrawer>
  </QueryTable>
</template>
