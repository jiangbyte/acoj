<script setup lang="ts">
import { NCard, NGi, NGrid, NIcon, NNumberAnimation, NSpace, NStatistic, NTabPane, NTabs } from 'naive-ui'
import TodayHourlySubmitTrend from './components/TodayHourlySubmitTrend.vue'
import ProblemRateChart from './components/ProblemRateChart.vue'
import LanguageChart from './components/LanguageChart.vue'
import WeeklySubmitTrend from './components/WeeklySubmitTrend.vue'

const totalUserAnalyse = ref()
const totalProblemAnalyse = ref()
const totalProblemSubmitAnalyse = ref()
async function loadData() {
  // const { data: totalUserAnalyseData } = await getTotalUserAnalyse()
  // totalUserAnalyse.value = totalUserAnalyseData

  // const { data: totalProblemAnalyseData } = await getTotalProblemAnalyse()
  // totalProblemAnalyse.value = totalProblemAnalyseData

  // const { data: totalProblemSubmitAnalyseData } = await getTotalProblemSubmitAnalyse()
  // totalProblemSubmitAnalyse.value = totalProblemSubmitAnalyseData
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
            <NStatistic label="Submissions Today">
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
              <span>Total Submissions</span>
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
            <NStatistic label="Accepted Today">
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
              <span>Total Accepted</span>
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
            <NStatistic label="Active This Week">
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
              <span>Registered Users</span>
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
            <NStatistic label="Problems">
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
              <span>Acceptance Rate</span>
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
            <NTabPane name="Weekly Submission Trend">
              <WeeklySubmitTrend />
            </NTabPane>
            <NTabPane name="Daily Submission Trend">
              <TodayHourlySubmitTrend />
            </NTabPane>
          </NTabs>
        </NCard>
      </NGi>
      <NGi :span="8">
        <NCard title="Problem Acceptance Rate">
          <ProblemRateChart />
        </NCard>
      </NGi>
      <NGi :span="6">
        <NCard title="Language Distribution">
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
