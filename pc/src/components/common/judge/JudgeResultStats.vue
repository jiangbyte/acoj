<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
    <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
      <div class="text-sm text-gray-500 dark:text-gray-400">
        代码长度
      </div>
      <div class="text-2xl font-bold">
        {{ resultTaskData?.codeLength ? resultTaskData?.codeLength : 0 }} B
      </div>
    </div>
    <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
      <div class="text-sm text-gray-500 dark:text-gray-400">
        运行时间
      </div>
      <div class="text-2xl font-bold">
        {{ resultTaskData?.maxTime }} ms
      </div>
    </div>
    <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
      <div class="text-sm text-gray-500 dark:text-gray-400">
        内存消耗
      </div>
      <div class="text-2xl font-bold">
        {{ resultTaskData?.maxMemory }} KB
      </div>
    </div>
  </div>
</template>
