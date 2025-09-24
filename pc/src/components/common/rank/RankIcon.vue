<script lang="ts" setup>
import { RankColorUtil } from '@/utils'

// 定义 props
const props = defineProps({
  rank: {
    type: [Number, String],
    required: true,
  },
})
</script>

<template>
  <div
    class="w-9 h-9 rounded-full text-white flex items-center justify-center mr-3 text-1.1rem"
    :style="{ backgroundColor: RankColorUtil.getColor(props.rank) }"
  >
    {{ RankColorUtil.getDisplayTextAPE(props.rank) }}
  </div>
</template>

<style scoped>

</style>
