<script setup lang="tsx">
import type { PaginationProps } from 'naive-ui'
import type { ProDataTableColumns, ProSearchFormColumns } from 'pro-naive-ui'
import type { User, UserFormModel, UserSearchParams } from './types'
import { Icon } from '@iconify/vue'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import {
  createProModalForm,
  createProSearchForm,
  ProCard,
  ProDataTable,
  ProModal,
  ProModalForm,
  ProSearchForm,
} from 'pro-naive-ui'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  genderLabelKeyMap,
  genderOptions,
  genderTagTypeMap,
  statusLabelKeyMap,
  statusOptions,
  statusTagTypeMap,
} from './constants'
import { mockUsers } from './mock'
import ModalDetail from './components/ModalDetail.vue'
import ModalForm from './components/ModalForm.vue'

const { t } = useI18n()
const users = ref<User[]>(mockUsers.map((item) => ({ ...item, roleIds: [...item.roleIds] })))
const searchValues = ref<UserSearchParams>({})
const detailUserId = ref<string | null>(null)
const showDetailModal = ref(false)
const checkedRowKeys = ref<string[]>([])
const page = ref(1)
const pageSize = ref(10)

const searchForm = createProSearchForm<UserSearchParams>({
  onSubmit(values) {
    searchValues.value = normalizeSearchValues(values)
    page.value = 1
  },
  onReset() {
    searchValues.value = {}
    page.value = 1
  },
})

const modalForm = createProModalForm<UserFormModel>({
  onSubmit(values) {
    saveUser(values)
  },
})

const searchColumns = computed<ProSearchFormColumns<UserSearchParams>>(() => [
  {
    title: t('pages.system.user.username'),
    path: 'username',
  },
  {
    title: t('pages.system.user.nickname'),
    path: 'nickname',
  },
  {
    title: t('pages.system.user.gender'),
    path: 'gender',
    field: 'select',
    fieldProps: {
      options: translateOptions(genderOptions),
    },
  },
  {
    title: t('common.often.status'),
    path: 'status',
    field: 'select',
    fieldProps: {
      options: translateOptions(statusOptions),
    },
  },
])

const filteredUsers = computed(() => {
  const values = searchValues.value
  const username = values.username?.trim().toLowerCase()
  const nickname = values.nickname?.trim().toLowerCase()

  return users.value.filter((item) => {
    const matchUsername = username ? item.username.toLowerCase().includes(username) : true
    const matchNickname = nickname ? item.nickname.toLowerCase().includes(nickname) : true
    const matchGender = values.gender ? item.gender === values.gender : true
    const matchStatus = values.status ? item.status === values.status : true

    return matchUsername && matchNickname && matchGender && matchStatus
  })
})

const tableData = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const pagination = computed<PaginationProps>(() => ({
  page: page.value,
  pageSize: pageSize.value,
  itemCount: filteredUsers.value.length,
  showSizePicker: true,
  pageSizes: [10, 20, 30],
  prefix: ({ itemCount }) => t('common.often.total', { count: itemCount }),
  onUpdatePage: (value) => {
    page.value = value
  },
  onUpdatePageSize: (value) => {
    pageSize.value = value
    page.value = 1
  },
}))

const tableColumns = computed<ProDataTableColumns<User>>(() => [
  {
    type: 'selection',
    fixed: 'left',
  },
  {
    title: t('common.often.index'),
    type: 'index',
    width: 80,
    render: (index) => (page.value - 1) * pageSize.value + index + 1,
  },
  {
    title: t('pages.system.user.username'),
    path: 'username',
    width: 120,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.system.user.nicknameShort'),
    path: 'nickname',
    width: 120,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.system.user.gender'),
    path: 'gender',
    width: 100,
    render: (row) => (
      <NTag type={genderTagTypeMap[row.gender]} bordered={false}>
        {t(genderLabelKeyMap[row.gender])}
      </NTag>
    ),
  },
  {
    title: t('pages.system.user.email'),
    path: 'email',
    width: 220,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('pages.system.user.phone'),
    path: 'phone',
    width: 140,
  },
  {
    title: t('common.often.status'),
    path: 'status',
    width: 100,
    render: (row) => (
      <NTag type={statusTagTypeMap[row.status]} bordered={false}>
        {t(statusLabelKeyMap[row.status])}
      </NTag>
    ),
  },
  {
    title: t('common.often.remark'),
    path: 'remark',
    width: 230,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.updateTime'),
    path: 'updateTime',
    width: 180,
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 170,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        <NButton type="info" size="small" text={true} onClick={() => openDetailModal(row)}>
          {t('common.often.detail')}
        </NButton>
        <NButton type="primary" size="small" text={true} onClick={() => openEditModal(row)}>
          {t('common.often.edit')}
        </NButton>
        <NButton type="error" size="small" text={true} onClick={() => confirmDeleteUser(row)}>
          {t('common.often.delete')}
        </NButton>
      </NFlex>
    ),
  },
])

const hasCheckedRows = computed(() => checkedRowKeys.value.length > 0)

function normalizeSearchValues(values: UserSearchParams) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== ''),
  ) as UserSearchParams
}

function translateOptions<T extends string>(options: Array<{ labelKey: string; value: T }>) {
  return options.map((item) => ({
    label: t(item.labelKey),
    value: item.value,
  }))
}

function createEmptyForm(): UserFormModel {
  return {
    username: '',
    nickname: '',
    password: '',
    roleIds: ['user'],
    gender: '1',
    status: '1',
    email: '',
    phone: '',
    remark: '',
  }
}

function openDetailModal(user: User) {
  detailUserId.value = user.id
  showDetailModal.value = true
}

function openCreateModal() {
  const values = createEmptyForm()
  modalForm.values.value = cloneFormValues(values)
  modalForm.setInitialValues(cloneFormValues(values))
  modalForm.show.value = true
}

function openEditModal(user: User) {
  const values: UserFormModel = {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    password: user.password,
    roleIds: [...user.roleIds],
    gender: user.gender,
    status: user.status,
    email: user.email,
    phone: user.phone,
    remark: user.remark,
  }

  modalForm.values.value = cloneFormValues(values)
  modalForm.setInitialValues(cloneFormValues(values))
  modalForm.show.value = true
}

function cloneFormValues(values: UserFormModel): UserFormModel {
  return {
    ...values,
    roleIds: [...values.roleIds],
  }
}

function saveUser(values: UserFormModel) {
  const now = formatDateTime(new Date())

  if (values.id) {
    users.value = users.value.map((item) =>
      item.id === values.id
        ? {
            ...item,
            ...values,
            id: item.id,
            roleIds: [...values.roleIds],
            updateTime: now,
          }
        : item,
    )
    window.$message.success(t('common.often.updateSuccess'))
  } else {
    users.value.unshift({
      ...values,
      id: String(Date.now()),
      roleIds: [...values.roleIds],
      createTime: now,
      updateTime: now,
    })
    page.value = 1
    window.$message.success(t('common.often.createSuccess'))
  }

  modalForm.show.value = false
}

function handleCheckedRowKeys(keys: Array<string | number>) {
  checkedRowKeys.value = keys.map(String)
}

function confirmDeleteCheckedUsers() {
  if (!hasCheckedRows.value) {
    return
  }

  window.$dialog.warning({
    title: t('common.often.batchDelete'),
    draggable: true,
    maskClosable: false,
    content: t('common.often.batchDeleteConfirm', { count: checkedRowKeys.value.length }),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: deleteCheckedUsers,
  })
}

function deleteCheckedUsers() {
  const deleteIds = new Set(checkedRowKeys.value)
  users.value = users.value.filter((item) => !deleteIds.has(item.id))

  if (detailUserId.value && deleteIds.has(detailUserId.value)) {
    showDetailModal.value = false
    detailUserId.value = null
  }

  checkedRowKeys.value = []
  syncCurrentPage()
  window.$message.success(t('common.often.deleteSuccess'))
}

function confirmDeleteUser(user: User) {
  window.$dialog.warning({
    title: t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: `${t('common.often.deleteConfirm')}${user.nickname}${t('common.often.deleteQuestion')}`,
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteUser(user.id),
  })
}

function deleteUser(id: string) {
  users.value = users.value.filter((item) => item.id !== id)
  checkedRowKeys.value = checkedRowKeys.value.filter((key) => key !== id)

  if (detailUserId.value === id) {
    showDetailModal.value = false
    detailUserId.value = null
  }

  syncCurrentPage()
  window.$message.success(t('common.often.deleteSuccess'))
}

function syncCurrentPage() {
  const maxPage = Math.max(1, Math.ceil(filteredUsers.value.length / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }
}

function formatDateTime(date: Date) {
  const parts = [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate()),
    pad(date.getHours()),
    pad(date.getMinutes()),
    pad(date.getSeconds()),
  ]

  return `${parts[0]}-${parts[1]}-${parts[2]} ${parts[3]}:${parts[4]}:${parts[5]}`
}

function pad(value: number) {
  return String(value).padStart(2, '0')
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical>
    <ProCard content-class="pb-0!">
      <ProSearchForm :form="searchForm" :columns="searchColumns" />
    </ProCard>

    <ProDataTable
      class="min-h-0 flex-1"
      :title="t('pages.system.user.title')"
      row-key="id"
      :scroll-x="1440"
      :columns="tableColumns"
      :data="tableData"
      :pagination="pagination"
      :checked-row-keys="checkedRowKeys"
      :on-update-checked-row-keys="handleCheckedRowKeys"
    >
      <template #toolbar>
        <NFlex>
          <NButton type="primary" ghost @click="openCreateModal">
            <template #icon>
              <NIcon>
                <Icon icon="ant-design:plus-outlined" />
              </NIcon>
            </template>
            {{ t('common.often.add') }}
          </NButton>
          <NButton
            type="error"
            ghost
            :disabled="!hasCheckedRows"
            @click="confirmDeleteCheckedUsers"
          >
            {{ t('common.often.batchDelete') }}
            {{ t('common.often.total', { count: checkedRowKeys.length }) }}
          </NButton>
        </NFlex>
      </template>
    </ProDataTable>

    <ProModalForm
      :form="modalForm"
      :title="
        modalForm.values.value.id ? t('pages.system.user.editUser') : t('pages.system.user.addUser')
      "
    >
      <NScrollbar class="max-h-[min(560px,calc(100vh-300px))] pr-16px">
        <ModalForm />
      </NScrollbar>
    </ProModalForm>

    <ProModal
      v-model:show="showDetailModal"
      preset="card"
      draggable
      :mask-closable="false"
      :title="t('pages.system.user.detailUser')"
      style="width: 520px"
    >
      <NScrollbar class="max-h-[min(560px,calc(100vh-300px))] pr-16px">
        <ModalDetail v-if="detailUserId" :id="detailUserId" />
      </NScrollbar>
    </ProModal>
  </NFlex>
</template>
