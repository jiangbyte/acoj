<script setup lang="ts">
import { useEcharts } from '@/hooks'
import type { ECOption } from '@/hooks'

import { useAnalyseFetch } from '@/composables'

const { getTodayHourlySubmitTrend } = useAnalyseFetch()

const chartData = ref()

async function loadData() {
  const { data } = await getTodayHourlySubmitTrend()
  chartData.value = data
  updateOption()
}
loadData()
const submitDate = computed(() => chartData.value?.map((v: any) => `${v.data} 时`))
const submitCountData = computed(() => chartData.value?.map((v: any) => v.submitCount))
const passCountData = computed(() => chartData.value?.map((v: any) => v.passCount))

// 折线图
const lineOptions = ref<ECOption>({
  tooltip: {
    trigger: 'axis',
  },
  grid: {
    left: '2%',
    right: '2%',
    bottom: '0%',
    top: '0%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: submitDate.value,
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },

  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: false,
    },
    axisTick: {
      show: false,
    },
    axisLabel: {
      show: false,
    },
  },
  series: [
    {
      color: '#0063ff',
      name: '提交数',
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: 'rgba(0, 99, 255, 0.3)', // 主题色半透明
            },
            {
              offset: 1,
              color: 'rgba(0, 99, 255, 0.05)', // 更淡的主题色
            },
          ],
        },
      },
      emphasis: {
        focus: 'series',
      },
      data: submitCountData.value,
    },
    {
      color: '#00c48c',
      name: '通过数',
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: 'rgba(0, 196, 140, 0.3)',
            },
            {
              offset: 1,
              color: 'rgba(0, 196, 140, 0.05)',
            },
          ],
        },
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          color: '#00c48c',
          borderWidth: 2,
        },
      },
      data: passCountData.value,
    },
  ],
  legend: {
    data: ['提交数', '通过数'],
    textStyle: {
      color: '#666',
    },
    itemGap: 20,
    itemWidth: 12,
    itemHeight: 12,
  },
}) as Ref<ECOption>
useEcharts('lineRef', lineOptions)

// 更新option的函数
function updateOption() {
  lineOptions.value = {
    ...lineOptions.value,
    xAxis: {
      ...lineOptions.value.xAxis,
      data: submitDate.value,
    },
    series: [
      {
        ...lineOptions.value.series[0],
        data: submitCountData.value,
      },
      {
        ...lineOptions.value.series[1],
        data: passCountData.value,
      },
    ],
  }
}
</script>

<template>
  <div
    ref="lineRef"
    class="h-400px"
  />
</template>

<style scoped>

</style>
