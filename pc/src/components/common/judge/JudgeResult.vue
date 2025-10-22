<script lang="ts" setup>
interface Props {
  isPolling: boolean
  pollingCount: number
  maxPollingCount: number
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <div>
    <n-alert v-if="isPolling" type="warning" :show-icon="false">
      <n-flex align="center" justify="center">
        <n-spin size="small" />
        <n-text type="info">
          判题中... ({{ pollingCount }}/{{ maxPollingCount }})
        </n-text>
      </n-flex>
    </n-alert>

    <n-card v-if="isPolling || resultTaskData.id" class="w-full max-w-3xl mx-auto" :bordered="false" size="small">
      <n-space vertical :size="16">
        <!-- 头部信息 -->
        <JudgeResultHeader :result-task-data="resultTaskData" />

        <!-- 判题结果详情 -->
        <JudgeResultStats :result-task-data="resultTaskData" />

        <!-- 代码相似度 -->
        <SimilarityReport v-if="resultTaskData.submitType" :result-task-data="resultTaskData" />

        <n-card v-if="resultTaskData?.message" size="small" hoverable>
          <n-code :language="resultTaskData?.language" :code="resultTaskData?.message" show-line-numbers word-wrap />
        </n-card>
        <!-- 错误信息 -->
        <!-- <div class="lg:col-span-2 w-full">
          <div class="bg-white dark:bg-gray-800 rounded-md shadow-sm overflow-hidden w-full">
            <CodeEditor
              v-if="resultTaskData?.message"
              :model-value="resultTaskData?.message"
              width="100%"
              class="flex-1"
              height="400px"
              :options="{
                readOnly: true,
              }"
            />
          </div>
        </div> -->
      </n-space>
    </n-card>

    <n-empty v-else class="flex flex-col items-center justify-center py-12" description="暂无判题结果">
      <template #icon>
        <n-icon size="40" class="text-gray-300 dark:text-gray-600">
          <icon-park-outline-info />
        </n-icon>
      </template>
      <n-text depth="3" class="text-center max-w-xs">
        您还没有提交过代码，或者找不到对应的判题结果
      </n-text>
    </n-empty>
  </div>
</template>
