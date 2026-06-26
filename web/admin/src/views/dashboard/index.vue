<template>
  <n-el class="dashboard-page">
    <n-h2>{{ $t('dashboard.title') }}</n-h2>
    <n-grid cols="1 s:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item>
        <n-card :title="$t('dashboard.pendingProblems')" :bordered="false">
          <n-statistic :label="$t('dashboard.todayNew')" value="12" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="$t('dashboard.submissions')" :bordered="false">
          <n-statistic :label="$t('dashboard.last24Hours')" value="248" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="$t('dashboard.activeUsers')" :bordered="false">
          <n-statistic :label="$t('dashboard.online')" value="36" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="$t('dashboard.systemStatus')" :bordered="false">
          <n-statistic :label="$t('dashboard.service')" :value="$t('dashboard.normal')" />
        </n-card>
      </n-grid-item>
    </n-grid>
  </n-el>
</template>

<style scoped>
.dashboard-page {
  min-height: 100%;
}
</style>
