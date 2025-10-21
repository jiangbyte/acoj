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

// 连接并发送消息
function sendMessage(messageType: string = 'DailyConversation') {
  // 生成会话ID（如果是新会话）
  if (!chatRequestParam.value.conversationId) {
    chatRequestParam.value.conversationId = `task-${uuidv4()}`
  }
  isLoading.value = true

  // 更新消息类型和内容
  chatRequestParam.value.messageType = messageType
  chatRequestParam.value.userCode = props.userCode
  chatRequestParam.value.language = props.language

  // 创建中止控制器
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
        console.log('SSE连接已建立')
      }
      else {
        window.$message?.error(`连接失败: ${response.status}`)
      }
    },
    onmessage: (event) => {
      try {
        if (event.data) {
          const data = JSON.parse(event.data)
          console.log('接收到消息:', data)
          // TODO 处理消息
        }
      }
      catch (error) {
        console.error('解析消息错误:', error)
        window.$message?.error('接收消息格式错误')
      }
    },
    onerror: (error) => {
      console.error('SSE错误:', error)
      window.$message?.error('连接错误，已停止接收消息')
      isLoading.value = false
    },
    onclose: () => {
      console.log('SSE连接关闭')
      isLoading.value = false
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
  chatRequestParam.value.conversationId = ''
}

const isCollapse = ref(false)
function handleCollapse() {
  isCollapse.value = !isCollapse.value
}

// 组件挂载时，如果有会话ID则加载历史记录
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
            思考中...
          </span>
        </div>
      </div>
    </n-flex>
    <!-- <MdViewer :model-value="messageContentRef" /> -->
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
