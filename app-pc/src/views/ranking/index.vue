<script setup lang="ts">
import { useUserRankingFetch } from '@/composables/v1'
import { AesCrypto, renderSafeAvatar } from '@/utils'
import { NEllipsis, NSpace, NTag, NText } from 'naive-ui'
import RankIcon from '@/components/common/rank/RankIcon.vue'

const totalRankingPageData = ref()
const activeUsersTop = ref()

const totalRankingPageParam = ref({
  current: 1,
  size: 10,
  sortField: null,
  sortOrder: null,
  keyword: '',
})

const isLoading = ref(false)
async function loadData() {
  isLoading.value = true
  useUserRankingFetch().useUserRankingPage(totalRankingPageParam.value).then(({ data }) => {
    if (data) {
      totalRankingPageData.value = data
    }
    isLoading.value = false
  })

  useUserRankingFetch().useUserActiveTop().then(({ data }) => {
    if (data) {
      activeUsersTop.value = data
    }
  })
}
loadData()

// Implementation note.
const userRankingColumns = [
  {
    title: 'Rank',
    key: 'rank',
    width: 80,
    render: (row: any) => {
      // return h(NTag, { round: true, bordered: false, color: { color: RankColorUtil.getColor(row.rank), textColor: '#fff' } }, { default: () => RankColorUtil.getDisplayText(row.rank), icon: () => RankColorUtil.getIcon(row.rank) })
      return h(RankIcon, { rank: row.rank })
    },
    // render: (row) => {
    //   const rank = row.rank
    //   const isTopThree = rank <= 3

    // Implementation note.
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
    title: 'User',
    key: 'user',
    width: 150,
    render: (row: any) => {
      return h(
        NSpace,
        { align: 'center' },
        {
          default: () => [
            renderSafeAvatar( { src: row.avatar, size: 'small', round: true }),
            h(
              NEllipsis,
              {
                style: {
                  maxWidth: '90px',
                },
              },
              { default: () => row.nickname },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Problems Solved',
    key: 'solvedCount',
    width: 100,
    align: 'center',
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.solvedCount })
    },
  },
  {
    title: 'Problems Submitted',
    key: 'submittedCount',
    width: 100,
    align: 'center',
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.submittedCount })
    },
  },
  // {
  //   title: 'Problems Submitted',
  //   key: 'submitCount',
  //   width: 100,
  // },
  {
    title: 'Total Submissions',
    key: 'totalSubmitCount',
    align: 'center',
    width: 100,
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.totalSubmitCount })
    },
  },
  {
    title: 'Acceptance Rate',
    key: 'acceptanceRate',
    width: 100,
    render: (row: any) => {
      return h(NTag, { type: 'info' }, { default: () => row.acceptanceRate })
    },
  },
  // {
  //   title: 'Runs',
  //   key: 'executionCount',
  //   width: 100,
  // },
  // {
  //   title: 'Total Submissions',
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
      <!-- Section -->
      <n-gi span="1 l:4">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                User Rankings
              </n-h2>
            </template>
            <template #header-extra>
              <NText>
                Total: <span class="text-blue-600 dark:text-blue-400 font-medium">
                  {{ totalRankingPageData?.total ? totalRankingPageData.total : 0 }}
                </span> items
              </NText>
            </template>
            <n-data-table
              :columns="userRankingColumns"
              :data="totalRankingPageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              :row-props="rowProps"
              :loading="isLoading"
              class="flex-1 h-full"
            />
            <template #footer>
              <n-pagination
                v-model:page="totalRankingPageParam.current"
                v-model:page-size="totalRankingPageParam.size"
                show-size-picker
                :page-count="totalRankingPageData ? Number(totalRankingPageData.pages) : 0"
                :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                  label: `${(i + 1) * 10} per page`,
                  value: (i + 1) * 10,
                }))"
                :page-slot="3"
                class="flex justify-center items-center p-6"
                @update:page="loadData"
                @update:page-size="() => {
                  totalRankingPageParam.current = 1
                  loadData()
                }"
              />
            </template>
          </n-card>
        </NSpace>
      </n-gi>
      <!-- Section -->
      <n-gi span="1 l:2">
        <NSpace
          vertical
          :size="24"
        >
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Active Users
              </n-h2>
            </template>
            <ListSkeleton03 v-if="!activeUsersTop" />
            <EmptyData v-else-if="activeUsersTop.length === 0" />
            <UserActiveRanking v-else :list-data="activeUsersTop" />
          </n-card>

          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Ranking Rules
              </n-h2>
            </template>
            <div class="p-x-5 pb-5 flex flex-col space-y-4">
              <div>
                <h4 class="font-medium mb-2">
                  🏆 User Rankings
                </h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>Ranked by the number of problems solved</li>
                  <li>Ties are ranked by submission efficiency</li>
                  <li>Acceptance rate = solved problems / submitted problems</li>
                  <li>Rankings refresh every 10 minutes</li>
                </ul>
              </div>

              <div>
                <h4 class="font-medium mb-2">
                  🎯 Active Users
                </h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>Activity score:</li>
                  <li>Sign in: +5</li>
                  <li>Valid submission: +2</li>
                  <li>Problem solved: +10</li>
                  <li>Daily cap: 50 points</li>
                  <li>The activity score updates in real time</li>
                  <li>Scores reset at the start of each month</li>
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
/* Component styles. */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* Component styles. */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* Component styles. */
tbody tr {
  transition: background-color 0.2s ease;
}

/* Component styles. */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
