<script lang="ts" setup>
import { useProblemUserRankingFetch } from '@/composables'
import { NAvatar, NSpace, NText } from 'naive-ui'

const { problemUserMonthlyRankingPage: problemMonthlyRankingPage } = useProblemUserRankingFetch()

const totalRankingPageData = ref()

const totalRankingPageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
})

async function loadData() {
  const { data: totalRankingPage } = await problemMonthlyRankingPage(totalRankingPageParam.value)
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
            h(NAvatar, { src: row.avatar, size: 'small' }),
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
        label: `${(i + 1) * 10} 每页`,
        value: (i + 1) * 10,
      }))"
      class="flex justify-center items-center"
      @update:page="loadData"
      @update:page-size="loadData"
    />
  </n-flex>
</template>

<style>

</style>
