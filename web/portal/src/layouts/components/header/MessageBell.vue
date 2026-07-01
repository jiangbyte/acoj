<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { messageApi } from '@/api'

const router = useRouter()
const { t } = useI18n()

const notificationUnread = ref(0)
const messageUnread = ref(0)
const loading = ref(false)

let eventSource: EventSource | null = null
let pollTimer: number | undefined

const unreadCount = computed(() => notificationUnread.value + messageUnread.value)

onMounted(() => {
  void refreshSummary()
  setupRealtime()
  window.addEventListener('portal-message-summary-refresh', refreshSummary)
  pollTimer = window.setInterval(refreshSummary, 60000)
})

onBeforeUnmount(() => {
  eventSource?.close()
  window.removeEventListener('portal-message-summary-refresh', refreshSummary)
  if (pollTimer) {
    window.clearInterval(pollTimer)
  }
})

async function refreshSummary() {
  if (loading.value) {
    return
  }

  loading.value = true
  try {
    const response = await messageApi.summary()
    notificationUnread.value = response.data?.notification_unread ?? 0
    messageUnread.value = response.data?.message_unread ?? 0
  } finally {
    loading.value = false
  }
}

function setupRealtime() {
  eventSource = messageApi.createEventSource()
  if (!eventSource) {
    return
  }

  eventSource.addEventListener('summary', (event) => {
    try {
      const data = JSON.parse((event as MessageEvent).data)
      notificationUnread.value = data.notification_unread ?? 0
      messageUnread.value = data.message_unread ?? 0
    } catch {
      void refreshSummary()
    }
  })
}

function openMessages() {
  router.push('/messages')
}
</script>

<template>
  <n-tooltip placement="bottom" trigger="hover">
    <template #trigger>
      <CommonWrapper @click="openMessages">
        <n-badge :value="unreadCount" :max="99" style="color: unset">
          <NovaIcon icon="icon-park-outline:remind" />
        </n-badge>
      </CommonWrapper>
    </template>
    {{ t('app.notifications_tips') }}
  </n-tooltip>
</template>
