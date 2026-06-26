<template>
  <n-el class="dashboard-page">
    <n-h2>工作台</n-h2>
    <n-grid cols="1 s:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item>
        <n-card title="待处理题目" :bordered="false">
          <n-statistic label="今日新增" value="12" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="提交记录" :bordered="false">
          <n-statistic label="近 24 小时" value="248" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="活跃用户" :bordered="false">
          <n-statistic label="在线" value="36" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="系统状态" :bordered="false">
          <n-statistic label="服务" value="正常" />
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
