<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NEllipsis, NPagination, NPopconfirm, NSpace } from 'naive-ui'
import { useDataSubmitFetch } from '@/composables/v1'
import { FormatModuleType, actionIconBtn, actionDeleteBtn, renderSafeAvatar } from '@/utils'
import Form from './form.vue'
import Detail from './detail.vue'
import JudgeCasePage from './judgecase.vue'

const formRef = ref()
const detailRef = ref()
const judgecaseIndexRef = ref()
const router = useRouter()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'User',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small', wrap: false },
        {
          default: () => [
            renderSafeAvatar(
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
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
    title: 'Language',
    key: 'languageName',
  },
  // {
  //   title: 'Source Code',
  //   key: 'code',
  // },
  {
    title: 'Length',
    key: 'codeLength',
  },
  {
    title: 'Run Type',
    key: 'submitTypeName',
  },
  {
    title: 'Time(ms)',
    key: 'maxTime',
  },
  {
    title: 'Memory(KB)',
    key: 'maxMemory',
  },
  // {
  //   title: 'Run Result Message',
  //   key: 'message',
  // },
  // {
  //   title: 'Test Case Results',
  //   key: 'testCase',
  // },
  {
    title: 'RunStatus',
    key: 'statusName',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Workflow Complete',
    key: 'isFinishName',
  },
  // {
  //   title: 'Similarity',
  //   key: 'similarity',
  //   render(row: any) {
  //     return row.similarity * 100
  //   },
  // },
  // {
  //   title: 'Similarity Task ID',
  //   key: 'taskId',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  // {
  //   title: 'Report ID',
  //   key: 'reportId',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  // {
  //   title: 'Similarity Level',
  //   key: 'similarityCategoryName',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  // {
  //   title: 'Similarity Task ID',
  //   key: 'judgeTaskId',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Actions',
    key: 'action',
    width: 140,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   onClick: () => formRef.value.doOpen(row, true),
        // }, () => 'Edit'),
        actionIconBtn({ kind: 'detail', onClick: () => detailRef.value.doOpen(row) }),
        actionIconBtn({ kind: 'testcase', title: 'Test Cases', onClick: () => judgecaseIndexRef.value.doOpen(row) }),
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   // Enable only when submitType is true and similarity is greater than zero
        //   // disabled is false only when both conditions are met
        //   disabled: !(row.submitType && row.similarity > 0),
        //   onClick: () => {
        //     router.push({
        //       path: `/visualization/submit/report/${row.reportId}/task/${row.taskId}`,
        //     })
        //   },
        // }, () => 'Similarity Check'),
        actionDeleteBtn(() => deleteHandle(row)),
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
  sortOrder: 'ASCEND',
  keyword: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const { dataSubmitPage, dataSubmitDelete } = useDataSubmitFetch()
const loading = ref(false)
async function loadData() {
  loading.value = true
  const { data } = await dataSubmitPage(pageParam.value)
  if (data) {
    pageData.value = data
    loading.value = false
    console.log(data)
  }
}

loadData()

async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await dataSubmitDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await dataSubmitDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
    checkedRowKeys.value = []
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NCard size="small" :bordered="false">
      <NSpace vertical>
        <NSpace align="center">
          <NSpace align="center">
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
            <!-- <NButton type="primary" @click="formRef.doOpen(null, false)">
              <template #icon>
                <IconParkOutlinePlus />
              </template>
              Create
            </NButton> -->
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
    <JudgeCasePage ref="judgecaseIndexRef" />
  </div>
</template>

<style scoped>

</style>
