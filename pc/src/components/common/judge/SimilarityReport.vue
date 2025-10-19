<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl">
    <div class="grid grid-cols-1 md:grid-cols-1 gap-6">
      <div class="col-span-1 md:col-span-1">
        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="flex justify-between items-center mb-2">
            <span class="font-medium">代码相似度</span>
            <n-tag size="small" :bordered="false" type="info">
              {{ (resultTaskData?.similarity || 0) * 100 }} %
            </n-tag>
          </div>
          <n-progress
            type="line"
            :show-indicator="false"
            :percentage="(resultTaskData?.similarity || 0) * 100"
          />

          <div class="mt-2 text-sm text-gray-500 dark:text-gray-400">
            您的代码与平台上已有代码存在{{ (resultTaskData?.similarity || 0) * 100 }}%的相似度，{{ resultTaskData?.similarityCategoryName }}
          </div>
          <div class="text-sm text-gray-500 dark:text-gray-400 italic">
            注：代码克隆检测用于辅助判断代码相似度，结果仅供参考
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
