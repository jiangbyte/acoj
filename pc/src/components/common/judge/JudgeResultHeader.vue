<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <n-card size="small" hoverable>
    <div class="flex items-center gap-4">
      <n-avatar
        round
        :size="64"
        :src="resultTaskData?.userAvatar"
        class="border-2 border-gray-200 dark:border-gray-600"
      />
      <n-flex vertical :size="6">
        <div class="flex items-center gap-2">
          <n-text class="text-xl font-bold">
            {{ resultTaskData?.userIdName || '未知用户' }}
          </n-text>
          <n-tag size="small" type="info">
            {{ resultTaskData?.statusName || '未知状态' }}
          </n-tag>
        </div>
        <n-flex vertical :size="6">
          <n-text class="text-gray-500 dark:text-gray-400" depth="3">
            提交了题目 <n-tag size="small">{{ resultTaskData?.problemIdName || '未知题目' }}</n-tag>
          </n-text>
          <n-flex align="center">
            <n-text class="text-gray-500 dark:text-gray-400" depth="3">
              使用语言 <n-tag size="small">{{ resultTaskData?.languageName || '未知语言' }}</n-tag>
            </n-text>
            <n-text class="text-gray-500 dark:text-gray-400" depth="3">
              提交类型 <n-tag size="small">{{ resultTaskData?.submitTypeName || '未知类型'  }}</n-tag>
            </n-text>
          </n-flex>
        </n-flex>
      </n-flex>
    </div>
    <template #footer>
      <n-flex align="center" justify="space-between">
        <n-text class="block text-gray-400 text-sm mt-1">
          提交时间: <n-time :time="resultTaskData?.createTime" />
        </n-text>
        <!-- <n-text class="block text-gray-400 text-sm mt-1">
          更新时间: <n-time :time="resultTaskData?.updateTime" />
        </n-text> -->
      </n-flex>
    </template>
  </n-card>
</template>
