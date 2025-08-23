<script setup lang="ts">
// 当前活跃标签
import { useProblemUserRankingFetch } from '@/composables'
import { NAvatar, NSpace, NText } from 'naive-ui'

const { problemUserTotalRankingPage: problemTotalRankingPage } = useProblemUserRankingFetch()

const totalRankingPageData = ref()

const totalRankingPageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
})

async function loadData() {
  const { data: totalRankingPage } = await problemTotalRankingPage(totalRankingPageParam.value)
  if (totalRankingPage) {
    totalRankingPageData.value = totalRankingPage
  }
}
loadData()

// 用户排行榜数据
const userRankingColumns = [
  {
    title: '排名',
    key: 'ranking',
  },
  {
    title: '用户',
    key: 'user',
    render: (row: any) => {
      return h(
        NSpace,
        { align: 'center' },
        {
          default: () => [
            h(NAvatar, { src: row.avatar, size: 'small', round: true }),
            h(NText, { depth: 3 }, row.nickname),
          ],
        },
      )
    },
  },
  {
    title: '解决题目数',
    key: 'solvedCount',
  },
  {
    title: '提交题目数',
    key: 'attemptedCount',
  },
  {
    title: '通过率',
    key: 'acceptanceRate',
  },
  {
    title: '提交数',
    key: 'submissionCount',
  },
  {
    title: '运行数',
    key: 'executionCount',
  },
  {
    title: '总提交数',
    key: 'totalSubmissionCount',
  },
]

const activeTab = ref('total')
const rankingType = ref('total')
const rankingOptions = [
  {
    label: '总排行榜',
    value: 'total',
  },
  {
    label: '月排行榜',
    value: 'monthly',
  },
  {
    label: '周排行榜',
    value: 'weekly',
  },
  {
    label: '日排行榜',
    value: 'daily',
  },
]
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- 页面标题和统计信息 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">
        排行榜
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        查看平台上最活跃的用户、最受欢迎的题目和最热门的题集
      </p>
    </div>
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8">
        <!-- 排行榜标签页 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
            <div class="p-5 border-b border-gray-100 dark:border-gray-700">
              <h3 class="font-semibold text-lg">
                用户排行榜
              </h3>
            </div>
            <div class="divide-y divide-gray-100 dark:divide-gray-700">
              <n-data-table
                :columns="userRankingColumns"
                :data="totalRankingPageData?.records"
                :bordered="false"
                :row-key="(row: any) => row.userId"
                class="flex-1 h-full"
              />
            </div>
            <n-pagination
              v-model:page="totalRankingPageParam.current"
              v-model:page-size="totalRankingPageParam.size"
              show-size-picker
              :page-count="totalRankingPageData ? Number(totalRankingPageData.pages) : 0"
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
      </div>
      <div class="space-y-8">
        <!-- 榜单：最新上线 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              最新上线
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 最新1 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center text-green-600 dark:text-green-300 font-bold mr-4">
                  <i class="fa fa-clock-o" />
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">机器学习算法基础</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    3天前 • 60题
                  </p>
                </div>
                <span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs rounded-full">新</span>
              </div>
            </div>
          </div>
        </div>
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

/* 隐藏滚动条但保留功能 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
