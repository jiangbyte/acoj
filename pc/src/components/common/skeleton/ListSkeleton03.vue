<script lang="ts" setup>

</script>

<template>
  <div class="divide-y divide-gray-100 dark:divide-gray-700">
    <!-- 排行榜项 -->
    <div
      v-for="item in 3" :key="item" class="p-4 flex items-center hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
    >
      <n-skeleton class=" w-10 h-10 mr-2" />
      <div class="flex-1 flex-col gap-2">
        <n-skeleton class="w-full mb-2" />
        <n-flex align="center">
          <n-skeleton class="w-10" />
          <n-skeleton class="w-10" />
          <n-skeleton class="w-10" />
        </n-flex>
      </div>
    </div>
  </div>
</template>

<style>

</style>
