<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import { Icon } from '@iconify/vue/offline'
import { groupApi } from '@/api'
import { createTagColor, hasPermission, normalizeSearchValues, renderButtonIcon } from '@/utils'
import { NButton, NDropdown, NFlex, NIcon, NTag } from 'naive-ui'
import { createProSearchForm, ProCard, ProDataTable, ProSearchForm } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { dictList, dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'
import ModalGrantPermission from '../role/components/ModalGrantPermission.vue'
import ModalGrantResource from '../role/components/ModalGrantResource.vue'
import ModalGrantUser from '../role/components/ModalGrantUser.vue'

const { t } = useI18n()
const formModalRef = ref<any>(null)
const detailModalRef = ref<any>(null)
const grantUserModalRef = ref<any>(null)
const grantRoleModalRef = ref<any>(null)
const grantResourceModalRef = ref<any>(null)
const grantPermissionModalRef = ref<any>(null)
const state = reactive({
  groups: [] as any[],
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
    title: t('resource.iam.group.name'),
    path: 'name',
    field: 'input',
  },
  {
    title: t('common.often.status'),
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
    title: t('resource.iam.group.name'),
    path: 'name',
    width: 180,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.group.description'),
    path: 'description',
    width: 260,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || row.status}
      </NTag>
    ),
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
          {hasPermission('iam:group:detail') ? (
            <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row.id)}>
              {renderButtonIcon('icon-park-outline:preview-open')}
            </NButton>
          ) : null}
          {hasPermission('iam:group:update') ? (
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
          {hasPermission('iam:group:delete') ? (
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
      label: t('resource.iam.group.grant_user'),
      key: 'user',
      permission: 'iam:group:grantuser',
    },
    {
      label: t('resource.iam.group.grant_role'),
      key: 'role',
      permission: 'iam:group:grantrole',
    },
    {
      label: t('resource.iam.group.grant_resource'),
      key: 'resource',
      permission: 'iam:group:grantresource',
    },
    {
      label: t('resource.iam.group.grant_permission'),
      key: 'permission',
      permission: 'iam:group:grantpermission',
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
    const response = await groupApi.page({
      current: state.page,
      size: state.pageSize,
      ...state.searchValues,
    })
    const data = response.data ?? {}
    state.groups = data.records ?? []
    state.total = data.total ?? 0
    state.page = data.current ?? state.page
    state.pageSize = data.size ?? state.pageSize
    state.checkedRowKeys = state.checkedRowKeys.filter((key) =>
      state.groups.some((item) => item.id === key),
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
  const group = {
    id: row.id,
    code: row.name,
    name: row.name,
  }
  if (type === 'user') {
    grantUserModalRef.value?.openModal(group, groupApi, t('resource.iam.group.grant_user'))
  } else if (type === 'role') {
    grantRoleModalRef.value?.openModal(group, groupApi, t('resource.iam.group.grant_role'), {
      ownMethod: 'ownRoles',
      grantMethod: 'grantRoles',
      listKey: 'roles',
      selectedKey: 'role_ids',
      submitKey: 'role_ids',
      searchFields: ['code', 'name'],
    })
  } else if (type === 'resource') {
    grantResourceModalRef.value?.openModal(group, groupApi, t('resource.iam.group.grant_resource'))
  } else if (type === 'permission') {
    grantPermissionModalRef.value?.openModal(
      group,
      groupApi,
      t('resource.iam.group.grant_permission'),
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
      ? t('resource.iam.group.batch_delete_confirm', { count: ids.length })
      : t('resource.iam.group.delete_confirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteData(ids),
  })
}

async function deleteData(ids: string[]) {
  await groupApi.remove({ ids })
  state.checkedRowKeys = state.checkedRowKeys.filter((key) => !ids.includes(key))

  window.$message.success(t('common.often.delete_success'))
  await fetchPage()
  if (!state.groups.length && state.total > 0 && state.page > 1) {
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
      :title="t('resource.iam.group.title')"
      row-key="id"
      :scroll-x="1100"
      :columns="tableColumns"
      :data="state.groups"
      :loading="state.loading"
      :pagination="pagination"
      :checked-row-keys="state.checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton v-if="hasPermission('iam:group:create')" type="primary" text :title="t('common.often.add')" :aria-label="t('common.often.add')" @click="openCreateModal">
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
            v-if="hasPermission('iam:group:delete')"
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
    <ModalGrantUser ref="grantUserModalRef" @saved="fetchPage" />
    <ModalGrantUser ref="grantRoleModalRef" @saved="fetchPage" />
    <ModalGrantResource ref="grantResourceModalRef" @saved="fetchPage" />
    <ModalGrantPermission ref="grantPermissionModalRef" @saved="fetchPage" />
  </NFlex>
</template>

<style scoped></style>
