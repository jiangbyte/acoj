<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { roleApi } from '@/api'
import { NAvatar, NButton } from 'naive-ui'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const mockOrgTree = [
  {
    id: 'root',
    name: '全部组织',
    children: [
      { id: 'platform', name: '平台中心' },
      { id: 'portal', name: '门户中心' },
    ],
  },
]

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  searchKey: '',
  orgId: '',
  role: {} as any,
  users: [] as any[],
  selectedData: [] as any[],
  page: 1,
  pageSize: 10,
  expandedKeys: ['root'] as Array<string | number>,
})

const modalTitle = computed(() =>
  state.role?.name
    ? `${t('pages.iam.role.grantUser')} - ${state.role.name}`
    : t('pages.iam.role.grantUser'),
)
const filteredUsers = computed(() => {
  const keyword = state.searchKey.trim().toLowerCase()
  return state.users.filter((item) => {
    const matchKeyword =
      !keyword ||
      [item.account, item.name]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    const matchOrg = !state.orgId || state.orgId === 'root' || item.org_id === state.orgId
    return matchKeyword && matchOrg
  })
})
const tableUsers = computed(() => {
  const start = (state.page - 1) * state.pageSize
  return filteredUsers.value.slice(start, start + state.pageSize)
})
const selectedIds = computed(() => new Set(state.selectedData.map((item) => String(item.id))))

const userColumns = computed<DataTableColumns<any>>(() => [
  {
    title: t('common.often.operation'),
    key: 'action',
    align: 'center',
    width: 70,
    render: (row) => (
      <NButton
        dashed
        size="small"
        disabled={selectedIds.value.has(String(row.id))}
        onClick={() => addRecord(row)}
      >
        +
      </NButton>
    ),
  },
  {
    title: t('pages.iam.account.avatar'),
    key: 'avatar',
    width: 70,
    render: (row) => (
      <NAvatar size="small" src={row.avatar}>
        {row.name?.slice(0, 1)}
      </NAvatar>
    ),
  },
  {
    title: t('pages.iam.account.name'),
    key: 'name',
    minWidth: 120,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.iam.account.account'),
    key: 'account',
    minWidth: 120,
    ellipsis: {
      tooltip: true,
    },
  },
])
const selectedColumns = computed<DataTableColumns<any>>(() => [
  {
    title: t('common.often.operation'),
    key: 'action',
    align: 'center',
    width: 70,
    render: (row) => (
      <NButton dashed type="error" size="small" onClick={() => delRecord(row)}>
        -
      </NButton>
    ),
  },
  {
    title: t('pages.iam.account.name'),
    key: 'name',
    minWidth: 120,
    ellipsis: {
      tooltip: true,
    },
  },
])

async function openModal(role: any) {
  state.role = role ?? {}
  state.searchKey = ''
  state.orgId = ''
  state.users = []
  state.selectedData = []
  state.page = 1
  state.pageSize = 10
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.role?.id) {
    return
  }
  state.loading = true
  try {
    const response = await roleApi.ownUsers(state.role.id)
    state.users = response.data?.users ?? []
    const accountIds = new Set((response.data?.accountIds ?? []).map(String))
    state.selectedData = state.users.filter((item) => accountIds.has(String(item.id)))
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    await roleApi.grantUsers({
      roleId: state.role.id,
      grantInfoList: state.selectedData.map((item) => item.id),
      accountIds: state.selectedData.map((item) => item.id),
    })
    window.$message.success(t('pages.iam.role.grantSuccess'))
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function closeModal() {
  state.users = []
  state.selectedData = []
  state.showModal = false
  state.submitLoading = false
}

function addRecord(record: any) {
  if (!selectedIds.value.has(String(record.id))) {
    state.selectedData.push(record)
  }
}

function addAllPageRecord() {
  tableUsers.value.forEach(addRecord)
}

function delRecord(record: any) {
  state.selectedData = state.selectedData.filter((item) => String(item.id) !== String(record.id))
}

function delAllRecord() {
  state.selectedData = []
}

function resetSearch() {
  state.searchKey = ''
  state.page = 1
}

function treeSelect(keys: Array<string | number>) {
  state.orgId = String(keys[0] ?? '')
  state.page = 1
}

defineExpose({
  openModal,
})
</script>

<template>
  <NDrawer
    v-model:show="state.showModal"
    :default-width="1000"
    placement="right"
    resizable
    :mask-closable="false"
  >
    <NDrawerContent :title="modalTitle" closable :native-scrollbar="false">
      <NGrid :cols="24" :x-gap="10">
        <NGi :span="7">
          <NCard size="small" class="selector-panel">
            <NSpin :show="state.loading">
              <NTree
                v-model:expanded-keys="state.expandedKeys"
                block-line
                key-field="id"
                label-field="name"
                :data="mockOrgTree"
                :selected-keys="state.orgId ? [state.orgId] : []"
                @update:selected-keys="treeSelect"
              />
            </NSpin>
          </NCard>
        </NGi>
        <NGi :span="11">
          <NSpace vertical>
            <NInputGroup>
              <NInput
                v-model:value="state.searchKey"
                clearable
                :placeholder="t('pages.iam.role.userSearchPlaceholder')"
                @keyup.enter="state.page = 1"
                @clear="resetSearch"
              />
              <NButton type="primary" @click="state.page = 1">
                {{ t('pages.iam.role.search') }}
              </NButton>
              <NButton @click="resetSearch">
                {{ t('common.reset') }}
              </NButton>
            </NInputGroup>
            <NFlex justify="space-between" align="center">
              <NText>
                {{ t('pages.iam.role.pendingUserCount', { count: filteredUsers.length }) }}
              </NText>
              <NButton dashed size="small" @click="addAllPageRecord">
                {{ t('pages.iam.role.addCurrentPage') }}
              </NButton>
            </NFlex>
            <NDataTable
              size="small"
              :row-key="(row) => row.id"
              :columns="userColumns"
              :data="tableUsers"
              :loading="state.loading"
              :bordered="true"
              :single-line="false"
              max-height="calc(100vh - 320px)"
            />
            <NPagination
              v-model:page="state.page"
              v-model:page-size="state.pageSize"
              show-size-picker
              size="small"
              :item-count="filteredUsers.length"
              :page-sizes="[10, 20, 50, 100]"
            />
          </NSpace>
        </NGi>
        <NGi :span="6">
          <NSpace vertical>
            <NFlex justify="space-between" align="center">
              <NText>{{
                t('pages.iam.role.selectedUserCount', { count: state.selectedData.length })
              }}</NText>
              <NButton dashed type="error" size="small" @click="delAllRecord">
                {{ t('pages.iam.role.removeAll') }}
              </NButton>
            </NFlex>
            <NDataTable
              size="small"
              :row-key="(row) => row.id"
              :columns="selectedColumns"
              :data="state.selectedData"
              :bordered="true"
              :single-line="false"
              max-height="calc(100vh - 260px)"
            />
          </NSpace>
        </NGi>
      </NGrid>

      <template #footer>
        <NSpace justify="end" align="center">
          <NButton @click="closeModal">
            {{ t('common.close') }}
          </NButton>
          <NButton type="primary" :loading="state.submitLoading" @click="submitGrant">
            {{ t('common.save') }}
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>
.selector-panel {
  min-height: calc(100vh - 180px);
}
</style>
