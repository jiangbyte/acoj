<script lang="ts" setup>
import { useAnalyseFetch } from '@/composables'

const { getTotalAnalyse } = useAnalyseFetch()

// 当前活跃标签
const activeTab = ref('total')

function fetchRankings(type: string) {
  // 实际项目中这里应该是API调用
  console.log(`Fetching ${type} rankings...`)
}

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

const analyse = ref()
async function loadData() {
  const { data } = await getTotalAnalyse()
  analyse.value = data
}
loadData()
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-grid
      cols="12 m:24"
      responsive="screen"
      :x-gap="16"
      :y-gap="16"
    >
      <NGi span="6 m:6">
        <NCard class="content-card" size="small">
          <NStatistic label="总用户数" :value="analyse?.totalUserCount ? analyse.totalUserCount : 0" />
        </NCard>
      </NGi>
      <NGi span="6 m:6">
        <NCard class="content-card" size="small">
          <NStatistic label="总题目数" :value="analyse?.totalProblemCount ? analyse.totalProblemCount : 0" />
        </NCard>
      </NGi>
      <NGi span="6 m:6">
        <NCard class="content-card" size="small">
          <NStatistic label="总题集数" :value="analyse?.totalSetCount ? analyse.totalSetCount : 0" />
        </NCard>
      </NGi>
      <NGi span="6 m:6">
        <NCard class="content-card" size="small">
          <NStatistic label="总提交数" :value="analyse?.totalSubmitCount ? analyse.totalSubmitCount : 0" />
        </NCard>
      </NGi>
    </n-grid>
    <n-card title="排行榜" class="content-card" size="small">
      <NTabs v-model:value="activeTab" type="line" animated @update:value="fetchRankings">
        <NTabPane name="total" tab="用户榜">
          <n-flex vertical class="w-full">
            <n-select v-model:value="rankingType" :options="rankingOptions" class="w-50" />
            <ProblemUserTotalRankingPage v-if="rankingType === 'total'" />
            <ProblemUserMonthlyRankingPage v-if="rankingType === 'monthly'" />
            <ProblemUserWeeklyRankingPage v-if="rankingType === 'weekly'" />
            <ProblemUserDailyRankingPage v-if="rankingType === 'daily'" />
          </n-flex>
        </NTabPane>
        <NTabPane name="problems" tab="题目榜">
          <ProblemRankingPage />
        </NTabPane>
        <NTabPane name="sets" tab="题集榜">
          <SetRankingPage />
        </NTabPane>
      </NTabs>
    </n-card>
  </div>
</template>

<style scoped>

</style>
