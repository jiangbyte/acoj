<script setup lang="ts">
import { useEcharts } from '@/hooks'
import type { ECOption } from '@/hooks'
import { graphic } from 'echarts'
import { useAnalyseFetch } from '@/composables'

const { getProblemRateDistribution } = useAnalyseFetch()

const chartData = ref()

async function loadData() {
  const { data } = await getProblemRateDistribution()
  chartData.value = data
  updateOption()
}
loadData()

const xData = computed(() => chartData.value?.map((v: any) => v.name))
const sData = computed(() => chartData.value?.map((v: any) => v.value))

const option = ref<ECOption>({
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
    data: xData.value,
    axisTick: {
      show: false,
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(151,151,151,0.5)',
        type: 'dashed',
      },
    },
    axisLabel: {
      margin: 10,
      color: '#666',
      fontSize: 14,
    },

  },
  yAxis: {
    type: 'value',
    splitLine: {
      lineStyle: {
        color: 'rgba(151,151,151,0.5)',
        type: 'dashed',
      },
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(151,151,151,0.5)',
        type: 'dashed',
      },
    },
    axisTick: {
      show: false,
    },
    axisLabel: {
      show: false,
    },
  },
  series: [{
    type: 'bar',
    barWidth: '20px',
    data: sData.value,
    itemStyle: {
      color: new graphic.LinearGradient(0, 0, 0, 1, [{
        offset: 0,
        color: '#00BD89', // 0% 处的颜色
      }, {
        offset: 1,
        color: '#C9F9E1', // 100% 处的颜色
      }], false),
    },
  }],
}) as Ref<ECOption>

useEcharts('lineRef', option)

// 更新option的函数
function updateOption() {
  option.value = {
    ...option.value,
    xAxis: {
      ...option.value.xAxis,
      data: xData.value,
    },
    series: [{
      ...option.value.series[0],
      data: sData.value,
    }],
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
