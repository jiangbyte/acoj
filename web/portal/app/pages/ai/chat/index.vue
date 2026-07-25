<script setup lang="ts">
import type { Message } from '~/utils/ai'
import { useChatHistory } from '~/composables/ai/useChatHistory'
import { useChatActions } from '~/composables/ai/useChatActions'
import { useChatFileUpload } from '~/composables/ai/useChatFileUpload'
import { useChatMessages } from '~/composables/ai/useChatMessages'
import { useModels } from '~/composables/ai/useModels'

definePageMeta({ layout: 'chat' })

const {
  conversations: _conversations,
  activeId,
  sidebarOpen,
  newConversation,
  selectConversation,
  deleteConversation,
  renameConversation,
  getActiveConversation,
  persist,
} = useChatHistory()

const { messages, status, sendMessage, regenerate, editMessage, resetMessages, loadMessages } =
  useChatMessages()
const { files, addFile: _addFile, removeFile, clearFiles, handleFileInput } = useChatFileUpload()
const { model: _model } = useModels()
const {
  showRenameModal,
  showDeleteModal,
  renameTarget,
  deleteTarget,
  renameChat,
  confirmRename,
  cancelRename,
  confirmDelete,
  cancelDelete,
} = useChatActions()

const input = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

function openFileDialog() {
  fileInput.value?.click()
}
const editingMessageId = ref<string | null>(null)
const votes = ref<Record<string, boolean | null>>({})

// 监听当前对话切换
watch(activeId, (id) => {
  if (id) {
    const conv = getActiveConversation()
    if (conv) {
      loadMessages(conv.messages)
      return
    }
  }
  resetMessages()
})

// 消息变更时持久化到对话
watch(
  messages,
  (msgs) => {
    const conv = getActiveConversation()
    if (conv) {
      conv.messages = msgs
      conv.updatedAt = new Date().toISOString()
      persist()
    }
  },
  { deep: true },
)

function handleSubmit() {
  if (!input.value.trim()) return

  if (!activeId.value) {
    newConversation()
  }

  const uploadedFiles = files.value
    .filter((f) => f.status === 'uploaded')
    .map((f) => ({ url: f.url, mediaType: f.type }))

  sendMessage(input.value, uploadedFiles.length > 0 ? uploadedFiles : undefined)
  input.value = ''
  clearFiles()
}

function _handleNew() {
  newConversation()
}

function _handleSelect(id: string) {
  selectConversation(id)
}

function _handleDelete(id: string) {
  deleteConversation(id)
}

function _handleRename(id: string, title: string) {
  renameConversation(id, title)
}

function handleQuickSubmit(label: string) {
  input = label
  handleSubmit()
}

function handleEdit(message: Message) {
  editingMessageId.value = message.id
}

function handleSaveEdit(message: Message, text: string) {
  editingMessageId.value = null
  editMessage(message.id, text)
}

function handleCancelEdit() {
  editingMessageId.value = null
}

function handleRegenerate(_message: Message) {
  regenerate()
}

const hasMessages = computed(() => messages.value.length > 0 || status.value !== 'ready')
const activeConv = computed(() => getActiveConversation())

// 空状态问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  let timeGreeting = '晚上好'
  if (hour < 6) timeGreeting = '夜深了'
  else if (hour < 12) timeGreeting = '上午好'
  else if (hour < 14) timeGreeting = '中午好'
  else if (hour < 18) timeGreeting = '下午好'
  return timeGreeting
})

const quickChats = [
  { label: '帮我写一封邮件', icon: 'i-lucide-mail' },
  { label: '解释一下 Vue 响应式原理', icon: 'i-lucide-code-2' },
  { label: '上海今天天气怎么样？', icon: 'i-lucide-sun' },
  { label: '给我展示一个图表', icon: 'i-lucide-line-chart' },
  { label: 'CSS 居中最佳实践', icon: 'i-lucide-layout' },
]
</script>

<template>
  <!-- 导航栏 -->
  <UDashboardNavbar class="absolute top-0 inset-x-0 border-b-0 z-10 backdrop-blur sm:px-4">
    <template #left>
      <UDashboardSidebarCollapse v-model:open="sidebarOpen" />
      <AiChatTitle
        :title="activeConv?.title ?? '新对话'"
        :is-owner="true"
        @rename="activeId && activeConv && renameChat(activeId, activeConv.title)"
        @delete="activeId && activeConv && deleteChat(activeId, activeConv.title)"
      />
    </template>

    <template #right>
      <UColorModeButton />
    </template>
  </UDashboardNavbar>

  <div class="flex-1 flex flex-col pt-(--ui-header-height) min-h-0">
    <!-- 空状态 -->
    <div v-if="!hasMessages" class="flex-1 flex items-center justify-center">
      <UContainer class="flex-1 flex flex-col items-center gap-4 sm:gap-6 py-8">
        <h1 class="text-3xl sm:text-4xl text-highlighted font-bold text-center">
          {{ greeting }}！我是 HEI AI
        </h1>
        <p class="text-muted text-sm text-center">输入你的问题，或选择一个快捷问题开始</p>

        <UChatPrompt
          v-model="input"
          status="ready"
          color="neutral"
          variant="subtle"
          class="w-full max-w-xl [view-transition-name:chat-prompt]"
          :ui="{ base: 'px-1.5' }"
          @submit="handleSubmit"
        >
          <template v-if="files.length > 0" #header>
            <AiChatFiles :files="files" @remove="removeFile" />
          </template>

          <template #footer>
            <div class="flex items-center gap-1">
              <AiChatFileUploadButton :open="openFileDialog" />
              <AiChatModelSelect />
            </div>
            <UChatPromptSubmit color="neutral" size="sm" />
          </template>
        </UChatPrompt>

        <div class="flex flex-wrap justify-center gap-2 max-w-xl">
          <UButton
            v-for="quick in quickChats"
            :key="quick.label"
            :icon="quick.icon"
            :label="quick.label"
            size="sm"
            color="neutral"
            variant="outline"
            class="rounded-full"
            @click="handleQuickSubmit(quick.label)"
          />
        </div>
      </UContainer>
    </div>

    <!-- 聊天区 -->
    <UChatPalette v-else class="flex-1 flex flex-col min-h-0">
      <UContainer class="flex-1 flex flex-col min-h-0 overflow-y-auto">
        <UChatMessages
          :messages="messages"
          :status="status"
          should-auto-scroll
          should-scroll-to-bottom
          auto-scroll
          :compact="true"
          :user="{ side: 'right', variant: 'soft', icon: 'i-lucide-user' }"
          :assistant="{ side: 'left', variant: 'naked', icon: 'i-lucide-sparkles' }"
          class="pb-4 sm:pb-6"
        >
          <template #indicator>
            <div class="flex items-center gap-1.5">
              <AiChatIndicator />
              <UChatShimmer text="正在思考..." class="text-sm" />
            </div>
          </template>

          <template #files="{ message, parts }">
            <AiChatFilePreview
              v-for="(part, index) in parts"
              :key="`${message.id}-${index}`"
              :file="{
                id: `${message.id}-${index}`,
                name: part.url?.split('/').pop() || 'file',
                type: part.mediaType || 'application/octet-stream',
                url: part.url || '',
                status: 'uploaded',
              }"
              size="3xl"
            />
          </template>

          <template #content="{ message }">
            <AiChatMessageContent
              :message="message as Message"
              :editing="editingMessageId === (message as Message).id"
              @save="handleSaveEdit"
              @cancel-edit="handleCancelEdit"
            />
          </template>

          <template #actions="{ message }">
            <AiChatMessageActions
              :message="message as Message"
              :streaming="
                status === 'streaming' &&
                (message as Message).id === messages[messages.length - 1]?.id
              "
              :editing="editingMessageId === (message as Message).id"
              :vote="votes[(message as Message).id] ?? null"
              @vote="handleVote"
              @edit="handleEdit"
              @regenerate="handleRegenerate"
            />
          </template>
        </UChatMessages>
      </UContainer>

      <!-- 输入区 -->
      <UContainer>
        <UChatPrompt
          v-model="input"
          :status="status"
          :error="status === 'error' ? new Error('发送失败，请重试') : undefined"
          color="neutral"
          variant="subtle"
          class="shrink-0 [view-transition-name:chat-prompt] z-10"
          :ui="{ base: 'px-1.5 !bg-transparent !ring-0 !shadow-none' }"
          @submit="handleSubmit"
        >
          <template v-if="files.length > 0" #header>
            <AiChatFiles :files="files" @remove="removeFile" />
          </template>

          <template #footer>
            <div class="flex items-center gap-1">
              <AiChatFileUploadButton :open="openFileDialog" />
              <AiChatModelSelect />
            </div>

            <UChatPromptSubmit
              :status="status"
              color="neutral"
              size="sm"
              @stop="status = 'ready'"
              @reload="regenerate"
            />
          </template>
        </UChatPrompt>
      </UContainer>
    </UChatPalette>
  </div>

  <!-- 文件选择器 -->
  <input ref="fileInput" type="file" multiple class="hidden" @change="handleFileInput" />

  <!-- 重命名弹窗 -->
  <AiModalRename
    :open="Boolean(showRenameModal && renameTarget)"
    :title="renameTarget?.title ?? ''"
    @confirm="
      (t: string) => {
        renameConversation(renameTarget!.id, t)
        confirmRename(t)
      }
    "
    @cancel="cancelRename"
  />

  <!-- 删除确认弹窗 -->
  <AiModalConfirm
    :open="Boolean(showDeleteModal && deleteTarget)"
    :title="'删除对话'"
    :description="deleteTarget ? `确定要删除「${deleteTarget.title}」吗？此操作无法撤销。` : ''"
    color="error"
    @confirm="
      () => {
        deleteConversation(deleteTarget!.id)
        confirmDelete()
      }
    "
    @cancel="cancelDelete"
  />
</template>
