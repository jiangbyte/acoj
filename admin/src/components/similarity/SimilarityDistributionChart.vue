<script setup lang="ts">
import { useEcharts } from '@/hooks'
import type { ECOption } from '@/hooks'

// 定义 props
const props = defineProps<{
  value: number[] // 接收父组件传递的值
}>()

// 横轴: 相似度百分比区间 (0-10%, 10-20%, ..., 90-100%)
// 纵轴: 该相似度区间内的克隆对数量
const similarityGroups = [
  '0-10%',
  '10-20%',
  '20-30%',
  '30-40%',
  '40-50%',
  '50-60%',
  '60-70%',
  '70-80%',
  '80-90%',
  '90-100%',
]

// 模拟的克隆对数量数据，通常高相似度区间会有更多克隆对
// const cloneCounts = [5, 12, 28, 45, 78, 120, 185, 230, 310, 275]

const option = ref<ECOption>({
  tooltip: {
    trigger: 'axis',
    formatter: '{b}: {c} 对克隆代码',
    axisPointer: {
      type: 'shadow',
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '8%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: similarityGroups,
    // name: '相似度区间',
    nameLocation: 'middle',
    nameGap: 40,
    nameTextStyle: {
      fontSize: 14,
      color: '#333',
    },
    axisTick: {
      alignWithLabel: true,
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(151,151,151,0.8)',
      },
    },
    axisLabel: {
      color: '#666',
      fontSize: 12,
      rotate: 30, // 旋转标签避免重叠
    },
  },
  yAxis: {
    type: 'value',
    // name: '克隆对数量',
    nameLocation: 'middle',
    nameGap: 40,
    nameTextStyle: {
      fontSize: 14,
      color: '#333',
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(151,151,151,0.3)',
        type: 'dashed',
      },
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(151,151,151,0.8)',
      },
    },
    axisLabel: {
      color: '#666',
      fontSize: 12,
    },
  },
  series: [{
    name: '克隆对数量',
    type: 'bar',
    barWidth: '60%',
    data: [],
    itemStyle: {
      color(params) {
        const colorList = [
          '#FFE0B2',
          '#FFCC80',
          '#FFB74D',
          '#FFA726',
          '#FF9800',
          '#FB8C00',
          '#F57C00',
          '#EF6C00',
          '#E65100',
          '#D32F2F',
        ]
        return colorList[params.dataIndex]
      },
      borderRadius: [4, 4, 0, 0],
    },
    // 显示每个柱子的数值
    label: {
      show: true,
      position: 'top',
      fontSize: 12,
      color: '#333',
    },
  }],
}) as Ref<ECOption>

watch(() => props.value, (data) => {
  option.value = {
    ...option.value,
    series: [{
      data,
    }],
  }
}, { deep: true })

useEcharts('similarityRef', option)
</script>

<template>
  <div ref="similarityRef" class="h-300px w-full" />
</template>

<style scoped>
</style>
