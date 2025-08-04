<script lang="ts" setup>
import { useSetRankingFetch } from '@/composables'
import { NAvatar, NSpace, NText } from 'naive-ui'

const { setRankingPage } = useSetRankingFetch()

const totalRankingPageData = ref()

const totalRankingPageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
})

async function loadData() {
  const { data: totalRankingPage } = await setRankingPage(totalRankingPageParam.value)
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
    title: '题集',
    key: 'set',
    render: (row: any) => {
      return h(
        NSpace,
        { align: 'center' },
        {
          default: () => [
            h(NAvatar, { src: row.cover, size: 'small' }),
            h(NText, { depth: 3 }, row.title),
          ],
        },
      )
    },
  },
  {
    title: '参与用户数',
    key: 'participantCount',
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
    <n-flex justify="end">
      <n-button size="small" @click="loadData">
        刷新
      </n-button>
    </n-flex>
    <n-data-table
      :columns="userRankingColumns"
      :data="totalRankingPageData?.records"
      :bordered="false"
      :row-key="(row: any) => row.setId"
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
