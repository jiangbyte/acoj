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

const messages = ref([{
  conversationId: '',
  messageContent: '',
  messageType: '',
  messageRole: '',
  responseTime: 0,
}])
const isLoading = ref(false)
const controller = ref<AbortController | null>(null)

const tokenStore = useTokenStore()

// Implementation note.
function sendMessage(messageType: string = 'DailyConversation') {
  // Implementation note.
  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
  }
  isLoading.value = true

  // Implementation note.
  chatRequestParam.value.messageType = messageType
  chatRequestParam.value.userCode = props.userCode
  chatRequestParam.value.language = props.language

  // Implementation note.
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
      else {
        window.$message?.error(`Connection failed: ${response.status}`)
      }
    },
    onmessage: (event) => {
      try {
        if (event.data) {
          const data = JSON.parse(event.data)
          console.log('Message received:', data)
          // TODO: Review this implementation.
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
  chatRequestParam.value.conversationId = ''
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

// Implementation note.
onMounted(() => {
})
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <n-flex ref="chatContainer" vertical class="overflow-scroll" :class="{ 'h-104': !isCollapse, 'h-145': isCollapse }">
      <div>
        <n-empty
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
        <div v-for="message in messages" :key="message.responseTime">
          <n-card v-if="message.messageRole === 'USER'" size="small" class="bg-#f0f7ff" :bordered="false">
            <n-flex vertical :size="4">
              <div class="flex items-center justify-between">
                <n-tag :type="message.messageType === 'chat' ? 'primary' : 'success'" size="small">
                  {{ message.messageType }}
                </n-tag>
                <n-time :time="message.responseTime" />
              </div>
              <n-text>
                {{ message.messageContent }}
              </n-text>
            </n-flex>
          </n-card>
          <div v-if="message.messageRole === 'ASSISTANT'">
            <n-card size="small" class="mb-4">
              <n-flex vertical :size="4">
                <n-flex align="center" justify="space-between">
                  <n-tag :type="message.messageType === 'chat' ? 'primary' : 'success'" size="small">
                    {{ message.messageType }}
                  </n-tag>
                  <n-time :time="message.responseTime" />
                </n-flex>
                <MdViewer :model-value="message.messageContent" />
              </n-flex>
            </n-card>
          </div>
          <span v-if="isLoading && message.messageRole === 'ASSISTANT' && !message.messageContent" class="typing-indicator">
            Thinking...
          </span>
        </div>
      </div>
    </n-flex>
    <!-- <MdViewer :model-value="messageContentRef" /> -->
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
