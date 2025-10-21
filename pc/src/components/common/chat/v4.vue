<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import { useTokenStore } from '@/stores'
import { useSysConversationFetch } from '@/composables/v1'

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
  // 问题变化时重新加载对话历史
  loadConversationHistory()
})

watch(() => props.userCode, (newVal) => {
  console.log('代码变化:', newVal)
})

watch(() => props.language, (newVal) => {
  console.log('语言变化:', newVal)
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

// 调整消息结构以匹配后端实体
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
// 加载对话历史
async function loadConversationHistory() {
  if (!chatRequestParam.value.conversationId)
    return

  isHistoryLoading.value = true
  try {
    const { data } = await sysConversationHistory(chatRequestParam.value.conversationId)
    if (data) {
      // 转换后端数据为前端消息格式
      messages.value = data.map((item: any) => [
        // 用户消息
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
        // AI回复消息
        {
          id: `${item.id}-bot`,
          content: item.aiResponse,
          type: 'bot' as const,
          timestamp: item.responseTime,
          messageType: item.messageType,
          aiResponse: item.aiResponse,
        },
      ]).flat().filter(msg => msg.content) // 过滤掉空内容的消息
    }
  }
  catch (error) {
    console.error('加载对话历史失败:', error)
  }
  finally {
    isHistoryLoading.value = false
  }
}

// 连接并发送消息
function sendMessage(messageType: string = 'DailyConversation') {
  // 生成会话ID（如果是新会话）
  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
  }

  // 更新消息类型和内容
  chatRequestParam.value.messageType = messageType
  chatRequestParam.value.userCode = props.userCode
  chatRequestParam.value.language = props.language

  isLoading.value = true

  // 创建中止控制器
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
          console.log('接收到消息:', data)
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
        console.error('解析消息错误:', error)
      }
    },

    onerror: (error) => {
      console.error('SSE错误:', error)
      isLoading.value = false
      // const botMessageIndex = messages.value.findIndex(msg => msg.id === botMessageId)
      // if (botMessageIndex !== -1) {
      //   messages.value[botMessageIndex].content = '抱歉，发生错误，请重试。'
      //   messages.value[botMessageIndex].aiResponse = '抱歉，发生错误，请重试。'
      // }
    },

    onclose: () => {
      console.log('SSE连接关闭')
      isLoading.value = false
      // 对话结束后重新加载历史记录以确保数据同步
      loadConversationHistory()
    },
  })
}

// 按钮点击处理函数
function handleButtonClick(messageType: any) {
  sendMessage(messageType)
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
  chatRequestParam.value.conversationId = ''
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

// 组件挂载时，如果有会话ID则加载历史记录
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
            思考中...
          </span>
        </div>
      </div>
    </n-flex> -->

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
              @click="handleButtonClick('ProblemSolvingIdeas')"
            >
              解题思路
            </n-button>
            <n-button
              size="small"
              :disabled="!props.userCode && !props.language"
              @click="handleButtonClick('AnalyzeComplexity')"
            >
              分析复杂度
            </n-button>
            <n-button
              size="small"
              :disabled="!props.userCode && !props.language"
              @click="handleButtonClick('OptimizeCode')"
            >
              优化代码
            </n-button>
            <n-button
              size="small"
              @click="handleButtonClick('AnalyzeBoundary')"
            >
              分析边界
            </n-button>
            <n-button
              size="small"
            >
              对话历史
            </n-button>
          </n-flex>
          <n-button
            size="small"
            @click="handleCollapse"
          >
            {{ isCollapse ? '展开' : '收起' }}
          </n-button>
        </n-flex>
        <NInput
          v-if="!isCollapse"
          v-model:value="chatRequestParam.messageContent"
          type="textarea"
          placeholder="遇到问题？向 AI 描述你的疑问或需要帮助的地方。例如：如何优化这段代码的时间复杂度？"
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
