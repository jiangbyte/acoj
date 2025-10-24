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
    key: 'solvedCount',
    width: 100,
    align: 'center',
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.solvedCount })
    },
  },
  {
    title: '提交题目数',
    key: 'submittedCount',
    width: 100,
    align: 'center',
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.submittedCount })
    },
  },
  // {
  //   title: '提交题目数',
  //   key: 'submitCount',
  //   width: 100,
  // },
  {
    title: '总提交数',
    key: 'totalSubmitCount',
    align: 'center',
    width: 100,
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.totalSubmitCount })
    },
  },
  {
    title: '通过率',
    key: 'acceptanceRate',
    width: 100,
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.acceptanceRate })
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
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:6"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <!-- 左侧主内容 -->
      <n-gi span="1 l:4">
        <!-- 公告内容 -->
        <NSpace vertical :size="24">
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                用户排行榜
              </n-h2>
            </template>
            <template #header-extra>
              <NText>
                当前共 <span class="text-blue-600 dark:text-blue-400 font-medium">
                  {{ totalRankingPageData?.total ? totalRankingPageData.total : 0 }}
                </span> 条数据
              </NText>
            </template>
            <n-data-table
              :columns="userRankingColumns"
              :data="totalRankingPageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              :row-props="rowProps"
              :loading="!totalRankingPageData?.records"
              class="flex-1 h-full"
            />
            <template #footer>
              <n-pagination
                v-model:page="totalRankingPageParam.current"
                v-model:page-size="totalRankingPageParam.size"
                show-size-picker
                :page-count="totalRankingPageData ? Number(totalRankingPageData.pages) : 0"
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
      <!-- 右侧边栏 -->
      <n-gi span="1 l:2">
        <NSpace
          vertical
          :size="24"
        >
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                活跃用户
              </n-h2>
            </template>
            <ListSkeleton03 v-if="!activeUsersTop" />
            <EmptyData v-else-if="activeUsersTop.length === 0" />
            <UserActiveRanking v-else :list-data="activeUsersTop" />
          </n-card>

          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                排名规则说明
              </n-h2>
            </template>
            <div class="p-x-5 pb-5 flex flex-col space-y-4">
              <div>
                <h4 class="font-medium mb-2">
                  🏆 用户排行榜
                </h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>按用户成功解决的题目数量从高到低排序</li>
                  <li>解决题目数相同的用户，按提交效率排序</li>
                  <li>通过率 = 用户通过题目数 / 用户总提交题目数</li>
                  <li>用户排行榜数据每十分钟刷新一次</li>
                </ul>
              </div>

              <div>
                <h4 class="font-medium mb-2">
                  🎯 活跃用户
                </h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>活跃度计算分数：</li>
                  <li>登录：+5分</li>
                  <li>有效提交代码：+2分</li>
                  <li>解题成功：+10分</li>
                  <li>每日上限：50分（防止刷分）</li>
                  <li>活跃度指数实时更新，反映用户活跃度</li>
                </ul>
              </div>
            </div>
          </n-card>
        </NSpace>
      </n-gi>
    </n-grid>
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
