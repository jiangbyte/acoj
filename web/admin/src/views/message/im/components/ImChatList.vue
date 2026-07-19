<script setup lang="ts">
import { computed, inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import { formatDateTime } from '@/utils'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const data = inject(IM_DATA_KEY)!
const themeVars = useThemeVars()
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

const selectedThreadId = computed(() => (ui as any).selectedThreadId ?? '')

const sortedThreads = computed(() =>
  [...data.threads].sort((a, b) => new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime()),
)

function getLatestMessageText(threadId: string) {
  const history = data.messagesByThread[threadId] ?? []
  const latest = history[history.length - 1]
  if (!latest) {
    const thread = data.threads.find((t) => t.id === threadId)
    return thread?.lastMessage ?? ''
  }
  return `${latest.senderName}：${latest.content}`
}
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <NScrollbar class="h-full">
      <NList v-if="sortedThreads.length" hoverable clickable>
        <NListItem v-for="thread in sortedThreads" :key="thread.id" class="im-list-item cursor-pointer"
          :style="thread.id === selectedThreadId ? { backgroundColor: themeVars.buttonColor2Hover } : {}"
          @click="actions.openThread(thread.id)">
          <div class="im-list-row flex items-start gap-3 px-4 py-3">
            <NBadge class="shrink-0" :value="thread.unreadCount" :max="99" :show-zero="false">
              <NAvatar round :size="40" class="shrink-0">{{ thread.avatarText }}</NAvatar>
            </NBadge>
            <div class="im-list-body">
              <div class="im-list-main-line flex items-center justify-between gap-3">
                <span class="im-ellipsis flex-1 text-sm font-600">{{ thread.title }}</span>
                <span class="shrink-0 text-xs" :style="{ color: themeVars.textColor3 }">{{ formatDateTime(thread.lastMessageAt) }}</span>
              </div>
              <span class="im-ellipsis mt-1 text-xs" :style="{ color: themeVars.textColor3 }">{{ getLatestMessageText(thread.id) }}</span>
            </div>
          </div>
        </NListItem>
      </NList>
      <NEmpty v-else class="py-12" description="暂无会话" />
    </NScrollbar>
  </div>
</template>
