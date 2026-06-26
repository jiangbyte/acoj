<template>
  <n-space vertical size="large">
    <n-page-header title="图表示例" subtitle="纯 CSS 的轻量数据展示" />

    <n-card title="近七日提交趋势" :bordered="false">
      <div class="chart-bars">
        <div v-for="item in chartData" :key="item.day" class="chart-item">
          <div class="chart-track">
            <div class="chart-value" :style="{ height: `${item.percent}%` }" />
          </div>
          <span>{{ item.day }}</span>
        </div>
      </div>
    </n-card>

    <n-grid cols="1 m:3" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item v-for="item in cards" :key="item.title">
        <n-card :title="item.title" :bordered="false">
          <n-progress type="line" :percentage="item.value" :indicator-placement="'inside'" />
        </n-card>
      </n-grid-item>
    </n-grid>
  </n-space>
</template>

<script setup lang="ts">
const chartData = [
  { day: '周一', percent: 42 },
  { day: '周二', percent: 56 },
  { day: '周三', percent: 38 },
  { day: '周四', percent: 72 },
  { day: '周五', percent: 64 },
  { day: '周六', percent: 84 },
  { day: '周日', percent: 60 },
]

const cards = [
  { title: '通过率', value: 68 },
  { title: '活跃度', value: 74 },
  { title: '完成度', value: 82 },
]
</script>

<style scoped>
.chart-bars {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 16px;
  min-height: 220px;
  align-items: end;
}

.chart-item {
  display: grid;
  gap: 8px;
  justify-items: center;
  color: var(--text-color-2);
}

.chart-track {
  position: relative;
  width: 100%;
  max-width: 42px;
  height: 180px;
  border-radius: 6px;
  background: var(--hover-color);
  overflow: hidden;
}

.chart-value {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 6px 6px 0 0;
  background: var(--primary-color);
}
</style>
