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
  //   title: 'Problem',
  //   key: 'problemIdName',
  // },
  {
    title: 'Programming Language',
    key: 'languageName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: 'Run Type',
    key: 'submitTypeName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.submitTypeName })
    },
  },
  {
    title: 'Size',
    key: 'codeLength',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.codeLength })
    },
  },
  {
    title: 'Time',
    key: 'maxTime',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxTime })
    },
  },
  {
    title: 'Memory',
    key: 'maxMemory',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxMemory })
    },
  },
  {
    title: 'Run Status',
    key: 'statusName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.statusName })
    },
  },
  {
    title: 'Similarity',
    key: 'similarity',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.similarity * 100 })
    },
  },
  // {
  //   title: 'Detection Task',
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
    { label: 'Created', value: 'createTime' },
    { label: 'Updated', value: 'updateTime' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: 'Ascending', value: 'ASCEND' },
  { label: 'Descending', value: 'DESCEND' },
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
        Status List
      </div>
      <n-flex
        align="center"
        :size="12"
      >
        <NText depth="3">
          {{ pageData?.total }} submissions
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
    <!-- Section -->
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
          label: `${(i + 1) * 10} per page`,
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
