<script setup lang="ts">
import { useClientStore } from '@/stores'
import { useClientStatusFetch } from '@/composables'
import { useAutoRequest } from 'alova/client'

const { serverHeartbeat } = useClientStatusFetch()

const gateway = import.meta.env.VITE_GATEWAY
const version = import.meta.env.VITE_VERSION
const hearbeatUrl = import.meta.env.VITE_HEARTBEAT_URL

const clientStore = useClientStore()
clientStore.initializeClientId()
console.log('Client ID:', clientStore.clientId)

const url = `${gateway}${hearbeatUrl}/${clientStore.clientId}`
const eventSource = new EventSource(url)

// 通用事件监听 - 所有未被特定事件处理器处理的事件都会触发
eventSource.addEventListener('initial_data', (event) => {
  console.log(event.data)
})
eventSource.addEventListener('heartbeat', (event) => {
  console.log(event.data)
  useAutoRequest(serverHeartbeat(clientStore.clientId as string))
})
eventSource.addEventListener('heartbeat_ack', (event) => {
  console.log(event.data)
})

// 主动关闭连接
onUnmounted(() => {
  eventSource.close()
})
</script>

<template>
  <n-text depth="3">
    Copyright © 2024-2025 Astro Code Online Judge. All rights reserved. Built with ❤️ by Charlie. Version {{ version }}
  </n-text>
</template>

<style scoped>

</style>
