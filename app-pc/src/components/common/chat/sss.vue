<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
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

// Implementation note.
watch(() => props.problemId, (newVal) => {
  console.log('Problem ID changed:', newVal)
})

watch(() => props.userCode, (newVal) => {
  console.log('Code changed:', newVal)
})

watch(() => props.language, (newVal) => {
  console.log('Language changed:', newVal)
})

const gateway = import.meta.env.VITE_GATEWAY
const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
const pathPrefix = `${context}/api/v1/`

const llmMessageParam = ref({
  conversantId: '',
  problemId: props.problemId,
  message: '',
  messageType: 'chat',
  userCode: props.userCode,
  language: props.language,
})

const messages = ref<Array<{ id: string, content: string, type: 'user' | 'bot', timestamp: any, messageType: string }>>([])
const isLoading = ref(false)
const controller = ref<AbortController | null>(null)

// Implementation note.
const promptTemplates = {
  generate_solution: 'Develop a detailed solution approach, including the algorithm and implementation.',
  analyze_code: 'Analyze my code and suggest improvements.',
  explain_complexity: 'Explain the time and space complexity of this solution.',
  boundary_cases: 'List edge cases and special test cases for this problem.',
  optimize_code: 'Optimize this code for performance and readability.',
  chat: '', // Implementation note.
}

// Implementation note.
function getMessageTypeLabel(messageType: string) {
  const labels = {
    generate_solution: 'Solution Approach',
    analyze_code: 'Code Analysis',
    explain_complexity: 'Complexity Analysis',
    boundary_cases: 'Edge Cases',
    optimize_code: 'Code Optimization',
    chat: 'Chat',
  }
  return labels[messageType] || 'Chat'
}

// Implementation note.
function sendMessage(customMessage?: string, messageType: string = 'chat') {
  const finalMessage = customMessage || llmMessageParam.value.message
  if (!finalMessage.trim())
    return

  // Implementation note.
  if (!llmMessageParam.value.conversantId) {
    llmMessageParam.value.conversantId = `task-${uuidv4()}`
  }

  // Implementation note.
  llmMessageParam.value.messageType = messageType
  llmMessageParam.value.userCode = props.userCode
  llmMessageParam.value.language = props.language

  // Implementation note.
  messages.value.push({
    id: uuidv4(),
    content: finalMessage,
    type: 'user',
    timestamp: Date.now(),
    messageType,
  })

  // Implementation note.
  if (customMessage) {
    llmMessageParam.value.message = ''
  }

  // Implementation note.
  const botMessageId = uuidv4()
  messages.value.push({
    id: botMessageId,
    content: '',
    type: 'bot',
    timestamp: Date.now(),
    messageType,
  })

  isLoading.value = true

  // Implementation note.
  controller.value = new AbortController()

  fetchEventSource(`${gateway + pathPrefix}chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      conversantId: llmMessageParam.value.conversantId,
      problemId: llmMessageParam.value.problemId,
      message: finalMessage,
      messageType,
      userCode: llmMessageParam.value.userCode,
      language: llmMessageParam.value.language,
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
          const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
          if (botMessageIndex !== -1) {
            messages.value[botMessageIndex].content += data.content || data.message || ''
            messages.value[botMessageIndex].timestamp = data.timestamp || ''
            messages.value[botMessageIndex].messageType = data.messageType || ''
          }
        }
      }
      catch (error) {
        console.error('Failed to parse message:', error)
      }
    },

    onerror: (error) => {
      console.error('SSE error:', error)
      isLoading.value = false
      const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
      if (botMessageIndex !== -1) {
        messages.value[botMessageIndex].content = 'Sorry, something went wrong. Please try again.'
      }
    },

    onclose: () => {
      console.log('SSE connection closed')
      isLoading.value = false
    },
  })
}

// Implementation note.
function handleButtonClick(messageType: string) {
  const prompt = promptTemplates[messageType]
  if (prompt) {
    sendMessage(prompt, messageType)
  }
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
  llmMessageParam.value.conversantId = ''
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <!-- Section -->
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
            <n-time :time="message.timestamp" style="display: flex; justify-content: end; width: 100%; margin-top: 6px;" />
            <n-card size="small">
              <n-flex vertical :size="4">
                <n-tag :type="message.messageType === 'chat' ? 'primary' : 'success'" size="small">
                  {{ getMessageTypeLabel(message.messageType) }}
                </n-tag>
                <MdViewer :model-value="message.content" />
              </n-flex>
            </n-card>
          </div>
          <span v-if="isLoading && message.type === 'bot' && !message.content" class="typing-indicator">
            Thinking...
          </span>
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
              :loading="isLoading"
              @click="handleButtonClick('generate_solution')"
            >
              Generate Solution
            </n-button>
            <n-button
              size="small"
              :loading="isLoading"
              @click="handleButtonClick('analyze_code')"
            >
              Analyze My Code
            </n-button>
            <n-button
              size="small"
              :loading="isLoading"
              @click="handleButtonClick('optimize_code')"
            >
              Optimize Code
            </n-button>
          </n-flex>
          <n-button
            size="small"
            @click="handleCollapse"
          >
            {{ isCollapse ? 'Expand' : 'Collapse' }}
          </n-button>
        </n-flex>
        <n-flex v-if="!isCollapse">
          <n-button round size="tiny" :loading="isLoading" @click="handleButtonClick('explain_complexity')">
            Explain Complexity
          </n-button>
          <n-button round size="tiny" :loading="isLoading" @click="handleButtonClick('boundary_cases')">
            What edge cases should I consider?
          </n-button>
        </n-flex>
        <NInput
          v-if="!isCollapse"
          v-model:value="llmMessageParam.message"
          type="textarea"
          placeholder="Need help? Ask AI a question, such as how to improve this code or why it fails a test case."
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
