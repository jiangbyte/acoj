<script lang="ts" setup>
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useTokenStore, useUserStore } from '@/stores'
import { v4 as uuidv4 } from 'uuid'

const tokenStore = useTokenStore()
const userStore = useUserStore()

const stompClient = ref<Client | null>(null)
const isConnected = ref(false)
const subscription = ref<any>(null)

const submitParam = ref({
  problemId: '1958869993346752514',
  setId: null,
  language: 'cpp',
  code: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b;\n    return 0;\n}',
  submitType: false,
  judgeTaskId: '',
  userId: userStore.getUserId,
})
const judgeResult = ref({})

function executeCode() {
  if (isConnected.value) {
    console.warn('WebSocket 已连接，不能重复连接')
    return
  }

  // 任务ID生成
  submitParam.value.judgeTaskId = `task-${uuidv4()}`
  const socket = new SockJS('/oj/ws/judge/status')

  // 添加连接错误事件监听
  socket.onerror = (error) => {
    console.error('SockJS 连接失败:', error)
    // 可以在这里添加重试逻辑或其他错误处理
  }

  // 添加连接关闭事件监听
  socket.onclose = (event) => {
    console.warn('SockJS 连接关闭:', event)
    isConnected.value = false
  }

  stompClient.value = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    connectHeaders: {
      Authorization: tokenStore.getToken || '',
    },

    onConnect: (frame) => {
      console.log('STOMP 连接成功:', frame)
      isConnected.value = true

      const topic = `/topic/judge/status/${submitParam.value.judgeTaskId}`

      subscription.value = stompClient.value?.subscribe(topic, (message) => {
        // 接收到关闭连接的消息
        if (message.body === 'CLOSE_CONNECTION') {
          disconnectWebSocket()
          return
        }

        // 如果不是关闭连接的消息，则解析JSON结果
        try {
          judgeResult.value = JSON.parse(message.body)
        }
        catch (error) {
          console.error('解析 JSON 错误:', error)
        }
      }, {
        Authorization: tokenStore.getToken || '',
      })

      // 发送连接消息到服务端
      const destination = `/app/judge/status/${submitParam.value.judgeTaskId}`
      stompClient.value?.publish({
        destination,
        body: JSON.stringify(submitParam.value),
        headers: {
          Authorization: tokenStore.getToken || '',
        },
      })
    },

    onStompError: (frame) => {
      console.error('STOMP 协议错误:', frame)
    },

    onWebSocketError: (event) => {
      console.error('WebSocket 底层错误:', event)
    },

  })

  // 激活连接
  stompClient.value.activate()
}

function disconnectWebSocket() {
  if (stompClient.value) {
    // 取消订阅
    if (subscription.value) {
      subscription.value.unsubscribe()
      subscription.value = null
    }

    stompClient.value.deactivate()
    stompClient.value = null
    isConnected.value = false
    console.log('WebSocket 连接已断开')
  }
}

// 在组件卸载时断开连接
onUnmounted(() => {
  disconnectWebSocket()
})

// 格式化JSON显示
const formattedSubmitParam = computed(() => {
  try {
    return JSON.stringify(submitParam.value, null, 2)
  }
  // eslint-disable-next-line unused-imports/no-unused-vars
  catch (e) {
    return JSON.stringify(submitParam.value)
  }
})

const formattedJudgeResult = computed(() => {
  if (!judgeResult.value)
    return '暂无结果'

  try {
    if (typeof judgeResult.value === 'string') {
      // 尝试解析字符串形式的JSON
      const parsed = JSON.parse(judgeResult.value)
      return JSON.stringify(parsed, null, 2)
    }
    return JSON.stringify(judgeResult.value, null, 2)
  }
  // eslint-disable-next-line unused-imports/no-unused-vars
  catch (e) {
    return judgeResult.value
  }
})
</script>

<template>
  <div>
    <n-flex vertical>
      <n-input v-model:value="submitParam.problemId" placeholder="请输入problemId" />
      <n-input v-model:value="submitParam.setId" placeholder="请输入setId" />
      <n-input v-model:value="submitParam.language" placeholder="请输入语言" />
      <n-input v-model:value="submitParam.code" type="textarea" placeholder="请输入代码" />
      <n-switch v-model:value="submitParam.submitType">
        <template #checked>
          提交类型: 测试运行
        </template>
        <template #unchecked>
          提交类型: 正式提交
        </template>
      </n-switch>
      <n-button :disabled="isConnected" type="primary" @click="executeCode">
        {{ isConnected ? '执行中...' : '执行代码' }}
      </n-button>
      <n-card title="提交内容" size="small">
        <pre class="json-display">{{ formattedSubmitParam }}</pre>
      </n-card>
      <n-card title="连接状态" size="small">
        <n-tag :type="isConnected ? 'success' : 'default'">
          {{ isConnected ? '已连接' : '未连接' }}
        </n-tag>
        <div v-if="submitParam.judgeTaskId" class="task-info">
          任务ID: {{ submitParam.judgeTaskId }}
        </div>
      </n-card>
      <n-card title="判题结果" size="small">
        <div v-if="judgeResult">
          <pre class="json-display">{{ formattedJudgeResult }}</pre>
        </div>
        <div v-else class="no-result">
          暂无结果
        </div>
      </n-card>
    </n-flex>
  </div>
</template>

<style scoped>
</style>
