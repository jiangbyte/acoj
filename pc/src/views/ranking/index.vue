<script setup lang="ts">
import { useUserRankingFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import { NAvatar, NSpace, NTag, NText } from 'naive-ui'
import RankIcon from '@/components/common/rank/RankIcon.vue'

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
  useUserRankingFetch().useUserRankingPage(totalRankingPageParam.value).then(({ data }) => {
    if (data) {
      totalRankingPageData.value = data
    }
  })

  useUserRankingFetch().useUserActiveTop().then(({ data }) => {
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
    key: 'rank',
    width: 80,
    render: (row: any) => {
      // return h(NTag, { round: true, bordered: false, color: { color: RankColorUtil.getColor(row.rank), textColor: '#fff' } }, { default: () => RankColorUtil.getDisplayText(row.rank), icon: () => RankColorUtil.getIcon(row.rank) })
      return h(RankIcon, { rank: row.rank })
    },
    // render: (row) => {
    //   const rank = row.rank
    //   const isTopThree = rank <= 3

    //   // 配置对象
    //   const config = {
    //     tagProps: {
    //       round: true,
    //       bordered: false,
    //       color: isTopThree
    //         ? { color: RankColorUtil.getColor(rank) }
    //         : RankColorUtil.getColor(rank),
    //     },
    //     slots: {
    //       default: () => isTopThree ? RankColorUtil.getDisplayText(rank) : rank.toString(),
    //       ...(isTopThree && {
    //         icon: () => h('span', {}, RankColorUtil.getIcon(rank)),
    //       }),
    //     },
    //   }

    //   return h(NTag, config.tagProps, config.slots)
    // },
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
            h(NText, { depth: 3 }, { default: () => row.nickname }),
          ],
        },
      )
    },
  },
  {
    title: '解决题目数',
    key: 'score',
    width: 100,
    align: 'center',
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.score })
    },
  },
  // {
  //   title: '提交题目数',
  //   key: 'submitCount',
  //   width: 100,
  // },
  // {
  //   title: '通过率',
  //   key: 'acceptanceRate',
  //   width: 100,
  // },
  {
    title: '总提交数',
    key: 'submitCount',
    align: 'center',
    width: 100,
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.submitCount })
    },
  },
  // {
  //   title: '运行数',
  //   key: 'executionCount',
  //   width: 100,
  // },
  // {
  //   title: '总提交数',
  //   key: 'totalSubmissionCount',
  //   width: 100,
  // },
]

const router = useRouter()
function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      router.push({
        name: 'user',
        query: { userId: AesCrypto.encrypt(row.id) },
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
                :row-props="rowProps"
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
          <div class="p-x-5 pt-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              活跃用户
            </h3>
          </div>
          <n-empty
            v-if="!activeUsersTop || activeUsersTop.length === 0"
            class="flex flex-col items-center justify-center py-10 bg-transparent"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <NText depth="3" class="text-center max-w-xs">
              暂无数据
            </NText>
          </n-empty>
          <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 最新1 -->
            <div
              v-for="item in activeUsersTop" :key="item.id" class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
                name: 'user',
                query: { userId: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="flex items-center">
                <NAvatar :src="item.avatar" round :size="40" class="mr-3" />
                <div class="flex-1">
                  <div class="font-medium">
                    {{ item.nickname }}
                  </div>
                  <div class="text-xs text-gray-500 dark:text-gray-400">
                    活跃指数: {{ Number(item.score).toFixed(2) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 排行榜和活跃度说明卡片 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              排名规则说明
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <div class="p-x-5 pb-5 flex flex-col space-y-4">
              <div>
                <h4 class="font-medium mb-2">
                  🏆 用户排行榜
                </h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>按用户成功解决的题目数量从高到低排序</li>
                  <!-- <li>解决题目数相同的用户，按通过率排序</li> -->
                  <!-- <li>通过率 = 通过题目数 / 总提交题目数</li> -->
                  <li>用户排行榜数据实时更新</li>
                </ul>
              </div>

              <div>
                <h4 class="font-medium mb-2">
                  🎯 活跃用户
                </h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>活跃度 = 提交次数(0.01)之和</li>
                  <li>活跃度指数实时更新，反映提交活跃度</li>
                </ul>
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
