<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
    <n-card size="small" hoverable>
      <n-text>Size (B)</n-text>
      <div class="text-xl font-bold">
        {{ resultTaskData?.codeLength ? resultTaskData?.codeLength : 0 }}
      </div>
    </n-card>
    <n-card size="small" hoverable>
      <n-text>Time (ms)</n-text>
      <div class="text-xl font-bold">
        {{ resultTaskData?.maxTime ? resultTaskData?.maxTime : 0 }}
      </div>
    </n-card>
    <n-card size="small" hoverable>
      <n-text>Memory (KB)</n-text>
      <div class="text-xl font-bold">
        {{ resultTaskData?.maxMemory ? resultTaskData?.maxMemory : 0 }}
      </div>
    </n-card>
  </div>
</template>
