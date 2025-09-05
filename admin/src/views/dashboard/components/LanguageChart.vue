<script setup lang="ts">
import { useEcharts } from '@/hooks'
import type { ECOption } from '@/hooks'
import { useAnalyseFetch } from '@/composables'

const { getLanguageDistribution } = useAnalyseFetch()

const option = ref<ECOption>({
  tooltip: {
    trigger: 'item',
    formatter: '{b} : {d}%',
  },
  legend: {
    orient: 'horizontal',
    top: 30,
    padding: 5,
    itemWidth: 20,
    itemHeight: 12,
    textStyle: {
      color: '#777',
    },
  },
  series: [{
    type: 'pie',
    radius: ['45%', '60%'],
    center: ['50%', '50%'],
    label: {
      show: true,
      formatter: '{b} : {d}%',
      color: '#777',
    },
    labelLine: {
      show: true,
      length2: 10,
    },
    data: [
    ],
  }],
}) as Ref<ECOption>
useEcharts('lineRef', option)

async function loadData() {
  const { data } = await getLanguageDistribution()
  option.value = {
    ...option.value,
    series: [{
      data,
    }],
  }
}

loadData()
</script>

<template>
  <div ref="lineRef" class="h-400px" />
</template>

<style scoped></style>
