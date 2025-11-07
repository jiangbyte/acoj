<script lang="ts" setup>
import { useDataContestProblemFetch, useDataProblemFetch, useDataSetFetch, useDataSubmitFetch } from '@/composables/v1'
import { AesCrypto, Poller } from '@/utils'
import { useUserStore } from '@/stores'
import { v4 as uuidv4 } from 'uuid'
import { NSpin } from 'naive-ui'

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<Props>()

interface Props {
  moduleType: string
}

// 懒加载重量级组件
const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
  loadingComponent: {
    setup() {
      return () => h('div', {
        class: 'h-full',
        style: {
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        },
      }, [
        h(NSpin, { size: 'small', description: '代码编辑器加载中...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const userStore = useUserStore()

const route = useRoute()
const activeTab = ref('description')
const detailData = ref()

const routeProblemId = AesCrypto.decrypt(route.query.problemId as string)
const routeSetId = AesCrypto.decrypt(route.query.setId as string) || null
const routeContestId = AesCrypto.decrypt(route.query.contestId as string) || null

const theModuleId = ref()

async function loadData() {
  if (props.moduleType === 'SET') {
    theModuleId.value = routeSetId
    useDataSetFetch().dataSetProblemDetail({ problemId: routeProblemId, id: routeSetId }).then(({ data }) => {
      if (data) {
        detailData.value = data
        console.log(data)
      }
    })
  }
  else if (props.moduleType === 'PROBLEM') {
    useDataProblemFetch().dataProblemClientDetail({ id: routeProblemId }).then(({ data }) => {
      detailData.value = data
    })
  }
  else if (props.moduleType === 'CONTEST') {
    theModuleId.value = routeContestId
    useDataContestProblemFetch().dataContestProblemDetailClient({ id: routeContestId, problemId: routeProblemId }).then(({ data }) => {
      detailData.value = data
    })
  }
}
loadData()

const submitParam = ref({
  problemId: routeProblemId,
  moduleId: undefined,
  moduleType: props.moduleType,
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
  userIdName: '未知用户',
  userAvatar: undefined,
  moduleId: undefined,
  moduleType: undefined,
  problemId: undefined,
  problemIdName: '未知题目',
  language: undefined,
  languageName: undefined,
  code: undefined,
  codeLength: 0,
  submitType: false,
  submitTypeName: undefined,
  maxTime: 0,
  maxMemory: 0,
  message: undefined,
  testCase: [],
  status: undefined,
  statusName: undefined,
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

  if (!taskId) {
    return
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
          // window.$message.warning('判题超时，请稍后手动查看结果')
          window.$dialog.warning({
            title: '判题超时',
            content: '请稍后手动查看结果',
          })
          poller.value?.stop()
          return null
        }

        try {
          // 调用获取判题结果的API
          useDataSubmitFetch().dataSubmitDetail({ id: taskId }).then(({ data }) => {
            // 更新结果数据
            resultTaskData.value = data

            if (data.isFinish) {
              // 触发结果轮询结束
              poller.value?.stop(() => {
                isPolling.value = false
                activeTab.value = 'result'
                window.$notification.success({
                  title: '判题完成',
                  duration: 3000,
                })
              })
            }
          })
        }
        catch (error) {
          console.error('获取判题结果失败:', error)
          // 发生错误时停止轮询
          poller.value?.stop(() => {
            // window.$message.error('获取判题结果失败')
            window.$notification.error({
              title: '获取判题结果失败',
              duration: 3000,
            })
          })
          throw error
        }
      },
      onStart: () => {
        console.log('开始轮询判题结果')
        isPolling.value = true
        activeTab.value = 'result'
        // window.$message.info('代码已提交，正在判题中...')
        window.$notification.info({
          title: '代码已提交，正在判题中...',
          duration: 3000,
        })
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
      // window.$message.info('已停止判题结果查询')
      window.$notification.info({
        title: '已停止判题结果查询',
        duration: 3000,
      })
      isPolling.value = false
    })
  }
}

function executeCode(type: boolean) {
  activeTab.value = 'result'
  submitParam.value.submitType = type as any
  if (isPolling.value) {
    return
  }

  // 任务ID生成
  submitParam.value.judgeTaskId = `task-${uuidv4()}`

  if (props.moduleType === 'SET') {
    submitParam.value.moduleType = 'SET'
    submitParam.value.moduleId = routeSetId as any
    useDataSubmitFetch().dataSubmitSetExecute(submitParam.value).then(({ data }) => {
      if (data) {
        submitTaskId.value = data
        // 开始轮询判题结果
        startResultPolling(data)
      }
    }).catch((error) => {
      console.error('提交代码失败:', error)
      // window.$message.error('提交失败，请重试')
      window.$notification.error({
        title: '提交失败',
        duration: 3000,
      })
    })
  }
  else if (props.moduleType === 'PROBLEM') {
    submitParam.value.moduleType = 'PROBLEM'
    submitParam.value.moduleId = routeProblemId as any
    useDataSubmitFetch().dataSubmitExecute(submitParam.value).then(({ data }) => {
      if (data) {
        submitTaskId.value = data
        // 开始轮询判题结果
        startResultPolling(data)
      }
    }).catch((error) => {
      console.error('提交代码失败:', error)
      // window.$message.error('提交失败，请重试')
      window.$notification.error({
        title: '提交失败',
        duration: 3000,
      })
    })
  }
  else if (props.moduleType === 'CONTEST') {
    submitParam.value.moduleType = 'CONTEST'
    submitParam.value.moduleId = routeContestId as any
    useDataSubmitFetch().dataSubmitContestExecute(submitParam.value).then(({ data }) => {
      if (data) {
        submitTaskId.value = data
        // 开始轮询判题结果
        startResultPolling(data)
      }
    }).catch((error) => {
      console.error('提交代码失败:', error)
      // window.$message.error('提交失败，请重试')
      window.$notification.error({
        title: '提交失败',
        duration: 3000,
      })
    })
  }
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
  // 不使用模板时，不处理
  if (!detailData.value.useTemplate) {
    submitParam.value.code = ''
    return
  }
  // 没有模板时，不处理
  if (!detailData.value?.codeTemplate) {
    return
  }

  if (detailData.value.useTemplate) {
    // 有模板时，使用模板
    const selectedTemplate = detailData.value.codeTemplate.find((t: { language: string }) => t.language === lang)
    if (selectedTemplate) {
      submitParam.value.code = selectedTemplate.template
    }
    else {
      submitParam.value.code = ''
    }
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

const problemListRef = ref()
</script>

<template>
  <!-- 移动端只显示题目内容 -->
  <template v-if="isMobile">
    <NCard class="h-full" size="small" :bordered="false">
      <NFlex vertical :size="16" class="h-full">
        <NTabs v-model:value="activeTab" class="pt-72px" type="line" animated>
          <NTabPane name="description" tab="题目描述">
            <ProblemDescription :detail-data="detailData" />
          </NTabPane>
          <NTabPane name="submissions" tab="提交历史">
            <UserSubmitPages v-if="props.moduleType === 'PROBLEM'" :module-id="routeProblemId" :problem-id="routeProblemId" module-type="PROBLEM" />
            <UserSubmitPages v-if="props.moduleType === 'SET'" :module-id="String(routeSetId)" :problem-id="routeProblemId" module-type="SET" />
            <UserSubmitPages v-if="props.moduleType === 'CONTEST'" :module-id="String(routeContestId)" :problem-id="routeProblemId" module-type="CONTEST" />
          </NTabPane>
          <NTabPane name="submit" tab="提交代码">
            <n-card size="small">
              <n-space align="center" justify="space-between">
                <n-select
                  v-model:value="submitParam.language"
                  :options="languageOptions"
                  placeholder="请选择语言"
                  class="w-50"
                  data-testid="language-select"
                  @update:value="handleLanguageChange"
                />
                <n-space align="center">
                  <n-button v-if="isPolling" size="small" data-testid="polling-stop-button" @click="stopPollingManually">
                    停止
                  </n-button>
                  <n-button data-testid="test-submit-button" :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(false)">
                    运行
                  </n-button>
                  <n-button data-testid="judge-submit-button" type="primary" :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(true)">
                    提交
                  </n-button>
                </n-space>
              </n-space>
            </n-card>
            <CodeEditor
              v-model="submitParam.code"
              :language="String(submitParam.language)"
            />
          </NTabPane>
          <NTabPane
            name="result"
            tab="判题结果"
          >
            <JudgeResult
              :is-polling="isPolling"
              :polling-count="pollingCount"
              :max-polling-count="MAX_POLLING_COUNT"
              :result-task-data="resultTaskData"
            />
          </NTabPane>
          <!--
            非题集时，使用题目自身的 useAi 来判是否要启用 LLM 聊天
            题集时 也就是 isSet = true 时，使用 setUseAi 来判断是否要启用 LLM 聊天
            -->
          <NTabPane
            v-if="props.moduleType !== 'CONTEST' && (props.moduleType === 'SET' ? detailData?.setUseAi : detailData?.useAi)"
            name="assistant"
            tab="增强解析"
          >
            <KeepAlive>
              <LLMChat
                :key="`llm-chat-${routeProblemId}`"
                :problem-id="routeProblemId"
                :language="submitParam.language || ''"
                :user-code="submitParam.code"
              />
            </KeepAlive>
          </NTabPane>
          <template v-if="props.moduleType !== 'PROBLEM'" #suffix>
            <n-button text @click="problemListRef.doOpen(props.moduleType, theModuleId)">
              题目列表
            </n-button>
          </template>
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
                <ProblemDescription :detail-data="detailData" />
              </NTabPane>
              <NTabPane
                name="submissions"
                tab="提交历史"
              >
                <UserSubmitPages v-if="props.moduleType === 'PROBLEM'" :module-id="routeProblemId" :problem-id="routeProblemId" module-type="PROBLEM" />
                <UserSubmitPages v-if="props.moduleType === 'SET'" :module-id="String(routeSetId)" :problem-id="routeProblemId" module-type="SET" />
                <UserSubmitPages v-if="props.moduleType === 'CONTEST'" :module-id="String(routeContestId)" :problem-id="routeProblemId" module-type="CONTEST" />
              </NTabPane>
              <NTabPane
                name="result"
                tab="判题结果"
              >
                <JudgeResult
                  :is-polling="isPolling"
                  :polling-count="pollingCount"
                  :max-polling-count="MAX_POLLING_COUNT"
                  :result-task-data="resultTaskData"
                />
              </NTabPane>
              <NTabPane
                v-if="props.moduleType !== 'CONTEST' && (props.moduleType === 'SET' ? detailData?.setUseAi : detailData?.useAi)"
                name="assistant"
                tab="增强解析"
              >
                <!-- <LLMChat
                  :problem-id="originalId"
                  :language="submitParam.language || ''"
                  :user-code="submitParam.code"
                /> -->
                <KeepAlive>
                  <LLMChat
                    :key="`llm-chat-${routeProblemId}`"
                    :problem-id="routeProblemId"
                    :language="submitParam.language || ''"
                    :user-code="submitParam.code"
                  />
                </KeepAlive>
              </NTabPane>
              <template v-if="props.moduleType !== 'PROBLEM'" #suffix>
                <n-button text @click="problemListRef.doOpen(props.moduleType, theModuleId)">
                  题目列表
                </n-button>
              </template>
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
              <n-button v-if="isPolling" @click="stopPollingManually">
                停止
              </n-button>
              <n-button :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(false)">
                运行
              </n-button>
              <n-button type="primary" :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(true)">
                提交
              </n-button>
            </n-space>
          </n-space>
        </n-card>
        <CodeEditor
          v-model="submitParam.code"
          :language="String(submitParam.language)"
        />
      </template>
    </NSplit>
  </template>
  <ProblemLists ref="problemListRef" />
</template>

<style scoped>
:deep(.n-split-pane) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.n-tab-pane) {
  height: calc(100vh - 72px - 60px);
  overflow: scroll;
}
</style>
