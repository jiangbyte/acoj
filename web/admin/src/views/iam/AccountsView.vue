<script setup lang="ts">
import { DownOutlined, PlusOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type { AccountItem } from '@/types/api'
import StatusTag from '@/components/common/StatusTag.vue'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listAccounts } from '@/apis/iam'
import { formatDateTime } from '@hei/shared'

const { t } = useI18n()
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

const columns = computed<TableColumnsType<AccountItem>>(() => [
  { title: '#', key: 'serial', fixed: 'left', width: 70 },
  { title: t('table.account'), dataIndex: 'account', key: 'account', fixed: 'left', width: 140 },
  { title: t('table.realName'), dataIndex: 'name', key: 'name', width: 120 },
  { title: t('table.department'), dataIndex: 'dept_name', key: 'dept_name', width: 140 },
  { title: t('table.role'), dataIndex: 'role_names', key: 'role_names', width: 180 },
  { title: t('common.status'), dataIndex: 'account_status', key: 'account_status', width: 100 },
  { title: t('table.phone'), dataIndex: 'phone', key: 'phone', width: 140 },
  { title: t('table.email'), dataIndex: 'email', key: 'email', width: 180 },
  { title: t('common.updatedAt'), dataIndex: 'updated_at', key: 'updated_at', width: 160 },
  { title: t('common.actions'), key: 'actions', fixed: 'right', width: 150 },
])

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
              <AFormItem :label="t('common.keyword')">
                <AInput v-model:value="query.keyword" allow-clear :placeholder="t('table.accountKeyword')" @press-enter="fetchData" />
              </AFormItem>
            </ACol>
            <ACol :md="8" :sm="24">
              <AFormItem :label="t('table.accountType')">
                <ASelect v-model:value="query.account_type" allow-clear :placeholder="t('common.pleaseSelect')">
                  <ASelectOption value="admin">{{ t('table.adminSide') }}</ASelectOption>
                  <ASelectOption value="portal">{{ t('table.portalSide') }}</ASelectOption>
                </ASelect>
              </AFormItem>
            </ACol>
            <ACol v-show="expanded" :md="8" :sm="24">
              <AFormItem :label="t('common.status')">
                <ASelect v-model:value="query.status" allow-clear :placeholder="t('common.pleaseSelect')">
                  <ASelectOption value="enabled">{{ t('common.enabled') }}</ASelectOption>
                  <ASelectOption value="disabled">{{ t('common.disabled') }}</ASelectOption>
                  <ASelectOption value="locked">{{ t('common.locked') }}</ASelectOption>
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
        <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">{{ t('table.accountList') }}</div>
        <ASpace>
          <AButton @click="fetchData">
            <template #icon><ReloadOutlined /></template>
            {{ t('common.refresh') }}
          </AButton>
          <AButton type="primary" @click="drawerOpen = true">
            <template #icon><PlusOutlined /></template>
            {{ t('table.createAccount') }}
          </AButton>
          <ADropdown v-if="selectedRowKeys.length > 0">
            <AButton>{{ t('common.batchActions') }} <DownOutlined /></AButton>
            <template #overlay>
              <AMenu>
                <AMenuItem key="disable">{{ t('common.disabled') }}</AMenuItem>
                <AMenuItem key="lock">{{ t('common.locked') }}</AMenuItem>
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
              <AButton size="small" type="link">{{ t('common.edit') }}</AButton>
              <AButton size="small" type="link">{{ t('table.assignRole') }}</AButton>
            </span>
          </template>
        </template>
      </ATable>
    </QueryTable>

    <ADrawer v-model:open="drawerOpen" :title="t('table.createAccount')" width="520">
      <AForm layout="vertical">
        <AFormItem :label="t('table.account')"><AInput :placeholder="t('table.loginAccountPlaceholder')" /></AFormItem>
        <AFormItem :label="t('table.realName')"><AInput :placeholder="t('table.realNamePlaceholder')" /></AFormItem>
        <AFormItem :label="t('table.phone')"><AInput :placeholder="t('table.phonePlaceholder')" /></AFormItem>
        <AFormItem :label="t('table.email')"><AInput :placeholder="t('table.emailPlaceholder')" /></AFormItem>
        <AFormItem :label="t('table.role')"><ASelect mode="multiple" :placeholder="t('table.selectRole')" /></AFormItem>
      </AForm>
      <template #footer>
        <ASpace>
          <AButton @click="drawerOpen = false">{{ t('common.cancel') }}</AButton>
          <AButton type="primary" @click="drawerOpen = false">{{ t('common.save') }}</AButton>
        </ASpace>
      </template>
    </ADrawer>
  </div>
</template>
