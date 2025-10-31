<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { NSpin, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

const props = defineProps({
  problemId: {
    type: String,
    required: true,
  },
  isSet: {
    type: Boolean,
    default: false,
  },
  setId: {
    type: [String, null],
    default: null,
  },
})

const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
  // loader: () =>
  //   new Promise((resolve) => {
  //     // 模拟 3 秒延迟
  //     setTimeout(() => {
  //       resolve(import('@/components/common/editor/code/CodeEditor.vue'))
  //     }, 3000)
  //   }),
  loadingComponent: {
    setup() {
      return () => h('div', {
        class: 'h-full p-4',
        style: {
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        },
      }, [
        h(NSpin, { size: 'small', description: '代码预览器加载中...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const { dataSubmitProblemPage, dataSubmitSetPage } = useDataSubmitFetch()

const columns: DataTableColumns<any> = [
  // {
  //   title: '题目',
  //   key: 'problemIdName',
  // },
  {
    title: '语言',
    key: 'languageName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
    render: (row) => {
      return h(NTag, { size: 'small', type: row.submitType ? 'info' : 'warning' }, { default: () => row.submitTypeName })
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
      return h(NTag, { size: 'small', type:
      row.status === 'COMPILATION_ERROR'
      || row.status === 'RUNTIME_ERROR'
      || row.status === 'TIME_LIMIT_EXCEEDED'
      || row.status === 'MEMORY_LIMIT_EXCEEDED'
      || row.status === 'WRONG_ANSWER'
      || row.status === 'SYSTEM_ERROR'
      || row.status === 'MEMORY_LIMIT_EXCEEDED'
        ? 'error'
        : 'success' }, { default: () => row.statusName })
    },
  },
  // {
  //   title: '相似度',
  //   key: 'similarity',
  //   render: (row) => {
  //     return h(NTag, { size: 'small' }, { default: () => row.similarity * 100 })
  //   },
  // },
  // {
  //   title: '检测任务',
  //   key: 'taskId',
  // },
]

const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 10,
  sortField: 'id',
  sortOrder: 'DESCEND',
  problemId: props.problemId,
  setId: null,
})
async function loadData() {
  if (props.isSet) {
    pageParam.value.setId = props.setId as any
    const { data } = await dataSubmitSetPage(pageParam.value)
    pageData.value = data
  }
  else {
    const { data } = await dataSubmitProblemPage(pageParam.value)
    pageData.value = data
    console.log(data)
  }
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

const showModal = ref(false)
const modalData = ref()
function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      showModal.value = true
      modalData.value = row
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
      scroll-x="550"
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
        @update:page-size="() => {
          pageParam.current = 1
          loadData()
        }"
      />
    </n-flex>

    <n-modal
      v-model:show="showModal"
      preset="card"
      title="提交详情"
      style="max-width: 700px;"
    >
      <n-scrollbar style="max-height: 500px">
        <n-space vertical :size="16">
          <!-- 头部信息 -->
          <JudgeResultHeader :result-task-data="modalData" />

          <!-- 判题结果详情 -->
          <JudgeResultStats :result-task-data="modalData" />

          <!-- 代码相似度 -->
          <!-- <SimilarityReport v-if="modalData.submitType" :result-task-data="modalData" /> -->

          <NSpace vertical :size="24">
            <n-card v-if="modalData?.message" size="small" class="rounded-xl">
              <template #header>
                <n-h2 class="pb-0 mb-0">
                  错误信息
                </n-h2>
              </template>
              <!-- <n-code :language="detailData?.language" :code="detailData?.message" show-line-numbers word-wrap /> -->
              <CodeEditor
                :model-value="modalData?.message"
                :language="modalData?.language"
                width="100%"
                style="height: 200px"
                :options="{
                  readOnly: true,
                  minimap: {
                    enabled: false,
                  },
                }"
              />
            </n-card>
            <n-card size="small" class="rounded-xl">
              <template #header>
                <n-h2 class="pb-0 mb-0">
                  提交代码
                </n-h2>
              </template>
              <template #header-extra>
                <span class="text-sm text-gray-500 dark:text-gray-400">{{ modalData?.languageName }}</span>
              </template>
              <CodeEditor
                :model-value="modalData?.code"
                :language="modalData?.language"
                width="100%"
                style="height: 300px"
                :options="{
                  readOnly: true,
                }"
              />
            </n-card>
          </NSpace>
        </n-space>
      </n-scrollbar>
    </n-modal>
  </n-space>
</template>

<style scoped>

</style>
