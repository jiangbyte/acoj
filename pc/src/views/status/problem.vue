<script lang="ts" setup>
import { useProSubmitFetch } from '@/composables'
import { AesCrypto } from '@/utils'
import CodeEditor from '@/components/common/editor/code/Editor.vue'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.submit as string)
async function loadData() {
  try {
    const { proSubmitDetail } = useProSubmitFetch()
    const { data } = await proSubmitDetail({ id: originalId })

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
    <!-- 提交状态概览 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 mb-8">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-bold mb-2">
            {{ detailData?.problemIdName }}
          </h1>
          <div class="flex flex-wrap items-center gap-3 text-sm">
            <span>提交时间:  <n-time :time="detailData?.createTime" /></span>
            <span>语言:  {{ detailData?.languageName }}</span>
            <span>耗时: {{ detailData?.maxTime }} ms</span>
            <span>内存: {{ detailData?.maxMemory }} KB</span>
          </div>
        </div>

        <div class="flex items-center">
          <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 mr-4">
            {{ detailData?.status }}
          </span>
        </div>
      </div>

      <!-- 判题结果统计 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            测试用例通过
          </div>
          <div class="text-2xl font-bold text-green-600 dark:text-green-400">
            10/10
          </div>
        </div>
        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            代码长度
          </div>
          <div class="text-2xl font-bold">
            856 B
          </div>
        </div>
        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            运行时间
          </div>
          <div class="text-2xl font-bold">
            {{ detailData?.maxTime }} ms
          </div>
        </div>
        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            内存消耗
          </div>
          <div class="text-2xl font-bold">
            {{ detailData?.maxMemory }} KB
          </div>
        </div>
      </div>
    </div>

    <!-- 代码克隆检测结果 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 mb-8">
      <h2 class="text-xl font-bold mb-4 flex items-center">
        <i class="fa fa-clone text-purple-500 mr-2" /> 代码克隆检测
      </h2>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="col-span-1 md:col-span-2">
          <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
            <div class="flex justify-between items-center mb-2">
              <span class="font-medium">代码相似度</span>
              <n-tag size="small" :bordered="false" type="info">
                28%
              </n-tag>
            </div>
            <n-progress
              type="line"
              :show-indicator="false"
              :percentage="20"
            />

            <div class="mt-2 text-sm text-gray-500 dark:text-gray-400">
              您的代码与平台上已有代码存在28%的相似度，未达到抄袭阈值(50%)
            </div>
          </div>
        </div>

        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="flex justify-between items-center mb-2">
            <span class="font-medium">检测结果</span>
            <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">
              未发现抄袭
            </span>
          </div>
          <div class="text-sm text-gray-600 dark:text-gray-300">
            代码通过克隆检测，未发现高度相似的已有提交
          </div>
        </div>
      </div>

      <div class="text-sm text-gray-500 dark:text-gray-400 italic">
        <i class="fa fa-info-circle mr-1" /> 代码克隆检测用于辅助判断代码相似度，结果仅供参考，最终判定由人工审核决定
      </div>
    </div>

    <!-- 代码和测试用例结果 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
      <!-- 提交的代码 -->
      <div class="lg:col-span-2">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
            <h2 class="text-lg font-bold">
              提交的代码
            </h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">{{ detailData?.language }}</span>
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

      <!-- 测试用例结果 -->
      <div>
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-lg font-bold">
              测试用例结果
            </h2>
          </div>
          <div class="divide-y divide-gray-200 dark:divide-gray-700 max-h-[500px] overflow-y-auto">
            <!-- 测试用例1 -->
            <div
              v-for="(testCase, index) in detailData?.testCase" :key="index"
              class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors"
            >
              <div class="flex justify-between items-center mb-2">
                <span class="font-medium">测试用例 #{{ index + 1 }}</span>
                <span class="text-green-600 dark:text-green-400 text-sm">{{ testCase.status }}</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mb-1">
                输入: {{ testCase.input }}
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mb-1">
                输出:   {{ testCase.output }}
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400">
                预期: {{ testCase.except }}
              </div>
              <div class="flex justify-end mt-2 text-xs text-gray-500 dark:text-gray-400">
                <span>耗时: {{ testCase.maxTime }} ms</span>
                <span class="mx-2">|</span>
                <span>内存: {{ testCase.maxMemory ? testCase.maxMemory : 0 }} KB</span>
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
