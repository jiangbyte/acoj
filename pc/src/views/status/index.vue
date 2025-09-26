<script lang="ts" setup>
import { useDataSubmitFetch, useSysDictFetch } from '@/composables/v1'
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NSpace, NTag, NText, NTime } from 'naive-ui'
import { AesCrypto, LanguageColorUtil, StatusColorUtil, SubmitTypeColorUtil } from '@/utils'

const { dataSubmitProblemPage } = useDataSubmitFetch()
const { sysDictOptions } = useSysDictFetch()

const languageOptions = ref()
const submitTypeOptions = ref()
const statusOptions = ref()

const pageParam = ref({
  current: 1,
  size: 10,
  sortField: null,
  sortOrder: null,
  keyword: '',
  problem: '',
  language: null,
  submitType: null,
  status: null,
})

const pageData = ref()

const statusCount = ref()

async function loadData() {
  const { data } = await dataSubmitProblemPage(pageParam.value)
  if (data) {
    pageData.value = data
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
      return h(NTag, { size: 'small', bordered: false, color: { color: StatusColorUtil.getColor(row.status), textColor: '#fff' } }, {default: () =>row.statusName})
    },
  },
  {
    title: '编程语言',
    key: 'languageName',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false, color: { color: LanguageColorUtil.getColor(row.language), textColor: '#fff' } }, {default: () =>row.languageName})
    },
  },
  {
    title: '长度(byte)',
    key: 'codeLength',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, {default: () =>row.codeLength})
    },
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false, color: { color: SubmitTypeColorUtil.getColor(row.submitType), textColor: '#fff' } }, {default: () =>row.submitTypeName})
    },
  },
  {
    title: '耗时(ms)',
    key: 'maxTime',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, {default: () =>row.maxTime})
    },
  },
  {
    title: '内存(Kb)',
    key: 'maxMemory',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, {default: () =>row.maxMemory})
    },
  },
  {
    title: '相似度(%)',
    key: 'similarity',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, {default: () =>row.similarity * 100})
    },
  },
  {
    title: '行为标记',
    key: 'similarityCategoryName',
    ellipsis: true,
    width: 80,
  },
  {
    title: '检测任务',
    key: 'taskId',
    ellipsis: {
      tooltip: true,
    },
    width: 80,
  },
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
  <main class="container mx-auto px-4 py-8">
    <!-- 页面标题和统计信息 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">
        提交状态
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        最近 <span class="text-blue-600 dark:text-blue-400 font-medium">
          {{ pageData?.total }}
        </span> 次提交
      </p>
    </div>

    <!-- 提交状态统计概览 -->
    <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4 mb-8">
      <div v-for="(item, index) in statusCount" :key="index" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
        <div class="font-bold">
          <!-- {{ item.status }} -->
          {{ item.statusName }}
        </div>
        <!-- <div class="text-sm text-gray-500 dark:text-gray-400">
          {{ item.statusName }}
        </div> -->
        <div class="mt-1 font-semibold">
          {{ item.count }}
        </div>
      </div>
    </div>

    <!-- 高级筛选区 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 mb-8">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- 题目筛选 -->
        <!-- <div>
          <label class="block text-sm font-medium mb-2">题目</label>
          <n-input
            v-model:value="pageParam.problem"
            placeholder="搜索题目"
            clearable
            @keyup.enter="loadData"
            @clear="resetHandle"
          />
        </div> -->

        <!-- 用户筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">用户</label>
          <n-input
            v-model:value="pageParam.keyword"
            placeholder="搜索用户"
            clearable
            @keyup.enter="loadData"
            @clear="resetHandle"
          />
        </div>

        <!-- 语言筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">编程语言</label>
          <n-select
            v-model:value="pageParam.language"
            placeholder="选择语言"
            clearable
            :options="languageOptions"
            @clear="resetHandle"
          />
        </div>

        <!-- 状态筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">判题状态</label>
          <n-select
            v-model:value="pageParam.status"
            placeholder="选择判题状态"
            clearable
            :options="statusOptions"
            @clear="resetHandle"
          />
        </div>

        <!-- 提交类型筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">提交类型</label>
          <n-select
            v-model:value="pageParam.submitType"
            placeholder="选择提交类型"
            clearable
            :options="submitTypeOptions"
            @clear="resetHandle"
          />
        </div>
      </div>

      <!-- 时间范围筛选 -->
      <!-- <div class="mt-4">
        <label class="block text-sm font-medium mb-2">时间范围</label>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="relative">
            <input
              type="date"
              class="w-full pl-4 pr-10 py-2.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
            >
          </div>
          <div class="relative">
            <input
              type="date"
              class="w-full pl-4 pr-10 py-2.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
            >
          </div>
        </div>
      </div> -->

      <!-- 筛选按钮 -->
      <div class="mt-6 flex justify-end space-x-3">
        <n-button type="warning" @click="resetHandle">
          重置筛选
        </n-button>
        <n-button type="primary" @click="loadData">
          应用筛选
        </n-button>
      </div>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
        <div class="p-5 border-b border-gray-100 dark:border-gray-700">
          <h3 class="font-semibold text-lg">
            提交状态
          </h3>
        </div>
        <div class="divide-y divide-gray-100 dark:divide-gray-700">
          <n-data-table
            :columns="columns"
            :data="pageData?.records"
            :bordered="false"
            :row-key="(row: any) => row.userId"
            class="flex-1 h-full"
            :row-props="rowProps"
            :scroll-x="1400"
          />
        </div>
        <n-pagination
          v-model:page="pageParam.current"
          v-model:page-size="pageParam.size"
          show-size-picker
          :page-count="pageData ? Number(pageData.pages) : 0"
          :page-sizes="Array.from({ length: 10 }, (_, i) => ({
            label: `${(i + 1) * 10} 每页`,
            value: (i + 1) * 10,
          }))"
          class="flex justify-center items-center p-6"
          @update:page="loadData"
          @update:page-size="loadData"
        />
      </div>
    </div>
  </main>
</template>

<style scoped>
/* 基础样式补充 */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* 解决select下拉箭头在部分浏览器不显示的问题 */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* 表格行悬停效果 */
tbody tr {
  transition: background-color 0.2s ease;
}
</style>
