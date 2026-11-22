<script lang="ts" setup>
import { renderSafeAvatar } from '@/utils'
import { useSetRankingFetch } from '@/composables'
import { NSpace, NText } from 'naive-ui'
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

// Implementation note.
const userRankingColumns = [
  {
    title: 'Rank',
    key: 'ranking',
  },
  {
    title: 'Problem Set',
    key: 'set',
    render: (row: any) => {
      return h(
        NSpace,
        { align: 'center' },
        {
          default: () => [
            renderSafeAvatar( { src: row.cover, size: 'small' }),
            h(NText, { depth: 3 }, row.title),
          ],
        },
      )
    },
  },
  {
    title: 'Participants',
    key: 'participantCount',
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
    <n-flex justify="end">
      <n-button size="small" @click="loadData">
        Refresh
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
        label: `${(i + 1) * 10} per page`,
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
