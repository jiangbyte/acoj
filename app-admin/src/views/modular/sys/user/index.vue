<script lang="ts" setup>
import { actionDeleteBtn, actionIconBtn } from '@/utils'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NImage, NPagination, NPopconfirm, NSpace } from 'naive-ui'
import { useSysGroupFetch, useSysUserFetch } from '@/composables/v1'
import Form from './form.vue'
import Detail from './detail.vue'
import Assign from './assign.vue'

const formRef = ref()
const detailRef = ref()
const assignRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'User Group',
    key: 'groupIdName',
    ellipsis: {
      tooltip: true,
    },
  },
  // {
  //   title: 'username',
  //   key: 'username',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Nickname',
    key: 'nickname',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Avatar',
    key: 'avatar',
    render: (row) => {
      return h(NImage, { src: row.avatar, width: 50, height: 50, objectFit: 'cover' })
    },
  },
  // {
  //   title: 'Background Image',
  //   key: 'background',
  //   render: (row) => {
  //     return h(NImage, { src: row.background, width: 50, height: 50, objectFit: 'cover' })
  //   },
  // },
  {
    title: 'Bio',
    key: 'quote',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Gender',
    key: 'gender',
    render: (value) => {
      if (value.gender === 1) {
        return 'Male'
      }
      else if (value.gender === 2) {
        return 'Female'
      }
      return 'Unknown'
    },
  },
  // {
  //   title: 'Student ID',
  //   key: 'studentNumber',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Email',
    key: 'email',
    ellipsis: {
      tooltip: true,
    },
  },
  // {
  //   title: 'Phone',
  //   key: 'telephone',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Actions',
    key: 'action',
    width: 184,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        actionIconBtn({ kind: 'edit', onClick: () => formRef.value.doOpen(row, true) }),
        actionIconBtn({ kind: 'role', onClick: () => assignRef.value.doOpen(row) }),
        actionIconBtn({ kind: 'detail', onClick: () => detailRef.value.doOpen(row) }),
        actionDeleteBtn(() => deleteHandle(row)),
      ])
    },
  },
]

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: 'username', value: 'username' },
    { label: 'Student ID', value: 'studentNumber' },
    { label: 'Created At', value: 'createTime' },
    { label: 'Updated At', value: 'updateTime' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: 'Ascending', value: 'ASCEND' },
  { label: 'Descending', value: 'DESCEND' },
])
const displayKey = ref<string[]>(
  columns
    .filter(col => 'key' in col && col.key !== 'selection' && col.key !== 'action')
    .map((col: any) => col.key as string),
)
const columnDisplayOptions = computed(() => {
  const filteredColumns = columns
    .filter(col => 'key' in col)
    .filter(col => col.key !== 'action' && col.key !== 'selection')

  return filteredColumns.map((col, index) => ({
    label: col.title as string,
    value: col.key as string,
    disabled: index === 0,
  }))
})

const filteredColumns = computed(() => {
  return columns.filter((col: any) => {
    if (col.type === 'selection' || col.key === 'action')
      return true
    return 'key' in col ? displayKey.value.includes(col.key as string) : false
  })
})

const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: 'id',
  sortOrder: 'ASCEND',
  keyword: '',
  groupId: '',
  type: 'username',
})
const groupKeyword = ref('')
function resetHandle() {
  pageParam.value.keyword = ''
  loadUserData()
}

const treeData = ref()
const { sysUserPage, sysUserDelete } = useSysUserFetch()
const loading = ref(false)

async function loadUserData() {
  loading.value = true
  const { data } = await sysUserPage(pageParam.value)
  if (data) {
    pageData.value = data
    loading.value = false
    console.log(data)
  }
}
async function loadGroupData() {
  console.log(groupKeyword.value)
  useSysGroupFetch().sysGroupAuthTree({ keyword: groupKeyword.value }).then(({ data }) => {
    treeData.value = data
  })
}
function resetGroupDataHandle() {
  groupKeyword.value = ''
  loadGroupData()
}
async function loadData() {
  loadUserData()
  loadGroupData()
}

loadData()
function handleTreeSelect(keys: string[]) {
  pageParam.value.groupId = keys[0]
  loadUserData()
}
async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await sysUserDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await sysUserDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
    checkedRowKeys.value = []
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NSplit direction="horizontal" :default-size="0.2" :max="0.8" :min="0.2">
      <template #1>
        <div class="flex flex-col h-full w-full">
          <NCard size="small">
            <NSpace vertical>
              <NInputGroup>
                <NInput
                  v-model:value="groupKeyword"
                  placeholder="Enter keywords"
                  clearable
                  @clear="resetGroupDataHandle"
                />
                <NButton type="primary" @click="loadGroupData">
                  <template #icon>
                    <IconParkOutlineSearch />
                  </template>
                  Search
                </NButton>
                <!-- <NButton type="warning" @click="resetGroupDataHandle">
                  <template #icon>
                    <IconParkOutlineRefresh />
                  </template>
                  Reset
                </NButton> -->
              </NInputGroup>
              <NScrollbar class="h-[calc(100vh-2.5rem-9.3rem)]">
                <NTree
                  show-line
                  block-line
                  key-field="id"
                  label-field="name"
                  :data="treeData"
                  :indent="12"
                  @update:selected-keys="handleTreeSelect"
                />
                <!-- <NTree
                  show-line
                  block-line
                  key-field="value"
                  label-field="label"
                  :data="treeData"
                  :indent="12"
                  :selected-keys="typeKey"
                  @update:selected-keys="handleTreeSelect"
                /> -->
              </NScrollbar>
            </NSpace>
            <!-- <template #header>
              <NButton type="primary" @click="dictformRef.doOpen(null, false)">
                <template #icon>
                  <IconParkOutlinePlus />
                </template>
                Add
              </NButton>
            </template> -->
          </NCard>
        </div>
      </template>
      <template #2>
        <div class="flex flex-col h-full w-full">
          <NCard size="small">
            <NSpace vertical>
              <NSpace align="center">
                <NSpace align="center">
                  <NFormItem :show-feedback="false" label="Search Type" label-placement="left">
                    <!-- <NInput v-model:value="pageParam.type" placeholder="Select " clearable @clear="resetHandle" /> -->
                    <n-select
                      v-model:value="pageParam.type" class="w-30" placeholder="Select " :options="[
                        {
                          label: 'Nickname',
                          value: 'nickname',
                        },
                        {
                          label: 'username',
                          value: 'username',
                        },
                        {
                          label: 'Email',
                          value: 'email',
                        },
                        {
                          label: 'Phone Number',
                          value: 'telephone',
                        },
                        {
                          label: 'Student ID',
                          value: 'studentNumber',
                        },
                      ]"
                    />
                  </NFormItem>
                  <NFormItem :show-feedback="false" label="Keywords" label-placement="left">
                    <NInput v-model:value="pageParam.keyword" placeholder="Enter keywords" clearable @clear="resetHandle" />
                  </NFormItem>
                  <NSpace align="center">
                    <NButton type="primary" @click="loadData">
                      <template #icon>
                        <IconParkOutlineSearch />
                      </template>
                      Search
                    </NButton>
                    <NButton type="warning" @click="resetHandle">
                      <template #icon>
                        <IconParkOutlineRefresh />
                      </template>
                      Reset
                    </NButton>
                  </NSpace>
                </NSpace>
              </NSpace>
              <NSpace align="center" justify="space-between">
                <NSpace align="center">
                  <NButton type="primary" @click="formRef.doOpen(null, false)">
                    <template #icon>
                      <IconParkOutlinePlus />
                    </template>
                    Create
                  </NButton>
                  <NPopconfirm v-if="checkedRowKeys.length > 0" @positive-click="deleteBatchHandle">
                    <template #default>
                      Confirm delete
                    </template>
                    <template #trigger>
                      <NButton type="error">
                        Delete
                      </NButton>
                    </template>
                  </NPopconfirm>
                </NSpace>
                <NSpace align="center">
                  <NFormItem :show-feedback="false" label-placement="left">
                    <NButton text @click="loadData">
                      <template #icon>
                        <IconParkOutlineRefresh />
                      </template>
                    </NButton>
                  </NFormItem>
                  <NFormItem :show-feedback="false" label-placement="left">
                    <NPopselect
                      v-model:value="displayKey"
                      :options="columnDisplayOptions"
                      multiple
                      @update:value="loadData"
                    >
                      <NButton text>
                        <template #icon>
                          <IconParkOutlineEyes />
                        </template>
                      </NButton>
                    </NPopselect>
                  </NFormItem>
                  <NFormItem :show-feedback="false" label-placement="left">
                    <NPopselect
                      v-model:value="pageParam.sortField"
                      :options="columnSortFieldOptions"
                      @update:value="loadData"
                    >
                      <NButton text>
                        <template #icon>
                          <IconParkOutlineSortOne />
                        </template>
                      </NButton>
                    </NPopselect>
                  </NFormItem>
                  <NFormItem :show-feedback="false" label-placement="left">
                    <NPopselect
                      v-model:value="pageParam.sortOrder"
                      :options="sortOrderOptions"
                      @update:value="loadData"
                    >
                      <NButton text>
                        <template #icon>
                          <IconParkOutlineSort />
                        </template>
                      </NButton>
                    </NPopselect>
                  </NFormItem>
                </NSpace>
              </NSpace>
            </NSpace>
          </NCard>
          <NCard size="small" class="flex-1">
            <NDataTable
              v-model:checked-row-keys="checkedRowKeys"
              :columns="filteredColumns"
              :data="pageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.id"
              :loading="loading"
              flex-height
              :scroll-x="1400"
              class="flex-1 h-full"
            />
            <template #action>
              <NSpace align="center" justify="space-between" class="w-full">
                <NSpace align="center">
                  <NP type="info" show-icon>
                    Current: {{ pageData?.records.length }} records
                  </NP>
                  <NP type="info" show-icon>
                    Selected: {{ checkedRowKeys.length }} rows
                  </NP>
                </NSpace>
                <NPagination
                  v-model:page="pageParam.current"
                  v-model:page-size="pageParam.size"
                  class="flex justify-end"
                  :page-count="pageData ? Number(pageData.pages) : 0"
                  show-size-picker
                  :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                    label: `${(i + 1) * 10} per page`,
                    value: (i + 1) * 10,
                  }))"
                  :page-slot="5"
                  @update:page="loadData"
                  @update:page-size="loadData"
                />
              </NSpace>
            </template>
          </NCard>
        </div>
      </template>
    </NSplit>
    <Form ref="formRef" @submit="loadData" />
    <Detail ref="detailRef" @submit="loadData" />
    <Assign ref="assignRef" @submit="loadData" />
  </div>
</template>

<style scoped>

</style>
