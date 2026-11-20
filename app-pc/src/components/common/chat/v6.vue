<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import { useTokenStore } from '@/stores'
import { useSysConversationFetch } from '@/composables/v1'
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

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

const { sysConversationStreamUrl } = useSysConversationFetch()

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

const tokenStore = useTokenStore()

// Implementation note.
function sendMessage(messageType: string = 'DailyConversation') {
  if (!chatRequestParam.value.messageContent.trim() && messageType === 'DailyConversation') {
    window.$message?.warning('Enter a message')
    return
  }

  // Implementation note.
  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
    console.log('Generated conversation ID:', chatRequestParam.value.conversationId)
  }

  isLoading.value = true
  currentAssistantMessage.value = ''
  currentMessageType.value = messageType

  // Implementation note.
  let messageContentToSend = ''
  let userDisplayMessage = ''

  if (messageType === 'DailyConversation') {
    messageContentToSend = chatRequestParam.value.messageContent
    userDisplayMessage = chatRequestParam.value.messageContent
  }
  else {
    // Implementation note.
    const buttonMessages = {
      ProblemSolvingIdeas: 'Help me develop an approach to this problem',
      AnalyzeComplexity: 'Analyze the time and space complexity of this code',
      OptimizeCode: 'Help me optimize this code',
      AnalyzeBoundary: 'Help me identify edge cases for this problem',
    }
    messageContentToSend = buttonMessages[messageType] || 'Help me analyze this problem'
    userDisplayMessage = messageContentToSend
  }

  // Implementation note.
  messages.value.push({
    conversationId: chatRequestParam.value.conversationId,
    messageContent: userDisplayMessage,
    messageType,
    messageRole: 'USER',
    responseTime: Date.now(),
  })

  // Implementation note.
  messages.value.push({
    conversationId: chatRequestParam.value.conversationId,
    messageContent: '',
    messageType,
    messageRole: 'ASSISTANT',
    responseTime: Date.now(),
  })

  // Implementation note.
  controller.value = new AbortController()

  console.log('Sending request:', {
    conversationId: chatRequestParam.value.conversationId,
    problemId: chatRequestParam.value.problemId,
    messageContent: messageContentToSend,
    messageType,
    userCode: chatRequestParam.value.userCode,
    language: chatRequestParam.value.language,
    setId: chatRequestParam.value.setId,
  })

  fetchEventSource(sysConversationStreamUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': tokenStore.getToken ? tokenStore.getToken : '',
    },
    body: JSON.stringify({
      conversationId: chatRequestParam.value.conversationId,
      problemId: chatRequestParam.value.problemId,
      messageContent: messageContentToSend, // Implementation note.
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
      else {
        window.$message?.error(`Connection failed: ${response.status}`)
        isLoading.value = false
        // Implementation note.
        messages.value.pop()
      }
    },
    onmessage: (event) => {
      try {
        if (event.data) {
          const data = JSON.parse(event.data)
          console.log('Message received:', data)

          // Implementation note.
          if (data.content || data.messageContent || data.data) {
            const content = data.content || data.messageContent || data.data
            currentAssistantMessage.value += content

            // Implementation note.
            const lastMessageIndex = messages.value.length - 1
            if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
              messages.value[lastMessageIndex].messageContent = currentAssistantMessage.value
            }
          }

          // Implementation note.
          if (data.finishReason || data.finished) {
            console.log('Message complete')
            isLoading.value = false
          }
        }
      }
      catch (error) {
        console.error('Failed to parse message:', error)
        window.$message?.error('Invalid response format')
      }
    },
    onerror: (error) => {
      console.error('SSE error:', error)
      window.$message?.error('Connection error. Response stopped')
      isLoading.value = false
    },
    onclose: () => {
      console.log('SSE connection closed')
      isLoading.value = false
      currentAssistantMessage.value = ''

      // Implementation note.
      const lastMessageIndex = messages.value.length - 1
      if (lastMessageIndex >= 0
        && messages.value[lastMessageIndex].messageRole === 'ASSISTANT'
        && !messages.value[lastMessageIndex].messageContent) {
        messages.value.pop()
      }
    },
  })

  // Implementation note.
  if (messageType === 'DailyConversation') {
    chatRequestParam.value.messageContent = ''
  }
}

// Implementation note.
function handleButtonClick(messageType: string | undefined) {
  sendMessage(messageType)
}

// StopSend
function stopSending() {
  if (controller.value) {
    controller.value.abort()
    isLoading.value = false
    currentAssistantMessage.value = ''
  }
}

// Clear Chat
function clearMessages() {
  messages.value = []
  chatRequestParam.value.conversationId = ''
  currentAssistantMessage.value = ''
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

// Implementation note.
function scrollToBottom() {
  nextTick(() => {
    const chatContainer = document.querySelector('.overflow-scroll')
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight
    }
  })
}

// Implementation note.
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// Implementation note.
onMounted(() => {
  // Implementation note.
})

// Implementation note.
function getMessageTypeText(type: string | number) {
  const typeMap = {
    DailyConversation: 'Chat',
    ProblemSolvingIdeas: 'Solution Approach',
    AnalyzeComplexity: 'Complexity Analysis',
    OptimizeCode: 'Code Optimization',
    AnalyzeBoundary: 'Edge Case Analysis',
  }
  return typeMap[type] || type
}
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <n-flex ref="chatContainer" vertical class="overflow-scroll" :class="{ 'h-104': !isCollapse, 'h-145': isCollapse }">
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

          <n-card v-else-if="message.messageRole === 'ASSISTANT'" size="small" class="mb-4">
            <n-flex vertical :size="4">
              <n-flex align="center" justify="space-between">
                <n-tag :type="message.messageType === 'DailyConversation' ? 'primary' : 'success'" size="small">
                  {{ getMessageTypeText(message.messageType) }}
                </n-tag>
                <n-time :time="message.responseTime" />
              </n-flex>
              <MdViewer v-if="message.messageContent" :model-value="message.messageContent" />
              <div v-else class="typing-indicator">
                Thinking...
              </div>
            </n-flex>
          </n-card>
        </div>
      </div>
    </n-flex>

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
  </NFlex>
</template>

<style scoped>
.typing-indicator {
  animation: blink 1.4s infinite;
  margin-bottom: 6px;
  color: #666;
  font-style: italic;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.overflow-scroll {
  overflow-y: auto;
}
</style>
