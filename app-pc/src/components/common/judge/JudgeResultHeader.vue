<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <n-card size="small" hoverable>
    <n-flex align="center">
      <SafeAvatar
        round
        :size="64"
        :src="resultTaskData?.userAvatar"
        class="border-2 border-gray-200 dark:border-gray-600"
      />
      <n-flex vertical :size="6">
        <div class="flex items-center gap-2">
          <n-text class="text-xl font-bold">
            <n-ellipsis style="width: 90px">
              {{ resultTaskData?.userIdName || 'Unknown User' }}
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
            {{ resultTaskData?.statusName || 'Unknown Status' }}
          </n-tag>
        </div>
        <n-flex vertical :size="6">
          <n-text class="text-gray-500 dark:text-gray-400" depth="3">
            submitted <n-tag size="small">
              <n-ellipsis style="width: 90px">
                {{ resultTaskData?.problemIdName || 'Unknown Problem' }}
              </n-ellipsis>
            </n-tag>
          </n-text>
          <n-flex align="center">
            <n-text class="text-gray-500 dark:text-gray-400" depth="3">
              Language <n-tag size="small">
                {{ resultTaskData?.languageName || 'Unknown Language' }}
              </n-tag>
            </n-text>
            <n-text class="text-gray-500 dark:text-gray-400" depth="3">
              Submission Type <n-tag size="small" :type="resultTaskData?.submitType ? 'info' : 'warning'">
                {{ resultTaskData?.submitTypeName || 'Unknown Type' }}
              </n-tag>
            </n-text>
          </n-flex>
        </n-flex>
      </n-flex>
    </n-flex>
    <template #footer>
      <n-flex align="center" justify="space-between">
        <n-text class="block text-gray-400 text-sm mt-1">
          Submitted: <n-time :time="resultTaskData?.createTime" />
        </n-text>
        <!-- <n-text class="block text-gray-400 text-sm mt-1">
          Updated: <n-time :time="resultTaskData?.updateTime" />
        </n-text> -->
      </n-flex>
    </template>
  </n-card>
</template>
