<script lang="ts" setup>
import { useDataSubmitFetch, useSysDictFetch } from '@/composables/v1'
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NSpace, NTag, NText, NTime } from 'naive-ui'
import { AesCrypto } from '@/utils'

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

  // 获取下拉列表数据
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
    title: '题目',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
    width: 120,
  },
  {
    title: '用户',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
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
              NText,
              {},
              { default: () => row.userIdName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '状态',
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
    title: '编程语言',
    key: 'languageName',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: '长度(byte)',
    key: 'codeLength',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.codeLength })
    },
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', type: row.submitType ? 'info' : 'warning' }, { default: () => row.submitTypeName })
    },
  },
  {
    title: '耗时(ms)',
    key: 'maxTime',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxTime })
    },
  },
  {
    title: '内存(Kb)',
    key: 'maxMemory',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxMemory })
    },
  },
  {
    title: '相似度(%)',
    key: 'similarity',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.similarity * 100 })
    },
  },
  {
    title: '行为标记',
    key: 'similarityCategoryName',
    ellipsis: true,
    width: 80,
    render: (row) => {
      return row.similarityCategoryName ? row.similarityCategoryName : '未触发'
    },
  },
  // {
  //   title: '检测任务',
  //   key: 'taskId',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  //   width: 80,
  // },
  {
    title: '提交时间',
    key: 'createTime',
    width: 80,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
  {
    title: '更新时间',
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
      <!-- 右侧边栏 -->
      <n-gi span="1 l:2">
        <NSpace
          vertical
          :size="24"
        >
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                筛选提交
              </n-h2>
            </template>
            <!-- <template #header-extra>
              <n-flex align="center">
                <n-text class="text-sm">
                  题目总数
                </n-text>
                <n-text class="text-2xl font-bold">
                  {{ problemcount?.total ? problemcount.total : 0 }}
                </n-text>
                <n-text class="text-green-600 dark:text-green-400 text-xs flex items-center">
                  <icon-park-outline-arrow-up class="mr-1" />
                  较上月增长 {{ problemcount?.growthRate * 100 }} %
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
                  placeholder="选择语言"
                  clearable
                  :options="languageOptions"
                  @clear="() => { pageParam.language = null; loadData() }"
                />
              </n-gi>
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.status"
                  placeholder="选择判题状态"
                  clearable
                  :options="statusOptions"
                  @clear="() => { pageParam.status = null; loadData() }"
                />
              </n-gi>
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.submitType"
                  placeholder="选择提交类型"
                  clearable
                  :options="submitTypeOptions"
                  @clear="() => { pageParam.submitType = null; loadData() }"
                />
              </n-gi>
            </n-grid>
            <template #footer>
              <n-input-group class="flex justify-end">
                <NButton type="warning" @click="resetHandle">
                  重置筛选
                </NButton>
                <NButton type="primary" @click="loadData">
                  应用筛选
                </NButton>
              </n-input-group>
            </template>
          </n-card>

          <!-- 提交状态统计概览 -->
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
      <!-- 左侧主内容 -->
      <n-gi span="1 l:4">
        <!-- 公告内容 -->
        <NSpace vertical :size="24">
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                提交记录
              </n-h2>
            </template>
            <template #header-extra>
              <NText>
                当前共 <span class="text-blue-600 dark:text-blue-400 font-medium">
                  {{ pageData?.total ? pageData?.total : 0 }}
                </span> 条数据
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
              :scroll-x="1400"
            />
            <template #footer>
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
                class="flex justify-center items-center p-6"
                @update:page="loadData"
                @update:page-size="loadData"
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
