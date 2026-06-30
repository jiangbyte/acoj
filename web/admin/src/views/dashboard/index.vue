<template>
  <n-el class="dashboard-page">
    <n-h2>{{ $t('resource.dashboard.title') }}</n-h2>
    <n-grid cols="1 s:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item>
        <n-card :title="$t('resource.dashboard.pending_tasks')" :bordered="false">
          <n-statistic :label="$t('resource.dashboard.today_new')" value="12" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="$t('resource.dashboard.activity_records')" :bordered="false">
          <n-statistic :label="$t('resource.dashboard.recent_hours')" value="248" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="$t('resource.dashboard.active_users')" :bordered="false">
          <n-statistic :label="$t('resource.dashboard.online')" value="36" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="$t('resource.dashboard.system_status')" :bordered="false">
          <n-statistic :label="$t('resource.dashboard.service')" :value="$t('resource.dashboard.normal')" />
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
