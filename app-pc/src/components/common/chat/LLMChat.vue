<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import { useTokenStore } from '@/stores'
import { useSysConversationFetch } from '@/composables/v1'
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const props = defineProps({
  problemId: {
    type: String,
    required: true,
  },
  language: {
    type: String,
    required: true,
  },
  userCode: {
    type: String,
    required: true,
  },
})

const {
  sysConversationStreamUrl,
  userSessionPage,
  sysConversationHistory,
} = useSysConversationFetch()

const chatRequestParam = ref({
  conversationId: '',
  problemId: props.problemId,
  messageContent: '',
  messageType: 'DailyConversation',
  userCode: props.userCode,
  language: props.language,
  setId: '',
})

const messages = ref<Array<any>>([])
const isLoading = ref(false)
const controller = ref<AbortController | null>(null)
const currentAssistantMessage = ref('')
const currentMessageType = ref('')
const isUserStopped = ref(false)
const sessionList = ref<any[]>([])
const sessionLoading = ref(false)
const switching = ref(false)
const historyShow = ref(false)

const tokenStore = useTokenStore()

/**
 * 打开历史抽屉并刷新会话列表。
 */
async function openHistory() {
  historyShow.value = true
  await loadSessions()
}

/**
 * 加载当前题目的历史会话列表。
 */
async function loadSessions() {
  if (!props.problemId) {
    sessionList.value = []
    return
  }
  sessionLoading.value = true
  try {
    const { data, success } = await userSessionPage({
      current: 1,
      size: 50,
      problemId: props.problemId,
    })
    if (success) {
      sessionList.value = data?.records || []
    }
  }
  finally {
    sessionLoading.value = false
  }
}

/**
 * 切换到指定会话并灌入历史消息。
 */
async function switchSession(conversationId: string) {
  if (!conversationId) {
    return
  }
  // 已是当前会话则仅关闭抽屉
  if (conversationId === chatRequestParam.value.conversationId && messages.value.length > 0) {
    historyShow.value = false
    return
  }
  if (isLoading.value) {
    stopSending()
  }
  switching.value = true
  try {
    // 1. 绑定 conversationId
    // 2. 拉历史消息映射到本地 messages
    // 3. 清空输入与流式中间态并关闭抽屉
    chatRequestParam.value.conversationId = conversationId
    const { data, success } = await sysConversationHistory(conversationId)
    if (!success) {
      window.$message?.error('Failed to load conversation')
      return
    }
    messages.value = (data || []).map((item: any) => ({
      conversationId: item.conversationId,
      messageContent: item.messageContent || '',
      messageType: item.messageType || 'DailyConversation',
      messageRole: item.messageRole,
      responseTime: Number(item.responseTime || item.createTime || Date.now()),
      isCompleted: true,
      isStopped: false,
    }))
    currentAssistantMessage.value = ''
    chatRequestParam.value.messageContent = ''
    isUserStopped.value = false
    historyShow.value = false
  }
  finally {
    switching.value = false
  }
}

/**
 * 新开对话：清空本地状态，下次发送生成新 conversationId。
 */
function newChat() {
  if (isLoading.value) {
    stopSending()
  }
  messages.value = []
  chatRequestParam.value.conversationId = ''
  currentAssistantMessage.value = ''
  isUserStopped.value = false
  chatRequestParam.value.messageContent = ''
}

function clearMessages() {
  newChat()
}

function sendMessage(messageType: string = 'DailyConversation') {
  if (!chatRequestParam.value.messageContent.trim() && messageType === 'DailyConversation') {
    window.$message?.warning('Enter a message')
    return
  }

  isUserStopped.value = false

  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
  }

  isLoading.value = true
  currentAssistantMessage.value = ''
  currentMessageType.value = messageType

  let messageContentToSend = ''
  let userDisplayMessage = ''

  if (messageType === 'DailyConversation') {
    messageContentToSend = chatRequestParam.value.messageContent
    userDisplayMessage = chatRequestParam.value.messageContent
  }
  else {
    const buttonMessages = {
      ProblemSolvingIdeas: 'Help me develop an approach to this problem',
      AnalyzeComplexity: 'Analyze the time and space complexity of this code',
      OptimizeCode: 'Help me optimize this code',
      AnalyzeBoundary: 'Help me identify edge cases for this problem',
    } as const
    messageContentToSend = buttonMessages[messageType as keyof typeof buttonMessages] || 'Help me analyze this problem'
    userDisplayMessage = messageContentToSend
  }

  messages.value.push({
    conversationId: chatRequestParam.value.conversationId,
    messageContent: userDisplayMessage,
    messageType,
    messageRole: 'USER',
    responseTime: Date.now(),
  })

  controller.value = new AbortController()

  fetchEventSource(sysConversationStreamUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': tokenStore.getToken ? tokenStore.getToken : '',
    },
    body: JSON.stringify({
      conversationId: chatRequestParam.value.conversationId,
      problemId: chatRequestParam.value.problemId,
      messageContent: messageContentToSend,
      messageType,
      userCode: chatRequestParam.value.userCode,
      language: chatRequestParam.value.language,
      setId: chatRequestParam.value.setId,
    }),
    signal: controller.value.signal,

    onopen: async (response) => {
      if (response.ok) {
        return
      }
      window.$message?.error(`Connection failed: ${response.status}`)
      handleRequestEnd(false)
      throw new Error(`SSE connection failed: ${response.status}`)
    },
    onmessage: (event) => {
      try {
        if (event.data) {
          const data = JSON.parse(event.data)

          if (data.messageContent) {
            const content = data.messageContent
            if (!currentAssistantMessage.value) {
              messages.value.push({
                conversationId: chatRequestParam.value.conversationId,
                messageContent: '',
                messageType: currentMessageType.value,
                messageRole: 'ASSISTANT',
                responseTime: Date.now(),
                isCompleted: false,
                isStopped: false,
              })
            }
            currentAssistantMessage.value += content

            const lastMessageIndex = messages.value.length - 1
            if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
              messages.value[lastMessageIndex].messageContent = currentAssistantMessage.value
            }
          }

          if (data.finishReason || data.finished) {
            handleRequestEnd(true)
          }
        }
      }
      catch (error) {
        console.error('Failed to parse message:', error)
        window.$message?.error('Invalid response format')
        handleRequestEnd(true)
      }
    },
    onerror: (error) => {
      console.error('SSE error:', error)
      if (!isUserStopped.value) {
        window.$message?.error('Connection error. Response stopped')
      }
      handleRequestEnd(true)
      throw error
    },
    onclose: () => {
      handleRequestEnd(true)
    },
  })

  if (messageType === 'DailyConversation') {
    chatRequestParam.value.messageContent = ''
  }
}

function handleButtonClick(messageType: string | undefined) {
  sendMessage(messageType)
}

function handleRequestEnd(isCompleted: boolean) {
  isLoading.value = false

  const lastMessageIndex = messages.value.length - 1
  if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
    messages.value[lastMessageIndex].isCompleted = isCompleted
    messages.value[lastMessageIndex].isStopped = isUserStopped.value

    if (!messages.value[lastMessageIndex].messageContent && !isUserStopped.value) {
      messages.value.pop()
    }
  }

  currentAssistantMessage.value = ''
  controller.value = null
  // 结束后刷新侧栏，确保新会话/更新时间可见
  loadSessions()
}

function stopSending() {
  if (controller.value) {
    isUserStopped.value = true
    controller.value.abort()
    isLoading.value = false

    const lastMessageIndex = messages.value.length - 1
    if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
      messages.value[lastMessageIndex].isStopped = true
    }

    window.$message?.info('Generation stopped')
  }
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

function shouldShowTyping(message: any) {
  return !message.messageContent && !message.isCompleted && !message.isStopped
}

function shouldShowStopped(message: any) {
  return message.isStopped && message.messageContent
}

function scrollToBottom() {
  nextTick(() => {
    const chatContainer = document.querySelector('.llm-chat-scroll')
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight
    }
  })
}

watch(messages, () => {
  scrollToBottom()
}, { deep: true })

watch(
  () => props.problemId,
  (id) => {
    chatRequestParam.value.problemId = id
    newChat()
    loadSessions()
  },
)

onMounted(() => {
  loadSessions()
})

function getMessageTypeText(type: string | number) {
  const typeMap = {
    DailyConversation: 'Chat',
    ProblemSolvingIdeas: 'Solution Approach',
    AnalyzeComplexity: 'Complexity Analysis',
    OptimizeCode: 'Code Optimization',
    AnalyzeBoundary: 'Edge Case Analysis',
  } as const
  return typeMap[type as keyof typeof typeMap] || String(type || '')
}

function sessionTitle(session: any) {
  if (session.preview) {
    return session.preview
  }
  return getMessageTypeText(session.lastMessageType) || 'Conversation'
}
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <n-flex ref="chatContainer" vertical class="overflow-scroll llm-chat-scroll" :class="{ 'h-112': !isCollapse, 'h-149': isCollapse }">
      <div>
        <n-empty
          v-if="messages.length === 0"
          class="flex flex-col items-center justify-center py-12"
          description="No conversations"
        >
          <template #icon>
            <n-icon size="40" class="text-gray-300 dark:text-gray-600">
              <icon-park-outline-info />
            </n-icon>
          </template>
          <n-text depth="3" class="text-center max-w-xs">
            No messages yet. Choose an action below to start
          </n-text>
        </n-empty>
        <div v-for="(message, index) in messages" :key="index">
          <n-card v-if="message.messageRole === 'USER'" size="small" class="bg-#f0f7ff mb-4" :bordered="false">
            <n-flex vertical :size="4">
              <div class="flex items-center justify-between">
                <n-tag :type="message.messageType === 'DailyConversation' ? 'primary' : 'success'" size="small">
                  {{ getMessageTypeText(message.messageType) }}
                </n-tag>
                <n-time :time="message.responseTime" />
              </div>
              <n-text>
                {{ message.messageContent }}
              </n-text>
            </n-flex>
          </n-card>

          <n-card v-if="message.messageRole === 'ASSISTANT'" size="small" class="mb-4">
            <n-flex vertical :size="4">
              <n-flex align="center" justify="space-between">
                <n-tag :type="message.messageType === 'DailyConversation' ? 'primary' : 'success'" size="small">
                  {{ getMessageTypeText(message.messageType) }}
                </n-tag>
                <n-time :time="message.responseTime" />
              </n-flex>

              <MdViewer v-if="message.messageContent" :model-value="message.messageContent" />
              <n-spin v-if="shouldShowTyping(message)" size="small">
                Thinking...
              </n-spin>

              <div v-if="shouldShowStopped(message)" class="stopped-indicator">
                <n-text type="warning" depth="3">
                  <n-icon size="14">
                    <icon-park-outline-info />
                  </n-icon>
                  Generation stopped
                </n-text>
              </div>
            </n-flex>
          </n-card>
        </div>
      </div>
    </n-flex>

    <n-card size="small">
      <NFlex
        vertical
        :size="8"
      >
        <n-flex justify="space-between">
          <n-flex align="center" :wrap="true">
            <n-button
              size="small"
              @click="openHistory"
            >
              <template #icon>
                <icon-park-outline-history />
              </template>
              History
            </n-button>
            <n-button
              size="small"
              @click="newChat"
            >
              New Chat
            </n-button>
            <n-button
              type="primary"
              size="small"
              @click="handleButtonClick('ProblemSolvingIdeas')"
            >
              Solution Approach
            </n-button>
            <n-button
              size="small"
              :disabled="!props.userCode && !props.language"
              @click="handleButtonClick('AnalyzeComplexity')"
            >
              Analyze Complexity
            </n-button>
            <n-button
              size="small"
              :disabled="!props.userCode && !props.language"
              @click="handleButtonClick('OptimizeCode')"
            >
              Optimize Code
            </n-button>
            <n-button
              size="small"
              @click="handleButtonClick('AnalyzeBoundary')"
            >
              Analyze Edge Cases
            </n-button>
            <n-button
              size="small"
              @click="clearMessages"
            >
              Clear Chat
            </n-button>
          </n-flex>
          <n-button
            size="small"
            @click="handleCollapse"
          >
            {{ isCollapse ? 'Expand' : 'Collapse' }}
          </n-button>
        </n-flex>
        <NInput
          v-if="!isCollapse"
          v-model:value="chatRequestParam.messageContent"
          type="textarea"
          placeholder="Need help? Ask AI a question, such as how to improve this code's time complexity."
          :autosize="{ minRows: 4, maxRows: 4 }"
          @keydown.enter.exact.prevent="sendMessage()"
        />
        <NFlex
          v-if="!isCollapse"
          justify="end"
          align="center"
        >
          <NButton
            type="primary"
            size="small"
            :loading="isLoading"
            @click="sendMessage()"
          >
            <template #icon>
              <icon-park-outline-send />
            </template>
            Send
          </NButton>
          <NButton
            secondary
            type="primary"
            size="small"
            :disabled="!isLoading"
            @click="stopSending"
          >
            <template #icon>
              <icon-park-outline-close />
            </template>
            Stop
          </NButton>
        </NFlex>
      </NFlex>
    </n-card>

    <NDrawer v-model:show="historyShow" placement="left" :width="320">
      <NDrawerContent title="Conversation History" closable>
        <template #header-extra>
          <n-button size="small" type="primary" quaternary @click="newChat(); historyShow = false">
            New Chat
          </n-button>
        </template>
        <n-spin :show="sessionLoading || switching">
          <n-empty
            v-if="!sessionLoading && sessionList.length === 0"
            class="py-8"
            description="No history yet"
            size="small"
          />
          <n-space v-else vertical :size="8">
            <div
              v-for="session in sessionList"
              :key="session.conversationId"
              class="px-3 py-2 cursor-pointer rounded border border-transparent hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
              :class="{ 'bg-#f0f7ff dark:bg-gray-800 border-blue-200!': session.conversationId === chatRequestParam.conversationId }"
              @click="switchSession(session.conversationId)"
            >
              <n-ellipsis :line-clamp="2" class="text-sm">
                {{ sessionTitle(session) }}
              </n-ellipsis>
              <div class="flex items-center justify-between mt-1 gap-1">
                <n-tag size="tiny" :bordered="false">
                  {{ getMessageTypeText(session.lastMessageType) || 'Chat' }}
                </n-tag>
                <n-time
                  v-if="session.lastActiveAt"
                  class="text-xs text-gray-400"
                  :time="Number(session.lastActiveAt)"
                  type="relative"
                />
              </div>
              <n-text depth="3" class="text-xs">
                {{ session.messageCount || 0 }} messages
              </n-text>
            </div>
          </n-space>
        </n-spin>
      </NDrawerContent>
    </NDrawer>
  </NFlex>
</template>

<style scoped>
.typing-indicator {
  animation: blink 1.4s infinite;
  margin-bottom: 6px;
  color: #666;
  font-style: italic;
}

.stopped-indicator {
  margin-top: 8px;
  padding: 4px 8px;
  background-color: #fff7e6;
  border-radius: 4px;
  border-left: 3px solid #ffa940;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.overflow-scroll {
  overflow-y: auto;
}
</style>
