<script lang="ts" setup>
import CodeEditor from '@/components/common/editor/code/Editor.vue'
import MdViewer from '@/components/common/editor/md/Viewer.vue'
import {
  BarChartOutline,
  HardwareChipOutline,
  TimeOutline,
} from '@vicons/ionicons5'
import { useProProblemFetch, useProSubmitFetch } from '@/composables'
import { AesCrypto } from '@/utils'

const route = useRoute()
const activeTab = ref('description')
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.problem as string)
async function loadData() {
  try {
    const { proProblemDetail } = useProProblemFetch()
    const { data } = await proProblemDetail({ id: originalId })

    if (data) {
      detailData.value = data
    }
  }
  catch {
  }
}
loadData()

const submitParam = ref({
  problemId: originalId,
  language: null,
  code: '',
  submitType: null,
})
// 获取语言选项
const languageOptions = computed(() => {
  if (!detailData.value?.codeTemplate)
    return []
  return detailData.value.allowedLanguages.map((language: any) => ({
    label: language.toUpperCase(),
    value: language,
  }))
})

// 语言切换处理
function handleLanguageChange(lang: string) {
  if (!detailData.value?.codeTemplate)
    return
  const selectedTemplate = detailData.value.codeTemplate.find((t: { language: string }) => t.language === lang)
  if (selectedTemplate) {
    submitParam.value.code = selectedTemplate.template
    window.$message.success(`已切换至 ${lang.toUpperCase()} 模板`)
  }
  else {
    submitParam.value.code = ''
    window.$message.warning('当前语言没有模板代码')
  }
}
const { proSubmitExecute, proSubmitDetail } = useProSubmitFetch()
const executeTaskId = ref(null)
async function execute(type: boolean) {
  if (!submitParam.value.language) {
    window.$message.warning('请选择语言')
    return
  }
  if (!submitParam.value.code) {
    window.$message.warning('请输入代码')
    return
  }
  if (type) {
    submitParam.value.submitType = true as any
    const { data } = await proSubmitExecute(submitParam.value)
    if (data) {
      executeTaskId.value = data
      startPolling() // 开始轮询
      window.$message.success('提交成功')
    }
    activeTab.value = 'result'
  }
  else {
    submitParam.value.submitType = false as any
    const { data } = await proSubmitExecute(submitParam.value)
    if (data) {
      executeTaskId.value = data
      startPolling() // 开始轮询
      window.$message.success('提交成功')
    }
    activeTab.value = 'result'
  }
}
// =============== 结果轮询 开始 =================
const resultTaskData = ref()
const pollingInterval = ref<NodeJS.Timeout | null>(null)
const maxPollingAttempts = 30 // 最大轮询次数
const pollingIntervalTime = 1000 // 轮询间隔时间(毫秒)
let pollingAttempts = 0

// 清除轮询
function clearPolling() {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
  pollingAttempts = 0
}

// 获取结果详情
async function refreshResult() {
  try {
    const { data } = await proSubmitDetail({ id: executeTaskId.value })
    if (data) {
      resultTaskData.value = data
      // 如果任务完成，停止轮询
      if (data.status !== 'PENDING' && data.status !== 'JUDGING') {
        clearPolling()
        // 根据结果显示不同消息
        if (data.status === 'ACCEPTED') {
          window.$message.success('提交通过!')
        }
        else if (data.status === 'COMPILATION_ERROR') {
          window.$message.error('编译错误')
        }
        else if (data.status === 'RUNTIME_ERROR') {
          window.$message.error('运行时错误')
        }
        else if (data.status === 'TIME_LIMIT_EXCEEDED') {
          window.$message.error('时间超出限制')
        }
        else if (data.status === 'MEMORY_LIMIT_EXCEEDED') {
          window.$message.error('内存超出限制')
        }
        else if (data.status === 'WRONG_ANSWER') {
          window.$message.error('答案错误')
        }
      }
    }
  }
  catch (error) {
    clearPolling()
    window.$message.error('获取结果失败')
  }
}

// 开始轮询
function startPolling() {
  clearPolling() // 先清除已有的轮询
  // 立即获取一次结果
  refreshResult()
  // 设置定时轮询
  pollingInterval.value = setInterval(() => {
    pollingAttempts++
    if (pollingAttempts >= maxPollingAttempts) {
      clearPolling()
      window.$message.warning('获取结果超时，请手动刷新')
      return
    }
    refreshResult()
  }, pollingIntervalTime)
}

// 在组件卸载时清除轮询
onUnmounted(() => {
  clearPolling()
})
// =============== 结果轮询 结束 =================
const isMobile = ref(false)
function checkIfMobile() {
  const userAgent = navigator.userAgent.toLowerCase()
  const mobileKeywords = ['mobile', 'android', 'iphone', 'ipad', 'ipod', 'windows phone']
  isMobile.value = mobileKeywords.some(keyword => userAgent.includes(keyword)) || window.innerWidth < 768
}
function handleResize() {
  checkIfMobile()
}
onMounted(() => {
  window.addEventListener('resize', handleResize)
  checkIfMobile()
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <!-- 移动端只显示题目内容 -->
  <template v-if="isMobile">
    <NCard class="h-full" :bordered="false">
      <NFlex vertical :size="16">
        <NTabs v-model:value="activeTab" type="line" animated>
          <NTabPane name="description" tab="题目描述">
            <n-space vertical>
              <n-space align="center">
                <n-tag round size="small">
                  {{ detailData?.categoryName }}
                </n-tag>
                <n-tag round size="small">
                  {{ detailData?.difficultyName }}
                </n-tag>
                <n-h3 style="margin: 0;">
                  {{ detailData?.title }}
                </n-h3>
              </n-space>

              <n-space align="center">
                <NTag
                  v-for="tag in detailData?.tagNames"
                  :key="tag"
                  size="small"
                  :bordered="false"
                  round
                  :color="{ color: '#E6F2FF', textColor: '#176DF2', borderColor: '#176DF2' }"
                >
                  {{ tag }}
                </NTag>
              </n-space>
              <n-flex>
                <NFlex align="center" :size="8">
                  <NIcon :component="TimeOutline" size="18" color="#176DF2" />
                  <NText depth="3">
                    时间限制: {{ detailData?.maxTime }} ms
                  </NText>
                </NFlex>
                <NFlex align="center" :size="8">
                  <NIcon :component="HardwareChipOutline" size="18" color="#176DF2" />
                  <NText depth="3">
                    内存限制: {{ detailData?.maxMemory }} KB
                  </NText>
                </NFlex>
                <NFlex align="center" :size="8">
                  <NIcon :component="BarChartOutline" size="18" color="#176DF2" />
                  <NText depth="3">
                    通过率: {{ detailData?.acceptance }} %
                  </NText>
                </NFlex>
              </n-flex>
              <MdViewer :model-value="detailData?.description" />
            </n-space>
          </NTabPane>
          <NTabPane name="submissions" tab="提交历史">
            <ProblemUserSubmit :problem="originalId" />
          </NTabPane>
        </NTabs>
      </NFlex>
    </NCard>
  </template>
  <template v-else>
    <!-- 题目内容与提交区域 -->
    <NSplit
      direction="horizontal"
      :max="0.7"
      :min="0.3"
      :default-size="0.5"
      class="max-w-full p-0 mt-18 h-[calc(100vh-72px)]"
    >
      <!-- 左侧题目内容区域 -->
      <template #1>
        <NCard
          class="h-full flex flex-col"
          :bordered="false"
        >
          <NFlex
            vertical
            :size="16"
          >
            <!-- 题目相关内容的标签页 -->
            <NTabs
              v-model:value="activeTab"
              type="line"
              animated
            >
              <NTabPane
                name="description"
                tab="题目描述"
              >
                <n-space
                  vertical
                >
                  <n-space
                    align="center"
                  >
                    <n-tag
                      round
                      size="small"
                    >
                      {{ detailData?.categoryName }}
                    </n-tag>
                    <n-tag
                      round
                      size="small"
                    >
                      {{ detailData?.difficultyName }}
                    </n-tag>
                    <n-h3 style="margin: 0;">
                      {{ detailData?.title }}
                    </n-h3>
                  </n-space>

                  <n-space align="center">
                    <NTag
                      v-for="tag in detailData?.tagNames"
                      :key="tag"
                      size="small"
                      :bordered="false"
                      round
                      :color="{ color: '#E6F2FF', textColor: '#176DF2', borderColor: '#176DF2' }"
                    >
                      {{ tag }}
                    </NTag>
                  </n-space>
                  <n-flex>
                    <NFlex
                      align="center"
                      :size="8"
                    >
                      <NIcon
                        :component="TimeOutline"
                        size="18"
                        color="#176DF2"
                      />
                      <NText depth="3">
                        时间限制: {{ detailData?.maxTime }} ms
                      </NText>
                    </NFlex>
                    <NFlex
                      align="center"
                      :size="8"
                    >
                      <NIcon
                        :component="HardwareChipOutline"
                        size="18"
                        color="#176DF2"
                      />
                      <NText depth="3">
                        内存限制: {{ detailData?.maxMemory }} KB
                      </NText>
                    </NFlex>
                    <NFlex
                      align="center"
                      :size="8"
                    >
                      <NIcon
                        :component="BarChartOutline"
                        size="18"
                        color="#176DF2"
                      />
                      <NText depth="3">
                        通过率: {{ detailData?.acceptance }} %
                      </NText>
                    </NFlex>
                  </n-flex>
                  <MdViewer :model-value="detailData?.description" />
                </n-space>
              </NTabPane>
              <NTabPane
                name="submissions"
                tab="提交历史"
              >
                <ProblemUserSubmit :problem="originalId" />
              </NTabPane>
              <NTabPane
                name="result"
                tab="判题结果"
              >
                <n-card v-if="resultTaskData" class="w-full max-w-3xl mx-auto" :bordered="false" size="large">
                  <!-- 头部信息 -->
                  <div class="flex items-start gap-4">
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
                      <n-text class="text-gray-500 dark:text-gray-400" depth="3">
                        提交了问题 {{ resultTaskData?.problemIdName }}
                      </n-text>
                      <n-flex align="center">
                        <n-text class="block text-gray-400 text-sm mt-1">
                          提交:  <n-time :time="detailData?.createTime" />
                        </n-text>
                      </n-flex>
                    </div>
                  </div>
                  <!-- 判题结果详情 -->
                  <n-divider class="!my-4" />
                  <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
                    <!-- <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            测试用例通过
          </div>
          <div class="text-2xl font-bold text-green-600 dark:text-green-400">
            10/10
          </div>
        </div> -->

                    <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
                      <div class="text-sm text-gray-500 dark:text-gray-400">
                        代码长度
                      </div>
                      <div class="text-2xl font-bold">
                        {{ resultTaskData?.codeLength ? resultTaskData?.codeLength : 0 }} B
                      </div>
                    </div>
                    <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
                      <div class="text-sm text-gray-500 dark:text-gray-400">
                        运行时间
                      </div>
                      <div class="text-2xl font-bold">
                        {{ resultTaskData?.maxTime }} ms
                      </div>
                    </div>
                    <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
                      <div class="text-sm text-gray-500 dark:text-gray-400">
                        内存消耗
                      </div>
                      <div class="text-2xl font-bold">
                        {{ resultTaskData?.maxMemory }} KB
                      </div>
                    </div>
                  </div>

                  <div class="bg-white dark:bg-gray-800 rounded-xl p-y-6 ">
                    <div class="grid grid-cols-1 md:grid-cols-1 gap-6 mb-6">
                      <div class="col-span-1 md:col-span-1">
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

                      <!-- <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
          <div class="flex justify-between items-center mb-2">
            <span class="font-medium">检测结果</span>
            <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">
              未发现抄袭
            </span>
          </div>
          <div class="text-sm text-gray-600 dark:text-gray-300">
            代码通过克隆检测，未发现高度相似的已有提交
          </div>
        </div> -->
                    </div>

                    <div class="text-sm text-gray-500 dark:text-gray-400 italic">
                      <i class="fa fa-info-circle mr-1" /> 代码克隆检测用于辅助判断代码相似度，结果仅供参考，最终判定由人工审核决定
                    </div>
                  </div>

                  <div class="lg:col-span-2">
                    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
                      <!-- <CodeEditor v-if="resultTaskData?.code"
            :model-value="resultTaskData?.code"
            :language="resultTaskData?.language"
            width="100%"
            height="400px"
            :options="{
              readOnly: true,
            }"
          /> -->
                      <CodeEditor
                        v-if="resultTaskData?.message"
                        :model-value="resultTaskData?.message"
                        width="100%"
                        height="400px"
                        :options="{
                          readOnly: true,
                        }"
                      />
                    </div>
                  </div>

                  <!-- <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <n-card embedded size="small" class="rounded-lg">
                      <div class="space-y-2">
                        <div class="flex items-center justify-between">
                          <n-text class="text-gray-500 dark:text-gray-400">
                            编程语言
                          </n-text>
                          <n-tag :bordered="false">
                            {{ resultTaskData?.languageName }}
                          </n-tag>
                        </div>
                        <div class="flex items-center justify-between">
                          <n-text class="text-gray-500 dark:text-gray-400">
                            提交类型
                          </n-text>
                          <n-tag :bordered="false" type="info">
                            {{ resultTaskData?.submitTypeName }}
                          </n-tag>
                        </div>
                      </div>
                    </n-card>

                    <n-card embedded size="small" class="rounded-lg">
                      <div class="space-y-2">
                        <div class="flex items-center justify-between">
                          <n-text class="text-gray-500 dark:text-gray-400">
                            最大时间
                          </n-text>
                          <n-tag :bordered="false" type="info">
                            {{ resultTaskData?.maxTime }} ms
                          </n-tag>
                        </div>
                        <div class="flex items-center justify-between">
                          <n-text class="text-gray-500 dark:text-gray-400">
                            最大内存
                          </n-text>
                          <n-tag :bordered="false" type="info">
                            {{ resultTaskData?.maxMemory }} KB
                          </n-tag>
                        </div>
                      </div>
                    </n-card>
                  </div> -->

                  <!-- 代码相似度 -->
                  <!-- <n-card embedded size="small" class="mt-4 rounded-lg">
                    <div class="flex items-center justify-between">
                      <n-text class="text-gray-500 dark:text-gray-400">
                        代码相似度
                      </n-text>
                      <n-progress
                        v-if="resultTaskData?.similarity"
                        type="circle"
                        :percentage="resultTaskData?.similarity"
                        :indicator-text="`${resultTaskData?.similarity}%`"
                        :height="60"
                        :show-indicator="true"
                      />
                      <n-text v-else>
                        未检测
                      </n-text>
                    </div>
                  </n-card> -->

                  <!-- 返回消息 -->
                  <!-- <n-card embedded size="small" class="mt-4 rounded-lg">
                    <n-code
                      :code="resultTaskData?.message"
                      :language="resultTaskData?.language.toLowerCase()"
                      show-line-numbers
                      class="rounded-md overflow-x-scroll"
                    />
                  </n-card> -->

                  <!-- 代码展示 -->
                  <!-- <n-card embedded size="small" class="mt-4 rounded-lg">
                    <n-code
                      :code="resultTaskData?.code"
                      :language="resultTaskData?.language.toLowerCase()"
                      show-line-numbers
                      class="rounded-md overflow-x-scroll"
                    />
                  </n-card> -->
                  <!-- <n-collapse :default-expanded-names="['code']" class="mt-4">
                    <n-collapse-item title="查看相似检测" name="code">
                    </n-collapse-item>
                  </n-collapse> -->
                </n-card>
                <n-empty
                  v-else
                  class="flex flex-col items-center justify-center py-12"
                  description="暂无判题结果"
                >
                  <template #icon>
                    <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                      <icon-park-outline-info />
                    </n-icon>
                  </template>
                  <n-text depth="3" class="text-center max-w-xs">
                    您还没有提交过代码，或者找不到对应的判题结果
                  </n-text>
                </n-empty>
              </NTabPane>
              <NTabPane
                name="assistant"
                tab="增强解析"
              >
                <AiHelp
                  :problem-id="originalId"
                  :language="submitParam.language"
                  :user-code="submitParam.code"
                />
              </NTabPane>
            </NTabs>
          </NFlex>
        </NCard>
      </template>

      <!-- 右侧代码编辑和提交区域 -->
      <template #2>
        <n-card size="small">
          <n-space align="center" justify="space-between">
            <n-select
              v-model:value="submitParam.language"
              :options="languageOptions"
              placeholder="请选择语言"
              class="w-50"
              @update:value="handleLanguageChange"
            />
            <n-space align="center">
              <n-button @click="execute(false)">
                运行
              </n-button>
              <n-button type="primary" @click="execute(true)">
                提交
              </n-button>
            </n-space>
          </n-space>
        </n-card>
        <CodeEditor
          v-model="submitParam.code"
          :language="submitParam.language"
        />
      </template>
    </NSplit>
  </template>
</template>

<style scoped>
:deep(.n-split-pane) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.n-tab-pane) {
  height: calc(100vh - 72px - 72px);
  overflow: scroll;
}
</style>
