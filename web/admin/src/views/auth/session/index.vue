<script setup lang="tsx">
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { sessionApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { dictList, dictTypeColor, dictTypeData } from '@/utils/dict'
import { NButton, NDataTable, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'

const state = reactive({
  rows: [] as any[],
  tokens: [] as any[],
  analysis: {} as any,
  total: 0,
  loading: false,
  tokenModalShow: false,
  searchValues: {} as any,
  page: 1,
  pageSize: 20,
})

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      account: (value) => String(value).trim(),
      account_id: (value) => String(value).trim(),
      ip: (value) => String(value).trim(),
    })
    state.page = 1
    fetchPage()
  },
  onReset() {
    state.searchValues = {}
    state.page = 1
    fetchPage()
  },
})

const analysisCards = computed(() => [
  { key: 'online_account_count', icon: 'icon-park-outline:people', color: '#2563eb' },
  { key: 'online_token_count', icon: 'icon-park-outline:devices', color: '#0f766e' },
  { key: 'admin_account_count', icon: 'icon-park-outline:permissions', color: '#7c3aed' },
  { key: 'portal_account_count', icon: 'icon-park-outline:user', color: '#0891b2' },
  { key: 'one_hour_new_count', icon: 'icon-park-outline:time', color: '#f59e0b' },
  { key: 'max_token_count', icon: 'icon-park-outline:connection', color: '#dc2626' },
])
const analysisTitleMap: Record<string, string> = {
  online_account_count: 'Online Accounts',
  online_token_count: 'Online Devices',
  admin_account_count: 'Admin Accounts',
  portal_account_count: 'Portal Accounts',
  one_hour_new_count: 'Logins in 1 Hour',
  max_token_count: 'Max Devices per Account',
}

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  {
    title: 'Account Type',
    path: 'account_type',
    field: 'select',
    fieldProps: { options: dictList('ACCOUNT_TYPE') },
  },
  { title: 'Account', path: 'account', field: 'input' },
  { title: 'Account ID', path: 'account_id', field: 'input' },
  { title: 'Client IP', path: 'ip', field: 'input' },
])

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => `${itemCount} total`,
  onUpdatePage: (value) => {
    state.page = value
    fetchPage()
  },
  onUpdatePageSize: (value) => {
    state.pageSize = value
    state.page = 1
    fetchPage()
  },
}))

const tableColumns = computed<ProDataTableColumns<any>>(() => [
  { title: 'Account ID', path: 'account_id', width: 170, ellipsis: { tooltip: true } },
  {
    title: 'Account Type',
    path: 'account_type',
    width: 130,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('ACCOUNT_TYPE', row.account_type))} bordered={false}>
        {dictTypeData('ACCOUNT_TYPE', row.account_type) || row.account_type}
      </NTag>
    ),
  },
  { title: 'Account', path: 'account', width: 160, ellipsis: { tooltip: true } },
  { title: 'Name', path: 'name', width: 160, ellipsis: { tooltip: true } },
  { title: 'Devices', path: 'token_count', width: 110 },
  {
    title: 'Client IP',
    key: 'client_ip',
    width: 150,
    render: (row) => row.tokens?.[0]?.client_ip || row.latest_login_ip || '-',
  },
  {
    title: 'Device',
    key: 'device',
    width: 140,
    render: (row) => row.tokens?.[0]?.device_label || '-',
  },
  { title: 'Latest Active', path: 'latest_active_at', width: 190, ellipsis: { tooltip: true } },
  {
    title: 'Operation',
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('auth:session:tokenlist') ? (
          <NButton type="info" size="small" text={true} onClick={() => openTokens(row)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
        ) : null}
        {hasPermission('auth:session:exit') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmExitAccount(row)}>
            {renderButtonIcon('icon-park-outline:logout')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

const tokenColumns = computed<DataTableColumns<any>>(() => [
  { title: 'Token', key: 'token', width: 220, ellipsis: { tooltip: true } },
  { title: 'Device', key: 'device_label', width: 110 },
  { title: 'Client IP', key: 'client_ip', width: 140 },
  { title: 'Login At', key: 'login_at', width: 180, ellipsis: { tooltip: true } },
  {
    title: 'Last Active',
    key: 'last_active_at',
    width: 180,
    ellipsis: { tooltip: true },
  },
  { title: 'Expires At', key: 'expires_at', width: 180, ellipsis: { tooltip: true } },
  {
    title: 'Operation',
    key: 'actions',
    width: 90,
    fixed: 'right',
    render: (row) => (
      hasPermission('auth:session:tokenexit') ? (
        <NButton type="error" size="small" text={true} onClick={() => confirmExitToken(row.token)}>
          {renderButtonIcon('icon-park-outline:logout')}
        </NButton>
      ) : null
    ),
  },
])

onMounted(fetchAll)

async function fetchAll() {
  await Promise.all([fetchAnalysis(), fetchPage()])
}

async function fetchAnalysis() {
  const response = await sessionApi.analysis()
  state.analysis = response.data ?? {}
}

async function fetchPage() {
  state.loading = true
  try {
    const response = await sessionApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.rows = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
  } finally {
    state.loading = false
  }
}

function openTokens(row: any) {
  state.tokens = row.tokens ?? []
  state.tokenModalShow = true
}

function confirmExitAccount(row: any) {
  window.$dialog.warning({
    title: 'Force Logout',
    draggable: true,
    maskClosable: false,
    content: 'Force logout all online devices for this account?',
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: async () => {
      await sessionApi.exit({ targets: [{ account_type: row.account_type, account_id: row.account_id }] })
      window.$message.success('Forced logout successfully')
      await fetchAll()
    },
  })
}

function confirmExitToken(token: string) {
  window.$dialog.warning({
    title: 'Force Logout',
    draggable: true,
    maskClosable: false,
    content: 'Force logout this online device?',
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: async () => {
      await sessionApi.tokenExit({ tokens: [token] })
      window.$message.success('Forced logout successfully')
      state.tokens = state.tokens.filter((item) => item.token !== token)
      await fetchAll()
    },
  })
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <NGrid cols="2 s:3 xl:6" responsive="screen" :x-gap="10" :y-gap="10">
      <NGridItem v-for="item in analysisCards" :key="item.key">
        <NCard class="session-stat" :bordered="false">
          <div class="session-stat__icon" :style="{ color: item.color, backgroundColor: `${item.color}14` }">
            <NovaIcon :icon="item.icon" :size="22" />
          </div>
          <div class="session-stat__title">{{ analysisTitleMap[item.key] ?? item.key }}</div>
          <div class="session-stat__value">{{ state.analysis[item.key] ?? 0 }}</div>
        </NCard>
      </NGridItem>
    </NGrid>

    <ProCard content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: 'Reset' }"
        :search-button-props="{ content: 'Search' }"
        :collapse-button-props="{
          content: searchForm.collapsed.value
            ? 'Expand'
            : 'Collapse',
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="'Online Sessions'"
      row-key="account_id"
      :scroll-x="1340"
      :columns="tableColumns"
      :data="state.rows"
      :loading="state.loading"
      :pagination="pagination"
    >
      <template #toolbar>
        <NButton text :title="'Reload'" :aria-label="'Reload'" :loading="state.loading" @click="fetchAll">
          <template #icon>
            <NIcon>
              <Icon icon="icon-park-outline:reload" />
            </NIcon>
          </template>
        </NButton>
      </template>
    </ProDataTable>

    <NModal
      v-model:show="state.tokenModalShow"
      preset="card"
      draggable
      :title="'Device Detail'"
      style="width: min(960px, calc(100vw - 32px))"
    >
      <NDataTable
        :row-key="(row) => row.token"
        :scroll-x="1170"
        :columns="tokenColumns"
        :data="state.tokens"
        :pagination="false"
      />
    </NModal>
  </NFlex>
</template>

<style scoped>
.session-stat :deep(.n-card__content) {
  display: grid;
  gap: 8px;
  min-height: 118px;
}

.session-stat__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
}

.session-stat__title {
  color: var(--text-color-3);
  font-size: 13px;
}

.session-stat__value {
  color: var(--text-color-base);
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}
</style>
