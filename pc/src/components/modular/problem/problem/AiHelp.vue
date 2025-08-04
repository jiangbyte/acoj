<script lang="ts" setup>
import {
  NAvatar,
  NButton,
  NFlex,
  NIcon,
  NInput,
  NSpin,
  NText,
  useMessage,
} from 'naive-ui'
import {
  CodeSlash,
  PaperPlaneOutline,
  Refresh,
  TrashBinOutline,
} from '@vicons/ionicons5'
import { v4 as uuidv4 } from 'uuid'

const props = defineProps({
  problemId: {
    type: String,
    default: '',
  },
  problemSetId: {
    type: String,
    default: '',
  },
  language: {
    type: String,
    default: '',
  },
  userCode: {
    type: String,
    default: '',
  },
})
const aiPrompt = ref('')
const isAIThinking = ref(false)
const message = useMessage()
const chatContent = ref<HTMLElement | null>(null)

// 生成随机会话ID
const conversationId = ref(uuidv4())

// AI对话相关数据
const aiMessages = ref<Array<{
  role: 'user' | 'assistant'
  content: string
  code?: string
  loading?: boolean
}>>([])

// 处理发送消息
async function handleSendMessage() {
  if (!aiPrompt.value.trim()) {
    message.warning('请输入消息内容')
    return
  }

  if (isAIThinking.value) {
    message.warning('请等待AI回复完成')
    return
  }

  try {
    // 添加用户消息
    aiMessages.value.push({
      role: 'user',
      content: aiPrompt.value,
    })

    // 添加AI回复占位
    const aiMessage = {
      role: 'assistant',
      content: '',
      loading: true,
    }
    aiMessages.value.push(aiMessage as any)

    // 清空输入框
    aiPrompt.value = ''
    isAIThinking.value = true

    // 滚动到底部
    scrollToBottom()

    // 发送请求
    await getAIHelp()
  }
  catch (error) {
    console.error('发送消息出错:', error)
    message.error('发送消息失败')
    // 移除加载中的AI消息
    if (aiMessages.value.length > 0 && aiMessages.value[aiMessages.value.length - 1].loading) {
      aiMessages.value.pop()
    }
  }
  finally {
    isAIThinking.value = false
  }
}

const gateway = import.meta.env.VITE_GATEWAY

async function getAIHelp() {
  try {
    // 请求参数
    const params = {
      message: aiMessages.value.filter(m => m.role === 'user').slice(-1)[0]?.content || '',
      problemId: props.problemId,
      conversantId: conversationId.value, // 可以从用户信息获取
    }

    // 创建EventSource连接
    const eventSource = new EventSource(`${gateway}/core/api/v1/chat/stream?${new URLSearchParams(params)}`)

    // 获取最后一条AI消息的索引
    const lastAIMessageIndex = aiMessages.value.length - 1

    return new Promise((resolve, reject) => {
      // 添加连接打开事件处理
      eventSource.onopen = () => {
        console.log('SSE连接已建立')
      }

      eventSource.onmessage = (event) => {
        const data = event.data
        if (data) {
          // 更新最后一条AI消息的内容
          aiMessages.value[lastAIMessageIndex].content += data
          aiMessages.value[lastAIMessageIndex].loading = false
          scrollToBottom()
        }
      }

      eventSource.onerror = (error) => {
        console.error('EventSource错误:', error)
        eventSource.close()
        if (aiMessages.value[lastAIMessageIndex].content === '') {
          aiMessages.value[lastAIMessageIndex].content = '抱歉，AI回复时出现错误，请重试。'
          aiMessages.value[lastAIMessageIndex].loading = false
        }
        reject(error)
      }

      eventSource.addEventListener('end', () => {
        eventSource.close()
        resolve(aiMessages.value[lastAIMessageIndex].content)
      })
    })
  }
  catch (error) {
    console.error('请求出错:', error)
    message.error('获取AI帮助失败')
    throw error
  }
}

// 重新生成AI回复
async function regenerateAIResponse() {
  if (aiMessages.value.length === 0)
    return

  // 找到最后一条用户消息
  const lastUserMessageIndex = [...aiMessages.value].reverse().findIndex(m => m.role === 'user')
  if (lastUserMessageIndex === -1)
    return

  // 移除之后的AI回复
  aiMessages.value = aiMessages.value.slice(0, aiMessages.value.length - lastUserMessageIndex)

  try {
    isAIThinking.value = true
    // 添加新的AI回复占位
    const aiMessage = {
      role: 'assistant',
      content: '',
      loading: true,
    }
    aiMessages.value.push(aiMessage as any)
    scrollToBottom()

    await getAIHelp()
  }
  catch (error) {
    console.error('重新生成回复出错:', error)
    message.error('重新生成回复失败')
  }
  finally {
    isAIThinking.value = false
  }
}

// 清空AI对话
function clearAIConversation() {
  aiMessages.value = []
  conversationId.value = uuidv4()
}

// 插入代码模板
function insertCodeTemplate() {
  if (!props.language || !props.userCode) {
    message.warning('没有可插入的代码')
    return
  }
  aiPrompt.value = `${aiPrompt.value}\n\`\`\`${props.language}\n${props.userCode}\n\`\`\`\n`
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (chatContent.value) {
      chatContent.value.scrollTop = chatContent.value.scrollHeight
    }
  })
}
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <!-- 对话内容区域 -->
    <div
      ref="chatContent"
      class="chat-content"
      style="height: 450px;"
    >
      <div
        v-for="(message, index) in aiMessages"
        :key="index"
        class="message-container"
        :class="message.role"
      >
        <!-- 头像 -->
        <div class="avatar-container">
          <NAvatar
            round
            :size="32"
            :src="message.role === 'user' ? 'https://randomuser.me/api/portraits/men/1.jpg' : 'https://img.icons8.com/color/96/ai.png'"
          />
        </div>

        <!-- 消息内容 -->
        <div
          class="message-bubble"
          :class="message.role"
        >
          <CommonEditorMdViewer
            v-if="message.content"
            :text="message.content"
            :style="{ background: 'transparent' }"
          />
          <!-- 加载状态 -->
          <NFlex
            v-if="message.loading"
            align="center"
            :size="8"
          >
            <NSpin size="small" />
            <NText depth="3">
              AI正在思考...
            </NText>
          </NFlex>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <NFlex
      vertical
      :size="8"
    >
      <NFlex
        justify="space-between"
        align="center"
      >
        <NText depth="3">
          提示：AI可以帮助解释题目、调试代码或提供优化建议
        </NText>
        <NButton
          text
          type="primary"
          size="small"
          @click="insertCodeTemplate"
        >
          <template #icon>
            <NIcon :component="CodeSlash" />
          </template>
          插入代码模板
        </NButton>
      </NFlex>
      <NInput
        v-model:value="aiPrompt"
        type="textarea"
        placeholder="遇到问题？向AI描述你的疑问或需要帮助的地方。例如：如何优化这段代码的时间复杂度？或者：为什么我的代码在这个测试用例上失败了？"
        :autosize="{ minRows: 4, maxRows: 4 }"
        @keyup.enter.exact="handleSendMessage"
      />
      <NFlex
        justify="end"
        align="center"
      >
        <NButton
          type="primary"
          :loading="isAIThinking"
          @click="handleSendMessage"
        >
          <template #icon>
            <NIcon :component="PaperPlaneOutline" />
          </template>
          发送
        </NButton>
        <NButton
          secondary
          type="primary"
          :disabled="aiMessages.length === 0 || isAIThinking"
          @click="regenerateAIResponse"
        >
          <template #icon>
            <NIcon :component="Refresh" />
          </template>
          重试
        </NButton>
        <NButton
          secondary
          type="error"
          :disabled="aiMessages.length === 0 || isAIThinking"
          @click="clearAIConversation"
        >
          <template #icon>
            <NIcon :component="TrashBinOutline" />
          </template>
          清空
        </NButton>
      </NFlex>
    </NFlex>
  </NFlex>
</template>

<style scoped>
.oj-problem-submit-container {
  height: calc(100vh - 72px);
  max-width: 100%;
  margin: 0;
  padding: 0;
}

:deep(.n-split-pane) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.problem-content-card {
  height: 100%;
  border-radius: 0;
  display: flex;
  flex-direction: column;
}

.submit-card {
  height: 100%;
  border-radius: 0;
  display: flex;
  flex-direction: column;
}

.problem-description {
  line-height: 1.8;
}

.problem-description :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
}

.problem-description :deep(pre) {
  background-color: #f6f8fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.problem-description :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.9em;
}

.problem-description :deep(ul) {
  padding-left: 20px;
}

.problem-description :deep(li) {
  margin-bottom: 4px;
}

.solution-item {
  padding: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.solution-item:hover {
  background-color: #f7fafc;
  transform: translateY(-2px);
}

.solution-summary {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.6;
}

.ai-assistant-collapse {
  margin-top: 8px;
}

.ai-assistant-collapse :deep(.n-collapse-item__header) {
  font-weight: 600;
  background-color: #f0f7ff;
}

.ai-assistant-collapse :deep(.n-collapse-item__content-inner) {
  padding-top: 12px;
  padding-bottom: 12px;
}

.result-collapse {
  margin-top: 8px;
}

.result-collapse :deep(.n-collapse-item__header) {
  font-weight: 600;
}

.result-collapse :deep(.n-collapse-item__content-inner) {
  padding-top: 12px;
  padding-bottom: 12px;
}

.submission-table {
  --n-th-color: #f7fafc;
  --n-td-color: white;
  --n-border-color: #edf2f7;
}

.error-message {
  white-space: pre-wrap;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.9em;
  line-height: 1.5;
  margin: 0;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .main-split {
    flex-direction: column;
  }

  :deep(.n-split-pane) {
    width: 100% !important;
    height: 50% !important;
  }

  .problem-content-card, .submit-card {
    border-radius: 0;
  }
}

/* 自定义 Naive UI 组件样式 */
:deep(.n-tag--primary-type) {
  background-color: #176DF2;
  color: white;
  box-shadow: 0 2px 6px rgba(23, 109, 242, 0.2);
}

:deep(.n-tabs-tab--active) {
  color: #176DF2;
  font-weight: 600;
}

:deep(.n-tabs-bar) {
  background-color: #176DF2;
  height: 3px;
}

:deep(.n-collapse-item__header) {
  font-weight: 600;
}

:deep(.n-collapse-item__content-inner) {
  padding-top: 12px;
}

:deep(.n-tab-pane) {
  height: calc(100vh - 240px - 24px);
  overflow: scroll;
}
.submit-card-content {
  height: calc(100vh - 72px - 48px);
  overflow: scroll;
}

.code-editor-container {
  flex: 1;
  position: relative;
  min-height: 400px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.code-editor {
  flex: 1;
  width: 100%;
}

.fixed-controls {
  position: sticky;
  top: 0;
  background-color: white;
  z-index: 10;
  padding: 8px 0 8px 0;
}

/* 添加对话样式 */
.chat-content {
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 16px;
}

.message-container {
  display: flex;
  gap: 12px;
}

.message-container.user {
  flex-direction: row-reverse;
}

.avatar-container {
  display: flex;
  align-items: flex-start;
  padding-top: 4px;
}

.message-bubble {
  margin-top: 12px;
  /* max-width: 80%; */
  width: 80%;
  padding: 8px 12px;
  border-radius: 12px;
  position: relative;
  line-height: 1.6;
}

.message-bubble.assistant {
  background-color: #ffffff;
  border-top-left-radius: 0;
  margin-right: auto;
}

.message-bubble.user {
  /* background-color: #176DF2; */
  /* color: white; */
  background-color: #ffffff;

  border-top-right-radius: 0;
  margin-left: auto;
}

.message-bubble.assistant::before {
  content: '';
  position: absolute;
  left: -7px;
  top: 0;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  border-right-color: #ffffff;
  border-left: 0;
  margin-top: 0;
}

.message-bubble.user::after {
  content: '';
  position: absolute;
  right: -7px;
  top: 0;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  /* border-left-color: #176DF2; */
  border-left-color: #ffffff;
  border-right: 0;
  margin-top: 0;
}

.code-block {
  margin-top: 8px;
  background-color: #f5f5f5;
  border-radius: 6px;
  overflow: hidden;
}

.n-input-group {
  gap: 8px;
}
</style>
