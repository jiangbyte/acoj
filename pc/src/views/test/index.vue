<script lang="ts" setup>
import { LLMClient } from '@/utils'
// import { useProSubmitFetch } from '@/composables'

// // 使用示例
// async function exampleUsage() {
//   const { proSubmitDetail } = useProSubmitFetch()
//   const apiCallTask = async () => {
//     proSubmitDetail({ id: '1961409364998406146' }).then(({ data }) => {
//       console.log('获取到的数据:', data)
//     })
//   }
//   // 创建轮询工具实例，每3秒执行一次
//   const poller = new PollingTool(apiCallTask, 1000)
//   // 启动轮询
//   poller.start()

//   // 10秒后暂停
//   setTimeout(() => {
//     poller.pause()
//     console.log('已暂停，状态:', poller.getStatus())
//   }, 10000)

//   // 15秒后恢复
//   setTimeout(() => {
//     poller.resume()
//     console.log('已恢复，状态:', poller.getStatus())
//   }, 15000)

//   // 20秒后更新间隔为5秒
//   setTimeout(() => {
//     poller.updateInterval(5000)
//     console.log('已更新间隔，状态:', poller.getStatus())
//   }, 20000)

//   // 30秒后停止
//   setTimeout(() => {
//     poller.stop()
//     console.log('已停止，状态:', poller.getStatus())
//   }, 30000)
// }

// const message = ref('')
// const gateway = import.meta.env.VITE_GATEWAY
// const client = new LLMClient(gateway, 'rUkInWFE4p7H0FxJMPmC3YQ9jdKkwphVWqiUDulOiiRu1LMzfDnf4PQI0Wbq1AA7')
// async function sendMessage() {

// }
</script>

<template>
  <div>
    <!-- <n-button @click="exampleUsage">
      开始提交轮询
    </n-button> -->
    <!-- <n-input v-model="message" />
    <n-button @click="sendMessage">
      发送消息
    </n-button> -->
  </div>
</template>

<style scoped>
.chat-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.history-section {
  margin-bottom: 20px;
}

.message {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 8px;
}

.message.user {
  background: #e6f7ff;
  border-left: 4px solid #1890ff;
}

.message.assistant {
  background: #f6ffed;
  border-left: 4px solid #52c41a;
}

.current-response {
  padding: 12px;
  background: #fff2e8;
  border-left: 4px solid #fa8c16;
  border-radius: 8px;
  margin-bottom: 20px;
  min-height: 20px;
}

.input-section {
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.error-message {
  color: #ff4d4f;
  background: #fff2f0;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ffccc7;
}
</style>
