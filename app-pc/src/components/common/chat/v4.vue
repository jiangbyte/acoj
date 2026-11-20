<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import { useTokenStore } from '@/stores'
import { useSysConversationFetch } from '@/composables/v1'

// Implementation note.
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

// Implementation note.
watch(() => props.problemId, (newVal) => {
  console.log('Problem ID changed:', newVal)
  // Implementation note.
  loadConversationHistory()
})

watch(() => props.userCode, (newVal) => {
  console.log('Code changed:', newVal)
})

watch(() => props.language, (newVal) => {
  console.log('Language changed:', newVal)
})

const gateway = import.meta.env.VITE_GATEWAY
const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
const pathPrefix = `${context}/api/v1`

const chatRequestParam = ref({
  conversationId: '',
  problemId: props.problemId,
  messageContent: '',
  messageType: 'chat',
  userCode: props.userCode,
  language: props.language,
  setId: '',
})

// Implementation note.
const messages = ref<Array<{
  id: string
  content: string
  type: 'user' | 'bot'
  timestamp: any
  messageType: string
  userMessage?: string
  aiResponse?: string
  userCode?: string
  language?: string
  setId?: string
}>>([])

const isLoading = ref(false)
const isHistoryLoading = ref(false)
const controller = ref<AbortController | null>(null)

const tokenStore = useTokenStore()

const { sysConversationHistory } = useSysConversationFetch()
// Implementation note.
async function loadConversationHistory() {
  if (!chatRequestParam.value.conversationId)
    return

  isHistoryLoading.value = true
  try {
    const { data } = await sysConversationHistory(chatRequestParam.value.conversationId)
    if (data) {
      // Implementation note.
      messages.value = data.map((item: any) => [
        // Implementation note.
        {
          id: `${item.id}-user`,
          content: item.userMessage,
          type: 'user' as const,
          timestamp: item.createTime,
          messageType: item.messageType,
          userMessage: item.userMessage,
          userCode: item.userCode,
          language: item.language,
        },
        // Implementation note.
        {
          id: `${item.id}-bot`,
          content: item.aiResponse,
          type: 'bot' as const,
          timestamp: item.responseTime,
          messageType: item.messageType,
          aiResponse: item.aiResponse,
        },
      ]).flat().filter(msg => msg.content) // Implementation note.
    }
  }
  catch (error) {
    console.error('Failed to load chat history:', error)
  }
  finally {
    isHistoryLoading.value = false
  }
}

// Implementation note.
function sendMessage(messageType: string = 'DailyConversation') {
  // Implementation note.
  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
  }

  // Implementation note.
  chatRequestParam.value.messageType = messageType
  chatRequestParam.value.userCode = props.userCode
  chatRequestParam.value.language = props.language

  isLoading.value = true

  // Implementation note.
  controller.value = new AbortController()

  fetchEventSource(`${gateway + pathPrefix}/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': tokenStore.getToken || '',
    },
    body: JSON.stringify({
      conversationId: chatRequestParam.value.conversationId,
      problemId: chatRequestParam.value.problemId,
      messageContent: chatRequestParam.value.messageContent,
      messageType,
      userCode: chatRequestParam.value.userCode,
      language: chatRequestParam.value.language,
      setId: chatRequestParam.value.setId,
    }),
    signal: controller.value.signal,

    onopen: async (response) => {
      if (response.ok) {
        console.log('SSE connection established')
      }
      else if (response.status === 401 || response.status === 403) {
        console.error('Authentication failed')
        throw new Error('Authentication failed')
      }
      else {
        console.error('Connection failed:', response.status)
        throw new Error(`Connection failed: ${response.status}`)
      }
    },

    onmessage: (event) => {
      try {
        if (event.data) {
          const data = JSON.parse(event.data)
          console.log('Message received:', data)
        //   const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
        //   if (botMessageIndex !== -1) {
        //     messages.value[botMessageIndex].content += data.content || data.message || ''
        //     messages.value[botMessageIndex].timestamp = data.timestamp || Date.now()
        //     messages.value[botMessageIndex].messageType = data.messageType || messageType
        //     messages.value[botMessageIndex].aiResponse = messages.value[botMessageIndex].content
        //   }
        }
      }
      catch (error) {
        console.error('Failed to parse message:', error)
      }
    },

    onerror: (error) => {
      console.error('SSE error:', error)
      isLoading.value = false
      // const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
      // if (botMessageIndex !== -1) {
      //   messages.value[botMessageIndex].content = 'Sorry, something went wrong. Please try again.'
      //   messages.value[botMessageIndex].aiResponse = 'Sorry, something went wrong. Please try again.'
      // }
    },

    onclose: () => {
      console.log('SSE connection closed')
      isLoading.value = false
      // Implementation note.
      loadConversationHistory()
    },
  })
}

// Implementation note.
function handleButtonClick(messageType: any) {
  sendMessage(messageType)
}

// StopSend
function stopSending() {
  if (controller.value) {
    controller.value.abort()
    isLoading.value = false
  }
}

// Clear Chat
function clearMessages() {
  messages.value = []
  chatRequestParam.value.conversationId = ''
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

// Implementation note.
onMounted(() => {
  if (chatRequestParam.value.conversationId) {
    loadConversationHistory()
  }
})
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <!-- <n-flex ref="chatContainer" vertical class="overflow-scroll" :class="{ 'h-104': !isCollapse, 'h-145': isCollapse }">
      <div>
        <n-spin v-if="isHistoryLoading" size="small" class="flex justify-center py-4" />

        <n-empty
          v-else-if="messages.length === 0"
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
        <div v-for="message in messages" :key="message.id">
          <n-card v-if="message.type === 'user'" size="small" class="bg-#f0f7ff" :bordered="false">
            <n-flex vertical :size="4">
              <div class="flex items-center justify-between">
                <n-tag :type="message.messageType === 'chat' ? 'primary' : 'success'" size="small">
                  {{ getMessageTypeLabel(message.messageType) }}
                </n-tag>
                <n-time :time="message.timestamp" />
              </div>
              <n-text>
                {{ message.content }}
              </n-text>
            </n-flex>
          </n-card>
          <div v-if="message.type === 'bot'">
            <n-card size="small" class="mb-4">
              <n-flex vertical :size="4">
                <n-flex align="center" justify="space-between">
                  <n-tag :type="message.messageType === 'chat' ? 'primary' : 'success'" size="small">
                    {{ getMessageTypeLabel(message.messageType) }}
                  </n-tag>
                  <n-time :time="message.timestamp" />
                </n-flex>
                <MdViewer :model-value="message.content" />
              </n-flex>
            </n-card>
          </div>
          <span v-if="isLoading && message.type === 'bot' && !message.content" class="typing-indicator">
            Thinking...
          </span>
        </div>
      </div>
    </n-flex> -->

    <!-- Section -->
    <n-card size="small">
      <NFlex
        vertical
        :size="8"
      >
        <n-flex justify="space-between">
          <n-flex align="center">
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
            >
              Chat History
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
          @keydown.enter.prevent="sendMessage()"
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
          <NButton
            secondary
            size="small"
            type="error"
            @click="clearMessages"
          >
            <template #icon>
              <icon-park-outline-clear />
            </template>
            Clear
          </NButton>
        </NFlex>
      </NFlex>
    </n-card>
  </NFlex>
</template>

<style scoped>
.typing-indicator {
  animation: blink 1.4s infinite;
  margin-bottom: 6px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
