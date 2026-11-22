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

// Implementation note.
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
        h(NSpin, { size: 'small', description: 'Loading code editor...' }, { }),
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
  userIdName: 'Unknown User',
  userAvatar: undefined,
  moduleId: undefined,
  moduleType: undefined,
  problemId: undefined,
  problemIdName: 'Unknown Problem',
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

// Implementation note.
const poller = ref<Poller<any> | null>(null)
const isPolling = ref(false)
const pollingCount = ref(0)
const MAX_POLLING_COUNT = 40 // Implementation note.

// Implementation note.
async function startResultPolling(taskId: string) {
  // Implementation note.
  if (poller.value) {
    poller.value.stop()
  }

  if (!taskId) {
    return
  }

  // Implementation note.
  pollingCount.value = 0

  // Implementation note.
  poller.value = new Poller(
    {
      interval: 2000, // Implementation note.
      immediate: true,
    },
    {
      onPoll: async () => {
        pollingCount.value++

        // Implementation note.
        if (pollingCount.value > MAX_POLLING_COUNT) {
          // window.$message.warning('Judging Timed Out，Please check the result again later')
          window.$dialog.warning({
            title: 'Judging Timed Out',
            content: 'Please check the result again later',
          })
          poller.value?.stop()
          return null
        }

        try {
          // Implementation note.
          useDataSubmitFetch().dataSubmitDetail({ id: taskId }).then(({ data }) => {
            // Implementation note.
            resultTaskData.value = data

            if (data.isFinish) {
              // Implementation note.
              poller.value?.stop(() => {
                isPolling.value = false
                activeTab.value = 'result'
                window.$notification.success({
                  title: 'Judging Complete',
                  duration: 3000,
                })
              })
            }
          })
        }
        catch (error) {
          console.error('Failed to get judge result:', error)
          // Implementation note.
          poller.value?.stop(() => {
            // window.$message.error('Failed to get judge result')
            window.$notification.error({
              title: 'Failed to get judge result',
              duration: 3000,
            })
          })
          throw error
        }
      },
      onStart: () => {
        console.log('Started polling judge result')
        isPolling.value = true
        activeTab.value = 'result'
        // window.$message.info('Code submitted. Judging...')
        window.$notification.info({
          title: 'Code submitted. Judging...',
          duration: 3000,
        })
      },
      onStop: () => {
        console.log('Stopped polling judge result')
        isPolling.value = false
        pollingCount.value = 0
      },
    },
  )
}
// Implementation note.
function stopPollingManually() {
  if (poller.value && isPolling.value) {
    poller.value.stop(() => {
      // window.$message.info('Stopped checking judge results')
      window.$notification.info({
        title: 'Stopped checking judge results',
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

  // Implementation note.
  submitParam.value.judgeTaskId = `task-${uuidv4()}`

  if (props.moduleType === 'SET') {
    submitParam.value.moduleType = 'SET'
    submitParam.value.moduleId = routeSetId as any
    useDataSubmitFetch().dataSubmitSetExecute(submitParam.value).then(({ data }) => {
      if (data) {
        submitTaskId.value = data
        // Started polling judge result
        startResultPolling(data)
      }
    }).catch((error) => {
      console.error('Failed to submit code:', error)
      // window.$message.error('Submission failed. Please try again')
      window.$notification.error({
        title: 'Submission Failed',
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
        // Started polling judge result
        startResultPolling(data)
      }
    }).catch((error) => {
      console.error('Failed to submit code:', error)
      // window.$message.error('Submission failed. Please try again')
      window.$notification.error({
        title: 'Submission Failed',
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
        // Started polling judge result
        startResultPolling(data)
      }
    }).catch((error) => {
      console.error('Failed to submit code:', error)
      // window.$message.error('Submission failed. Please try again')
      window.$notification.error({
        title: 'Submission Failed',
        duration: 3000,
      })
    })
  }
}
// Implementation note.
onUnmounted(() => {
  if (poller.value) {
    poller.value.destroy()
  }
})

// Implementation note.
const languageOptions = computed(() => {
  // if (!detailData.value?.codeTemplate)
  //   return []
  return detailData.value?.allowedLanguages.map((language: any) => ({
    label: language.charAt(0).toUpperCase() + language.slice(1),
    value: language,
  }))
})

// Implementation note.
function handleLanguageChange(lang: string) {
  stopPollingManually()
  // Implementation note.
  if (!detailData.value.useTemplate) {
    submitParam.value.code = ''
    return
  }
  // Implementation note.
  if (!detailData.value?.codeTemplate) {
    return
  }

  if (detailData.value.useTemplate) {
    // Implementation note.
    const selectedTemplate = detailData.value.codeTemplate.find((t: { language: string }) => t.language === lang)
    if (selectedTemplate) {
      submitParam.value.code = selectedTemplate.template
    }
    else {
      submitParam.value.code = ''
    }
  }
}

// Implementation note.
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
  <!-- Section -->
  <template v-if="isMobile">
    <NCard class="h-full" size="small" :bordered="false">
      <NFlex vertical :size="16" class="h-full">
        <NTabs v-model:value="activeTab" class="pt-72px" type="line" animated>
          <NTabPane name="description" tab="Problem Description">
            <ProblemDescription :detail-data="detailData" />
          </NTabPane>
          <NTabPane name="submissions" tab="Submission History">
            <UserSubmitPages v-if="props.moduleType === 'PROBLEM'" :module-id="routeProblemId" :problem-id="routeProblemId" module-type="PROBLEM" />
            <UserSubmitPages v-if="props.moduleType === 'SET'" :module-id="String(routeSetId)" :problem-id="routeProblemId" module-type="SET" />
            <UserSubmitPages v-if="props.moduleType === 'CONTEST'" :module-id="String(routeContestId)" :problem-id="routeProblemId" module-type="CONTEST" />
          </NTabPane>
          <NTabPane name="submit" tab="Submit Code">
            <n-card size="small">
              <n-space align="center" justify="space-between">
                <n-select
                  v-model:value="submitParam.language"
                  :options="languageOptions"
                  placeholder="Select a language"
                  class="w-50"
                  data-testid="language-select"
                  @update:value="handleLanguageChange"
                />
                <n-space align="center">
                  <n-button v-if="isPolling" size="small" data-testid="polling-stop-button" @click="stopPollingManually">
                    Stop
                  </n-button>
                  <n-button data-testid="test-submit-button" :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(false)">
                    Run
                  </n-button>
                  <n-button data-testid="judge-submit-button" type="primary" :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(true)">
                    Submit
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
            tab="Judge Result"
          >
            <JudgeResult
              :is-polling="isPolling"
              :polling-count="pollingCount"
              :max-polling-count="MAX_POLLING_COUNT"
              :result-task-data="resultTaskData"
            />
          </NTabPane>
          <!--
            Standalone problems use their own useAi setting to enable AI chat.
            Problem sets use setUseAi instead.
            -->
          <NTabPane
            v-if="props.moduleType !== 'CONTEST' && (props.moduleType === 'SET' ? detailData?.setUseAi : detailData?.useAi)"
            name="assistant"
            tab="AI Analysis"
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
              Problem List
            </n-button>
          </template>
        </NTabs>
      </NFlex>
    </NCard>
  </template>
  <template v-else>
    <!-- Section -->
    <NSplit
      direction="horizontal"
      :max="0.7"
      :min="0.3"
      :default-size="0.5"
      class="max-w-full p-0 mt-18 h-[calc(100vh-72px)]"
    >
      <!-- Section -->
      <template #1>
        <NCard
          class="h-full flex flex-col"
          :bordered="false"
        >
          <NFlex
            vertical
            :size="16"
          >
            <!-- Section -->
            <NTabs
              v-model:value="activeTab"
              type="line"
              animated
            >
              <NTabPane
                name="description"
                tab="Problem Description"
              >
                <ProblemDescription :detail-data="detailData" />
              </NTabPane>
              <NTabPane
                name="submissions"
                tab="Submission History"
              >
                <UserSubmitPages v-if="props.moduleType === 'PROBLEM'" :module-id="routeProblemId" :problem-id="routeProblemId" module-type="PROBLEM" />
                <UserSubmitPages v-if="props.moduleType === 'SET'" :module-id="String(routeSetId)" :problem-id="routeProblemId" module-type="SET" />
                <UserSubmitPages v-if="props.moduleType === 'CONTEST'" :module-id="String(routeContestId)" :problem-id="routeProblemId" module-type="CONTEST" />
              </NTabPane>
              <NTabPane
                name="result"
                tab="Judge Result"
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
                tab="AI Analysis"
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
                  Problem List
                </n-button>
              </template>
            </NTabs>
          </NFlex>
        </NCard>
      </template>

      <!-- Section -->
      <template #2>
        <n-card size="small">
          <n-space align="center" justify="space-between">
            <n-select
              v-model:value="submitParam.language"
              :options="languageOptions"
              placeholder="Select a language"
              class="w-50"
              @update:value="handleLanguageChange"
            />
            <n-space align="center">
              <n-button v-if="isPolling" @click="stopPollingManually">
                Stop
              </n-button>
              <n-button :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(false)">
                Run
              </n-button>
              <n-button type="primary" :disabled="!submitParam.language || !submitParam.code || isPolling" @click="executeCode(true)">
                Submit
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
