<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

const props = defineProps({
  problem: {
    type: String,
    required: true,
  },
  set: {
    type: String,
    required: true,
  },
})

const { dataSubmitSetPage } = useDataSubmitFetch()

const columns: DataTableColumns<any> = [
  // {
  //   title: '题目',
  //   key: 'problemIdName',
  // },
  {
    title: '编程语言',
    key: 'languageName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.submitTypeName })
    },
  },
  {
    title: '长度',
    key: 'codeLength',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.codeLength })
    },
  },
  {
    title: '耗时',
    key: 'maxTime',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxTime })
    },
  },
  {
    title: '内存',
    key: 'maxMemory',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxMemory })
    },
  },
  {
    title: '执行状态',
    key: 'statusName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.statusName })
    },
  },
  {
    title: '相似度',
    key: 'similarity',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.similarity * 100 })
    },
  },
  // {
  //   title: '检测任务',
  //   key: 'taskId',
  // },
]

const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  problemId: props.problem,
  setId: props.set,
})
async function loadData() {
  const { data } = await dataSubmitSetPage(pageParam.value)
  pageData.value = data
  console.log(data)
}
loadData()
const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: '创建时间', value: 'createTime' },
    { label: '更新时间', value: 'updateTime' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: '升序', value: 'ASCEND' },
  { label: '降序', value: 'DESCEND' },
])
function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      console.log(row)
    },
  }
}
</script>

<template>
  <n-space vertical>
    <n-flex
      align="center"
      justify="space-between"
    >
      <div>
        状态列表
      </div>
      <n-flex
        align="center"
        :size="12"
      >
        <NText depth="3">
          共 {{ pageData?.total }} 次提交
        </NText>
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
      </n-flex>
    </n-flex>
    <!-- 题目表格 -->
    <n-data-table
      :columns="columns"
      :data="pageData?.records"
      :bordered="false"
      :row-key="(row: any) => row.id"
      :row-props="rowProps"
      scroll-x="700"
      class="flex-1 h-full"
    />
    <n-flex justify="center">
      <n-pagination
        v-model:page="pageParam.current"
        v-model:page-size="pageParam.size"
        show-size-picker
        :page-count="pageData ? Number(pageData.pages) : 0"
        :page-sizes="Array.from({ length: 10 }, (_, i) => ({
          label: `${(i + 1) * 10} 每页`,
          value: (i + 1) * 10,
        }))"
        :page-slot="3"
        @update:page="loadData"
        @update:page-size="loadData"
      />
    </n-flex>
  </n-space>
</template>

<style scoped>

</style>
