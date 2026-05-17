<template>
  <div class="dashboard">
    <a-row :gutter="12">
      <!-- Left column -->
      <a-col :xs="24" :lg="16" class="mb-4">
        <div class="flex flex-col gap-3">
          <UserInfoCard />
          <StatCards :sys-data="data?.stats ?? null" :client-data="data?.client_stats ?? null" :loading="loading" />
          <TrendChart :sys-data="data?.user_trend ?? []" :client-data="data?.client_trend ?? []" :loading="loading" :is-dark="isDark" />
        </div>
      </a-col>

      <!-- Right column -->
      <a-col :xs="24" :lg="8" class="mb-4">
        <div class="flex flex-col gap-3">
          <DistributionCharts
            :org-data="data?.org_user_distribution ?? []"
            :role-data="data?.role_category_distribution ?? []"
            :loading="loading"
            :is-dark="isDark"
          />
          <SysInfoCard :data="data?.sys_info ?? null" :loading="loading" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysDashboard' })
import { ref, shallowRef, onMounted } from 'vue'
import { fetchDashboard, type DashboardData } from '@/api/dashboard'
import UserInfoCard from './components/UserInfoCard.vue'
import StatCards from './components/StatCards.vue'
import TrendChart from './components/TrendChart.vue'
import DistributionCharts from './components/DistributionCharts.vue'
import SysInfoCard from './components/SysInfoCard.vue'

const data = shallowRef<DashboardData | null>(null)
const loading = ref(true)
const isDark = ref(false)

onMounted(async () => {
  isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'

  try {
    const res = await fetchDashboard()
    if (res.success) {
      data.value = res.data
    }
  } catch {
    /* ignore */
  }
  loading.value = false
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}
.mb-4 {
  margin-bottom: 16px;
}
</style>
