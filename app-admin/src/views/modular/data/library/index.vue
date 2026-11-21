<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NButton, NCard, NDataTable, NEllipsis, NPagination, NPopconfirm, NSpace, NTime } from 'naive-ui'
import { useDataLibraryFetch } from '@/composables/v1'
import { FormatModuleType, actionIconBtn} from '@/utils'
import Form from './form.vue'
import Detail from './detail.vue'

const formRef = ref()
const detailRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'User',
    key: 'user',
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small', wrap: false },
        {
          default: () => [
            h(
              NAvatar,
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
              {},
            ),
            h(
              NEllipsis,
              {
                style: {
                  maxWidth: '90px',
                },
              },
              { default: () => row.userIdName },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Submission Module',
    key: 'moduleType',
    render: (row) => FormatModuleType(row.moduleType),
  },
  {
    title: 'Problem',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Submission ID',
    key: 'submitId',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Submitted At',
    key: 'submitTime',
    width: 180,
    render: (row) => {
      return h(NTime, { time: row.submitTime })
    },
  },
  {
    title: 'Language',
    key: 'languageName',
  },
  // {
  //   title: 'Source Code',
  //   key: 'code',
  // },
  {
    title: 'Source Length',
    key: 'codeLength',
  },
  // {
  //   title: 'Check Activity',
  //   key: 'accessCount',
  // },
  {
    title: 'Actions',
    key: 'action',
    width: 72,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   onClick: () => formRef.value.doOpen(row, true),
        // }, () => 'Edit'),
        actionIconBtn({ kind: 'detail', onClick: () => detailRef.value.doOpen(row) }),
        // h(NPopconfirm, {
        //   onPositiveClick: () => deleteHandle(row),
        // }, {
        //   default: () => 'Confirm delete',
        //   trigger: () => h(NButton, { size: 'small', type: 'error' }, () => 'Delete'),
        // }),
      ])
    },
  },
]

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
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
  sortOrder: 'DESCEND',
  keyword: '',
  userId: '',
  problemId: '',
  submitId: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  pageParam.value.userId = ''
  pageParam.value.problemId = ''
  pageParam.value.submitId = ''
  loadData()
}

const { dataLibraryPage, dataLibraryDelete } = useDataLibraryFetch()
const loading = ref(false)
async function loadData() {
  loading.value = true
  const { data } = await dataLibraryPage(pageParam.value)
  if (data) {
    pageData.value = data
    loading.value = false
  }
}

loadData()

async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await dataLibraryDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await dataLibraryDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
    checkedRowKeys.value = []
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NCard size="small">
      <NSpace vertical>
        <NSpace align="center">
          <NSpace align="center">
            <NFormItem :show-feedback="false" label="Keywords" label-placement="left">
              <NInput v-model:value="pageParam.keyword" placeholder="Enter keywords" clearable @clear="resetHandle" />
            </NFormItem>
            <NFormItem :show-feedback="false" label="User" label-placement="left">
              <NInput v-model:value="pageParam.userId" placeholder="User ID" clearable />
            </NFormItem>
            <NFormItem :show-feedback="false" label="Problem" label-placement="left">
              <NInput v-model:value="pageParam.problemId" placeholder="Problem ID" clearable />
            </NFormItem>
            <NFormItem :show-feedback="false" label="Submit" label-placement="left">
              <NInput v-model:value="pageParam.submitId" placeholder="Submit ID" clearable />
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

    <Form ref="formRef" @submit="loadData" />
    <Detail ref="detailRef" @submit="loadData" />
  </div>
</template>

<style scoped>

</style>
