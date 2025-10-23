<script lang="ts" setup>
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { v4 as uuidv4 } from 'uuid'
import { useTokenStore } from '@/stores'
import { useSysConversationFetch } from '@/composables/v1'
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

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
const isUserStopped = ref(false)

const tokenStore = useTokenStore()

// 连接并发送消息
function sendMessage(messageType: string = 'DailyConversation') {
  if (!chatRequestParam.value.messageContent.trim() && messageType === 'DailyConversation') {
    window.$message?.warning('请输入消息内容')
    return
  }

  // 重置停止状态
  isUserStopped.value = false

  // 生成会话ID（如果是新会话）
  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
    console.log('生成会话ID:', chatRequestParam.value.conversationId)
  }

  isLoading.value = true
  currentAssistantMessage.value = ''
  currentMessageType.value = messageType

  // 根据消息类型确定要发送的内容
  let messageContentToSend = ''
  let userDisplayMessage = ''

  if (messageType === 'DailyConversation') {
    messageContentToSend = chatRequestParam.value.messageContent
    userDisplayMessage = chatRequestParam.value.messageContent
  }
  else {
    // 对于功能按钮，设置对应的消息内容
    const buttonMessages = {
      ProblemSolvingIdeas: '请帮我分析这道题的解题思路',
      AnalyzeComplexity: '请帮我分析这段代码的时间复杂度和空间复杂度',
      OptimizeCode: '请帮我优化这段代码',
      AnalyzeBoundary: '请帮我分析这道题的边界情况',
    } as const
    messageContentToSend = buttonMessages[messageType as keyof typeof buttonMessages] || '请帮我分析这个问题'
    userDisplayMessage = messageContentToSend
  }

  // 添加用户消息到消息列表
  messages.value.push({
    conversationId: chatRequestParam.value.conversationId,
    messageContent: userDisplayMessage,
    messageType,
    messageRole: 'USER',
    responseTime: Date.now(),
  })

  // 创建中止控制器
  controller.value = new AbortController()

  console.log('发送请求:', {
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
      messageContent: messageContentToSend,
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
      else {
        window.$message?.error(`连接失败: ${response.status}`)
        handleRequestEnd(false)
      }
    },
    // onmessage: (event) => {
    //   try {
    //     if (event.data) {
    //       const data = JSON.parse(event.data)
    //       console.log('接收到消息:', data)

    //       // 处理流式消息
    //       if (data.messageContent) {
    //         const content = data.messageContent
    //         currentAssistantMessage.value += content

    //         // 更新最后一条助手消息的内容
    //         const lastMessageIndex = messages.value.length - 1
    //         if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
    //           messages.value[lastMessageIndex].messageContent = currentAssistantMessage.value
    //         }
    //       }

    //       // 处理消息结束
    //       if (data.finishReason || data.finished) {
    //         console.log('消息接收完成')
    //         handleRequestEnd(true)
    //       }
    //     }
    //   }
    //   catch (error) {
    //     console.error('解析消息错误:', error)
    //     window.$message?.error('接收消息格式错误')
    //     handleRequestEnd(true)
    //   }
    // },
    onmessage: (event) => {
      try {
        if (event.data) {
          const data = JSON.parse(event.data)
          console.log('接收到消息:', data)

          // 处理流式消息
          if (data.messageContent) {
            const content = data.messageContent
            // 如果是第一条助手消息，先创建消息条目
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

            // 更新最后一条助手消息的内容
            const lastMessageIndex = messages.value.length - 1
            if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
              messages.value[lastMessageIndex].messageContent = currentAssistantMessage.value
            }
          }

          // 处理消息结束
          if (data.finishReason || data.finished) {
            console.log('消息接收完成')
            handleRequestEnd(true)
          }
        }
      }
      catch (error) {
        console.error('解析消息错误:', error)
        window.$message?.error('接收消息格式错误')
        handleRequestEnd(true)
      }
    },
    onerror: (error) => {
      console.error('SSE错误:', error)
      // 如果是用户主动停止，不显示错误消息
      if (!isUserStopped.value) {
        window.$message?.error('连接错误，已停止接收消息')
      }
      handleRequestEnd(true)
    },
    onclose: () => {
      console.log('SSE连接关闭')
      handleRequestEnd(true)
    },
  })

  // 清空输入框（仅对日常对话）
  if (messageType === 'DailyConversation') {
    chatRequestParam.value.messageContent = ''
  }
}
// 按钮点击处理函数
function handleButtonClick(messageType: string | undefined) {
  sendMessage(messageType)
}
// 统一处理请求结束逻辑
function handleRequestEnd(isCompleted: boolean) {
  isLoading.value = false

  const lastMessageIndex = messages.value.length - 1
  if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
    // 更新消息状态
    messages.value[lastMessageIndex].isCompleted = isCompleted
    messages.value[lastMessageIndex].isStopped = isUserStopped.value

    // 如果消息为空且不是用户停止，可能是连接异常，移除空消息
    if (!messages.value[lastMessageIndex].messageContent && !isUserStopped.value) {
      messages.value.pop()
    }
  }

  currentAssistantMessage.value = ''
  controller.value = null
}

// 停止发送
function stopSending() {
  if (controller.value) {
    isUserStopped.value = true
    controller.value.abort()
    isLoading.value = false

    // 立即更新最后一条助手消息的状态
    const lastMessageIndex = messages.value.length - 1
    if (lastMessageIndex >= 0 && messages.value[lastMessageIndex].messageRole === 'ASSISTANT') {
      messages.value[lastMessageIndex].isStopped = true
    }

    window.$message?.info('已停止生成')
  }
}

// 清空对话
function clearMessages() {
  messages.value = []
  chatRequestParam.value.conversationId = ''
  currentAssistantMessage.value = ''
  isUserStopped.value = false
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

// 判断是否显示加载状态
function shouldShowTyping(message: any) {
  return !message.messageContent && !message.isCompleted && !message.isStopped
}

// 判断是否显示停止提示
function shouldShowStopped(message: any) {
  return message.isStopped && message.messageContent
}

// 自动滚动到底部
function scrollToBottom() {
  nextTick(() => {
    const chatContainer = document.querySelector('.overflow-scroll')
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// 组件挂载时，如果有会话ID则加载历史记录
onMounted(() => {
  // 这里可以添加加载历史消息的逻辑
})

// 添加消息类型映射函数
function getMessageTypeText(type: string | number) {
  const typeMap = {
    DailyConversation: '对话',
    ProblemSolvingIdeas: '解题思路',
    AnalyzeComplexity: '复杂度分析',
    OptimizeCode: '代码优化',
    AnalyzeBoundary: '边界分析',
  } as const
  return typeMap[type as keyof typeof typeMap]
}
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <n-flex ref="chatContainer" vertical class="overflow-scroll" :class="{ 'h-112': !isCollapse, 'h-149': isCollapse }">
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
        <!-- 消息列表 -->
        <div v-for="(message, index) in messages" :key="index">
          <!-- 用户消息 -->
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

          <!-- 助手消息 -->
          <n-card v-if="message.messageRole === 'ASSISTANT'" size="small" class="mb-4">
            <n-flex vertical :size="4">
              <n-flex align="center" justify="space-between">
                <n-tag :type="message.messageType === 'DailyConversation' ? 'primary' : 'success'" size="small">
                  {{ getMessageTypeText(message.messageType) }}
                </n-tag>
                <n-time :time="message.responseTime" />
              </n-flex>

              <!-- 优化后的消息显示逻辑 -->
              <MdViewer v-if="message.messageContent" :model-value="message.messageContent" />
              <n-spin v-if="shouldShowTyping(message)" size="small">
                思考中...
              </n-spin>

              <div v-if="shouldShowStopped(message)" class="stopped-indicator">
                <n-text type="warning" depth="3">
                  <n-icon size="14">
                    <icon-park-outline-info />
                  </n-icon>
                  已停止生成
                </n-text>
              </div>
            </n-flex>
          </n-card>
        </div>
      </div>
    </n-flex>

    <!-- 输入区域保持不变 -->
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
              @click="clearMessages"
            >
              清空对话
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
