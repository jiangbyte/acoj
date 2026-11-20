<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { NSpin, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

const props = defineProps({
  moduleType: {
    type: String,
    required: true,
  },
  moduleId: {
    type: String,
    required: true,
  },
  problemId: {
    type: String,
    required: true,
  },
})

const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
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
        h(NSpin, { size: 'small', description: 'Loading code preview...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const { dataModuleSubmitPage } = useDataSubmitFetch()

const columns: DataTableColumns<any> = [
  {
    title: 'Language',
    key: 'languageName',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: 'Run Type',
    key: 'submitTypeName',
    render: (row) => {
      return h(NTag, { size: 'small', type: row.submitType ? 'info' : 'warning' }, { default: () => row.submitTypeName })
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
]

const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 10,
  sortField: 'id',
  sortOrder: 'DESCEND',
  moduleId: props.moduleId,
  moduleType: props.moduleType,
  problemId: props.problemId,
  isAuth: true,
})
const isLoading = ref(false)
async function loadData() {
  isLoading.value = true
  const { data } = await dataModuleSubmitPage(pageParam.value)
  pageData.value = data
  console.log(data)
  isLoading.value = false
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

const showModal = ref(false)
const modalData = ref()

const similarityData = ref()
function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      showModal.value = true
      modalData.value = row
      similarityData.value = row
    },
  }
}

function refreshSimilarityData() {
  // Implementation note.
  useDataSubmitFetch().dataSubmitDetail({ id: similarityData.value.id }).then(({ data }) => {
    if (data) {
      // Implementation note.
      similarityData.value = data

      window.$message.success('Refreshed')
    }
  })
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
        <n-button text @click="loadData">
          <template #icon>
            <IconParkOutlineRefresh />
          </template>
        </n-button>
      </n-flex>
    </n-flex>
    <!-- Section -->
    <n-data-table
      :columns="columns"
      :data="pageData?.records"
      :bordered="false"
      :row-key="(row: any) => row.id"
      :row-props="rowProps"
      scroll-x="550"
      class="flex-1 h-full"
      :loading="isLoading"
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
        @update:page-size="() => {
          pageParam.current = 1
          loadData()
        }"
      />
    </n-flex>

    <n-modal
      v-model:show="showModal"
      preset="card"
      title="Submission Details"
      style="max-width: 700px;"
    >
      <n-scrollbar style="max-height: 500px">
        <n-space vertical :size="16">
          <!-- Section -->
          <JudgeResultHeader :result-task-data="modalData" />

          <n-flex justify="end">
            <n-button size="small" type="primary" @click="refreshSimilarityData">
              Refresh
            </n-button>
          </n-flex>

          <!-- Section -->
          <JudgeResultStats :result-task-data="modalData" />

          <NSpace vertical :size="24">
            <n-card v-if="modalData?.message" size="small" class="rounded-xl">
              <template #header>
                <n-h2 class="pb-0 mb-0">
                  Error Details
                </n-h2>
              </template>
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
                  Submit Code
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
