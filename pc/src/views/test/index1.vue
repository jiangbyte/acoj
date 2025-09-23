<script lang="ts" setup>
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useTokenStore } from '@/stores'
import { onUnmounted, ref } from 'vue'

// 存储STOMP客户端实例
const stompClient = ref<Client | null>(null)
const messageRef = ref('')
const tokenStore = useTokenStore()
const submitId = ref('')
const messages = ref<string[]>([])
const isConnected = ref(false)
const subscription = ref<any>(null)

function connectWebSocket() {
  if (!submitId.value) {
    console.error('请先输入submitId')
    return
  }

  // 创建SockJS连接
  const socket = new SockJS('/oj/ws/judge/status')
  // 创建STOMP客户端
  stompClient.value = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    connectHeaders: {
      Authorization: tokenStore.getToken || '',
    },

    onConnect: (frame) => {
      console.log('STOMP连接成功:', frame)
      isConnected.value = true

      const topic = `/topic/judge/status/${submitId.value}`

      subscription.value = stompClient.value?.subscribe(topic, (message) => {
        console.log('收到服务端消息:', message.body)
        messageRef.value = message.body
        messages.value.push(`收到: ${message.body} - ${new Date().toLocaleTimeString()}`)

        // 检查关闭指令
        if (message.body === 'CLOSE_CONNECTION') {
          setTimeout(() => {
            disconnectWebSocket()
            messages.value.push('服务端要求关闭连接，连接已断开')
          }, 1000)
        }
      }, {
        Authorization: tokenStore.getToken || '',
      })

      // 发送连接消息到服务端
      const destination = `/app/judge/status/${submitId.value}`
      stompClient.value?.publish({
        destination,
        body: JSON.stringify({
          problemId: '1958869993346752514',
          setId: null,
          language: 'cpp',
          code: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b;\n    return 0;\n}',
          submitType: true,
        }),
        headers: {
          Authorization: tokenStore.getToken || '',
        },
      })

      messages.value.push('已连接，等待服务端发送消息...')
    },

    // onStompError: (frame) => {
    //   console.error('STOMP错误:', frame)
    //   isConnected.value = false
    //   messages.value.push(`STOMP错误: ${frame.headers?.message}`)
    // },

    // onWebSocketError: (event) => {
    //   console.error('WebSocket错误:', event)
    //   isConnected.value = false
    //   messages.value.push('WebSocket错误')
    // },

    // onDisconnect: () => {
    //   console.log('连接断开')
    //   isConnected.value = false
    //   messages.value.push('连接已断开')
    // },
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
    console.log('WebSocket连接已断开')
    messages.value.push('主动断开连接')
  }
}

// 在组件卸载时断开连接
onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<template>
  <div class="status-container">
    <h3>WebSocket 测试客户端</h3>

    <div class="connection-status">
      连接状态: {{ isConnected ? '已连接' : '未连接' }}
    </div>

    <div class="input-group">
      <n-input v-model:value="submitId" placeholder="请输入submitId" />
      <n-button :disabled="isConnected" @click="connectWebSocket">
        开始连接
      </n-button>
      <n-button :disabled="!isConnected" @click="disconnectWebSocket">
        断开连接
      </n-button>
    </div>

    <div class="messages-container">
      <h4>消息记录:</h4>
      <div v-for="(msg, index) in messages" :key="index" class="message-item">
        {{ msg }}
      </div>
    </div>

    <div class="current-message">
      <h4>最新消息:</h4>
      {{ messageRef }}
    </div>
  </div>
</template>

<style scoped>
.status-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.input-group {
  display: flex;
  gap: 10px;
  margin: 20px 0;
  align-items: center;
}

.input-group .n-input {
  flex: 1;
}

.messages-container {
  margin: 20px 0;
  border: 1px solid #ddd;
  padding: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.message-item {
  padding: 5px;
  border-bottom: 1px solid #eee;
  font-family: monospace;
  font-size: 12px;
}

.current-message {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.connection-status {
  margin-bottom: 10px;
  font-weight: bold;
  color: v-bind('isConnected ? "green" : "red"');
}
</style>
