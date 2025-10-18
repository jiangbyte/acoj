<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <div class="flex items-center gap-4">
    <n-avatar
      round
      :size="64"
      :src="resultTaskData?.userAvatar"
      class="border-2 border-gray-200 dark:border-gray-600"
    />
    <div class="flex-1">
      <div class="flex items-center gap-2">
        <n-text class="text-xl font-bold">
          {{ resultTaskData?.userIdName }}
        </n-text>
        <n-tag size="small" round>
          {{ resultTaskData?.statusName }}
        </n-tag>
      </div>
      <div>
        <n-text class="text-gray-500 dark:text-gray-400" depth="3">
          提交了题目 {{ resultTaskData?.problemIdName }}
        </n-text>
        <n-flex align="center">
          <n-text class="block text-gray-400 text-sm mt-1">
            提交时间: <n-time :time="resultTaskData?.createTime" />
          </n-text>
        </n-flex>
        <n-flex align="center">
          <n-text class="block text-gray-400 text-sm mt-1">
            更新时间: <n-time :time="resultTaskData?.updateTime" />
          </n-text>
        </n-flex>
      </div>
    </div>
  </div>
</template>
