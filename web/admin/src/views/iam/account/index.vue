<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { accountApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NDropdown, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'
import ModalGrantDept from './components/ModalGrantDept.vue'
import ModalGrantPermission from '../role/components/ModalGrantPermission.vue'
import ModalGrantResource from '../role/components/ModalGrantResource.vue'
import ModalGrantUser from '../role/components/ModalGrantUser.vue'

const { t } = useI18n()
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const grantRoleModalRef = ref<any>(null)
const grantGroupModalRef = ref<any>(null)
const grantDeptModalRef = ref<any>(null)
const grantResourceModalRef = ref<any>(null)
const grantPermissionModalRef = ref<any>(null)
const state = reactive({
  accounts: [] as any[],
  total: 0,
  loading: false,
  searchValues: {} as any,
  checkedRowKeys: [] as string[],
  page: 1,
  pageSize: 20,
})

const searchForm = createProSearchForm<any>({
  defaultCollapsed: true,
  onSubmit(values) {
    state.searchValues = normalizeSearchValues(values, {
      account: (value) => String(value).trim(),
      name: (value) => String(value).trim(),
      phone: (value) => String(value).trim(),
      email: (value) => String(value).trim(),
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

const searchColumns = computed<ProSearchFormColumns<any>>(() => [
  {
    title: t('resource.iam.account.account'),
    path: 'account',
    field: 'input',
  },
  {
    title: t('resource.iam.account.name'),
    path: 'name',
    field: 'input',
  },
  {
    title: t('resource.iam.account.phone'),
    path: 'phone',
    field: 'input',
  },
  {
    title: t('resource.iam.account.email'),
    path: 'email',
    field: 'input',
  },
  {
    title: t('resource.iam.account.account_type'),
    path: 'account_type',
    field: 'select',
    fieldProps: {
      options: dictList('ACCOUNT_TYPE'),
    },
  },
  {
    title: t('resource.iam.account.account_status'),
    path: 'account_status',
    field: 'select',
    fieldProps: {
      options: dictList('ACCOUNT_STATUS'),
    },
  },
])

const pagination = computed<PaginationProps>(() => ({
  page: state.page,
  pageSize: state.pageSize,
  itemCount: state.total,
  showSizePicker: true,
  pageSizes: [10, 20, 30, 50],
  prefix: ({ itemCount }) => t('common.often.total', { count: itemCount }),
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
  {
    type: 'selection',
    fixed: 'left',
  },
  {
    title: t('common.often.index'),
    width: 80,
    path: 'id',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.account.account'),
    path: 'account',
    width: 140,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.account.name'),
    path: 'name',
    width: 130,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.account.nickname'),
    path: 'nickname',
    width: 130,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.account.account_type'),
    path: 'account_type',
    width: 120,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('ACCOUNT_TYPE', row.account_type))}
        bordered={false}
      >
        {dictTypeData('ACCOUNT_TYPE', row.account_type)}
      </NTag>
    ),
  },
  {
    title: t('resource.iam.account.account_status'),
    path: 'account_status',
    width: 120,
    render: (row) => (
      <NTag
        color={createTagColor(dictTypeColor('ACCOUNT_STATUS', row.account_status))}
        bordered={false}
      >
        {dictTypeData('ACCOUNT_STATUS', row.account_status)}
      </NTag>
    ),
  },
  {
    title: t('resource.iam.account.phone'),
    path: 'phone',
    width: 150,
  },
  {
    title: t('resource.iam.account.email'),
    path: 'email',
    width: 220,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.account.latest_login_time'),
    path: 'latest_login_time',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.updated_at'),
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => {
      const options = grantOptions.value
      return (
        <NFlex size={12}>
          <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
            {renderButtonIcon('icon-park-outline:preview-open')}
          </NButton>
          <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
          {options.length ? (
            <NDropdown
              trigger="click"
              options={options}
              onSelect={(key) => openGrantModal(String(key), row)}
            >
              <NButton type="warning" size="small" text={true}>
                {renderButtonIcon('icon-park-outline:permissions')}
              </NButton>
            </NDropdown>
          ) : null}
          <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        </NFlex>
      )
    },
  },
])

const grantOptions = computed(() =>
  [
    {
      label: t('resource.iam.account.grant_role'),
      key: 'role',
      permission: 'iam:account:grantrole',
    },
    {
      label: t('resource.iam.account.grant_group'),
      key: 'group',
      permission: 'iam:account:grantgroup',
    },
    {
      label: t('resource.iam.account.grant_dept'),
      key: 'dept',
      permission: 'iam:account:grantdept',
    },
    {
      label: t('resource.iam.account.grant_resource'),
      key: 'resource',
      permission: 'iam:account:grantresource',
    },
    {
      label: t('resource.iam.account.grant_permission'),
      key: 'permission',
      permission: 'iam:account:grantpermission',
    },
  ].filter((item) => hasPermission(item.permission)),
)
const hasCheckedRows = computed(() => state.checkedRowKeys.length > 0)

onMounted(() => {
  fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await accountApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.accounts = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.accounts.some((item) => item.id === key),
    )
  } finally {
    state.loading = false
  }
}

function openDetailModal(id: string) {
  detailModalRef.value?.openModal(id)
}

function openCreateModal() {
  formModalRef.value?.openModal()
}

function openEditModal(id: string) {
  formModalRef.value?.openModal(id)
}

function openGrantModal(type: string, row: any) {
  const account = {
    id: row.id,
    code: row.account,
    name: row.name || row.account,
  }
  if (type === 'role') {
    grantRoleModalRef.value?.openModal(account, accountApi, t('resource.iam.account.grant_role'), {
      ownMethod: 'ownRoles',
      grantMethod: 'grantRoles',
      listKey: 'roles',
      selectedKey: 'role_ids',
      submitKey: 'role_ids',
      searchFields: ['code', 'name'],
    })
  } else if (type === 'group') {
    grantGroupModalRef.value?.openModal(
      account,
      accountApi,
      t('resource.iam.account.grant_group'),
      {
        ownMethod: 'ownGroups',
        grantMethod: 'grantGroups',
        listKey: 'groups',
        selectedKey: 'group_ids',
        submitKey: 'group_ids',
        searchFields: ['name'],
      },
    )
  } else if (type === 'dept') {
    grantDeptModalRef.value?.openModal(account)
  } else if (type === 'resource') {
    grantResourceModalRef.value?.openModal(
      account,
      accountApi,
      t('resource.iam.account.grant_resource'),
    )
  } else if (type === 'permission') {
    grantPermissionModalRef.value?.openModal(
      account,
      accountApi,
      t('resource.iam.account.grant_permission'),
    )
  }
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  state.checkedRowKeys = keys.map(String)
}

function confirmDelete(value: string | string[]) {
  const ids = Array.isArray(value) ? value : [value]
  if (!ids.length) {
    return
  }
  const isBatch = ids.length > 1

  window.$dialog.warning({
    title: isBatch ? t('common.often.batch_delete') : t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? t('resource.iam.account.batch_delete_confirm', { count: ids.length })
      : t('resource.iam.account.delete_confirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await accountApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success(t('common.often.delete_success'))
  await fetchPage()
  if (!state.accounts.length && state.total > 0 && state.page > 1) {
    state.page -= 1
    await fetchPage()
  }
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm
        :form="searchForm"
        :columns="searchColumns"
        :reset-button-props="{ content: t('common.search_form.reset') }"
        :search-button-props="{ content: t('common.search_form.search') }"
        :collapse-button-props="{
          content: searchForm.collapsed.value
            ? t('common.search_form.expand')
            : t('common.search_form.collapse'),
        }"
      />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      remote
      :title="t('resource.iam.account.title')"
      row-key="id"
      :scroll-x="1960"
      :columns="tableColumns"
      :data="state.accounts"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton type="primary" text :title="t('common.often.add')" :aria-label="t('common.often.add')" @click="openCreateModal">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton text :title="t('common.reload')" :aria-label="t('common.reload')" :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            type="error"
            text
            :title="t('common.often.batch_delete')"
            :aria-label="t('common.often.batch_delete')"
            :disabled="!hasCheckedRows"
            @click="confirmDelete(state.checkedRowKeys)"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:delete" />
              </NIcon>
            </template>
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ModalForm ref="formModalRef" @saved="fetchPage" />
    <ModalDetail ref="detailModalRef" />
    <ModalGrantUser ref="grantRoleModalRef" @saved="fetchPage" />
    <ModalGrantUser ref="grantGroupModalRef" @saved="fetchPage" />
    <ModalGrantDept ref="grantDeptModalRef" @saved="fetchPage" />
    <ModalGrantResource ref="grantResourceModalRef" @saved="fetchPage" />
    <ModalGrantPermission ref="grantPermissionModalRef" @saved="fetchPage" />
  </NFlex>
</template>

<style scoped></style>
