<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import MdViewer from '@/components/common/editor/md/Viewer.vue'

const gateway = import.meta.env.VITE_GATEWAY
const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
const pathPrefix = `${context}/api/v1/`

const llmMessageParam = ref({
  conversantId: '',
  problemId: '1958869993346752514',
  message: '',
})

const messages = ref<Array<{ id: string, content: string, type: 'user' | 'bot' }>>([])
const isLoading = ref(false)
const controller = ref<AbortController | null>(null)

// 连接并发送消息
function sendMessage() {
  if (!llmMessageParam.value.message.trim())
    return

  // 生成会话ID（如果是新会话）
  if (!llmMessageParam.value.conversantId) {
    llmMessageParam.value.conversantId = `task-${uuidv4()}`
  }

  const userMessage = llmMessageParam.value.message
  const currentMessage = userMessage

  // 添加用户消息到界面
  messages.value.push({
    id: uuidv4(),
    content: userMessage,
    type: 'user',
  })

  // 清空输入框
  llmMessageParam.value.message = ''

  // 添加机器人消息占位符
  const botMessageId = uuidv4()
  messages.value.push({
    id: botMessageId,
    content: '',
    type: 'bot',
  })

  isLoading.value = true

  // 创建中止控制器
  controller.value = new AbortController()

  fetchEventSource(`${gateway + pathPrefix}chat/stream`, {
    method: 'POST', // 通常是POST请求发送消息
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      conversantId: llmMessageParam.value.conversantId,
      problemId: llmMessageParam.value.problemId,
      message: currentMessage,
    }),
    signal: controller.value.signal,

    onopen: async (response) => {
      if (response.ok) {
        console.log('SSE连接已建立')
      }
      else if (response.status === 401 || response.status === 403) {
        console.error('认证失败')
        throw new Error('认证失败')
      }
      else {
        console.error('连接失败:', response.status)
        throw new Error(`连接失败: ${response.status}`)
      }
    },

    onmessage: (event) => {
      // 处理服务器返回的消息
      try {
        if (event.data) {
          const data = JSON.parse(event.data)
          // 更新机器人消息内容
          const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
          if (botMessageIndex !== -1) {
            messages.value[botMessageIndex].content += data.content || data.message || ''
          }
        }
      }
      catch (error) {
        console.error('解析消息错误:', error)
      }
    },

    onerror: (error) => {
      console.error('SSE错误:', error)
      isLoading.value = false
      // 可以在这里显示错误信息
      const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
      if (botMessageIndex !== -1) {
        messages.value[botMessageIndex].content = '抱歉，发生错误，请重试。'
      }
    },

    onclose: () => {
      console.log('SSE连接关闭')
      isLoading.value = false
    },
  })
}

// 停止发送
function stopSending() {
  if (controller.value) {
    controller.value.abort()
    isLoading.value = false
  }
}

// 清空对话
function clearMessages() {
  messages.value = []
  llmMessageParam.value.conversantId = ''
}
</script>

<template>
  <div class="chat-container">
    <!-- 消息显示区域 -->
    <div class="messages-container">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message" :class="[message.type]"
      >
        <div class="message-content">
          <MdViewer :model-value="message.content" style="padding: 15px;" />
          <span v-if="isLoading && message.type === 'bot' && !message.content" class="typing-indicator">
            思考中...
          </span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-container">
      <n-input
        v-model:value="llmMessageParam.message"
        placeholder="请输入消息"
        type="textarea"
        :rows="3"
        @keydown.enter.prevent="sendMessage"
      />
      <div class="button-group">
        <n-button
          type="primary"
          :disabled="isLoading || !llmMessageParam.message.trim()"
          @click="sendMessage"
        >
          {{ isLoading ? '发送中...' : '发送' }}
        </n-button>
        <n-button
          :disabled="!isLoading"
          secondary
          @click="stopSending"
        >
          停止
        </n-button>
        <n-button
          secondary
          @click="clearMessages"
        >
          清空
        </n-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  height: 70vh;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message {
  margin-bottom: 16px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.bot {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  word-wrap: break-word;
}

.typing-indicator {
  animation: blink 1.4s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.input-container {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
}

.button-group {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
</style>
