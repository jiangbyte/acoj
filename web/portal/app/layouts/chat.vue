<script setup lang="ts">
import { useChatHistory } from '~/composables/ai/useChatHistory'

const {
  conversations,
  activeId,
  sidebarOpen,
  searchOpen,
  selectConversation,
  deleteConversation,
  newConversation,
  groups: _groups,
} = useChatHistory()
</script>

<template>
  <UDashboardGroup unit="rem" class="will-change-[grid-template-columns]">
    <UDashboardSidebar
      id="chat-sidebar"
      v-model:open="sidebarOpen"
      collapsible
      :min-size="14"
      class="border-r-0 py-4"
    >
      <template #default="{ collapsed }">
        <AiChatSidebar
          :conversations="conversations"
          :active-id="activeId"
          :collapsed="collapsed"
          @select="selectConversation"
          @delete="deleteConversation"
          @rename="
            (id, title) => {
              const { renameConversation } = useChatHistory()
              renameConversation(id, title)
            }
          "
          @new="newConversation"
          @search="searchOpen = true"
        />
      </template>
    </UDashboardSidebar>

    <UDashboardSearch
      v-model:open="searchOpen"
      placeholder="搜索对话..."
      :groups="[
        {
          id: 'links',
          items: [{ label: '新对话', suffix: '', onSelect: () => newConversation() }],
        },
        {
          id: 'chats',
          label: '对话',
          items: conversations.map((conv) => ({
            label: conv.title,
            suffix: conv.messages.length > 0 ? `${conv.messages.length} 条消息` : '',
            onSelect: () => selectConversation(conv.id),
          })),
        },
      ]"
    />

    <div class="relative flex-1 flex min-w-0">
      <slot />
    </div>
  </UDashboardGroup>
</template>
