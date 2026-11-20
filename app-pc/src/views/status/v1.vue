<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { AesCrypto, StatusUtils } from '@/utils'
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
  }
}
loadData()
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- Section -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 mb-8">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <n-flex>
            <NTag :type="detailData?.submitType ? 'info' : 'warning'">
              {{ detailData?.submitTypeName }}
            </NTag>
            <h1 class="text-2xl font-bold mb-2">
              {{ detailData?.problemIdName }}
            </h1>
          </n-flex>
          <div class="flex flex-wrap items-center gap-3 text-sm">
            <span>Submitted  <n-time :time="detailData?.createTime" /></span>
            <span>Language
              <NTag
                class="ml-1"
                size="small"
              >
                {{ detailData?.languageName }}
              </NTag>
            </span>
            <span>Max Time
              <NTag
                class="ml-1"
                size="small"
              >
                {{ detailData?.maxTime }}
              </NTag> ms</span>
            <span>Max Memory
              <NTag
                class="ml-1"
                size="small"
              >
                {{ detailData?.maxMemory }}
              </NTag> KB
            </span>
          </div>
        </div>

        <div class="flex items-center">
          <NTag>
            {{ detailData?.statusName }}
          </NTag>
        </div>
      </div>

      <!-- Section -->
      <JudgeResultStats :result-task-data="detailData" class="w-full" />
    </div>

    <!-- Error Details -->
    <n-card v-if="detailData?.message" size="small" hoverable>
      <n-code :language="detailData?.language" :code="detailData?.message" show-line-numbers word-wrap />
    </n-card>

    <!-- Section -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
      <!-- Submitted Code -->
      <div class="lg:col-span-2">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
            <h2 class="text-lg font-bold">
              Submitted Code
            </h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">{{ detailData?.languageName }}</span>
            </div>
          </div>
          <CodeEditor
            :model-value="detailData?.code"
            :language="detailData?.language"
            width="100%"
            height="500px"
            :options="{
              readOnly: true,
            }"
          />
        </div>
      </div>

      <!-- Section -->
      <div v-if="detailData?.submitType" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 mb-8">
        <h2 class="text-xl font-bold mb-4 flex items-center">
          <i class="fa fa-clone text-purple-500 mr-2" /> Code Clone Detection
        </h2>

        <n-space vertical class="w-full" :size="16">
          <n-space align="center" justify="space-between">
            <n-text>
              Code Similarity
            </n-text>
            <n-space align="center">
              <n-tag v-if="detailData?.similarityCategoryName" type="info">
                {{ detailData?.similarityCategoryName }}
              </n-tag>
              <n-tag type="info">
                {{ (detailData?.similarity || 0) * 100 }} %
              </n-tag>
            </n-space>
          </n-space>
          <n-progress
            type="line"
            :show-indicator="false"
            :percentage="(detailData?.similarity || 0) * 100"
          />
          <n-alert type="info" show-icon>
            Clone detection estimates code similarity and is for reference only
          </n-alert>
        </n-space>
      </div>

      <!-- Test Case Results -->
      <div>
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-lg font-bold">
              Test Case Results
            </h2>
          </div>
          <div class="divide-y divide-gray-200 dark:divide-gray-700 max-h-[500px] overflow-y-auto">
            <!-- Test Case1 -->
            <div
              v-for="(testCase, index) in detailData?.testCase" :key="index"
              class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors"
            >
              <div class="flex justify-between items-center mb-2">
                <span class="font-medium">Test Case <NTag
                  class="ml-1"
                  size="small"
                >
                  #{{ index + 1 }}
                </NTag></span>
                <!-- <span class="text-green-600 dark:text-green-400 text-sm">{{ testCase.status }}</span> -->
                <NTag>
                  {{ StatusUtils.getStatusText(testCase.status) }}
                </NTag>
              </div>
              <!--              <div class="text-xs text-gray-500 dark:text-gray-400 mb-1"> -->
              <!--                Input: {{ testCase.input }} -->
              <!--              </div> -->
              <!--              <div class="text-xs text-gray-500 dark:text-gray-400 mb-1"> -->
              <!--                Output:   {{ testCase.output }} -->
              <!--              </div> -->
              <!--              <div class="text-xs text-gray-500 dark:text-gray-400"> -->
              <!--                Expected: {{ testCase.except }} -->
              <!--              </div> -->
              <div class="flex justify-end mt-2 text-xs text-gray-500 dark:text-gray-400">
                <span>Time
                  <NTag
                    class="ml-1"
                    size="small"
                  >
                    {{ testCase.maxTime ? testCase.maxTime : 0 }}
                  </NTag> ms</span>
                <span class="mx-1" />
                <span>Memory
                  <NTag
                    class="ml-1"
                    size="small"
                  >
                    {{ testCase.maxMemory ? testCase.maxMemory : 0 }}
                  </NTag> KB
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.test-case-collapse {
  --n-title-font-size: 14px;
}

.test-case-collapse :deep(.n-collapse-item__header) {
  padding: 8px 12px;
}

.test-case-collapse :deep(.n-collapse-item__content-inner) {
  padding: 12px;
  padding-top: 8px;
}
</style>
