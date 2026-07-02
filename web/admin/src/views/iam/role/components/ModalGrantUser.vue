<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { roleApi } from '@/api'
import { resolveFileUrl } from '@/utils'
import { NAvatar, NButton } from 'naive-ui'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  searchKey: '',
  subject: {} as any,
  grantApi: roleApi as any,
  title: '',
  ownMethod: 'ownUsers',
  grantMethod: 'grantUsers',
  listKey: 'users',
  selectedKey: 'account_ids',
  submitKey: 'account_ids',
  searchFields: ['account', 'name'] as string[],
  items: [] as any[],
  selectedData: [] as any[],
  page: 1,
  pageSize: 10,
})

const modalTitle = computed(() =>
  state.subject?.name
    ? `${state.title || t('resource.iam.role.grant_user')} - ${state.subject.name}`
    : state.title || t('resource.iam.role.grant_user'),
)
const filteredUsers = computed(() => {
  const keyword = state.searchKey.trim().toLowerCase()
  return state.items.filter((item) => {
    const matchKeyword =
      !keyword ||
      state.searchFields
        .map((field) => item[field])
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    return matchKeyword
  })
})
const tableUsers = computed(() => {
  const start = (state.page - 1) * state.pageSize
  return filteredUsers.value.slice(start, start + state.pageSize)
})
const selectedIds = computed(() => new Set(state.selectedData.map((item) => String(item.id))))
const secondaryTitle = computed(() =>
  state.searchFields.includes('code')
    ? t('resource.iam.role.code')
    : t('resource.iam.account.account'),
)

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
    title: t('resource.iam.account.avatar'),
    key: 'avatar',
    width: 70,
    render: (row) => {
      const avatar = resolveFileUrl(row.avatar)
      if (avatar) {
        return <NAvatar size="small" src={avatar} imgProps={avatarImgProps} />
      }
      return row.name ? <NAvatar size="small">{row.name?.slice(0, 1)}</NAvatar> : null
    },
  },
  {
    title: t('resource.iam.account.name'),
    key: 'name',
    minWidth: 120,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: secondaryTitle.value,
    key: 'account',
    minWidth: 120,
    ellipsis: {
      tooltip: true,
    },
    render: (row) => row.account ?? row.code ?? row.status ?? '',
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
    title: t('resource.iam.account.name'),
    key: 'name',
    minWidth: 120,
    ellipsis: {
      tooltip: true,
    },
  },
])

async function openModal(subject: any, grantApi: any = roleApi, title = '', config: any = {}) {
  state.subject = subject ?? {}
  state.grantApi = grantApi
  state.title = title
  state.ownMethod = config.ownMethod ?? 'ownUsers'
  state.grantMethod = config.grantMethod ?? 'grantUsers'
  state.listKey = config.listKey ?? 'users'
  state.selectedKey = config.selectedKey ?? 'account_ids'
  state.submitKey = config.submitKey ?? state.selectedKey
  state.searchFields = config.searchFields ?? ['account', 'name']
  state.searchKey = ''
  state.items = []
  state.selectedData = []
  state.page = 1
  state.pageSize = 10
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.subject?.id) {
    return
  }
  state.loading = true
  try {
    const response = await state.grantApi[state.ownMethod](state.subject.id)
    state.items = response.data?.[state.listKey] ?? []
    const selectedIds = new Set((response.data?.[state.selectedKey] ?? []).map(String))
    state.selectedData = state.items.filter((item) => selectedIds.has(String(item.id)))
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    await state.grantApi[state.grantMethod]({
      id: state.subject.id,
      [state.submitKey]: state.selectedData.map((item) => item.id),
    })
    window.$message.success(t('resource.iam.role.grant_success'))
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function closeModal() {
  state.items = []
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
        <NGi :span="16">
          <NSpace vertical>
            <NInputGroup>
              <NInput
                v-model:value="state.searchKey"
                clearable
                :placeholder="t('resource.iam.role.placeholder.user_search')"
                @keyup.enter="state.page = 1"
                @clear="resetSearch"
              />
              <NButton type="primary" @click="state.page = 1">
                {{ t('resource.iam.role.search') }}
              </NButton>
              <NButton @click="resetSearch">
                {{ t('common.reset') }}
              </NButton>
            </NInputGroup>
            <NFlex justify="space-between" align="center">
              <NText>
                {{ t('resource.iam.role.pending_user_count', { count: filteredUsers.length }) }}
              </NText>
              <NButton dashed size="small" @click="addAllPageRecord">
                {{ t('resource.iam.role.add_current_page') }}
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
        <NGi :span="8">
          <NSpace vertical>
            <NFlex justify="space-between" align="center">
              <NText>{{
                t('resource.iam.role.selected_user_count', { count: state.selectedData.length })
              }}</NText>
              <NButton dashed type="error" size="small" @click="delAllRecord">
                {{ t('resource.iam.role.remove_all') }}
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
