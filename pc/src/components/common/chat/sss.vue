<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import MdViewer from '@/components/common/editor/md/Viewer.vue'

// 定义 props
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

// 监听 props 变化
watch(() => props.problemId, (newVal) => {
  console.log('问题ID变化:', newVal)
})

watch(() => props.userCode, (newVal) => {
  console.log('代码变化:', newVal)
})

watch(() => props.language, (newVal) => {
  console.log('语言变化:', newVal)
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

// 预定义的提示词模板
const promptTemplates = {
  generate_solution: '请为这个问题生成详细的解题思路，包括算法设计和实现方法。',
  analyze_code: '请分析我的代码，指出其中的问题和改进建议。',
  explain_complexity: '请解释这个解法的时间复杂度和空间复杂度。',
  boundary_cases: '请列出这个问题的边界情况和特殊测试用例。',
  optimize_code: '请帮我优化这段代码，提高性能和可读性。',
  chat: '', // 用户自定义输入
}

// 获取消息类型对应的标签
function getMessageTypeLabel(messageType: string) {
  const labels = {
    generate_solution: '解题思路',
    analyze_code: '代码分析',
    explain_complexity: '复杂度分析',
    boundary_cases: '边界情况',
    optimize_code: '代码优化',
    chat: '对话',
  }
  return labels[messageType] || '对话'
}

// 连接并发送消息
function sendMessage(customMessage?: string, messageType: string = 'chat') {
  const finalMessage = customMessage || llmMessageParam.value.message
  if (!finalMessage.trim())
    return

  // 生成会话ID（如果是新会话）
  if (!llmMessageParam.value.conversantId) {
    llmMessageParam.value.conversantId = `task-${uuidv4()}`
  }

  // 更新消息类型和内容
  llmMessageParam.value.messageType = messageType
  llmMessageParam.value.userCode = props.userCode
  llmMessageParam.value.language = props.language

  // 添加用户消息到界面
  messages.value.push({
    id: uuidv4(),
    content: finalMessage,
    type: 'user',
    timestamp: Date.now(),
    messageType,
  })

  // 如果是按钮触发的，清空输入框
  if (customMessage) {
    llmMessageParam.value.message = ''
  }

  // 添加机器人消息占位符
  const botMessageId = uuidv4()
  messages.value.push({
    id: botMessageId,
    content: '',
    type: 'bot',
    timestamp: Date.now(),
    messageType,
  })

  isLoading.value = true

  // 创建中止控制器
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
        console.error('解析消息错误:', error)
      }
    },

    onerror: (error) => {
      console.error('SSE错误:', error)
      isLoading.value = false
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

// 按钮点击处理函数
function handleButtonClick(messageType: string) {
  const prompt = promptTemplates[messageType]
  if (prompt) {
    sendMessage(prompt, messageType)
  }
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
    <!-- 对话内容区域 -->
    <n-flex ref="chatContainer" vertical class="overflow-scroll" :class="{ 'h-104': !isCollapse, 'h-145': isCollapse }">
      <div>
        <n-empty
          v-if="messages.length === 0"
          class="flex flex-col items-center justify-center py-12"
          description="暂无对话记录"
        >
          <template #icon>
            <n-icon size="40" class="text-gray-300 dark:text-gray-600">
              <icon-park-outline-info />
            </n-icon>
          </template>
          <n-text depth="3" class="text-center max-w-xs">
            还没有消息，点击下方按钮开始对话！
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
            思考中...
          </span>
        </div>
      </div>
    </n-flex>

    <!-- 输入区域 -->
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
              生成解题思路
            </n-button>
            <n-button
              size="small"
              :loading="isLoading"
              @click="handleButtonClick('analyze_code')"
            >
              分析我的代码
            </n-button>
            <n-button
              size="small"
              :loading="isLoading"
              @click="handleButtonClick('optimize_code')"
            >
              优化代码
            </n-button>
          </n-flex>
          <n-button
            size="small"
            @click="handleCollapse"
          >
            {{ isCollapse ? '展开' : '收起' }}
          </n-button>
        </n-flex>
        <n-flex v-if="!isCollapse">
          <n-button round size="tiny" :loading="isLoading" @click="handleButtonClick('explain_complexity')">
            解释时间复杂度
          </n-button>
          <n-button round size="tiny" :loading="isLoading" @click="handleButtonClick('boundary_cases')">
            有哪些边界情况需要考虑
          </n-button>
        </n-flex>
        <NInput
          v-if="!isCollapse"
          v-model:value="llmMessageParam.message"
          type="textarea"
          placeholder="遇到问题？向AI描述你的疑问或需要帮助的地方。例如：如何优化这段代码的时间复杂度？或者：为什么我的代码在这个测试用例上失败了？"
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
            发送
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
            停止
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
            清空
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
