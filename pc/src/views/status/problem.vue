<script lang="ts" setup>
import { useProSubmitFetch } from '@/composables'
import { AesCrypto } from '@/utils'

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
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-card
      hoverable
      class="content-card"
      size="small"
    >
      <div class="flex items-start gap-4">
        <n-avatar
          round
          :size="64"
          :src="detailData?.userAvatar"
          class="border-2 border-gray-200 dark:border-gray-600"
        />
        <div class="flex-1">
          <div class="flex items-center gap-2">
            <n-text class="text-xl font-bold">
              {{ detailData?.userIdName }}
            </n-text>
            <n-tag size="small" round>
              {{ detailData?.statusName }}
            </n-tag>
          </div>
          <n-text class="text-gray-500 dark:text-gray-400" depth="3">
            提交了问题 {{ detailData?.problemIdName }}
          </n-text>
          <n-text class="block text-gray-400 text-sm mt-1">
            提交时间:  <n-time :time="detailData?.createTime" />
          </n-text>
          <n-text class="block text-gray-400 text-sm mt-1">
            更新时间:  <n-time :time="detailData?.updateTime" />
          </n-text>
        </div>
      </div>
    </n-card>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 基本信息 -->
      <n-card
        hoverable
        class="content-card"
        size="small"
      >
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <n-text class="text-gray-500 dark:text-gray-400">
              编程语言
            </n-text>
            <n-tag :bordered="false" type="primary">
              {{ detailData?.languageName }}
            </n-tag>
          </div>
          <div class="flex items-center justify-between">
            <n-text class="text-gray-500 dark:text-gray-400">
              提交类型
            </n-text>
            <n-tag :bordered="false" type="info">
              {{ detailData?.submitTypeName }}
            </n-tag>
          </div>
          <div class="flex items-center justify-between">
            <n-text class="text-gray-500 dark:text-gray-400">
              任务ID
            </n-text>
            <n-text>{{ detailData?.taskId || '无' }}</n-text>
          </div>
        </div>
      </n-card>

      <!-- 资源消耗 -->
      <n-card
        hoverable
        class="content-card"
        size="small"
      >
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <n-text class="text-gray-500 dark:text-gray-400">
              最大时间
            </n-text>
            <n-tag :bordered="false">
              {{ detailData?.maxTime }} ms
            </n-tag>
          </div>
          <div class="flex items-center justify-between">
            <n-text class="text-gray-500 dark:text-gray-400">
              最大内存
            </n-text>
            <n-tag :bordered="false">
              {{ detailData?.maxMemory }} KB
            </n-tag>
          </div>
          <div class="flex items-center justify-between">
            <n-text class="text-gray-500 dark:text-gray-400">
              相似检测
            </n-text>
            <n-progress
              v-if="detailData?.similarity"
              type="line"
              :percentage="detailData?.similarity"
              :height="24"
              :show-indicator="false"
              :border-radius="4"
              :fill-border-radius="0"
            >
              <span class="text-xs">{{ detailData?.similarity }}%</span>
            </n-progress>
            <n-text v-else>
              未检测
            </n-text>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 执行消息 -->
    <n-card
      v-if="detailData?.message"
      hoverable
      class="content-card"
      size="small"
    >
      <template #header>
        <n-text>
          执行消息
        </n-text>
      </template>
      <n-text depth="3">
        {{ detailData.message }}
      </n-text>
    </n-card>
    <!-- 测试用例结果 -->
    <n-card
      v-if="detailData?.testCase?.length > 0"
      hoverable
      class="content-card"
      size="small"
    >
      <template #header>
        <n-text>
          测试用例详情
        </n-text>
      </template>
      <n-collapse arrow-placement="right" class="test-case-collapse">
        <n-collapse-item
          v-for="(testCase, index) in detailData.testCase"
          :key="index"
          :name="index"
          :title="`测试用例 #${index + 1}`"
        >
          <template #header-extra>
            <n-tag size="small">
              {{ testCase.status ? testCase.status : '未运行' }}
            </n-tag>
          </template>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <!-- 输入 -->
            <div>
              <n-text class="text-gray-500 dark:text-gray-400 text-sm">
                输入
              </n-text>
              <div class="p-2 mt-1 bg-gray-50 dark:bg-gray-800 rounded">
                <pre class="text-sm whitespace-pre-wrap">{{ testCase.input }}</pre>
              </div>
            </div>

            <!-- 输出 -->
            <div>
              <n-text class="text-gray-500 dark:text-gray-400 text-sm">
                输出
              </n-text>
              <div class="p-2 mt-1 bg-gray-50 dark:bg-gray-800 rounded">
                <pre class="text-sm whitespace-pre-wrap">{{ testCase.output }}</pre>
              </div>
            </div>

            <!-- 预期输出 -->
            <div>
              <n-text class="text-gray-500 dark:text-gray-400 text-sm">
                预期输出
              </n-text>
              <div class="p-2 mt-1 bg-gray-50 dark:bg-gray-800 rounded">
                <pre class="text-sm whitespace-pre-wrap">{{ testCase.except }}</pre>
              </div>
            </div>

            <!-- 执行详情 -->
            <div>
              <n-text class="text-gray-500 dark:text-gray-400 text-sm">
                执行详情
              </n-text>
              <div class="p-2 mt-1 bg-gray-50 dark:bg-gray-800 rounded">
                <div class="flex justify-between text-sm">
                  <span>时间: {{ testCase.maxTime }} ms</span>
                  <span>内存: {{ testCase.maxMemory }} KB</span>
                </div>
                <div v-if="testCase.message" class="mt-1">
                  <n-text class="text-sm whitespace-pre-wrap">
                    {{ testCase.message }}
                  </n-text>
                </div>
              </div>
            </div>
          </div>
        </n-collapse-item>
      </n-collapse>
    </n-card>

    <!-- 代码展示 -->
    <n-card
      hoverable
      class="content-card"
      size="small"
    >
      <template #header>
        <div class="flex justify-between items-center">
          <n-text>
            提交代码
          </n-text>
          <n-tag :bordered="false" type="primary">
            {{ detailData?.languageName }}
          </n-tag>
        </div>
      </template>
      <n-code
        :code="detailData?.code"
        :language="detailData?.language.toLowerCase()"
        show-line-numbers
        class="rounded-md"
      />
    </n-card>
  </div>
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
