<script setup lang="ts">
import type { Conversation } from '~/composables/ai/useChatHistory'
import { useChatHistory } from '~/composables/ai/useChatHistory'
import { useChatActions } from '~/composables/ai/useChatActions'

defineProps<{
  conversations: Conversation[]
  activeId: string | null
  collapsed: boolean
}>()

const emit = defineEmits<{
  select: [id: string]
  delete: [id: string]
  rename: [id: string, title: string]
  new: []
  search: []
}>()

const { groups } = useChatHistory()
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

async function handleRename(conv: Conversation) {
  const newTitle = await renameChat(conv.id, conv.title)
  if (newTitle) {
    emit('rename', conv.id, newTitle)
  }
}

async function handleDelete(conv: Conversation) {
  const confirmed = await deleteChat(conv.id, conv.title)
  if (confirmed) {
    emit('delete', conv.id)
  }
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 固定头部 -->
    <div v-if="!collapsed" class="shrink-0 px-3">
      <NuxtLink
        to="/"
        class="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm text-muted hover:bg-accented/50 hover:text-default transition-colors no-underline"
      >
        <UIcon name="i-lucide-home" class="size-4 shrink-0" />
        <span class="truncate">首页</span>
      </NuxtLink>
      <div
        class="flex items-center gap-2 px-3 py-1.5 rounded-md cursor-pointer text-sm text-muted hover:bg-accented/50 hover:text-default transition-colors"
        @click="emit('new')"
      >
        <UIcon name="i-lucide-circle-plus" class="size-4 shrink-0" />
        <span class="truncate">新对话</span>
      </div>
      <div
        class="flex items-center gap-2 px-3 py-1.5 rounded-md cursor-pointer text-sm text-muted hover:bg-accented/50 hover:text-default transition-colors"
        @click="emit('search')"
      >
        <UIcon name="i-lucide-search" class="size-4 shrink-0" />
        <span class="truncate">搜索</span>
      </div>
      <USeparator class="my-2" />
    </div>

    <template v-if="!collapsed">
      <div v-if="conversations.length === 0" class="px-3 py-4 text-center shrink-0">
        <p class="text-xs text-muted">暂无对话</p>
      </div>

      <!-- 可滚动的对话列表 -->
      <div class="flex-1 overflow-y-auto min-h-0 space-y-4 px-3">
        <div v-for="group in groups" :key="group.label" class="space-y-0.5">
          <p class="text-xs font-medium text-muted uppercase tracking-wider">{{ group.label }}</p>
          <div class="space-y-0.5">
            <div
              v-for="conv in group.conversations"
              :key="conv.id"
              class="group flex items-center gap-1 px-3 py-1.5 rounded-md cursor-pointer text-sm transition-colors"
              :class="
                conv.id === activeId
                  ? 'bg-primary/10 text-primary font-medium'
                  : 'hover:bg-accented/50 text-muted hover:text-default'
              "
              @click="emit('select', conv.id)"
            >
              <UIcon name="i-lucide-message-circle" class="size-4 shrink-0" />
              <span class="truncate flex-1">{{ conv.title }}</span>
              <UDropdownMenu
                :items="[
                  [
                    {
                      label: '重命名',
                      icon: 'i-lucide-pencil',
                      onSelect: () => handleRename(conv),
                    },
                    {
                      label: '删除',
                      icon: 'i-lucide-trash-2',
                      color: 'error' as const,
                      onSelect: () => handleDelete(conv),
                    },
                  ],
                ]"
                :content="{ align: 'start' }"
              >
                <UButton
                  icon="i-lucide-more-horizontal"
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  class="opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
                />
              </UDropdownMenu>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="flex flex-col items-center gap-1 px-2">
        <UTooltip
          v-for="conv in conversations.slice(0, 5)"
          :key="conv.id"
          :text="conv.title"
          side="right"
        >
          <UButton
            icon="i-lucide-message-circle"
            color="neutral"
            variant="ghost"
            size="sm"
            :class="{ 'text-primary': conv.id === activeId }"
            @click="emit('select', conv.id)"
          />
        </UTooltip>
      </div>
    </template>

    <!-- 重命名弹窗 -->
    <AiModalRename
      v-if="showRenameModal && renameTarget"
      :title="renameTarget.title"
      @confirm="confirmRename"
      @cancel="cancelRename"
    />

    <!-- 删除确认弹窗 -->
    <AiModalConfirm
      v-if="showDeleteModal && deleteTarget"
      :title="'删除对话'"
      :description="`确定要删除「${deleteTarget.title}」吗？此操作无法撤销。`"
      color="error"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>
