<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <n-card size="small" hoverable>
    <n-flex align="center">
      <n-avatar
        round
        :size="64"
        :src="resultTaskData?.userAvatar"
        class="border-2 border-gray-200 dark:border-gray-600"
      />
      <n-flex vertical :size="6">
        <div class="flex items-center gap-2">
          <n-text class="text-xl font-bold">
            <n-ellipsis style="width: 90px">
              {{ resultTaskData?.userIdName || '未知用户' }}
            </n-ellipsis>
          </n-text>
          <n-tag
            size="small" :type="resultTaskData?.status === 'COMPILATION_ERROR'
              || resultTaskData?.status === 'RUNTIME_ERROR'
              || resultTaskData?.status === 'TIME_LIMIT_EXCEEDED'
              || resultTaskData?.status === 'MEMORY_LIMIT_EXCEEDED'
              || resultTaskData?.status === 'WRONG_ANSWER'
              || resultTaskData?.status === 'SYSTEM_ERROR'
              || resultTaskData?.status === 'MEMORY_LIMIT_EXCEEDED'
              ? 'error'
              : 'success'"
          >
            {{ resultTaskData?.statusName || '未知状态' }}
          </n-tag>
        </div>
        <n-flex vertical :size="6">
          <n-text class="text-gray-500 dark:text-gray-400" depth="3">
            提交了题目 <n-tag size="small">
              <n-ellipsis style="width: 90px">
                {{ resultTaskData?.problemIdName || '未知题目' }}
              </n-ellipsis>
            </n-tag>
          </n-text>
          <n-flex align="center">
            <n-text class="text-gray-500 dark:text-gray-400" depth="3">
              使用语言 <n-tag size="small">
                {{ resultTaskData?.languageName || '未知语言' }}
              </n-tag>
            </n-text>
            <n-text class="text-gray-500 dark:text-gray-400" depth="3">
              提交类型 <n-tag size="small" :type="resultTaskData?.submitType ? 'info' : 'warning'">
                {{ resultTaskData?.submitTypeName || '未知类型' }}
              </n-tag>
            </n-text>
          </n-flex>
        </n-flex>
      </n-flex>
    </n-flex>
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
