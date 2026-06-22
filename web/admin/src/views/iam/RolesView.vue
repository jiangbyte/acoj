<script setup lang="ts">
import { DownOutlined, PlusOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type { RoleItem } from '@/types/api'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listRoles } from '@/apis/iam'
import { formatDateTime } from '@hei/shared'

const { t } = useI18n()
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

const columns = computed<TableColumnsType<RoleItem>>(() => [
  { title: '#', key: 'serial', width: 70 },
  { title: t('table.roleName'), dataIndex: 'name', key: 'name' },
  { title: t('common.code'), dataIndex: 'code', key: 'code' },
  { title: t('common.type'), dataIndex: 'category', key: 'category' },
  { title: t('table.dataScope'), dataIndex: 'scope_type', key: 'scope_type' },
  { title: t('table.userCount'), dataIndex: 'user_count', key: 'user_count' },
  { title: t('common.createdAt'), dataIndex: 'created_at', key: 'created_at' },
  { title: t('common.actions'), key: 'actions', width: 180 },
])

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
            <AFormItem :label="t('common.keyword')">
              <AInput v-model:value="query.keyword" allow-clear :placeholder="t('table.roleKeyword')" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('table.roleType')">
              <ASelect v-model:value="query.category" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption value="system">{{ t('table.systemRole') }}</ASelectOption>
                <ASelectOption value="business">{{ t('table.businessRole') }}</ASelectOption>
                <ASelectOption value="audit">{{ t('table.auditRole') }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('table.dataScope')">
              <ASelect v-model:value="query.scope_type" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption value="all">{{ t('table.all') }}</ASelectOption>
                <ASelectOption value="dept">{{ t('table.currentDept') }}</ASelectOption>
                <ASelectOption value="custom">{{ t('table.custom') }}</ASelectOption>
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
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">{{ t('table.roleList') }}</div>
      <ASpace>
        <AButton type="primary" @click="drawerOpen = true">
          <template #icon><PlusOutlined /></template>
          {{ t('table.createRole') }}
        </AButton>
        <ADropdown v-if="selectedRowKeys.length > 0">
          <AButton>{{ t('common.batchActions') }} <DownOutlined /></AButton>
          <template #overlay>
            <AMenu>
              <AMenuItem key="delete">{{ t('common.delete') }}</AMenuItem>
              <AMenuItem key="disable">{{ t('common.disabled') }}</AMenuItem>
            </AMenu>
          </template>
        </ADropdown>
      </ASpace>
    </template>

    <template #alert>
      <AAlert show-icon type="info">
        <template #message>
          {{ t('common.selectedCount', { count: selectedRowKeys.length }) }}
          <a class="ml-3" @click="selectedRowKeys = []">{{ t('common.clear') }}</a>
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
            <AButton size="small" type="link">{{ t('common.edit') }}</AButton>
            <AButton size="small" type="link">{{ t('table.assignPermission') }}</AButton>
          </span>
        </template>
      </template>
    </ATable>

    <ADrawer v-model:open="drawerOpen" :title="t('table.createRole')" width="560">
      <AForm layout="vertical">
        <AFormItem :label="t('table.roleName')"><AInput /></AFormItem>
        <AFormItem :label="t('table.roleCode')"><AInput /></AFormItem>
        <AFormItem :label="t('table.dataScope')"><ASelect :placeholder="t('table.dataScope')" /></AFormItem>
        <AFormItem :label="t('table.resourcePermission')">
          <ATree checkable default-expand-all />
        </AFormItem>
      </AForm>
    </ADrawer>
  </QueryTable>
</template>
