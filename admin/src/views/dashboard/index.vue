<script setup lang="ts">
import { NCard, NGi, NGrid, NIcon, NNumberAnimation, NSpace, NStatistic, NTabPane, NTabs } from 'naive-ui'

import TodayHourlySubmitTrend from './components/TodayHourlySubmitTrend.vue'
import ProblemRateChart from './components/ProblemRateChart.vue'
import LanguageChart from './components/LanguageChart.vue'
import WeeklySubmitTrend from './components/WeeklySubmitTrend.vue'

import { useAnalyseFetch } from '@/composables'

const { getTotalUserAnalyse, getTotalProblemAnalyse, getTotalProblemSubmitAnalyse } = useAnalyseFetch()

const totalUserAnalyse = ref()
const totalProblemAnalyse = ref()
const totalProblemSubmitAnalyse = ref()
async function loadData() {
  const { data: totalUserAnalyseData } = await getTotalUserAnalyse()
  totalUserAnalyse.value = totalUserAnalyseData

  const { data: totalProblemAnalyseData } = await getTotalProblemAnalyse()
  totalProblemAnalyse.value = totalProblemAnalyseData

  const { data: totalProblemSubmitAnalyseData } = await getTotalProblemSubmitAnalyse()
  totalProblemSubmitAnalyse.value = totalProblemSubmitAnalyseData
}

loadData()
</script>

<template>
  <NCard size="small">
    <NGrid
      :x-gap="16"
      :y-gap="16"
    >
      <NGi :span="6">
        <NCard>
          <NSpace
            justify="space-between"
            align="center"
          >
            <NStatistic label="今日提交">
              <NNumberAnimation
                :from="0"
                :to="totalProblemSubmitAnalyse?.dailySubmit"
                show-separator
              />
            </NStatistic>
            <NIcon
              color="#de4307"
              size="42"
            >
              <icon-park-outline-chart-histogram />
            </NIcon>
          </NSpace>
          <template #footer>
            <NSpace justify="space-between">
              <span>总提交数</span>
              <span><NNumberAnimation
                :from="0"
                :to="totalProblemSubmitAnalyse?.totalSubmit"
                show-separator
              /></span>
            </NSpace>
          </template>
        </NCard>
      </NGi>
      <NGi :span="6">
        <NCard>
          <NSpace
            justify="space-between"
            align="center"
          >
            <NStatistic label="今日AC">
              <NNumberAnimation
                :from="0"
                :to="totalProblemSubmitAnalyse?.dailyAC"
                show-separator
              />
            </NStatistic>
            <NIcon
              color="#18a058"
              size="42"
            >
              <icon-park-outline-check-correct />
            </NIcon>
          </NSpace>
          <template #footer>
            <NSpace justify="space-between">
              <span>总AC数</span>
              <span><NNumberAnimation
                :from="0"
                :to="totalProblemSubmitAnalyse?.totalAC"
                show-separator
              /></span>
            </NSpace>
          </template>
        </NCard>
      </NGi>
      <NGi :span="6">
        <NCard>
          <NSpace
            justify="space-between"
            align="center"
          >
            <NStatistic label="本周活跃">
              <NNumberAnimation
                :from="0"
                :to="totalUserAnalyse?.weeklyActiveUser"
                show-separator
              />
            </NStatistic>
            <NIcon
              color="#1687a7"
              size="42"
            >
              <icon-park-outline-user />
            </NIcon>
          </NSpace>
          <template #footer>
            <NSpace justify="space-between">
              <span>注册用户</span>
              <span><NNumberAnimation
                :from="0"
                :to="totalUserAnalyse?.registerUser"
                show-separator
              /></span>
            </NSpace>
          </template>
        </NCard>
      </NGi>
      <NGi :span="6">
        <NCard>
          <NSpace
            justify="space-between"
            align="center"
          >
            <NStatistic label="题目数量">
              <NNumberAnimation
                :from="0"
                :to="totalProblemAnalyse?.problems"
                show-separator
              />
            </NStatistic>
            <NIcon
              color="#42218E"
              size="42"
            >
              <icon-park-outline-list />
            </NIcon>
          </NSpace>
          <template #footer>
            <NSpace justify="space-between">
              <span>通过率</span>
              <span>{{ totalProblemAnalyse?.passRate ? totalProblemAnalyse?.passRate : 0 }}%</span>
            </NSpace>
          </template>
        </NCard>
      </NGi>
      <NGi :span="10">
        <NCard content-style="padding: 0;">
          <NTabs
            type="line"
            size="large"
            :tabs-padding="20"
            pane-style="padding: 20px;"
          >
            <NTabPane name="周提交趋势">
              <WeeklySubmitTrend />
            </NTabPane>
            <NTabPane name="日提交趋势">
              <TodayHourlySubmitTrend />
            </NTabPane>
          </NTabs>
        </NCard>
      </NGi>
      <NGi :span="8">
        <NCard title="题目通过率">
          <ProblemRateChart />
        </NCard>
      </NGi>
      <NGi :span="6">
        <NCard title="语言分布">
          <LanguageChart />
        </NCard>
      </NGi>
    </NGrid>
  </NCard>
</template>

<style scoped>
.n-tag {
  font-weight: bold;
}
</style>
