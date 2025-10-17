<script lang="ts" setup>
import CodeEditor from '@/components/common/editor/code/Editor.vue'
import MdViewer from '@/components/common/editor/md/Viewer.vue'
import {
  BarChartOutline,
  HardwareChipOutline,
  TimeOutline,
} from '@vicons/ionicons5'
import { useDataProblemFetch, useDataSubmitFetch } from '@/composables/v1'
import { AesCrypto, DifficultyColorUtil, Poller, RandomColorUtil } from '@/utils'
import { useUserStore } from '@/stores'
import { v4 as uuidv4 } from 'uuid'

defineOptions({
  inheritAttrs: false,
})

const userStore = useUserStore()

const isConnected = ref(false)

const route = useRoute()
const activeTab = ref('description')
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.problem as string)
async function loadData() {
  useDataProblemFetch().dataProblemClientDetail({ id: originalId }).then(({ data }) => {
    detailData.value = data
  })
}
loadData()

const submitParam = ref({
  problemId: originalId,
  setId: null,
  language: null,
  code: '',
  submitType: null,
  judgeTaskId: '',
  userId: userStore.getUserId,
})
const resultTaskData = ref({
  id: undefined,
  judgeTaskId: undefined,
  userId: undefined,
  userIdName: '加载中',
  userAvatar: undefined,
  setId: undefined,
  setIdName: undefined,
  isSet: false,
  problemId: undefined,
  problemIdName: '加载中',
  language: undefined,
  languageName: '加载中',
  code: undefined,
  codeLength: 0,
  submitType: false,
  submitTypeName: '加载中',
  maxTime: 0,
  maxMemory: 0,
  message: undefined,
  testCase: [],
  status: undefined,
  statusName: '加载中',
  isFinish: false,
  similarity: 0,
  taskId: undefined,
  reportId: undefined,
  similarityCategory: undefined,
  similarityCategoryName: undefined,
  createTime: Date.now(),
  updateTime: Date.now(),
})
const submitTaskId = ref('')

// 在组件数据部分添加
const poller = ref<Poller<any> | null>(null)
const isPolling = ref(false)
const pollingCount = ref(0)
const MAX_POLLING_COUNT = 40 // 最大轮询次数，防止无限轮询
// 判题结果轮询函数
async function startResultPolling(taskId: string) {
  // 如果已有轮询器在运行，先停止
  if (poller.value) {
    poller.value.stop()
  }

  // 重置轮询计数
  pollingCount.value = 0

  // 创建新的轮询器
  poller.value = new Poller(
    {
      interval: 2000, // 2秒轮询一次
      immediate: true,
    },
    {
      onPoll: async () => {
        pollingCount.value++

        // 检查是否超过最大轮询次数
        if (pollingCount.value > MAX_POLLING_COUNT) {
          window.$message.warning('判题超时，请稍后手动查看结果')
          poller.value?.stop()
          isConnected.value = false
          return null
        }

        try {
          // 调用获取判题结果的API
          useDataSubmitFetch().dataSubmitDetail({ id: taskId }).then(({ data }) => {
            // 更新结果数据
            resultTaskData.value = data

            if (data.isFinish) {
              activeTab.value = 'result'
              isPolling.value = false
              window.$message.success('判题完成！')
              stopPollingManually()
            }
          })
        }
        catch (error) {
          console.error('获取判题结果失败:', error)
          // 发生错误时停止轮询
          poller.value?.stop(() => {
            window.$message.error('获取判题结果失败')
            isConnected.value = false
          })
          throw error
        }
      },
      onStart: () => {
        console.log('开始轮询判题结果')
        isPolling.value = true
        isConnected.value = true
        activeTab.value = 'result'
        window.$message.info('代码已提交，正在判题中...')
      },
      onStop: () => {
        console.log('停止轮询判题结果')
        isPolling.value = false
        pollingCount.value = 0
      },
    },
  )
}
// 手动停止判题轮询
function stopPollingManually() {
  if (poller.value && isPolling.value) {
    poller.value.stop(() => {
      window.$message.info('已停止判题结果查询')
      isConnected.value = false
      isPolling.value = false
    })
  }
}
function executeCode(type: boolean) {
  activeTab.value = 'result'
  submitParam.value.submitType = type as any
  if (isConnected.value) {
    return
  }

  // 任务ID生成
  submitParam.value.judgeTaskId = `task-${uuidv4()}`

  useDataSubmitFetch().dataSubmitExecute(submitParam.value).then(({ data }) => {
    submitTaskId.value = data
    // 开始轮询判题结果
    startResultPolling(data)
  }).catch((error) => {
    console.error('提交代码失败:', error)
    window.$message.error('提交失败，请重试')
    isConnected.value = false
  })
}
// 在组件卸载时清理轮询器
onUnmounted(() => {
  if (poller.value) {
    poller.value.destroy()
  }
})
// 获取语言选项
const languageOptions = computed(() => {
  // if (!detailData.value?.codeTemplate)
  //   return []
  return detailData.value?.allowedLanguages.map((language: any) => ({
    label: language.charAt(0).toUpperCase() + language.slice(1),
    value: language,
  }))
})

// 语言切换处理
function handleLanguageChange(lang: string) {
  stopPollingManually()
  // 首字母大写
  window.$message.success(`已切换至 ${lang.charAt(0).toUpperCase() + lang.slice(1)} 语言`)
  if (!detailData.value?.codeTemplate)
    return
  const selectedTemplate = detailData.value.codeTemplate.find((t: { language: string }) => t.language === lang)
  if (selectedTemplate) {
    submitParam.value.code = selectedTemplate.template
  }
  else {
    submitParam.value.code = ''
  }
}

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
                <n-tag :bordered="false" :color="{ color: RandomColorUtil.generate(), textColor: '#fff' }">
                  {{ detailData?.categoryName }}
                </n-tag>
                <n-tag :bordered="false" :color="{ color: DifficultyColorUtil.getColor(detailData?.difficulty), textColor: '#fff' }">
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
                  :color="{ color: RandomColorUtil.generate(), textColor: '#fff' }"
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
                      size="small"
                      :bordered="false"
                      :color="{ color: RandomColorUtil.generate(), textColor: '#fff' }"
                    >
                      {{ detailData?.categoryName }}
                    </n-tag>
                    <n-tag
                      size="small"
                      :bordered="false"
                      :color="{ color: DifficultyColorUtil.getColor(detailData?.difficulty), textColor: '#fff' }"
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
                      :color="{ color: RandomColorUtil.generate(), textColor: '#fff' }"
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
                <n-alert v-if="isPolling" type="warning" :show-icon="false">
                  <n-flex align="center" justify="center">
                    <n-spin size="small" />
                    <n-text type="info">
                      判题中... ({{ pollingCount }}/{{ MAX_POLLING_COUNT }})
                    </n-text>
                  </n-flex>
                </n-alert>
                <n-card v-if="isPolling || resultTaskData.id" class="w-full max-w-3xl mx-auto" :bordered="false" size="small">
                  <!-- 头部信息 -->
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
                            提交时间:  <n-time :time="resultTaskData?.createTime" />
                          </n-text>
                        </n-flex>
                        <n-flex align="center">
                          <n-text class="block text-gray-400 text-sm mt-1">
                            更新时间:  <n-time :time="resultTaskData?.updateTime" />
                          </n-text>
                        </n-flex>
                      </div>
                    </div>
                  </div>
                  <!-- 判题结果详情 -->
                  <n-divider class="!my-4" />
                  <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
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
                    <div class="grid grid-cols-1 md:grid-cols-1 gap-6 ">
                      <div class="col-span-1 md:col-span-1">
                        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg">
                          <div class="flex justify-between items-center mb-2">
                            <span class="font-medium">代码相似度</span>
                            <n-tag size="small" :bordered="false" type="info">
                              {{ resultTaskData?.similarity * 100 }} %
                            </n-tag>
                          </div>
                          <n-progress
                            type="line"
                            :show-indicator="false"
                            :percentage="resultTaskData?.similarity * 100"
                          />

                          <div class="mt-2 text-sm text-gray-500 dark:text-gray-400">
                            您的代码与平台上已有代码存在{{ resultTaskData?.similarity * 100 }}%的相似度，{{ resultTaskData?.similarityCategoryName }}
                          </div>
                          <div class="text-sm text-gray-500 dark:text-gray-400 italic">
                            注：代码克隆检测用于辅助判断代码相似度，结果仅供参考
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="lg:col-span-2">
                    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
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
              </NTabPane>
              <NTabPane
                v-if="detailData?.useAi"
                name="assistant"
                tab="增强解析"
              >
                <!-- <AiHelp
                  :problem-id="originalId"
                  :language="submitParam.language"
                  :user-code="submitParam.code"
                /> -->
                <LLMChat
                  :problem-id="originalId"
                  :language="submitParam.language || ''"
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
              <n-text type="info">
                ({{ pollingCount }}/{{ MAX_POLLING_COUNT }})
              </n-text>
              <n-button v-if="isPolling" size="small" @click="stopPollingManually">
                停止
              </n-button>
              <n-button :disabled="!submitParam.language || !submitParam.code || isConnected" @click="executeCode(false)">
                运行
              </n-button>
              <n-button type="primary" :disabled="!submitParam.language || !submitParam.code || isConnected" @click="executeCode(true)">
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
