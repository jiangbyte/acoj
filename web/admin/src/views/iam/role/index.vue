<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { roleApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NDropdown, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeData, dictTypeColor } from '@/utils/dict'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'
import ModalGrantPermission from './components/ModalGrantPermission.vue'
import ModalGrantResource from './components/ModalGrantResource.vue'
import ModalGrantUser from './components/ModalGrantUser.vue'

const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const grantResourceModalRef = ref<any>(null)
const grantPermissionModalRef = ref<any>(null)
const grantUserModalRef = ref<any>(null)
const state = reactive({
  roles: [] as any[],
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
      code: (value) => String(value).trim(),
      name: (value) => String(value).trim(),
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
    title: 'Role Code',
    path: 'code',
    field: 'input',
  },
  {
    title: 'Role Name',
    path: 'name',
    field: 'input',
  },
  {
    title: 'Role Category',
    path: 'category',
    field: 'select',
    fieldProps: {
      options: dictList('SYS_BIZ_CATEGORY'),
    },
  },
  {
    title: 'Scope Type',
    path: 'scope_type',
    field: 'select',
    fieldProps: {
      options: dictList('ROLE_SCOPE_TYPE'),
    },
  },
  {
    title: 'Status',
    path: 'status',
    field: 'select',
    fieldProps: {
      options: dictList('COMMON_STATUS'),
    },
  },
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
  {
    type: 'selection',
    fixed: 'left',
  },
  {
    title: 'ID',
    width: 80,
    path: 'id',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Role Code',
    path: 'code',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Role Name',
    path: 'name',
    width: 160,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Role Category',
    path: 'category',
    width: 130,
    render: (row) => dictTypeData('SYS_BIZ_CATEGORY', row.category) || row.category,
  },
  {
    title: 'Scope Type',
    path: 'scope_type',
    width: 130,
    render: (row) => dictTypeData('ROLE_SCOPE_TYPE', row.scope_type) || row.scope_type,
  },
  {
    title: 'Owner Department ID',
    path: 'owner_dept_id',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Sort',
    path: 'sort',
    width: 90,
  },
  {
    title: 'Builtin Role',
    path: 'is_builtin',
    width: 110,
    render: (row) => (row.is_builtin ? 'Yes' : 'No'),
  },
  {
    title: 'Status',
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || row.status}
      </NTag>
    ),
  },
  {
    title: 'Updated At',
    path: 'updated_at',
    width: 190,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Operation',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => {
      const options = grantOptions.value
      return (
        <NFlex size={12}>
          {hasPermission('iam:role:detail') ? (
            <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
              {renderButtonIcon('icon-park-outline:preview-open')}
            </NButton>
          ) : null}
          {hasPermission('iam:role:update') ? (
            <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row.id)}>
              {renderButtonIcon('icon-park-outline:edit')}
            </NButton>
          ) : null}
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
          {hasPermission('iam:role:delete') ? (
            <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
              {renderButtonIcon('icon-park-outline:delete')}
            </NButton>
          ) : null}
        </NFlex>
      )
    },
  },
])

const grantOptions = computed(() =>
  [
    {
      label: 'Grant Resources',
      key: 'resource',
      permission: 'iam:role:grantresource',
    },
    {
      label: 'Grant Permissions',
      key: 'permission',
      permission: 'iam:role:grantpermission',
    },
    {
      label: 'Grant Users',
      key: 'user',
      permission: 'iam:role:grantuser',
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
    const response = await roleApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.roles = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.roles.some((item) => item.id === key),
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
  const role = {
    id: row.id,
    code: row.code,
    name: row.name,
  }
  if (type === 'resource') {
    grantResourceModalRef.value?.openModal(role)
  } else if (type === 'permission') {
    grantPermissionModalRef.value?.openModal(role)
  } else if (type === 'user') {
    grantUserModalRef.value?.openModal(role)
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
    title: isBatch ? 'Batch Delete' : 'Delete',
    draggable: true,
    maskClosable: false,
    content: isBatch
      ? `Delete ${ids.length} selected roles?`
      : 'Delete this role?',
    positiveText: 'Confirm',
    negativeText: 'Cancel',
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await roleApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success('Deleted successfully')
  await fetchPage()
  if (!state.roles.length && state.total > 0 && state.page > 1) {
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
      :title="'Role Management'"
      row-key="id"
      :scroll-x="1600"
      :columns="tableColumns"
      :data="state.roles"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('iam:role:create')" type="primary" text :title="'Add'" :aria-label="'Add'" @click="openCreateModal">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton text :title="'Reload'" :aria-label="'Reload'" :loading="state.loading" @click="fetchPage">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
          <NButton
            v-if="hasPermission('iam:role:delete')"
            type="error"
            text
            :title="'Batch Delete'"
            :aria-label="'Batch Delete'"
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
    <ModalGrantResource ref="grantResourceModalRef" @saved="fetchPage" />
    <ModalGrantPermission ref="grantPermissionModalRef" @saved="fetchPage" />
    <ModalGrantUser ref="grantUserModalRef" @saved="fetchPage" />
  </NFlex>
</template>

<style scoped></style>
