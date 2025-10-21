<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import CodeEditor from '@/components/common/editor/code/CodeEditor.vue'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.submit as string)

async function loadData() {
  try {
    const { dataSubmitDetail } = useDataSubmitFetch()
    const { data } = await dataSubmitDetail({ id: originalId })

    if (data) {
      detailData.value = data
    }
  }
  catch {
    // 错误处理
  }
}

loadData()
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 提交状态概览 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 mb-4">
      <JudgeResultHeader :result-task-data="detailData" class="mb-4"/>

      <!-- 判题结果详情 -->
      <JudgeResultStats :result-task-data="detailData" class="w-full" />
      <n-space v-if="detailData?.submitType" vertical class="w-full mt-4" :size="16">
        <n-space align="center" justify="space-between">
          <n-text>代码相似度</n-text>
          <n-space align="center">
            <n-tag v-if="detailData?.similarityCategoryName" type="info">
              {{ detailData?.similarityCategoryName }}
            </n-tag>
            <n-tag type="info">
              {{ ((detailData?.similarity || 0) * 100).toFixed(2) }} %
            </n-tag>
          </n-space>
        </n-space>
        <n-progress
          type="line"
          :show-indicator="false"
          :percentage="(detailData?.similarity || 0) * 100"
        />
        <n-alert type="info" show-icon>
          代码克隆检测用于辅助判断代码相似度，结果仅供参考
        </n-alert>
      </n-space>
    </div>

    <div v-if="detailData?.message" class="bg-white mb-4 dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden h-full flex flex-col">
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
        <h2 class="text-lg font-bold">
          错误信息
        </h2>
      </div>
      <div class="flex-1 p-x-6 max-h-[250px] overflow-y-scroll pb-4">
        <n-code :language="detailData?.language" :code="detailData?.message" show-line-numbers word-wrap />
      </div>
    </div>

    <!-- 提交的代码 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden h-full flex flex-col">
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
        <h2 class="text-lg font-bold">
          提交的代码
        </h2>
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-500 dark:text-gray-400">{{ detailData?.languageName }}</span>
        </div>
      </div>
      <div class="flex-1">
        <CodeEditor
          :model-value="detailData?.code"
          :language="detailData?.language"
          width="100%"
          height="400px"
          :options="{
            readOnly: true,
          }"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
