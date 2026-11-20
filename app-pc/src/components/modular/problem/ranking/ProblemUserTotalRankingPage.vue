<script lang="ts" setup>
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

// Implementation note.
const userRankingColumns = [
  {
    title: 'Rank',
    key: 'ranking',
  },
  {
    title: 'User',
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
    title: 'Problems Solved',
    key: 'solvedCount',
  },
  {
    title: 'Problems Submitted',
    key: 'attemptedCount',
  },
  {
    title: 'Acceptance Rate',
    key: 'acceptanceRate',
  },
  {
    title: 'Submissions',
    key: 'submissionCount',
  },
  {
    title: 'Runs',
    key: 'executionCount',
  },
  {
    title: 'Total Submissions',
    key: 'totalSubmissionCount',
  },
]
</script>

<template>
  <n-flex vertical>
    <n-data-table
      :columns="userRankingColumns"
      :data="totalRankingPageData?.records"
      :bordered="false"
      :row-key="(row: any) => row.userId"
      class="flex-1 h-full"
      scroll-x="1200"
    />
    <n-pagination
      v-model:page="totalRankingPageParam.current"
      v-model:page-size="totalRankingPageParam.size"
      show-size-picker
      :page-count="totalRankingPageData ? Number(totalRankingPageData.pages) : 0"
      :page-sizes="Array.from({ length: 10 }, (_, i) => ({
        label: `${(i + 1) * 10} per page`,
        value: (i + 1) * 10,
      }))"
      class="flex justify-center items-center mt-2"
      @update:page="loadData"
      @update:page-size="loadData"
    />
  </n-flex>
</template>

<style>

</style>
