<script setup lang="ts">
import { computed, inject, nextTick, reactive, ref, watch } from 'vue'
import { useThemeVars } from 'naive-ui'
import { formatDateTime } from '@/utils'
import type { MockAttachment, MockMessage, MockThread } from '../mock'
import { IM_ACTIONS_KEY, IM_UI_STATE_KEY, IM_DATA_KEY } from '../im-provide'

const props = defineProps<{
  thread: MockThread
  draft?: { text: string; attachments: MockAttachment[] }
}>()
const emit = defineEmits<{
  close: []
  'update:draft': [draft: { text: string; attachments: MockAttachment[] }]
}>()

const themeVars = useThemeVars()
const data = inject(IM_DATA_KEY)!
const profile = data.profile
const actions = inject(IM_ACTIONS_KEY)!
const ui = inject(IM_UI_STATE_KEY)!

const composerText = ref(props.draft?.text ?? '')
const selectedAttachments = ref<MockAttachment[]>(props.draft?.attachments ?? [])
watch(() => props.draft, (d) => {
  if (d) { composerText.value = d.text; selectedAttachments.value = d.attachments }
}, { immediate: false })
watch([composerText, selectedAttachments], () => {
  emit('update:draft', { text: composerText.value, attachments: selectedAttachments.value })
}, { deep: true })
const fileInputRef = ref<HTMLInputElement | null>(null)
const messageListRef = ref<HTMLElement | null>(null)

const messageState = reactive({
  visibleStart: 0,
  visibleMessages: [] as MockMessage[],
  loadingOlder: false,
})

const visibleMessages = computed(() => messageState.visibleMessages)
const hasMoreOlder = computed(() => messageState.visibleStart > 0)

const threadMessages = computed(() => props.thread ? (data.messagesByThread[props.thread.id] ?? []) : [])

function syncVisibleMessages() {
  if (!props.thread) return
  const history = threadMessages.value
  messageState.visibleStart = Math.max(0, history.length - 6)
  messageState.visibleMessages = history.slice(messageState.visibleStart)
}

async function loadOlderMessages() {
  if (messageState.loadingOlder || !hasMoreOlder.value) return
  const history = threadMessages.value
  const currentStart = messageState.visibleStart
  const nextStart = Math.max(0, currentStart - 6)
  const previousNode = messageListRef.value
  const previousHeight = previousNode?.scrollHeight ?? 0
  messageState.loadingOlder = true
  messageState.visibleStart = nextStart
  messageState.visibleMessages = [...history.slice(nextStart, currentStart), ...messageState.visibleMessages]
  await nextTick()
  if (previousNode) previousNode.scrollTop = previousNode.scrollHeight - previousHeight + (previousNode?.scrollTop ?? 0)
  messageState.loadingOlder = false
}

function handleMessageScroll(event: Event) {
  const target = event.currentTarget as HTMLElement
  if (target.scrollTop <= 24) void loadOlderMessages()
}

function scrollMessagesToBottom() {
  const target = messageListRef.value
  if (target) target.scrollTop = target.scrollHeight
}

function handleAddFileButtonClick() { fileInputRef.value?.click() }

function handleFileInputChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (!files.length) return
  selectedAttachments.value = [
    ...selectedAttachments.value,
    ...files.map((file) => ({ name: file.name, size: file.size, type: file.type || 'application/octet-stream' })),
  ]
  input.value = ''
}

function removeAttachment(index: number) {
  selectedAttachments.value = selectedAttachments.value.filter((_, i) => i !== index)
}

function formatFileSize(size: number) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function messageBubbleStyle(isMine: boolean) {
  const v = themeVars.value
  return isMine
    ? { backgroundColor: v.primaryColor, border: `1px solid ${v.primaryColor}`, color: '#ffffff' }
    : { backgroundColor: v.cardColor, border: `1px solid ${v.borderColor}`, color: v.textColor1 }
}

function sendMessage() {
  const content = composerText.value.trim()
  const attachments = selectedAttachments.value.map((item) => ({ ...item }))
  if (!content && !attachments.length) return

  const history = threadMessages.value
  const message: MockMessage = {
    id: `${props.thread.id}-local-${Date.now()}`,
    threadId: props.thread.id,
    senderName: profile.nickname,
    senderSide: 'me',
    content,
    createdAt: new Date().toISOString(),
    attachments: attachments.length ? attachments : undefined,
  }
  history.push(message)
  data.messagesByThread[props.thread.id] = history

  const thread = data.threads.find((t) => t.id === props.thread.id)
  if (thread) {
    thread.lastMessage = content || `发送了 ${attachments.length} 个文件`
    thread.lastMessageAt = message.createdAt
  }

  composerText.value = ''
  selectedAttachments.value = []
  syncVisibleMessages()
  void nextTick(() => scrollMessagesToBottom())
}

watch(() => props.thread?.id, (newId) => {
  if (newId) syncVisibleMessages()
}, { immediate: true })
</script>

<template>
  <div v-if="thread" class="flex h-full min-h-0 flex-col">
    <div class="flex items-center justify-between gap-3 border-b px-4 py-3" :style="{ borderColor: themeVars.borderColor }">
      <div class="flex min-w-0 items-center gap-3 overflow-hidden">
        <NButton v-if="ui.isMobile.value" text size="small" @click="actions.backToListPane()">
          <template #icon><NovaIcon icon="icon-park-outline:arrow-left" :size="18" /></template>
        </NButton>
        <NAvatar round :size="42" class="shrink-0">{{ thread.avatarText }}</NAvatar>
        <NThing :title="thread.title" :description="thread.subtitle" />
      </div>
      <NFlex :size="4">
        <NButton text size="small" aria-label="关闭会话" @click="emit('close')">
          <template #icon><NovaIcon icon="icon-park-outline:close" :size="18" /></template>
        </NButton>
      </NFlex>
    </div>

    <div class="flex min-h-0 flex-1 flex-col">
      <div v-if="hasMoreOlder" class="border-b px-4 py-2 text-center" :style="{ borderColor: themeVars.borderColor }">
        <NButton text size="small" :loading="messageState.loadingOlder" @click="loadOlderMessages">上滑加载更早消息</NButton>
      </div>
      <div ref="messageListRef" class="flex min-h-0 flex-1 flex-col gap-3 overflow-auto px-4 py-4" @scroll.passive="handleMessageScroll">
        <div v-if="visibleMessages.length" class="flex flex-col gap-3">
          <div v-for="message in visibleMessages" :key="message.id" class="flex items-start gap-2"
            :class="message.senderSide === 'me' ? 'flex-row-reverse' : ''">
            <NAvatar round :size="28" class="shrink-0">
              {{ message.senderSide === 'me' ? profile.avatarText : thread.avatarText }}
            </NAvatar>
            <div class="min-w-0 max-w-[min(68%,640px)]">
              <div class="mb-1 flex gap-2 text-xs" :class="message.senderSide === 'me' ? 'justify-end' : 'justify-start'" :style="{ color: themeVars.textColor3 }">
                <span>{{ message.senderName }}</span>
                <span>{{ formatDateTime(message.createdAt) }}</span>
              </div>
              <div class="rounded-2 px-3 py-2 text-sm leading-6" :style="messageBubbleStyle(message.senderSide === 'me')">
                <div class="break-words">{{ message.content }}</div>
                <div v-if="message.attachments?.length" class="mt-2 flex flex-col gap-2">
                  <div v-for="attachment in message.attachments" :key="attachment.name"
                    class="flex items-center gap-3 rounded-1 border px-3 py-2"
                    :class="message.senderSide === 'me' ? 'border-white/20 bg-white/10' : ''"
                    :style="message.senderSide !== 'me' ? { borderColor: themeVars.borderColor, backgroundColor: themeVars.bodyColor } : {}">
                    <NovaIcon icon="icon-park-outline:file" :size="16" />
                    <div class="min-w-0 flex-1">
                      <div class="im-ellipsis text-xs font-600">{{ attachment.name }}</div>
                      <div class="mt-0.5 text-[10px] opacity-80">{{ formatFileSize(attachment.size) }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <NEmpty v-else class="py-12" description="暂无消息" />
      </div>

      <div class="border-t p-4" :style="{ borderColor: themeVars.borderColor }">
        <input ref="fileInputRef" type="file" multiple class="hidden" @change="handleFileInputChange" />
        <div v-if="selectedAttachments.length" class="mb-3 flex flex-wrap gap-2">
          <NTag v-for="(attachment, index) in selectedAttachments" :key="`${attachment.name}-${index}`"
            closable :bordered="false" @close="removeAttachment(index)">
            <template #icon><NovaIcon icon="icon-park-outline:file" :size="14" /></template>
            {{ attachment.name }}
          </NTag>
        </div>
        <NInput v-model:value="composerText" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }"
          placeholder="输入消息，Enter 发送，Shift + Enter 换行" @keydown.enter.exact.prevent="sendMessage" />
        <div class="mt-3 flex items-center justify-between gap-3 text-xs" :style="{ color: themeVars.textColor3 }">
          <div class="flex items-center gap-2">
            <NButton quaternary size="small" aria-label="发送文件" @click="handleAddFileButtonClick">
              <template #icon><NovaIcon icon="icon-park-outline:folder-upload" :size="16" /></template>
            </NButton>
          </div>
          <NButton type="primary" :disabled="!composerText.trim() && !selectedAttachments.length" @click="sendMessage">发送</NButton>
        </div>
      </div>
    </div>
  </div>
<NEmpty v-else class="h-full flex items-center justify-center" description="请选择会话" />
</template>
