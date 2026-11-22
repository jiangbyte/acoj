<script lang="ts" setup>
import { useDataSubmitFetch, useSysDictFetch } from '@/composables/v1'
import type { DataTableColumns } from 'naive-ui'
import { NEllipsis, NSpace, NTag, NText, NTime } from 'naive-ui'
import { AesCrypto, renderSafeAvatar } from '@/utils'

const { dataSubmitProblemPage } = useDataSubmitFetch()
const { sysDictOptions } = useSysDictFetch()

const languageOptions = ref()
const submitTypeOptions = ref()
const statusOptions = ref()

const pageParam = ref({
  current: 1,
  size: 10,
  sortField: 'id',
  sortOrder: 'DESCEND',
  keyword: '',
  problem: '',
  language: null,
  submitType: null,
  status: null,
  isAuth: false,
})

const pageData = ref()

const statusCount = ref()
const isLoading = ref(false)
async function loadData() {
  isLoading.value = true
  const { data } = await dataSubmitProblemPage(pageParam.value)
  if (data) {
    pageData.value = data
    isLoading.value = false
  }

  // Implementation note.
  const { data: languageData } = await sysDictOptions({ dictType: 'ALLOW_LANGUAGE' })
  if (languageData) {
    languageOptions.value = languageData
  }

  const { data: submitTypeData } = await sysDictOptions({ dictType: 'SUBMIT_TYPE' })
  if (submitTypeData) {
    submitTypeOptions.value = submitTypeData
  }

  const { data: statusData } = await sysDictOptions({ dictType: 'JUDGE_STATUS' })
  if (statusData) {
    statusOptions.value = statusData
  }

  useDataSubmitFetch().dataSubmitProblemStatusCount().then(({ data }) => {
    if (data) {
      statusCount.value = data
    }
  })
}
loadData()

const columns: DataTableColumns<any> = [
  // {
  //   title: 'ID',
  //   key: 'id',
  //   width: 80,
  //   ellipsis: true,
  // },
  {
    title: 'Problem',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
    width: 120,
  },
  {
    title: 'User',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
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
    title: 'Status',
    key: 'statusName',
    width: 100,
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
  {
    title: 'Programming Language',
    key: 'languageName',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: 'Size (bytes)',
    key: 'codeLength',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.codeLength })
    },
  },
  {
    title: 'Run Type',
    key: 'submitTypeName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', type: row.submitType ? 'info' : 'warning' }, { default: () => row.submitTypeName })
    },
  },
  {
    title: 'Time (ms)',
    key: 'maxTime',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxTime })
    },
  },
  {
    title: 'Memory (KB)',
    key: 'maxMemory',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxMemory })
    },
  },
  // {
  //   title: 'Similarity(%)',
  //   key: 'similarity',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, { size: 'small' }, { default: () => row.similarity * 100 })
  //   },
  // },
  // {
  //   title: 'Flag',
  //   key: 'similarityCategoryName',
  //   ellipsis: true,
  //   width: 80,
  //   render: (row) => {
  //     return row.similarityCategoryName ? row.similarityCategoryName : 'Not Triggered'
  //   },
  // },
  // {
  //   title: 'Detection Task',
  //   key: 'taskId',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  //   width: 80,
  // },
  {
    title: 'Submitted',
    key: 'createTime',
    width: 80,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
  {
    title: 'Updated',
    key: 'updateTime',
    width: 80,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
]
function resetHandle() {
  pageParam.value.keyword = ''
  pageParam.value.problem = ''
  pageParam.value.language = null
  pageParam.value.submitType = null
  pageParam.value.status = null
  loadData()
}

const router = useRouter()

function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      router.push({
        name: 'problem_submit_detail',
        query: { submit: AesCrypto.encrypt(row.id) },
      })
    },
  }
}
</script>

<template>
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:6"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <!-- Section -->
      <n-gi span="1 l:2">
        <NSpace
          vertical
          :size="24"
        >
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Filter Submissions
              </n-h2>
            </template>
            <!-- <template #header-extra>
              <n-flex align="center">
                <n-text class="text-sm">
                  Total Problems
                </n-text>
                <n-text class="text-2xl font-bold">
                  {{ problemcount?.total ? problemcount.total : 0 }}
                </n-text>
                <n-text class="text-green-600 dark:text-green-400 text-xs flex items-center">
                  <icon-park-outline-arrow-up class="mr-1" />
                  Up from last month: {{ problemcount?.growthRate * 100 }} %
                </n-text>
              </n-flex>
            </template> -->
            <n-grid
              cols="1 l:1"
              :x-gap="24"
              :y-gap="24"
              responsive="screen"
            >
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.language"
                  placeholder="Select a language"
                  clearable
                  :options="languageOptions"
                  @clear="() => { pageParam.language = null; loadData() }"
                />
              </n-gi>
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.status"
                  placeholder="Select a judge status"
                  clearable
                  :options="statusOptions"
                  @clear="() => { pageParam.status = null; loadData() }"
                />
              </n-gi>
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.submitType"
                  placeholder="Select a submission type"
                  clearable
                  :options="submitTypeOptions"
                  @clear="() => { pageParam.submitType = null; loadData() }"
                />
              </n-gi>
            </n-grid>
            <template #footer>
              <n-input-group class="flex justify-end">
                <NButton type="warning" @click="resetHandle">
                  Reset
                </NButton>
                <NButton type="primary" @click="loadData">
                  Apply Filters
                </NButton>
              </n-input-group>
            </template>
          </n-card>

          <!-- Section -->
          <!-- <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4 mb-8">
            <div v-for="(item, index) in statusCount" :key="index" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
              <div class="font-bold">
                {{ item.statusName }}
              </div>
              <div class="mt-1 font-semibold">
                {{ item.count }}
              </div>
            </div>
          </div> -->
        </NSpace>
      </n-gi>
      <!-- Section -->
      <n-gi span="1 l:4">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Submissions
              </n-h2>
            </template>
            <template #header-extra>
              <NText>
                Total: <span class="text-blue-600 dark:text-blue-400 font-medium">
                  {{ pageData?.total ? pageData?.total : 0 }}
                </span> items
              </NText>
            </template>
            <n-data-table
              :columns="columns"
              :data="pageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              class="flex-1 h-full"
              :row-props="rowProps"
              :loading="isLoading"
              :scroll-x="1200"
            />
            <template #footer>
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
                class="flex justify-center items-center p-6"
                @update:page="loadData"
                @update:page-size="() => {
                  pageParam.current = 1
                  loadData()
                }"
              />
            </template>
          </n-card>
        </NSpace>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>

</style>
