<template>
  <n-space vertical size="large">
    <n-page-header :title="t('resource.demo.chart')" :subtitle="t('resource.demo.chart_subtitle')" />

    <n-card :title="t('resource.demo.weekly_submissions')" :bordered="false">
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
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const chartData = computed(() => [
  { day: t('resource.demo.monday'), percent: 42 },
  { day: t('resource.demo.tuesday'), percent: 56 },
  { day: t('resource.demo.wednesday'), percent: 38 },
  { day: t('resource.demo.thursday'), percent: 72 },
  { day: t('resource.demo.friday'), percent: 64 },
  { day: t('resource.demo.saturday'), percent: 84 },
  { day: t('resource.demo.sunday'), percent: 60 },
])

const cards = computed(() => [
  { title: t('resource.demo.pass_rate'), value: 68 },
  { title: t('resource.demo.activity'), value: 74 },
  { title: t('resource.demo.completion'), value: 82 },
])
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
