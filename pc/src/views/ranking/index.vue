<script setup lang="ts">
// 当前活跃标签
import { useProblemUserRankingFetch } from '@/composables'
import { NAvatar, NSpace, NText } from 'naive-ui'

const { problemUserTotalRankingPage: problemTotalRankingPage, problemActiveUsersTop } = useProblemUserRankingFetch()

const totalRankingPageData = ref()
const activeUsersTop = ref()

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

  problemActiveUsersTop().then(({ data }) => {
    if (data) {
      activeUsersTop.value = data
    }
  })
}
loadData()

// 用户排行榜数据
const userRankingColumns = [
  {
    title: '排名',
    key: 'ranking',
    width: 80,
  },
  {
    title: '用户',
    key: 'user',
    width: 150,
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
    width: 100,
  },
  {
    title: '提交题目数',
    key: 'attemptedCount',
    width: 100,
  },
  {
    title: '通过率',
    key: 'acceptanceRate',
    width: 100,
  },
  {
    title: '提交数',
    key: 'submissionCount',
    width: 100,
  },
  {
    title: '运行数',
    key: 'executionCount',
    width: 100,
  },
  {
    title: '总提交数',
    key: 'totalSubmissionCount',
    width: 100,
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
        查看平台上用户解题排行榜、活跃的用户等
      </p>
    </div>
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <div class="lg:col-span-3 space-y-8">
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
              30天活跃用户
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 最新1 -->
            <div v-for="item in activeUsersTop" :key="item.userId" class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <NAvatar :src="item.avatar" round :size="40" class="mr-3" />
                <div class="flex-1">
                  <div class="font-medium">
                    {{ item.nickname }}
                  </div>
                <!-- <div class="text-xs text-gray-500 dark:text-gray-400">
                  解题: {{ item.solvedCount }}
                </div> -->
                </div>
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
